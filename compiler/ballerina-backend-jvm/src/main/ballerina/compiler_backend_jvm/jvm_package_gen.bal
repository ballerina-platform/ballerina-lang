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

type BIRFunctionWrapper record {
    string orgName;
    string moduleName;
    string versionValue;
    bir:Function func;
    string fullQualifiedClassName;
    string jvmMethodDescription;
};

map<BIRFunctionWrapper> birFunctionMap = {};

map<bir:TypeDef> typeDefMap = {};

map<string> globalVarClassNames = {};

map<(bir:AsyncCall|bir:FPLoad,string)> lambdas = {};

map<bir:Package> compiledPkgCache = {};

map<string> externalMapCache = {};

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

function lookupTypeDef(bir:TypeDef|bir:TypeRef key) returns bir:TypeDef {
    if (key is bir:TypeDef) {
        return key;
    } else {
        string className = typeRefToClassName(key, key.name.value);
        var typeDef = typeDefMap[className];
        if (typeDef is bir:TypeDef) {
            return typeDef;
        }

        error err = error("Reference to unknown type " + className);
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

public function generatePackage(bir:ModuleID moduleId, JarFile jarFile, boolean isEntry) {
    string orgName = moduleId.org;
    string moduleName = moduleId.name;
    string pkgName = getPackageName(orgName, moduleName);

    var (module, isFromCache) = lookupModule(moduleId);

    if (!isEntry && isFromCache) {
        return;
    }

    addBuiltinImports(moduleId, module);

    // generate imported modules recursively
    foreach var mod in module.importModules {
        generatePackage(importModuleToModuleId(mod), jarFile, false);
    }

    typeOwnerClass = getModuleLevelClassName(untaint orgName, untaint moduleName, MODULE_INIT_CLASS_NAME);
    map<JavaClass> jvmClassMap = generateClassNameMappings(module, pkgName, typeOwnerClass, untaint lambdas);

    if (!isEntry) {
        return;
    }

    boolean serviceEPAvailable = isServiceDefAvailable(module.typeDefs);

    // generate object value classes
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, jarFile.pkgEntries);
    generateFrameClasses(module, jarFile.pkgEntries);
    foreach var (moduleClass, v) in jvmClassMap {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        currentClass = untaint moduleClass;
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

            if (isEntry) {
                bir:Function? mainFunc = getMainFunc(module.functions);
                string mainClass = "";
                if (mainFunc is bir:Function) {
                    mainClass = getModuleLevelClassName(untaint orgName, untaint moduleName,
                                                        cleanupBalExt(mainFunc.pos.sourceFileName));
                }

                generateMainMethod(mainFunc, cw, module, mainClass, moduleClass, serviceEPAvailable);
                if (mainFunc is bir:Function) {
                    generateLambdaForMain(mainFunc, cw, module, mainClass, moduleClass);
                }
                generateLambdaForPackageInits(cw, module, mainClass, moduleClass);
                jarFile.manifestEntries["Main-Class"] = moduleClass;
            }
            generateStaticInitializer(module.globalVars, cw, moduleClass, serviceEPAvailable);
            generateCreateTypesMethod(cw, module.typeDefs);
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
        jarFile.pkgEntries[moduleClass + ".class"] = classContent;
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

function generateStaticInitializer(bir:GlobalVariableDcl?[] globalVars, jvm:ClassWriter cw, string className,
                                    boolean serviceEPAvailable) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", (), ());

    foreach var globalVar in globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            mv.visitTypeInsn(NEW, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, className, computeLockName(globalVar), "Ljava/lang/Object;");
        }
    }

    setServiceEPAvailableField(cw, mv, serviceEPAvailable, className);

    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
}

function setServiceEPAvailableField(jvm:ClassWriter cw, jvm:MethodVisitor mv, boolean serviceEPAvailable,
                                        string initClass) {
    jvm:FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, "serviceEPAvailable", "Z");
    fv.visitEnd();

    if (serviceEPAvailable) {
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, initClass, "serviceEPAvailable", "Z");
    } else {
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, initClass, "serviceEPAvailable", "Z");
    }
}

function computeLockName(bir:GlobalVariableDcl globalVar) returns string {
    string varName = globalVar.name.value;
    return computeLockNameFromString(varName);
}

function computeLockNameFromString(string varName) returns string {
    return "$lock" + varName;
}

function lookupModule(bir:ModuleID modId) returns (bir:Package, boolean) {
        string orgName = modId.org;
        string moduleName = modId.name;
        string versionName = modId.modVersion;

        var pkgFromCache = compiledPkgCache[orgName + moduleName];
        if (pkgFromCache is bir:Package) {
            return (pkgFromCache, true);
        }

        var cacheDir = findCacheDirFor(modId);
        var parsedPkg = bir:populateBIRModuleFromBinary(readFileFully(calculateBirCachePath(cacheDir, modId, ".bir")), true);
        var mappingFile = calculateBirCachePath(cacheDir, modId, ".map.json");
        internal:Path mappingPath = new(mappingFile);
        if (mappingPath.exists()) {
            var externalMap = readMap(mappingFile);
            foreach var (key,val) in externalMap {
                externalMapCache[key] = val;
            }
        }
        compiledPkgCache[orgName + moduleName] = parsedPkg;
        return (parsedPkg, false);

}

function findCacheDirFor(bir:ModuleID modId) returns string {
    foreach var birCacheDir in birCacheDirs {
        internal:Path birPath = new(calculateBirCachePath(birCacheDir, modId, ".bir"));
        if (birPath.exists()) {
            return birCacheDir;
        }
    }
    error e = error(io:sprintf("%s not found in %s", calculateBirCachePath("", modId, ".bir"), birCacheDirs));
    panic e;
}

function calculateBirCachePath(string birCacheDir, bir:ModuleID modId, string ext) returns string {
    string nonEmptyVersion = modId.modVersion;
    if (nonEmptyVersion == "") {
        nonEmptyVersion = "0.0.0";
    }

    return birCacheDir + "/" + modId.org + "/" + modId.name + "/" + nonEmptyVersion + "/" + modId.name + ext;
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
        JavaClass class = { sourceFileName:initFunc.pos.sourceFileName, moduleClass:initClass };
        class.functions[0] = initFunc;
        jvmClassMap[initClass] = class;
        birFunctionMap[pkgName + functionName] = getFunctionWrapper(getFunction(initFunc), orgName, moduleName,
                                                                    versionValue, initClass);
        count += 1;

        bir:Function startFunc = <bir:Function>functions[1];
        functionName = startFunc.name.value;

        if (functionName == getModuleStartFuncName(module)) {
            class.functions[1] = startFunc;
            count += 1;
        }

        // Generate classes for other functions.
        while (count < funcSize) {
            bir:Function birFunc = <bir:Function>functions[count];
            count = count + 1;
            // link the bir function for lookup
            string birFuncName = birFunc.name.value;
            string balFileName = birFunc.pos.sourceFileName;
            string birModuleClassName = getModuleLevelClassName(untaint orgName, untaint moduleName,
                                                        untaint cleanupPathSeperators(cleanupBalExt(balFileName)));

            if !isBallerinaBuiltinModule(orgName, moduleName) {
                var javaClass = jvmClassMap[birModuleClassName];
                if (javaClass is JavaClass) {
                    javaClass.functions[javaClass.functions.length()] = birFunc;
                } else {
                    class = { sourceFileName:balFileName, moduleClass:birModuleClassName };
                    class.functions[0] = birFunc;
                    jvmClassMap[birModuleClassName] = class;
                }
            }

            BIRFunctionWrapper birFuncWrapper;
            if (isExternFunc(getFunction(birFunc))) { // if this function is an extern
                var jClassName = lookupExternClassName(cleanupPackageName(pkgName), birFuncName);
                if (jClassName is string) {
                    if isBallerinaBuiltinModule(orgName, moduleName) {
                        birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, jClassName);
                    } else {
                        bir:BType?[] jMethodPramTypes = birFunc.typeValue.paramTypes.clone();
                        addDefaultableBooleanVarsToSignature(birFunc);
                        birFuncWrapper = createExternalFunctionWrapper(birFunc, orgName, moduleName, versionValue,
                                                    birModuleClassName, jClassName, jMethodPramTypes);
                    }
                } else {
                    error err = error("cannot find full qualified class name for extern function : " + pkgName +
                        birFuncName);
                    panic err;
                }
            } else {
                addDefaultableBooleanVarsToSignature(birFunc);
                birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, birModuleClassName);
            }
            birFunctionMap[pkgName + birFuncName] = birFuncWrapper;
        }
    }
    // link typedef - object attached native functions
    bir:TypeDef?[] typeDefs = module.typeDefs;

    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;

        if (bType is bir:BObjectType || bType is bir:BRecordType) {
            string key = getModuleLevelClassName(orgName, moduleName, typeDef.name.value);
            typeDefMap[key] = typeDef;
        }

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

                var jClassName = lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
                if (jClassName is string) {
                    bir:BType?[] jMethodPramTypes = currentFunc.typeValue.paramTypes.clone();
                    addDefaultableBooleanVarsToSignature(currentFunc);
                    birFunctionMap[pkgName + lookupKey] = createExternalFunctionWrapper(currentFunc, orgName,
                                                               moduleName, versionValue, jClassName, jClassName,
                                                               jMethodPramTypes);
                } else {
                    error err = error("native function not available: " + pkgName + lookupKey);
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

// TODO: remove name/org form Package and replace with ModuleID
function packageToModuleId(bir:Package module) returns bir:ModuleID {
    return {org : module.org.value, name : module.name.value, modVersion : module.versionValue.value};
}

// TODO: remove ImportModule type replace with ModuleID
function importModuleToModuleId(bir:ImportModule mod) returns bir:ModuleID {
     return {org: mod.modOrg.value, name: mod.modName.value, modVersion: mod.modVersion.value};
}

function addBuiltinImports(bir:ModuleID moduleId, bir:Package module) {

    // Add the builtin and utils modules to the imported list of modules
    bir:ImportModule builtinModule = {modOrg : {value:"ballerina"}, 
                                      modName : {value:"builtin"}, 
                                      modVersion : {value:""}};

    bir:ImportModule utilsModule = {modOrg : {value:"ballerina"}, 
                                      modName : {value:"utils"}, 
                                      modVersion : {value:""}};

    if (isSameModule(moduleId, builtinModule)) {
        return;
    }

    if (isSameModule(moduleId, utilsModule)) {
        module.importModules[module.importModules.length()] = builtinModule;
        return;
    }

    module.importModules[module.importModules.length()] = utilsModule;
}

function isSameModule(bir:ModuleID moduleId, bir:ImportModule importModule) returns boolean {
    if (moduleId.org != importModule.modOrg.value) {
        return false;
    } else if (moduleId.name != importModule.modName.value) {
        return false;
    } else {
        return moduleId.modVersion == importModule.modVersion.value;
    }
}

// TODO: this only works for 1000000 byte or less files, get a proper method in stdlib
function readFileFully(string path) returns byte[]  = external;

public function lookupExternClassName(string pkgName, string functionName) returns string? {
    return externalMapCache[pkgName + "/" + functionName];
}
