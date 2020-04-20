package org.wso2.ballerinalang.compiler.packageloader;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;

public class PackageLoaderImpl implements PackageLoader {

    public static org.wso2.ballerinalang.compiler.PackageLoader getInstance(CompilerContext context){
        return null;
    }

    @Override public BLangPackage loadEntryPackage(PackageID pkgId, PackageID enclPackageId, PrintStream outStream) {
        return null;
    }

    @Override public BLangPackage loadPackage(PackageID packageID) {
        return null;
    }

    @Override public BLangPackage loadAndDefinePackage(PackageID packageID) {
        return null;
    }

    @Override public BPackageSymbol loadPackageSymbol(PackageID packageId, PackageID enclPackageId,
            RepoHierarchy encPkgRepoHierarchy) {
        // resolve the module version -> PackageID = versionresolver.resolve();
        // resolve module -> ModuleLoader = moduleResolver.resolve(PackageID);
        // BLangPackage = ModuleLoader.loadModule()
        return null;
    }
}
