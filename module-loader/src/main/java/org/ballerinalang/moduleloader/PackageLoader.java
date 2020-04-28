package org.ballerinalang.moduleloader;

import org.ballerinalang.model.elements.PackageID;

import java.io.PrintStream;

public interface PackageLoader {

    void loadEntryPackage(PackageID pkgId, PackageID enclPackageId, PrintStream outStream);

    void loadPackage(PackageID packageID);

    void loadAndDefinePackage(PackageID packageID);

    void loadPackageSymbol(PackageID packageId, PackageID enclPackageId);
}
