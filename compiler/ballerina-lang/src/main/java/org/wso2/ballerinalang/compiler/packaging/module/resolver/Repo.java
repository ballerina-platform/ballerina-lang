package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ModuleResolveException;

import java.util.List;

public interface Repo {

    List<String> resolveVersions(PackageID moduleId, String filter) throws ModuleResolveException;

    boolean isModuleExists(PackageID moduleId);

    void pullModule(PackageID moduleId);
}
