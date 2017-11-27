package org.ballerinalang.langserver.workspace.repository;

import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Workspace Package repository for language server.
 */
public class WorkspacePackageRepository extends GeneralFSPackageRepository {

    private WorkspaceDocumentManager documentManager;

    public WorkspacePackageRepository(String programDirRoot, WorkspaceDocumentManager documentManager) {
        super(Paths.get(programDirRoot));
        this.documentManager = documentManager;
    }

    protected PackageSource lookupPackageSource(PackageID pkgID) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        return new WorkspacePackageSource(pkgID, path);
    }

    protected PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        try {
            return new WorkspacePackageSource(pkgID, path, entryName);
        } catch (FSPackageEntityNotAvailableException e) {
            return null;
        }
    }

    /**
     * Workspace Package source implementation for language server.
     */
    public class WorkspacePackageSource extends GeneralFSPackageRepository.FSPackageSource {

        private WorkspacePackageSource(PackageID pkgID, Path pkgPath) {
            super(pkgID, pkgPath);
        }

        private WorkspacePackageSource(PackageID pkgID, Path pkgPath, String entryName)
                throws GeneralFSPackageRepository.FSPackageEntityNotAvailableException {
            super(pkgID, pkgPath, entryName);
        }

        @Override
        public PackageSourceEntry getPackageSourceEntry(String name) {
            return new WorkspacePackageSourceEntry(name);
        }

        @Override
        public List<PackageSourceEntry> getPackageSourceEntries() {
            return this.getEntryNames().stream().map(e -> getPackageSourceEntry(e)).collect(Collectors.toList());
        }

        /**
         * This represents workspace based {@link PackageSourceEntry}.
         */
        public class WorkspacePackageSourceEntry implements PackageSourceEntry {

            private String name;

            private byte[] code;

            private WorkspacePackageSourceEntry(String name) {
                this.name = name;
                Path filePath = basePath.resolve(pkgPath).resolve(name);
                if (documentManager.isFileOpen(filePath)) {
                    try {
                        this.code = documentManager.getFileContent(filePath).getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Error in loading package source entry '" + filePath +
                                "': " + e.getMessage(), e);
                    }
                } else {
                    try {
                        this.code = Files.readAllBytes(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("Error in loading package source entry '" + filePath +
                                "': " + e.getMessage(), e);
                    }
                }
            }

            @Override
            public PackageID getPackageID() {
                return pkgID;
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code.clone();
            }

        }
    }



}
