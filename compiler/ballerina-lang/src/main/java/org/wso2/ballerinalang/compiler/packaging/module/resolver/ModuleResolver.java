package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;

public interface ModuleResolver {

    PackageID resolveVersion(PackageID moduleId, PackageID enclModuleId);

    PackageEntity resolveModule(PackageID moduleId);
}
