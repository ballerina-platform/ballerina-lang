package org.ballerinalang.packageloader;

import org.ballerinalang.model.elements.PackageID;

import java.io.PrintStream;

public class PackageLoaderImpl implements PackageLoader {

    public static org.wso2.ballerinalang.compiler.PackageLoader getInstance(){
        return null;
    }

    @Override public void loadEntryPackage(PackageID pkgId, PackageID enclPackageId, PrintStream outStream) {
    }

    @Override public void loadPackage(PackageID packageID) {
    }

    @Override public void loadAndDefinePackage(PackageID packageID) {
    }

    @Override public void loadPackageSymbol(PackageID packageId, PackageID enclPackageId) {
        // resolve the module version -> PackageID = versionresolver.resolve();
        // resolve module -> ModuleLoader = moduleResolver.resolve(PackageID);
        // BLangPackage = ModuleLoader.loadModule()
    }
}
