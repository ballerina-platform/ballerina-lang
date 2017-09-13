package org.ballerinalang.composer.service.workspace.rest.datamodel;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * InMemoryPackageRepository
 */
public class InMemoryPackageRepository implements PackageRepository {

    private byte[] code;

    public InMemoryPackageRepository(byte[] code) {
        this.code = code.clone();
    }

    /**
     * InMemoryPackageSource
     */
    public static class InMemoryPackageSource implements PackageSource {

        private byte[] code;

        public InMemoryPackageSource(byte[] code) {
            this.code = code.clone();
        }

        @Override
        public PackageID getPackageId() {
            return new PackageID(new Name("."), new Name("0.0.0"));
        }

        @Override
        public List<String> getEntryNames() {
            return Stream.of("untitled")
                    .collect(Collectors.toList());
        }

        @Override
        public PackageSourceEntry getPackageSourceEntry(String s) {
            return null;
        }

        @Override
        public List<PackageSourceEntry> getPackageSourceEntries() {
            return Stream.of(new InMemoryPackageRepository.InMemoryPackageSource.InMemoryPackageSourceEntry(code))
                    .collect(Collectors.toList());
            //return this.getEntryNames().stream().map(e -> new InMemoryPackageRepository.InMemoryPackageSource.
            // InMemoryPackageSourceEntry(code)).collect(Collectors.toList());
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public String getName() {
            return "untitled";
        }

        @Override
        public PackageRepository getPackageRepository() {
            return null;
        }

        /**
         * InMemoryPackageSourceEntry
         */
        public static class InMemoryPackageSourceEntry implements PackageSourceEntry {

            private byte[] code;

            InMemoryPackageSourceEntry(byte[] code) {
                this.code = code.clone();
            }

            @Override
            public PackageID getPackageID() {
                return new PackageID(new Name("."), new Name("0.0.0"));
            }

            @Override
            public String getEntryName() {
                return null;
            }

            @Override
            public byte[] getCode() {
                return code.clone();
            }
        }
    }

    @Override
    public PackageEntity loadPackage(PackageID packageID) {
        return new InMemoryPackageSource(code);
    }

    @Override
    public PackageEntity loadPackage(PackageID packageID, String s) {
        return new InMemoryPackageSource(code);
    }
}
