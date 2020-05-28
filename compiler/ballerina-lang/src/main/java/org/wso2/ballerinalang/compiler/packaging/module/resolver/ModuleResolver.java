package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Module;

import java.io.IOException;

public interface ModuleResolver {

    PackageID resolveVersion(PackageID moduleId, PackageID enclModuleId) throws IOException;

    Module resolveModule(PackageID moduleId);
}
