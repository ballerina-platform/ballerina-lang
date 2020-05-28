package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Module;

import java.util.List;

public abstract class Cache implements Repo {

    @Override
    public abstract List<String> resolveVersions(PackageID moduleId, String filter);

    // if exact version is there, use this method
    @Override
    public abstract boolean isModuleExists(PackageID moduleId);

    @Override
    public void pullModule(PackageID moduleId) {
        throw new UnsupportedOperationException("pullModule is not supported in Cache implementation");
    }

    abstract Module getModule(PackageID moduleId);
}
