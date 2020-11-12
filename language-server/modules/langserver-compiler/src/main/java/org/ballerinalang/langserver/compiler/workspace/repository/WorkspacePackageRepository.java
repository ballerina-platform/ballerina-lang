package org.ballerinalang.langserver.compiler.workspace.repository;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Workspace Package repository for language server.
 */
public class WorkspacePackageRepository extends GeneralFSPackageRepository {
    private static final String BAL_SOURCE_EXT = ".bal";

    private WorkspaceDocumentManager documentManager;

    public WorkspacePackageRepository(String programDirRoot, WorkspaceDocumentManager documentManager) {
        super(Paths.get(programDirRoot));
        this.documentManager = documentManager;
    }

    protected PackageSource lookupPackageSource(PackageID pkgID) {
        Path path = this.generatePath(pkgID);
        return new WorkspacePackageSource(pkgID, path);
    }

    protected PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePath(pkgID);
        try {
            return new WorkspacePackageSource(pkgID, path, entryName);
        } catch (FSPackageEntityNotAvailableException e) {
            return null;
        }
    }

    /**
     * Returns resolved path of the package inside workspace.
     *
     * @param pkgPath Path of the package compiler wants to resolve
     *
     * @return Resolved pkg path which points to correct entry in workspace
     */
    private Path getResolvedPathFromPackagePath(Path pkgPath) {
        Path resolvedPath = pkgPath;
        if (Files.exists(pkgPath)) {
            try {
                resolvedPath = resolvedPath.toRealPath();
            } catch (IOException e) {
                // Do Nothing For Now
            }
        } else if (pkgPath.getName(pkgPath.getNameCount() - 1).toString()
                .equals(PackageID.DEFAULT.getName().toString())) {
            resolvedPath = pkgPath.getRoot().resolve(pkgPath.subpath(0, pkgPath.getNameCount() - 1));
        }
        return resolvedPath;
    }

    /**
     * Workspace Package source implementation for language server.
     */
    public class WorkspacePackageSource implements PackageSource {

        PackageID pkgID;

        Path pkgPath;

        private List<String> cachedEntryNames;

        WorkspacePackageSource(PackageID pkgID, Path pkgPath) {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
        }

        WorkspacePackageSource(PackageID pkgID, Path pkgPath, String entryName)
                throws FSPackageEntityNotAvailableException {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
            Path resolvedPath = getResolvedPathFromPackagePath(pkgPath).resolve(entryName);
            if (Files.exists(resolvedPath)
                    || documentManager.isFileOpen(resolvedPath)) {
                this.cachedEntryNames = Arrays.asList(entryName);
            } else {
                throw new FSPackageEntityNotAvailableException();
            }
        }

        @Override
        public PackageID getPackageId() {
            return pkgID;
        }

        @Override
        public List<String> getEntryNames() {
            if (this.cachedEntryNames == null && Files.exists(this.pkgPath)) {
                try {
                    List<Path> files = Files.walk(this.pkgPath, 1).filter(
                            Files::isRegularFile).filter(e -> e.getFileName().toString().endsWith(BAL_SOURCE_EXT)).
                            collect(Collectors.toList());
                    this.cachedEntryNames = new ArrayList<>(files.size());
                    files.stream().forEach(e -> this.cachedEntryNames.add(e.getFileName().toString()));
                } catch (IOException e) {
                    throw new RuntimeException("Error in listing packages at '" + this.pkgID +
                            "': " + e.getMessage(), e);
                }
            }
            return this.cachedEntryNames;
        }

        @Override
        public RepoHierarchy getRepoHierarchy() {
            return null;
        }

        @Override
        public CompilerInput getPackageSourceEntry(String name) {
            return new WorkspaceCompilerInput(name);
        }

        @Override
        public List<CompilerInput> getPackageSourceEntries() {
            if (this.getEntryNames() == null) {
                return new ArrayList<>();
            }
            return this.getEntryNames().stream().map(this::getPackageSourceEntry).collect(Collectors.toList());
        }

        public PackageRepository getPackageRepository() {
            return WorkspacePackageRepository.this;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public String getName() {
            return this.getPackageId().toString();
        }

        /**
         * This represents workspace based {@link CompilerInput}.
         */
        public class WorkspaceCompilerInput implements CompilerInput {

            private String name;

            private byte[] code;
            private final SyntaxTree tree;

            private WorkspaceCompilerInput(String name) {
                this.name = name;
                Path filePath = getResolvedPathFromPackagePath(basePath.resolve(pkgPath)).resolve(name);
                if (documentManager.isFileOpen(filePath)) {
                    try {
                        this.code = documentManager.getFileContent(filePath).getBytes("UTF-8");
                    } catch (WorkspaceDocumentException | UnsupportedEncodingException e) {
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
                TextDocument textDocument = TextDocuments.from(new String(this.code));
                this.tree = SyntaxTree.from(textDocument, name);
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code.clone();
            }

            @Override
            public SyntaxTree getTree() {
                return this.tree;
            }

        }
    }



}
