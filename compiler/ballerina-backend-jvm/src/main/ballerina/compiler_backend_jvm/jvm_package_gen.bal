final map<string> fullQualifiedClassNames = {};

function lookupFullQualifiedClassName(string key) returns string {
    var result = fullQualifiedClassNames[key];

    if (result is string) {
        return result;
    } else {
        error err = error("cannot find full qualified class for : " + key);
        panic err;
    }
}

public function generateImportedPackage(bir:Package module, map<byte[]> pkgEntries) {

    // generate imported modules recursively
    foreach var mod in module.importModules {
        bir:Package importedPkg = lookupModule(mod, currentBIRContext);
        generateImportedPackage(importedPkg, pkgEntries);
    }

    string orgName = module.org.value;
    string moduleName = module.name.value;

    // TODO: need to get bal source file name for class name mapping
    string moduleClass = getModuleLevelClassName(untaint orgName, untaint moduleName, untaint moduleName);

    // TODO: remove once the package init class is introduced
    typeOwnerClass = moduleClass;

    // generate object value classes
    ObjectGenerator objGen = new();
    objGen.generateValueClasses(module.typeDefs, pkgEntries);

    generateFrameClasses(module, pkgEntries);

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, OBJECT, null);
    generateDefaultConstructor(cw);

    generateUserDefinedTypeFields(cw, module.typeDefs);

    string pkgName = getPackageName(orgName, moduleName);

    // populate global variable to class name mapping and generate them
    foreach var globalVar in module.globalVars {
        fullQualifiedClassNames[pkgName + globalVar.name.value] = moduleClass;
        generatePackageVariable(globalVar, cw);
    }

    // populate function to class name mapping
    foreach var func in module.functions {
        fullQualifiedClassNames[pkgName + func.name.value] = moduleClass;
    }

    // generate methods
    foreach var func in module.functions {
        generateMethod(func, cw, module);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();
    pkgEntries[moduleClass + ".class"] = classContent;
}

public function generateEntryPackage(bir:Package module, string sourceFileName, map<byte[]> pkgEntries,
        map<string> manifestEntries) {

    string orgName = module.org.value;
    string moduleName = module.name.value;

    string moduleClass = getModuleLevelClassName(untaint orgName, untaint moduleName, untaint sourceFileName);

    // TODO: remove once the package init class is introduced
    typeOwnerClass = moduleClass;

    // generate object value classes
    ObjectGenerator objGen = new();
    objGen.generateValueClasses(module.typeDefs, pkgEntries);

    generateFrameClasses(module, pkgEntries);

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, OBJECT, null);
    generateDefaultConstructor(cw);

    generateUserDefinedTypeFields(cw, module.typeDefs);

    string pkgName = getPackageName(orgName, moduleName);

    // populate global variable to class name mapping and generate them
    foreach var globalVar in module.globalVars {
        fullQualifiedClassNames[pkgName + globalVar.name.value] = moduleClass;
        generatePackageVariable(globalVar, cw);
    }

    // populate function to class name mapping
    foreach var func in module.functions {
        fullQualifiedClassNames[pkgName + func.name.value] = moduleClass;
    }

    bir:Function? mainFunc = getMainFunc(module.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc, cw, module);
        manifestEntries["Main-Class"] = getMainClassName(orgName, moduleName, sourceFileName);
    }

    // generate methods
    foreach var func in module.functions {
        generateMethod(func, cw, module);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();
    pkgEntries[moduleClass + ".class"] = classContent;
}

function generatePackageVariable(bir:GlobalVariableDcl globalVar, jvm:ClassWriter cw) {
    string varName = globalVar.name.value;
    bir:BType bType = globalVar.typeValue;

    if (bType is bir:BTypeInt) {
        jvm:FieldVisitor fv = cw.visitField(ACC_STATIC, varName, "J");
        fv.visitEnd();
    } else if (bType is bir:BMapType) {
        jvm:FieldVisitor fv = cw.visitField(ACC_STATIC, varName, io:sprintf("L%s;", MAP_VALUE));
        fv.visitEnd();
    } else {
        error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
        panic err;
    }
}

function lookupModule(bir:ImportModule importModule, bir:BIRContext birContext) returns bir:Package {
    bir:ModuleID moduleId = {org: importModule.modOrg.value, name: importModule.modName.value,
                                modVersion: importModule.modVersion.value};
    return birContext.lookupBIRModule(moduleId);
}

function getModuleLevelClassName(string orgName, string moduleName, string sourceFileName) returns string {
    string name = sourceFileName;

    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        name = orgName + "/" + moduleName + "/" + sourceFileName;
    }
    return name;
}

function getMainClassName(string orgName, string moduleName, string sourceFileName) returns string {
    string name = sourceFileName;

    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        name = orgName + "." + moduleName + "." + sourceFileName;
    }
    return name;
}

function getPackageName(string orgName, string moduleName) returns string {
    string name = "";

    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        name = orgName + "/" + moduleName + "/";
    }

    return name;
}
