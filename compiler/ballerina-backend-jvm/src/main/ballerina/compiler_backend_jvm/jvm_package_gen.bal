// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

final map<string> fullQualifiedClassNames = {};

final map<(bir:AsyncCall,string)> lambdas = {};

function lookupFullQualifiedClassName(string key) returns string {
    var result = fullQualifiedClassNames[key];

    if (result is string) {
        return result;
    } else {
        (string, string) (pkgName, functionName) = getPackageAndFunctionName(key);
        result = jvm:lookupExternClassName(pkgName, functionName);
        if (result is string) {
            fullQualifiedClassNames[key] = result;
            return result;
        }
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
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, pkgEntries);

    generateFrameClasses(module, pkgEntries);

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), OBJECT, ());
    generateDefaultConstructor(cw);

    generateUserDefinedTypeFields(cw, module.typeDefs);

    string pkgName = getPackageName(orgName, moduleName);

    // populate global variable to class name mapping and generate them
    foreach var globalVar in module.globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            fullQualifiedClassNames[pkgName + globalVar.name.value] = moduleClass;
            generatePackageVariable(globalVar, cw);
        }
    }

    // populate function to class name mapping
    foreach var func in module.functions {
        fullQualifiedClassNames[pkgName + getFunction(func).name.value] = moduleClass;
    }

    // generate methods
    foreach var func in module.functions {
        generateMethod(getFunction(func), cw, module);
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
    string pkgName = getPackageName(orgName, moduleName);

    // generate class name mappings for functions and global vars
    generateClassNameMappings(module, pkgName, moduleClass);

    // TODO: remove once the package init class is introduced
    typeOwnerClass = moduleClass;

    // generate object value classes
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, pkgEntries);

    generateFrameClasses(module, pkgEntries);

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), OBJECT, ());
    generateDefaultConstructor(cw);

    generateUserDefinedTypeFields(cw, module.typeDefs);

    // generate global variables
    foreach var globalVar in module.globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            generatePackageVariable(globalVar, cw);
        }
    }

    bir:Function? mainFunc = getMainFunc(module.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc, cw, module);
        generateLambdaForMain(mainFunc, cw, module);
        manifestEntries["Main-Class"] = getMainClassName(orgName, moduleName, sourceFileName);
    }

    // generate methods
    foreach var func in module.functions {
        generateMethod(getFunction(func), cw, module);
    }

    foreach var (k,v) in lambdas {
        generateLambdaMethod(v[0], cw, v[1], k);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();
    pkgEntries[moduleClass + ".class"] = classContent;
}

function generateClassNameMappings(bir:Package module, string pkgName, string moduleClass) {
    // populate global variable to class name mapping
    foreach var globalVar in module.globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            fullQualifiedClassNames[pkgName + globalVar.name.value] = moduleClass;
        }
    }

    // populate function to class name mapping
    foreach var func in module.functions {
        fullQualifiedClassNames[pkgName + getFunction(func).name.value] = moduleClass;
    }
}

function generatePackageVariable(bir:GlobalVariableDcl globalVar, jvm:ClassWriter cw) {
    string varName = globalVar.name.value;
    bir:BType bType = globalVar.typeValue;
    generateField(cw, bType, varName, true);
}

function lookupModule(bir:ImportModule importModule, bir:BIRContext birContext) returns bir:Package {
    bir:ModuleID moduleId = {org: importModule.modOrg.value, name: importModule.modName.value,
                                modVersion: importModule.modVersion.value};
    return birContext.lookupBIRModule(moduleId);
}

function getModuleLevelClassName(string orgName, string moduleName, string sourceFileName) returns string {
    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        return orgName + "/" + cleanupName(moduleName) + "/" + cleanupName(sourceFileName);
    }
    return cleanupName(sourceFileName);
}

function getMainClassName(string orgName, string moduleName, string sourceFileName) returns string {
    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        return orgName + "." + cleanupName(moduleName) + "." + cleanupName(sourceFileName);
    }
    return cleanupName(sourceFileName);
}

function getPackageName(string orgName, string moduleName) returns string {
    if (!moduleName.equalsIgnoreCase(".") && !orgName.equalsIgnoreCase("$anon")) {
        return orgName + "/" + cleanupName(moduleName) + "/";
    }
    return "";
}

function getPackageAndFunctionName(string key) returns (string, string) {
    int index = key.lastIndexOf("/");
    string pkgName = key.substring(0, index);
    string functionName = key.substring(index + 1, key.length());

    return (pkgName, functionName);
}

function cleanupName(string name) returns string {
    return name.replace(".","_");
}
