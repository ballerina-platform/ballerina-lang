package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;

import java.util.List;

public abstract class Cache implements Repo {

    @Override
    public abstract List<String> resolveVersions(PackageID moduleId, String filter);

    @Override
    public void pullModule(PackageID moduleId) {
        throw new UnsupportedOperationException("pullModule is not supported in Cache implementation");
    }

    abstract PackageEntity getModule(PackageID moduleId);
}
