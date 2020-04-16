package org.wso2.ballerinalang.compiler.packageloader;

import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

public interface ModuleLoader {

    BLangPackage load(Resolution resolution);
}
