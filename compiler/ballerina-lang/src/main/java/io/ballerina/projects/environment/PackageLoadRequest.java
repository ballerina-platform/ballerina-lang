package io.ballerina.projects.environment;

import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.SemanticVersion;

import java.util.Objects;
import java.util.Optional;

public class PackageLoadRequest {
    private final String orgName;
    private final PackageName packageName;
    private final SemanticVersion version;

    public PackageLoadRequest(String orgName, PackageName packageName, SemanticVersion version) {
        if (orgName != null && orgName.isEmpty()) {
            throw new IllegalArgumentException("The orgName cannot be an empty string. " +
                    "It should be either null or a non-empty string value");
        }
        this.orgName = orgName;
        this.packageName = packageName;
        this.version = version;
    }

    public static PackageLoadRequest from(ModuleLoadRequest moduleLoadRequest) {
        return new PackageLoadRequest(moduleLoadRequest.orgName().orElse(null),
                moduleLoadRequest.packageName(),
                moduleLoadRequest.version().orElse(null));
    }

    public Optional<String> orgName() {
        return Optional.of(orgName);
    }

    public PackageName packageName() {
        return packageName;
    }

    public Optional<SemanticVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageLoadRequest that = (PackageLoadRequest) o;
        return Objects.equals(orgName, that.orgName) &&
                packageName.equals(that.packageName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, packageName, version);
    }
}
