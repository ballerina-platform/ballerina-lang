/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import org.eclipse.lsp4j.Position;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds last known cursor position for the current document.
 *
 * @since 0.984.0
 */
public class CursorTracker {
    private static final CursorTracker INSTANCE = new CursorTracker();

    /* Make updating lastSeenUri thread-safe */
    private final Lock lock = new ReentrantLock();
    private String lastSeenUri = "";
    private Position lastSeenCursor = new Position(0, 0);

    private CursorTracker() {
    }

    public static CursorTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Updates last seen cursor position.
     *
     * @param fileUri  file uri received from LS Client
     * @param position current cursor position
     */
    public void update(String fileUri, Position position) {
        lock.lock();
        try {
            lastSeenUri = fileUri;
            lastSeenCursor = position;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns optional last seen cursor position.
     *
     * @param fileUri file uri received from LS Client
     * @return Optional {@link Position}
     */
    public Position get(String fileUri) {
        lock.lock();
        try {
            if (!this.lastSeenUri.equals(fileUri)) {
                lastSeenCursor = new Position(0, 0);
            }
            return lastSeenCursor;
        } finally {
            lock.unlock();
        }
    }
}
