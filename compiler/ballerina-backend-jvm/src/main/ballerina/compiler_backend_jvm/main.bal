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

    generateStrandClass(jarEntries);
    foreach var importModule in entryMod.importModules {
        bir:ModuleID moduleId = {org: importModule.modOrg.value, name: importModule.modName.value,
                                    modVersion: importModule.modVersion.value};
        bir:Package module = birContext.lookupBIRModule(moduleId);
        generateImportedPackage(module, jarEntries);
    }
    generateEntryPackage(entryMod, progName, jarEntries, manifestEntries);

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

function generateStrandClass(map<byte[]> pkgEntries){
    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "org/ballerina/jvm/Strand", null, "java/lang/Object", null);

    var fv = cw.visitField(ACC_PUBLIC, "yield", "Z");
    fv.visitEnd();

    fv = cw.visitField(ACC_PUBLIC, "frames", "[Ljava/lang/Object;");
    fv.visitEnd();

    fv = cw.visitField(ACC_PUBLIC, "resumeIndex", "I");
    fv.visitEnd();

    cw.visitEnd();
    pkgEntries["org/ballerina/jvm/Strand.class"] = cw.toByteArray();
}
