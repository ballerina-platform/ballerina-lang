package org.wso2.ballerinalang.compiler.packageloader;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.PrintStream;

public interface PackageLoader {

    BLangPackage loadEntryPackage(PackageID pkgId, PackageID enclPackageId, PrintStream outStream);

    BLangPackage loadPackage(PackageID packageID);

    BLangPackage loadAndDefinePackage(PackageID packageID);

    BPackageSymbol loadPackageSymbol(PackageID packageId, PackageID enclPackageId, RepoHierarchy encPkgRepoHierarchy);
}
