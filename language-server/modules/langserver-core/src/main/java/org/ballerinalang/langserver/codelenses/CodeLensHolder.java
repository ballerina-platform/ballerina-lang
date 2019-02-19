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
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.CodeLens;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds code lenses sent and last known number of lines for the current document.
 *
 * @since 0.984.0
 */
public class CodeLensHolder {
    /* Make updating lastSeenUri thread-safe */
    private final Lock lock = new ReentrantLock();
    private final WorkspaceDocumentManager documentManager;
    private String lastSeenUri = "";
    private int lastSeenNumOfLines = -1;
    private int lastSeenTextModGap = 0;

    public CodeLensHolder(WorkspaceDocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    /**
     * Updates last seen number of lines in the source document.
     *
     * @param fileUri    file uri received from LS Client
     * @param numOfLines number of lines
     */
    public void updateNumOfLines(String fileUri, int numOfLines) {
        lock.lock();
        try {
            lastSeenUri = fileUri;
            lastSeenTextModGap = (lastSeenNumOfLines != -1) ? numOfLines - lastSeenNumOfLines : 0;
            lastSeenNumOfLines = numOfLines;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns optional last seen number of lines.
     *
     * @param fileUri fileUri file uri received from LS Client
     * @return Optional {@link Integer} number of lines
     */
    public Optional<Integer> getCodeLensLastTextModsGap(String fileUri) {
        lock.lock();
        try {
            if (this.lastSeenUri.equals(fileUri)) {
                return Optional.of(lastSeenTextModGap);
            } else {
                return Optional.empty();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates code lenses sent to client for this document.
     *
     * @param compilationPath compilation path
     * @param lenses          code lenses
     * @throws WorkspaceDocumentException when file is not found or open
     */
    public void updateCodeLenses(Path compilationPath, List<CodeLens> lenses) throws WorkspaceDocumentException {
        documentManager.updateCodeLenses(compilationPath, lenses);
    }
}
