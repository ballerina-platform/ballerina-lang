package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;

import java.io.IOException;
import java.util.List;

public interface Repo {

    List<String> resolveVersions(PackageID moduleId, String filter) throws IOException;

    boolean isModuleExists(PackageID moduleId);

    void pullModule(PackageID moduleId);
}
