package org.ballerinalang.composer.service.workspace.rest.datamodel;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * InMemoryPackageRepository
 */
public class InMemoryPackageRepository extends GeneralFSPackageRepository {

    private byte[] code;
    private PackageID pkgID;
    private String name;

    public InMemoryPackageRepository(PackageID pkgID, String basePath, String name, byte[] code) {
        super(Paths.get(basePath));
        this.name = name;
        this.code = code.clone();
        this.pkgID = pkgID;

    }

    private PackageSource lookupPackageSource(PackageID pkgID, byte[] code) {
        return new InMemoryPackageSource(pkgID, name, code);
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID) {
        PackageEntity result = null;
        //TODO check compiled packages first
        if (result == null) {
            result = this.lookupPackageSource(pkgID, code);
        }
        return result;
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID, String entryName) {
        PackageEntity result = this.lookupPackageSource(pkgID, code);
        return result;
    }

    /**
     * InMemoryPackageSource
     */
    public class InMemoryPackageSource extends FSPackageSource {

        private byte[] code;
        private String name;

        public InMemoryPackageSource(PackageID pkgID, String name, byte[] code) {
            super(pkgID, null);
            this.code = code;
            this.name = name;
        }

        @Override
        public List<PackageSourceEntry> getPackageSourceEntries() {
            return Stream.of(new InMemoryPackageRepository.InMemoryPackageSource.InMemorySourceEntry(name, code))
                    .collect(Collectors.toList());
        }

        /**
         * InMemorySourceEntry
         */
        public class InMemorySourceEntry implements PackageSourceEntry {

            private String name;
            private byte[] code;

            public InMemorySourceEntry(String name, byte[] code) {
                this.name = name;
                this.code = code.clone();
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
    }
}

