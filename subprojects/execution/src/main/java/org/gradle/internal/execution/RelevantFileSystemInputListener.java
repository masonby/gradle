/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.internal.execution;

import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.internal.service.scopes.EventScope;
import org.gradle.internal.service.scopes.Scope.Global;

@EventScope(Global.class)
public interface RelevantFileSystemInputListener {

    /**
     * Called before the execution of the given work unit with its relevant file system inputs.
     *
     * The given files may only contain the relevant subset of all the inputs of the work.
     * This happens when the work has some source input properties that are empty.
     * In such a case any non-source change will be ignored by
     * {@link org.gradle.internal.execution.steps.SkipEmptyWorkStep} until some source
     * files are added.
     */
    void handleRelevantFileSystemInputsOf(UnitOfWork.Identity identity, FileCollectionInternal files);

}
