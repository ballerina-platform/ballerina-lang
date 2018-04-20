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
package org.ballerinalang.langserver.compiler.workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class provides an abstraction layer for a given filePath. All other changes are reflected to the underline
 * WorkspaceDocumentManagerImpl except given filePath.
 */
public class ExtendedWorkspaceDocumentManagerImpl extends WorkspaceDocumentManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedWorkspaceDocumentManagerImpl.class);

    private volatile WorkspaceDocument tempDocument;

    private boolean explicitMode;

    private static final ExtendedWorkspaceDocumentManagerImpl INSTANCE = new ExtendedWorkspaceDocumentManagerImpl();

    public static ExtendedWorkspaceDocumentManagerImpl getInstance() {
        return INSTANCE;
    }

    private ExtendedWorkspaceDocumentManagerImpl() {
        super();
    }

    @Override
    public boolean isFileOpen(Path filePath) {
        if (!isExplicitMode() && !isTempFile(filePath)) {
            return super.isFileOpen(filePath);
        }
        return true;
    }

    @Override
    public void openFile(Path filePath, String content) {
        if (isExplicitMode() && isTempFile(filePath)) {
            tempDocument.setContent(content);
        }
        super.openFile(filePath, content);
    }

    @Override
    public void updateFile(Path filePath, String updatedContent) {
        if (isExplicitMode() && isTempFile(filePath)) {
            tempDocument.setContent(updatedContent);
        }
        super.updateFile(filePath, updatedContent);
    }

    @Override
    public void closeFile(Path filePath) {
        if (!isExplicitMode() && !isTempFile(filePath)) {
            super.closeFile(filePath);
        }
    }

    @Override
    public String getFileContent(Path filePath) {
        if (isExplicitMode() && isTempFile(filePath)) {
            return tempDocument.getContent();
        }
        return super.getFileContent(filePath);
    }

    private boolean isTempFile(Path filePath) {
        try {
            return Files.isSameFile(tempDocument.getPath(), filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isExplicitMode() {
        return explicitMode;
    }

    /**
     * Enables explicit mode. When explicit mode is enabled; changes for the temp file will not be reflected to the
     * WorkspaceDocument Manager and kept in an abstraction layer. Changes for the other files will be served as usual.
     * @param tempFile temp file path
     */
    public void enableExplicitMode(Path tempFile) {
        if (tempDocument == null) {
            tempDocument = new WorkspaceDocument(tempFile, "");
        } else {
            tempDocument.setPath(tempFile);
        }
        this.explicitMode = true;
    }

    /**
     * Disables explicit mode. When explicit mode is disabled; All changes for the files will be served as usual.
     */
    public void disableExplicitMode() {
        this.explicitMode = false;
    }
}
