/*
 * Copyright 2018 the original author or authors.
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Try;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.file.ReservedFileSystemLocation;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;

@NotThreadSafe
public class MutableTransformationWorkspaceProvider implements TransformationWorkspaceProvider, ReservedFileSystemLocation {

    private final Cache<UnitOfWork.Identity, Try<ImmutableList<File>>> identityCache = CacheBuilder.newBuilder().build();
    private final Provider<Directory> baseDirectory;
    private final ExecutionHistoryStore executionHistoryStore;

    public MutableTransformationWorkspaceProvider(Provider<Directory> baseDirectory, ExecutionHistoryStore executionHistoryStore) {
        this.baseDirectory = baseDirectory;
        this.executionHistoryStore = executionHistoryStore;
    }

    @Override
    public ExecutionHistoryStore getExecutionHistoryStore() {
        return executionHistoryStore;
    }

    @Override
    public Cache<UnitOfWork.Identity, Try<ImmutableList<File>>> getIdentityCache() {
        return identityCache;
    }

    @Override
    public <T> T withWorkspace(UnitOfWork.Identity identity, TransformationWorkspaceAction<T> workspaceAction) {
        String workspacePath = identity.getUniqueId();
        File workspaceDir = new File(baseDirectory.get().getAsFile(), workspacePath);
        return workspaceAction.useWorkspace(workspacePath, workspaceDir);
    }

    @Override
    public Provider<? extends FileSystemLocation> getReservedFileSystemLocation() {
        return baseDirectory;
    }
}
