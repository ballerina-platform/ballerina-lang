package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Source file in the real file system (as opposed to in memory).
 */
public class FileSystemSourceInput implements CompilerInput {

    private final Path path;

    public FileSystemSourceInput(Path path) {
        this.path = path;
    }

    @Override
    public String getEntryName() {
        Path fileName = path.getFileName();
        return fileName == null ? path.toString() : fileName.toString();
    }

    @Override
    public byte[] getCode() {
        try {
            byte[] code = Files.readAllBytes(path);
            if (isBLangBinaryFile(path)) {
                path.getFileSystem().close();
            }
            return code;
        } catch (IOException e) {
            throw new BLangCompilerException("Error reading source file " + path);
        }
    }

    /**
     * Checks if the source is from tests.
     *
     * @return true if its a test source, else false
     */
    public boolean isTestSource() {
        Path parentPath = path.getParent();
        if (parentPath != null) {
            Path parentFileName = parentPath.getFileName();
            if (parentFileName != null) {
                return ProjectDirConstants.TEST_DIR_NAME.equals(parentFileName.toString());
            }
        }
        return false;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

    private boolean isBLangBinaryFile(Path path) {
        return path.toString().endsWith(BLANG_COMPILED_PKG_BINARY_EXT);
    }
}
