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
