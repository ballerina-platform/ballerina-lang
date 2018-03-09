package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Combines list of paths to a single package source.
 */
public class PathListPackageSource implements PackageSource {
    private final PackageID pkgId;
    private final List<Path> paths;
    private final RepoHierarchy hierarchy;

    public PathListPackageSource(PackageID pkgId, List<Path> paths, RepoHierarchy hierarchy) {
        this.pkgId = pkgId;
        this.paths = paths;
        this.hierarchy = hierarchy;
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public String getName() {
        return pkgId.getName().value;
    }

    @Override
    public PackageRepository getPackageRepository() {
        return null;
    }

    @Override
    public PackageID getPackageId() {
        return pkgId;
    }

    @Override
    public List<String> getEntryNames() {
        return null;
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        return hierarchy;
    }

    @Override
    public PackageSourceEntry getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<PackageSourceEntry> getPackageSourceEntries() {
        return paths.stream().map(PathSourceEntry::new).collect(Collectors.toList());
    }

    private class PathSourceEntry implements PackageSourceEntry {
        private final Path p;

        public PathSourceEntry(Path path) {
            this.p = path;
        }

        @Override
        public PackageID getPackageID() {
            return pkgId;
        }

        @Override
        public String getEntryName() {
            Path fileName = p.getFileName();
            return fileName != null ? fileName.toString() : p.toString();
        }

        @Override
        public byte[] getCode() {
            try {
                return Files.readAllBytes(p);
            } catch (IOException e) {
                return new byte[]{};
            }
        }
    }
}
