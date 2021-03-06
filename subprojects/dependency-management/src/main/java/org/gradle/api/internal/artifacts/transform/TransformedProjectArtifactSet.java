/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.transform;

import org.gradle.api.Action;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedArtifactSet;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.Try;
import org.gradle.internal.operations.BuildOperationQueue;
import org.gradle.internal.operations.RunnableBuildOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An artifact set containing transformed project artifacts.
 */
public class TransformedProjectArtifactSet implements ResolvedArtifactSet, FileCollectionInternal.Source {
    private final ComponentIdentifier componentIdentifier;
    private final ImmutableAttributes targetAttributes;
    private final Collection<TransformationNode> transformedArtifacts;

    public TransformedProjectArtifactSet(
        ComponentIdentifier componentIdentifier,
        ResolvedArtifactSet delegate,
        ImmutableAttributes targetAttributes,
        Transformation transformation,
        ExtraExecutionGraphDependenciesResolverFactory dependenciesResolverFactory,
        TransformationNodeRegistry transformationNodeRegistry
    ) {
        this.componentIdentifier = componentIdentifier;
        this.targetAttributes = targetAttributes;
        this.transformedArtifacts = transformationNodeRegistry.getOrCreate(delegate, transformation, dependenciesResolverFactory.create(componentIdentifier, transformation));
    }

    public TransformedProjectArtifactSet(ComponentIdentifier componentIdentifier, ImmutableAttributes targetAttributes, Collection<TransformationNode> transformedArtifacts) {
        this.componentIdentifier = componentIdentifier;
        this.targetAttributes = targetAttributes;
        this.transformedArtifacts = transformedArtifacts;
    }

    public ComponentIdentifier getOwnerId() {
        return componentIdentifier;
    }

    public ImmutableAttributes getTargetAttributes() {
        return targetAttributes;
    }

    public Collection<TransformationNode> getTransformedArtifacts() {
        return transformedArtifacts;
    }

    @Override
    public Completion startVisit(BuildOperationQueue<RunnableBuildOperation> actions, AsyncArtifactListener listener) {
        FileCollectionStructureVisitor.VisitType visitType = listener.prepareForVisit(this);
        if (visitType == FileCollectionStructureVisitor.VisitType.NoContents) {
            return visitor -> visitor.endVisitCollection(this);
        }

        List<ResolvableArtifact> result = new ArrayList<>(transformedArtifacts.size());
        List<Throwable> failures = new ArrayList<>(1);
        for (TransformationNode node : transformedArtifacts) {
            node.executeIfNotAlready();
            Try<TransformationSubject> transformedSubject = node.getTransformedSubject();
            if (transformedSubject.isSuccessful()) {
                for (File file : transformedSubject.get().getFiles()) {
                    result.add(node.getInputArtifacts().transformedTo(file));
                }
            } else {
                Throwable failure = transformedSubject.getFailure().get();
                failures.add(new TransformException(String.format("Failed to transform %s to match attributes %s.", node.getInputArtifacts().getDisplayName(), targetAttributes), failure));
            }
        }
        return visitor -> {
            DisplayName displayName = Describables.of(componentIdentifier);
            for (ResolvableArtifact artifact : result) {
                visitor.visitArtifact(displayName, targetAttributes, artifact);
            }
            for (Throwable failure : failures) {
                visitor.visitFailure(failure);
            }
            visitor.endVisitCollection(this);
        };
    }

    @Override
    public void visitDependencies(TaskDependencyResolveContext context) {
        if (!transformedArtifacts.isEmpty()) {
            context.add(new DefaultTransformationDependency(transformedArtifacts));
        }
    }

    @Override
    public void visitLocalArtifacts(LocalArtifactVisitor visitor) {
        throw new UnsupportedOperationException("Should not be called.");
    }

    @Override
    public void visitExternalArtifacts(Action<ResolvableArtifact> visitor) {
        throw new UnsupportedOperationException("Should not be called.");
    }
}
