package org.wso2.ballerinalang.compiler.packageloader;

import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.Resolution;

public interface ModuleResolver {

    Resolution resolve(RepoHierarchy repoHierarchy);
}
