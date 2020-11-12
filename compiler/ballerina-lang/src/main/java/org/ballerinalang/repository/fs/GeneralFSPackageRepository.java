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
package org.ballerinalang.repository.fs;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This represents a general file system based {@link PackageRepository}.
 *
 * @since 0.94
 */
public class GeneralFSPackageRepository implements PackageRepository {

    private static final String BAL_SOURCE_EXT = ".bal";

    protected Path basePath;
    private final Name orgName;

    public GeneralFSPackageRepository(Path basePath, Name orgName) {
        this.basePath = basePath;
        this.orgName = orgName;
    }

    public GeneralFSPackageRepository(Path basePath) {
        this(basePath, null);
    }

    protected PackageSource lookupPackageSource(PackageID pkgID) {

        Path path;
        if (orgName == null) {
            path = this.generatePathNew(pkgID);
        } else {
            path = this.generatePath(pkgID);
        }

        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            // TODO remove, temp hack until builtin are flattned
            path = this.generatePathOld(pkgID);
            if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
        }
        return new FSPackageSource(pkgID, path);
    }

    protected PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePathOld(pkgID);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        try {
            return new FSPackageSource(pkgID, path, entryName);
        } catch (FSPackageEntityNotAvailableException e) {
            return null;
        }
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID) {
        return this.lookupPackageSource(pkgID);
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID, String entryName) {
        return this.lookupPackageSource(pkgID, entryName);
    }

    private String sanitize(String name, String separator) {
        if (name.startsWith(separator)) {
            name = name.substring(separator.length());
        }
        if (name.endsWith(separator)) {
            name = name.substring(0, name.length() - separator.length());
        }
        return name;
    }

    private boolean isBALFile(Path path) {
        return !Files.isDirectory(path) && path.getFileName().toString().endsWith(BAL_SOURCE_EXT);
    }

    @Override
    public Set<PackageID> listPackages(int maxDepth) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be greater than zero");
        }
        if (!Files.isDirectory(this.basePath)) {
            return Collections.emptySet();
        }
        Set<PackageID> result = new LinkedHashSet<>();
        int baseNameCount = this.basePath.getNameCount();
        String separator = this.basePath.getFileSystem().getSeparator();
        try {
            Files.walkFileTree(this.basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (Files.isHidden(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    List<Name> nameComps = new ArrayList<>();
                    if (Files.list(dir).filter(f -> isBALFile(f)).count() > 0) {
                        int dirNameCount = dir.getNameCount();
                        if (dirNameCount > baseNameCount) {
                            dir.subpath(baseNameCount, dirNameCount).forEach(
                                    f -> nameComps.add(new Name(sanitize(f.getFileName().toString(), separator))));
                            result.add(new PackageID(Names.ANON_ORG, nameComps, Names.DEFAULT_VERSION));
                        }
                    }
                    if ((dir.getNameCount() + 1) - baseNameCount > maxDepth) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error in listing modules: " + e.getMessage(), e);
        }
        return result;
    }

    protected Path generatePathOld(PackageID pkgID) {
        if (pkgID.isUnnamed) {
            return this.basePath;
        }

        Path pkgDirPath = this.basePath;
        for (Name comp : pkgID.getNameComps()) {
            pkgDirPath = pkgDirPath.resolve(comp.value);
        }
        return pkgDirPath;
    }

    /**
     * Generate path for given package.
     *
     * @param pkgID package ID
     * @return {@link Path} generated path
     */
    public Path generatePath(PackageID pkgID) {
        return this.basePath.resolve(createPackageNameWithDots(pkgID));
    }

    private Path generatePathNew(PackageID pkgID) {
        Path pkgDirPath = this.basePath.resolve(pkgID.getOrgName().value);
        return pkgDirPath.resolve(createPackageNameWithDots(pkgID));
    }

    private String createPackageNameWithDots(PackageID pkgID) {
        List<Name> nameComps = pkgID.getNameComps();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nameComps.size(); i++) {
            if (i != 0) {
                builder.append(".");
            }
            builder.append(nameComps.get(i).value);
        }
        return builder.toString();
    }


    /**
     * This represents a local file system based {@link FSPackageSource}.
     *
     * @since 0.94
     */
    public class FSPackageSource implements PackageSource {

        protected PackageID pkgID;

        protected Path pkgPath;

        private List<String> cachedEntryNames;

        public FSPackageSource(PackageID pkgID, Path pkgPath) {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
        }

        public FSPackageSource(PackageID pkgID, Path pkgPath, String entryName)
                throws FSPackageEntityNotAvailableException {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
            if (Files.exists(pkgPath.resolve(entryName))) {
                this.cachedEntryNames = Collections.singletonList(entryName);
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
            if (this.cachedEntryNames == null) {
                try {
                    List<Path> files = Files.walk(this.pkgPath, 1).filter(
                            Files::isRegularFile).filter(e -> e.getFileName().toString().endsWith(BAL_SOURCE_EXT)).
                            collect(Collectors.toList());
                    this.cachedEntryNames = new ArrayList<>(files.size());
                    files.stream().forEach(e -> this.cachedEntryNames.add(e.getFileName().toString()));
                } catch (IOException e) {
                    throw new RuntimeException("Error in listing modules at '" + this.pkgID +
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
            return new FSCompilerInput(name);
        }

        @Override
        public List<CompilerInput> getPackageSourceEntries() {
            return this.getEntryNames().stream().map(e -> new FSCompilerInput(e)).collect(Collectors.toList());
        }

        /**
         * This represents local file system based {@link CompilerInput}.
         *
         * @since 0.94
         */
        public class FSCompilerInput implements CompilerInput {

            private String name;

            private byte[] code;

            private SyntaxTree tree;

            public FSCompilerInput(String name) {
                this.name = name;
                Path filePath = basePath.resolve(name);
                try {
                    this.code = Files.readAllBytes(basePath.resolve(pkgPath).resolve(name));
                    this.tree = SyntaxTree.from(TextDocuments.from(new String(this.code)), name);
                } catch (IOException e) {
                    throw new RuntimeException("Error in loading module source entry '" + filePath +
                            "': " + e.getMessage(), e);
                }
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code;
            }

            @Override
            public SyntaxTree getTree() {
                return tree;
            }
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public String getName() {
            return this.getPackageId().toString();
        }

    }

    /**
     * Represents an FS repository not available scenario.
     *
     * @since 0.94
     */
    public class FSPackageEntityNotAvailableException extends Exception {

        private static final long serialVersionUID = 1528033476455781589L;

    }

}
