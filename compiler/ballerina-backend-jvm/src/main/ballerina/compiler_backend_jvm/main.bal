import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

public type JarFile record {
    map<string> manifestEntries;
    map<byte[]> jarEntries;
    !...;
};

bir:BIRContext currentBIRContext = new;

public function main(string... args) {
    //do nothing
}

function generateJarBinary(bir:BIRContext birContext, bir:ModuleID entryModId, string progName) returns JarFile {
    currentBIRContext = birContext;
    bir:Package entryMod = birContext.lookupBIRModule(entryModId);

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    foreach var importModule in entryMod.importModules {
        bir:Package module = lookupModule(importModule, birContext);
        generateImportedPackage(module, jarEntries);
    }

    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}
