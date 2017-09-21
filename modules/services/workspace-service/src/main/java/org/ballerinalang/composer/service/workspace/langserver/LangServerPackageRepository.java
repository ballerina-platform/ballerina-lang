package org.ballerinalang.composer.service.workspace.langserver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a general file system based {@link PackageRepository} for Language server content suggestion.
 *
 * @since 0.94
 */
public class LangServerPackageRepository extends GeneralFSPackageRepository {

    protected Path basePath;

    protected HashMap<String, byte[]> contentMap;

    public LangServerPackageRepository(Path basePath, HashMap<String, byte[]> contentMap) {
        super(basePath);
        this.contentMap = contentMap;
    }

    private PackageSource lookupPackageSource(PackageID pkgID) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        return new LangServerPackageSource(pkgID, path);
    }

    private PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        return new LangServerPackageSource(pkgID, path, entryName);
    }

    /**
     * This represents a local file system based {@link LangServerPackageRepository.FSPackageSource}.
     *
     * @since 0.94
     */
    public class LangServerPackageSource extends FSPackageSource {

        public LangServerPackageSource(PackageID pkgID, Path pkgPath) {
            super(pkgID, pkgPath);
        }

        public LangServerPackageSource(PackageID pkgID, Path pkgPath, String entryName) {
            super(pkgID, pkgPath, entryName);
        }

        @Override
        public PackageSourceEntry getPackageSourceEntry(String name) {
            return new LangServerPackageSourceEntry(name);
        }

        @Override
        public List<PackageSourceEntry> getPackageSourceEntries() {
            return this.getEntryNames().stream().map(e -> new LangServerPackageSourceEntry(e)).collect(Collectors.toList());
        }

        /**
         * This represents local file system based {@link PackageSourceEntry}.
         *
         * @since 0.94
         */
        public class LangServerPackageSourceEntry implements PackageSourceEntry {

            private String name;

            private byte[] code;

            public LangServerPackageSourceEntry(String name) {
                this.name = name;
                Path filePath = basePath.resolve(name);
                try {
                    // If there is a particular file in the contentMap, retrieve the content from the map, otherwise
                    // read content from the File System
                    // TODO: Handle multiple files with the same name
                    if (contentMap.containsKey(name)) {
                        this.code = contentMap.get(name);
                    } else {
                        this.code = Files.readAllBytes(basePath.resolve(pkgPath).resolve(name));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error in loading package source entry '" + filePath +
                            "': " + e.getMessage(), e);
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
                return code;
            }
        }

        @Override
        public PackageRepository getPackageRepository() {
            return LangServerPackageRepository.this;
        }
    }
}
