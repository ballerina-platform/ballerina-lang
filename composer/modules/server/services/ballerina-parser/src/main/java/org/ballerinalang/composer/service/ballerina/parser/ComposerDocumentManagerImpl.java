/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.ballerina.parser;

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * This class provides an abstraction layer for a given filePath. All other changes are reflected to the underline
 * WorkspaceDocumentManagerImpl except given filePath.
 */
public class ComposerDocumentManagerImpl extends WorkspaceDocumentManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(ComposerDocumentManagerImpl.class);

    private WorkspaceDocument tempDocument;

    public ComposerDocumentManagerImpl(Path tempFile) {
        super();
        this.tempDocument = new WorkspaceDocument(tempFile, "");
    }

    @Override
    public void openFile(Path filePath, String content) {
        if (isTempFile(filePath)) {
            tempDocument.setContent(content);
        }
        super.openFile(filePath, content);
    }

    @Override
    public void updateFile(Path filePath, String updatedContent) {
        if (isTempFile(filePath)) {
            tempDocument.setContent(updatedContent);
        }
        super.updateFile(filePath, updatedContent);
    }

    @Override
    public void closeFile(Path filePath) {
        if (!isTempFile(filePath)) {
            super.closeFile(filePath);
        }
    }

    @Override
    public String getFileContent(Path filePath) {
        if (isTempFile(filePath)) {
            return tempDocument.getContent();
        }
        return super.getFileContent(filePath);
    }

    @Override
    public Optional<Lock> lockFile(Path filePath) {
        if (isTempFile(filePath)) {
            Lock lock = tempDocument.getLock();
            lock.lock();
            return Optional.of(lock);
        }
        return super.lockFile(filePath);
    }

    private boolean isTempFile(Path filePath) {
        try {
            return Files.isSameFile(tempDocument.getPath(), filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
