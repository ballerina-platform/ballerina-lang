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

    className = getInvocationClassName(untaint orgName, untaint moduleName, progName);

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, OBJECT_VALUE, null);

    bir:Function? mainFunc = getMainFunc(pkg.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc, cw);
        manifestEntries["Main-Class"] = getMainClassName(orgName, moduleName, progName);
    }

    foreach var func in pkg.functions {
        generateMethod(func, cw);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();
    jarEntries[className + ".class"] = classContent;

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
