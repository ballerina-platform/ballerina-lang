import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

public type JarFile record {
    map<string> manifestEntries;
    map<byte[]> jarEntries;
    !...;
};

public function main(string... args) {
    //do nothing
}

function generateExecutableJar(bir:BIRContext birContext, bir:ModuleID entryModId, string progName)
        returns JarFile {

    bir:Package entryMod = birContext.lookupBIRModule(entryModId);

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    foreach var importModule in entryMod.importModules {
        bir:ModuleID moduleId = {org: importModule.modOrg.value, name: importModule.modName.value,
                                    modVersion: importModule.modVersion.value};
        bir:Package module = birContext.lookupBIRModule(moduleId);
        generateImportedPackage(module, jarEntries);
    }

    generateEntryPackage(entryMod, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}
