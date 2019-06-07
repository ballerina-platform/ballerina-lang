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

type BIRFunctionWrapper record {|
    string orgName;
    string moduleName;
    string versionValue;
    bir:Function func;
    string fullQualifiedClassName;
    string jvmMethodDescription;
|};

map<BIRFunctionWrapper> birFunctionMap = {};

map<string> globalVarClassNames = {};

map<(bir:AsyncCall|bir:FPLoad,string)> lambdas = {};

string currentClass = "";

function lookupFullQualifiedClassName(string key) returns string {
    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return functionWrapper.fullQualifiedClassName;
    } else {
        error err = error("cannot find full qualified class for : " + key);
        panic err;
    }
}

function lookupJavaMethodDescription(string key) returns string {
    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return functionWrapper.jvmMethodDescription;
    } else {
        error err = error("cannot find jvm method description for : " + key);
        panic err;
    }
}

function isBIRFunctionExtern(string key) returns boolean {
    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return isExternFunc(functionWrapper.func);
    } else {
        error err = error("cannot find function definition for : " + key);
        panic err;
    }
}

function getBIRFunctionWrapper(BIRFunctionWrapper? wrapper) returns BIRFunctionWrapper {
    if (wrapper is BIRFunctionWrapper) {
        return wrapper;
    } else {
        error err = error("invalid bir function linking");
        panic err;
    }
}

function lookupGlobalVarClassName(string key) returns string {
    var result = globalVarClassNames[key];
    if (result is string) {
        return result;
    } else {
       error err = error("cannot find full qualified class for global variable : " + key);
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
    string pkgName = getPackageName(orgName, moduleName);
    string sourceFileName = module.name.value;
    foreach var func in module.functions {
        addDefaultableBooleanVarsToSignature(func);
    }
    typeOwnerClass = getModuleLevelClassName(untaint orgName, untaint moduleName, MODULE_INIT_CLASS_NAME);
    map<JavaClass> jvmClassMap = generateClassNameMappings(module, pkgName, typeOwnerClass, untaint lambdas);

    // generate object value classes
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, pkgEntries);
    generateFrameClasses(module, pkgEntries);
    foreach var (moduleClass, v) in jvmClassMap {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        currentClass = moduleClass;
        if (moduleClass == typeOwnerClass) {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), VALUE_CREATOR, ());
            generateDefaultConstructor(cw, VALUE_CREATOR);
            generateUserDefinedTypeFields(cw, module.typeDefs);
            generateValueCreatorMethods(cw, module.typeDefs, pkgName);
            // populate global variable to class name mapping and generate them
            foreach var globalVar in module.globalVars {
                if (globalVar is bir:GlobalVariableDcl) {
                    generatePackageVariable(globalVar, cw);
                    generateLockForVariable(globalVar, cw);
                }
            }
            generateStaticInitializer(module.globalVars, cw, moduleClass);
        } else {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), OBJECT, ());
            generateDefaultConstructor(cw, OBJECT);
        }
        cw.visitSource(v.sourceFileName);
        // generate methods
        foreach var func in v.functions {
            generateMethod(getFunction(func), cw, module);
        }
        // generate lambdas created during generating methods
        foreach var (name, call) in lambdas {
            generateLambdaMethod(call[0], cw, call[1], name);
        }
        // clear the lambdas
        lambdas = {};
        cw.visitEnd();
        byte[] classContent = cw.toByteArray();
        pkgEntries[moduleClass + ".class"] = classContent;
    }
}

public function generateEntryPackage(bir:Package module, string sourceFileName, map<byte[]> pkgEntries,
        map<string> manifestEntries) {

    string orgName = module.org.value;
    string moduleName = module.name.value;
    string pkgName = getPackageName(orgName, moduleName);

    foreach var func in module.functions {
        addDefaultableBooleanVarsToSignature(func);
    }
    typeOwnerClass = getModuleLevelClassName(untaint orgName, untaint moduleName, MODULE_INIT_CLASS_NAME);
    map<JavaClass> jvmClassMap = generateClassNameMappings(module, pkgName, typeOwnerClass, untaint lambdas);

    // generate object value classes
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, pkgEntries);
    generateFrameClasses(module, pkgEntries);
    bir:Function? mainFunc = getMainFunc(module.functions);
    string mainClass = "";
    if (mainFunc is bir:Function) {
        mainClass = getModuleLevelClassName(untaint orgName, untaint moduleName,
                                            cleanupBalExt(mainFunc.pos.sourceFileName));
    }
    foreach var (moduleClass, v) in jvmClassMap {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        currentClass = moduleClass;
        if (moduleClass == typeOwnerClass) {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), VALUE_CREATOR, ());
            generateDefaultConstructor(cw, VALUE_CREATOR);
            generateUserDefinedTypeFields(cw, module.typeDefs);
            generateValueCreatorMethods(cw, module.typeDefs, pkgName);
            // populate global variable to class name mapping and generate them
            foreach var globalVar in module.globalVars {
                if (globalVar is bir:GlobalVariableDcl) {
                    generatePackageVariable(globalVar, cw);
                    generateLockForVariable(globalVar, cw);
                }
            }
            generateStaticInitializer(module.globalVars, cw, moduleClass);
            if (mainFunc is bir:Function) {
                generateMainMethod(mainFunc, cw, module, mainClass, moduleClass);
                generateLambdaForMain(mainFunc, cw, module, mainClass, moduleClass);
                manifestEntries["Main-Class"] = moduleClass;
            }
        } else {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), OBJECT, ());
            generateDefaultConstructor(cw, OBJECT);
        }
        cw.visitSource(v.sourceFileName);
        // generate methods
        foreach var func in v.functions {
            generateMethod(getFunction(func), cw, module);
        }
        
        // generate lambdas
        foreach var (name, call) in lambdas {
            generateLambdaMethod(call[0], cw, call[1], name);
        }
        // clear the lambdas
        lambdas = {};
        cw.visitEnd();
        byte[] classContent = cw.toByteArray();
        pkgEntries[moduleClass + ".class"] = classContent;
    }
}

function generatePackageVariable(bir:GlobalVariableDcl globalVar, jvm:ClassWriter cw) {
    string varName = globalVar.name.value;
    bir:BType bType = globalVar.typeValue;
    generateField(cw, bType, varName, true);
}

function generateLockForVariable(bir:GlobalVariableDcl globalVar, jvm:ClassWriter cw) {
    string lockClass = "Ljava/lang/Object;";
    jvm:FieldVisitor fv;
    fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, computeLockName(globalVar), lockClass);
    fv.visitEnd();
}

function generateStaticInitializer(bir:GlobalVariableDcl?[] globalVars, jvm:ClassWriter cw, string className) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", (), ());

    foreach var globalVar in globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            mv.visitTypeInsn(NEW, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, className, computeLockName(globalVar), "Ljava/lang/Object;");
        }
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function computeLockName(bir:GlobalVariableDcl globalVar) returns string {
    string varName = globalVar.name.value;
    return computeLockNameFromString(varName);
}

function computeLockNameFromString(string varName) returns string {
    return "$lock" + varName;
}

function lookupModule(bir:ImportModule importModule, bir:BIRContext birContext) returns bir:Package {
    bir:ModuleID moduleId = {org: importModule.modOrg.value, name: importModule.modName.value,
                                modVersion: importModule.modVersion.value};
    return birContext.lookupBIRModule(moduleId);
}

function getModuleLevelClassName(string orgName, string moduleName, string sourceFileName) returns string {
    string className = cleanupName(sourceFileName);
    if (!moduleName.equalsIgnoreCase(".")) {
        className = cleanupName(moduleName) + "/" + className;
    }

    if (!orgName.equalsIgnoreCase("$anon")) {
        className = cleanupName(orgName) + "/" + className;
    }

    return className;
}

function getPackageName(string orgName, string moduleName) returns string {
    string packageName = "";
    if (!moduleName.equalsIgnoreCase(".")) {
        packageName = cleanupName(moduleName) + "/";
    }

    if (!orgName.equalsIgnoreCase("$anon")) {
        packageName = cleanupName(orgName) + "/" + packageName;
    }

    return packageName;
}

function splitPkgName(string key) returns (string, string) {
    int index = key.lastIndexOf("/");
    string pkgName = key.substring(0, index);
    string functionName = key.substring(index + 1, key.length());
    return (pkgName, functionName);
}

function cleanupName(string name) returns string {
    return name.replace(".","_");
}

function cleanupPackageName(string pkgName) returns string {
    int index = pkgName.lastIndexOf("/");
    if (index > 0) {
        return pkgName.substring(0, index);
    } else {
        return pkgName;
    }
}

# Java Class will be generate for each source file. This method add class mappings to globalVar and filters the 
# functions based on their source file name and then returns map of associated java class contents.
#
# + module - The module
# + pkgName - The module package Name
# + initClass - The module init class
# + lambdaCalls - The lambdas
# + return - The map of javaClass records on given source file name
function generateClassNameMappings(bir:Package module, string pkgName, string initClass, 
                                   map<(bir:AsyncCall|bir:FPLoad,string)> lambdaCalls) returns map<JavaClass> {
    
    string orgName = module.org.value;
    string moduleName = module.name.value;
    string versionValue = module.versionValue.value;
    map<JavaClass> jvmClassMap = {};

    foreach var globalVar in module.globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            globalVarClassNames[pkgName + globalVar.name.value] = initClass;
        }
    }
    // filter out functions.
    bir:Function?[] functions = module.functions;
    if (functions.length() > 0) {
        int funcSize = functions.length();
        int count  = 0;
        // Generate init class. Init function should be the first function of the package, hence check first 
        // function.
        bir:Function initFunc = <bir:Function>functions[0];
        string functionName = initFunc.name.value;
        if (functionName == getModuleInitFuncName(module)) {
            JavaClass class = { sourceFileName:initFunc.pos.sourceFileName, moduleClass:initClass };
            class.functions[0] = initFunc;
            jvmClassMap[initClass] = class;
            birFunctionMap[pkgName + functionName] = getFunctionWrapper(getFunction(initFunc), orgName, moduleName,
                                                                        versionValue, initClass);
            count = 1;
        }
        // Generate classes for other functions.
        while (count < funcSize) {
            bir:Function func = <bir:Function>functions[count];
            count = count + 1;
            string  moduleClass = "";
            // link the bir function for lookup
            bir:Function currentFunc = getFunction(func);
            functionName = getFunction(func).name.value;
            if (isExternFunc(getFunction(func))) { // if this function is an extern
                var result = jvm:lookupExternClassName(cleanupPackageName(pkgName), functionName);
                if (result is string) {
                    moduleClass = result;
                } else {
                    error err = error("cannot find full qualified class name for extern function : " + pkgName +
                        functionName);
                    panic err;
                }
            } else {
                string? balFileName = func.pos.sourceFileName;
                if (balFileName is string) {
                    moduleClass = getModuleLevelClassName(untaint orgName, untaint moduleName,
                                                          untaint cleanupBalExt(balFileName));
                    var javaClass = jvmClassMap[moduleClass];
                    if (javaClass is JavaClass) {
                        javaClass.functions[javaClass.functions.length()] = func;
                    } else {
                        JavaClass class = { sourceFileName:balFileName, moduleClass:moduleClass };
                        class.functions[0] = func;
                        jvmClassMap[moduleClass] = class;
                    }
                }
            }
            birFunctionMap[pkgName + functionName] = getFunctionWrapper(currentFunc, orgName, moduleName,
                                                                        versionValue, moduleClass);
        }
    }
    // link typedef - object attached native functions
    bir:TypeDef?[] typeDefs = module.typeDefs;

    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;

        if (bType is bir:BObjectType && !bType.isAbstract) {
            bir:Function?[] attachedFuncs = getFunctions(typeDef.attachedFuncs);
            foreach var func in attachedFuncs {

                // link the bir function for lookup
                bir:Function currentFunc = getFunction(func);
                string functionName = currentFunc.name.value;
                string lookupKey = bType.name.value + "." + functionName;

                if (!isExternFunc(currentFunc)) {
                    continue;
                }

                var result = jvm:lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
                if (result is string) {
                    bir:BInvokableType functionTypeDesc = currentFunc.typeValue;
                    bir:BType? attachedType = bType;
                    string jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType,
                                                                attachedType = attachedType);
                    birFunctionMap[pkgName + lookupKey] = getFunctionWrapper(currentFunc, orgName, moduleName,
                                                                        versionValue, result);
                } else {
                    error err = error("cannot find full qualified class name for extern function : " + pkgName +
                                        lookupKey);
                    panic err;
                }
            }
        }
    }
    return jvmClassMap;
}

function getFunctionWrapper(bir:Function currentFunc, string orgName ,string moduleName, 
                            string versionValue,  string  moduleClass) returns BIRFunctionWrapper {

    bir:BInvokableType functionTypeDesc = currentFunc.typeValue;
    bir:BType? attachedType = currentFunc.receiverType;
    string jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType,
                                                attachedType = attachedType);
    return {
        orgName : orgName,
        moduleName : moduleName,
        versionValue : versionValue,
        func : currentFunc,
        fullQualifiedClassName : moduleClass,
        jvmMethodDescription : jvmMethodDescription
    };
}

function generateBuiltInPackages(bir:BIRContext birContext, map<byte[]> jarEntries) {
    bir:ImportModule utilsBIRMod = {modOrg: {value: "ballerina"}, modName: {value: "utils"},
                                        modVersion: {value: ""}};

    bir:ImportModule builtInBIRMod = {modOrg: {value: "ballerina"}, modName: {value: "builtin"},
                                        modVersion: {value: ""}};

    bir:Package utilsModule = lookupModule(utilsBIRMod, birContext);
    bir:Package builtInModule = lookupModule(builtInBIRMod, birContext);

    generateImportedPackage(utilsModule, jarEntries);
    generateImportedPackage(builtInModule, jarEntries);
}
