public type JarFile record {
    map<string> manifestEntries;
    map<byte[]> jarEntries;
};

function generateJarFile(bir:Package pkg, string progName) returns JarFile {
    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    //todo : need to generate java package/classes here based on BIR imported package(s)

    string orgName = pkg.org.value;
    string moduleName = pkg.name.value;

    invokedClassName = getInvocationClassName(untaint orgName, untaint moduleName, progName);

    // TODO: remove once the package init class is introduced
    typeOwnerClass = progName;

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, invokedClassName, null, OBJECT_VALUE, null);

    generateUserDefinedTypeFields(cw, pkg.typeDefs);

    bir:Function? mainFunc = getMainFunc(pkg.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc, cw, pkg);
        manifestEntries["Main-Class"] = getMainClassName(orgName, moduleName, progName);
    }

    foreach var func in pkg.functions {
        generateMethod(func, cw);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();
    jarEntries[invokedClassName + ".class"] = classContent;

    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

function getInvocationClassName(string orgName, string moduleName, string progName) returns string {
    string name = progName;

    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        name = orgName + "/" + moduleName + "/" + progName;
    }
    return name;
}

function getMainClassName(string orgName, string moduleName, string progName) returns string {
    string name = progName;

    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        name = orgName + "." + moduleName + "." + progName;
    }
    return name;
}
