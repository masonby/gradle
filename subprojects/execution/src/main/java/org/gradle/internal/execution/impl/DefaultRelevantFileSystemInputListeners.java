/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.execution.impl;

import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.internal.event.AnonymousListenerBroadcast;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.execution.RelevantFileSystemInputListener;
import org.gradle.internal.execution.RelevantFileSystemInputListeners;
import org.gradle.internal.execution.UnitOfWork;

public class DefaultRelevantFileSystemInputListeners implements RelevantFileSystemInputListeners {
    private final AnonymousListenerBroadcast<RelevantFileSystemInputListener> broadcaster;

    public DefaultRelevantFileSystemInputListeners(ListenerManager listenerManager) {
        broadcaster = listenerManager.createAnonymousBroadcaster(RelevantFileSystemInputListener.class);
    }

    @Override
    public void addListener(RelevantFileSystemInputListener listener) {
        broadcaster.add(listener);
    }

    @Override
    public void removeListener(RelevantFileSystemInputListener listener) {
        broadcaster.remove(listener);
    }

    @Override
    public void broadcastRelevantFileSystemInputsOf(UnitOfWork.Identity identity, FileCollectionInternal fileSystemInputs) {
        broadcaster.getSource().handleRelevantFileSystemInputsOf(identity, fileSystemInputs);
    }

}
