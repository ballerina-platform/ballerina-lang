/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.compiler.workspace;

import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProjectDirectory;
import org.eclipse.lsp4j.CodeLens;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * An in-memory document manager that keeps dirty files in-memory and will match the collection of files currently open
 * in tool's workspace.
 */
public class WorkspaceDocumentManagerImpl implements WorkspaceDocumentManager {

    private volatile Map<Path, DocumentPair> documentList = new ConcurrentHashMap<>();

    private static final WorkspaceDocumentManagerImpl INSTANCE = new WorkspaceDocumentManagerImpl();

    protected WorkspaceDocumentManagerImpl() {
    }
    
    public static WorkspaceDocumentManagerImpl getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFileOpen(Path filePath) {
        return filePath != null
                && documentList.containsKey(filePath)
                && documentList.get(filePath).getDocument().isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openFile(Path filePath, String content) throws WorkspaceDocumentException {
        if (isFileOpen(filePath)) {
            throw new WorkspaceDocumentException(
                    "File " + filePath.toString() + " is already opened in document manager."
            );
        }
        documentList.put(filePath, new DocumentPair(new WorkspaceDocument(filePath, content)));
        LSDocument document = new LSDocument(filePath.toUri().toString());
        if (document.isWithinProject()) {
            rescanProjectRoot(filePath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFile(Path filePath, String updatedContent) throws WorkspaceDocumentException {
        if (isFileOpen(filePath)) {
            documentList.get(filePath).getDocument().ifPresent(document -> document.setContent(updatedContent));
        } else {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " is not opened in document manager.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCodeLenses(Path filePath, List<CodeLens> codeLens) throws WorkspaceDocumentException {
        if (isFileOpen(filePath)) {
            documentList.get(filePath).getDocument().ifPresent(document -> document.setCodeLenses(codeLens));
        } else {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " is not opened in document manager.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrunedContent(Path filePath, String prunedSource) throws WorkspaceDocumentException {
        if (isFileOpen(filePath)) {
            documentList.get(filePath).getDocument().ifPresent(document -> document.setPrunedContent(prunedSource));
        } else {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " is not opened in document manager.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPrunedContent(Path filePath) throws WorkspaceDocumentException {

        if (isFileOpen(filePath)) {
            documentList.get(filePath).getDocument().ifPresent(WorkspaceDocument::resetPrunedContent);
        } else {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " is not opened in document manager.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeFile(Path filePath) throws WorkspaceDocumentException {
        if (isFileOpen(filePath)) {
            Lock lock = documentList.get(filePath).getLock();
            try {
                lock.lock();
                documentList.get(filePath).setDocument(null);
                documentList.remove(filePath);
            } finally {
                lock.unlock();
            }
            // TODO: within the workspace document we need to keep the LSDocument
            LSDocument document = new LSDocument(filePath.toUri().toString());
            if (document.isWithinProject()) {
                rescanProjectRoot(filePath);
            }
        } else {
            throw new WorkspaceDocumentException("File " + filePath.toString() + " is not opened in document manager.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getCodeLenses(Path filePath) {
        if (isFileOpen(filePath) && documentList.get(filePath) != null) {
            return documentList.get(filePath).getDocument().map(WorkspaceDocument::getCodeLenses).orElse(null);
        }
        return new ArrayList<>();
    }

    @Override
    public LSDocument getLSDocument(Path filePath) throws WorkspaceDocumentException {
        DocumentPair documentPair = documentList.get(filePath);
        if (isFileOpen(filePath) && documentPair != null && documentPair.getDocument().isPresent()) {
            return documentPair.getDocument().get().getLSDocument();
        }
        throw new WorkspaceDocumentException("Cannot find LSDocument for the give file path: ["
                + filePath.toString() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileContent(Path filePath) throws WorkspaceDocumentException {
        if (isFileOpen(filePath) && documentList.get(filePath) != null) {
            return documentList.get(filePath).getDocument().map(WorkspaceDocument::getContent).orElse(null);
        }
        return readFromFileSystem(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Lock> lockFile(Path filePath) {
        Optional<Lock> lock = Optional.ofNullable(filePath).map(path -> Optional.ofNullable(documentList.get(path))
                .map(DocumentPair::getLock)
                .orElseGet(() -> {
                    synchronized (this) {
                        // No lock found, double-check
                        return Optional.ofNullable(documentList.get(path))
                                .map(DocumentPair::getLock)
                                .orElseGet(() -> {
                                    // No lock found, create and return a new DocumentPair
                                    DocumentPair docPair = new DocumentPair(null);
                                    documentList.put(filePath, docPair);
                                    return docPair.getLock();
                                });
                    }
                })
        );
        lock.ifPresent(Lock::lock);
        return lock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Path> getAllFilePaths() {
        return documentList.entrySet().stream()
                .filter(entry -> entry.getValue().getDocument().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllFilePaths() {
        documentList.clear();
    }

    private String readFromFileSystem(Path filePath) throws WorkspaceDocumentException {
        try {
            if (Files.exists(filePath)) {
                byte[] encoded = Files.readAllBytes(filePath);
                return new String(encoded, Charset.defaultCharset());
            }
            throw new WorkspaceDocumentException("Error in reading non-existent file '" + filePath);
        } catch (IOException e) {
            throw new WorkspaceDocumentException("Error in reading file '" + filePath + "': " + e.getMessage(), e);
        }
    }

    private void rescanProjectRoot(Path filePath) {
        Path projectRoot = Paths.get(LSCompilerUtil.getProjectRoot(filePath));
        LangServerFSProjectDirectory projectDirectory = LangServerFSProjectDirectory.getInstance(projectRoot, this);
        projectDirectory.rescanProjectRoot();
    }

    /**
     * This class holds workspace document and its lock.
     */
    public static class DocumentPair {

        private final Lock lock;
        private WorkspaceDocument document;

        public DocumentPair(WorkspaceDocument document) {
            this.document = document;
            lock = new ReentrantLock(true);
        }

        /**
         * Returns the associated lock for the file.
         *
         * @return {@link Lock}
         */
        public Lock getLock() {
            return this.lock;
        }

        /**
         * Returns the workspace document.
         *
         * @return {@link WorkspaceDocumentManager}
         */
        public Optional<WorkspaceDocument> getDocument() {
            return Optional.ofNullable(this.document);
        }

        /**
         * Set workspace document.
         *
         * @param document {@link WorkspaceDocument}
         */
        public void setDocument(WorkspaceDocument document) {
            this.document = document;
        }
    }
}
