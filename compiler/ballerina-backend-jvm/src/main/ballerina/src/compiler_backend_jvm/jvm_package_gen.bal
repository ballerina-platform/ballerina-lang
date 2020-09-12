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

import ballerina/bir;
import ballerina/file;
import ballerina/io;
import ballerina/jvm;
import ballerina/stringutils;

type BIRFunctionWrapper record {
    string orgName;
    string moduleName;
    string versionValue;
    bir:Function func;
    string fullQualifiedClassName;
    string jvmMethodDescription;
    string? jvmMethodDescriptionBString = ();
};

DiagnosticLogger dlogger = new ();

map<BIRFunctionWrapper> birFunctionMap = {};

map<bir:TypeDef> typeDefMap = {};

map<string> globalVarClassNames = {};

map<bir:AsyncCall|bir:FPLoad> lambdas = {};

map<bir:Package> compiledPkgCache = {};

map<string> externalMapCache = {};

map<bir:ModuleID> dependentModules = {};

bir:ModuleID[] dependentModuleArray = [];

string currentClass = "";

int lambdaIndex = 0;

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

function lookupJavaMethodDescription(string key, boolean useBString) returns string {
    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return useBString ? ( functionWrapper.jvmMethodDescriptionBString ?: "<error>") : functionWrapper.jvmMethodDescription;
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

function generateDependencyList(bir:ModuleID moduleId, @tainted JarFile jarFile,
                                jvm:InteropValidator interopValidator) {
    generatePackage(moduleId, jarFile, interopValidator, false);
    string pkgName = getPackageName(moduleId.org, moduleId.name);
    if (!dependentModules.hasKey(pkgName)) {
        dependentModules[pkgName] = moduleId;
    }
}

function injectBStringFunctions(bir:Package module) {

    bir:Function?[] functions = module.functions;
    if (functions.length() > 0) {
        int funcSize = functions.length();
        int count  = 3;

        bir:Function[] bStringFuncs = [];
        while (count < funcSize) {
            bir:Function birFunc = <bir:Function>functions[count];
            count = count + 1;
            if (IS_BSTRING) {
                var bStringFunc = birFunc.clone();
                bStringFunc.name = {value: nameOfBStringFunc(birFunc.name.value)};
                bStringFuncs.push(bStringFunc);
            }
        }

        foreach var func in bStringFuncs {
            functions.push(func);
        }
    }

}

public function generatePackage(bir:ModuleID moduleId, @tainted JarFile jarFile,
                                jvm:InteropValidator interopValidator, boolean isEntry) {
    string orgName = moduleId.org;
    string moduleName = moduleId.name;
    string pkgName = getPackageName(orgName, moduleName);

    [bir:Package, boolean] [ module, isFromCache ] = lookupModule(moduleId);
    if(!isEntry && isFromCache) {
        return;
    }

    addBuiltinImports(moduleId, module);

    // generate imported modules recursively
    foreach var mod in module.importModules {
        generateDependencyList(importModuleToModuleId(mod), jarFile, interopValidator);
        if (dlogger.getErrorCount() > 0) {
            return;
        }
    }

    injectBStringFunctions(module);

    typeOwnerClass = getModuleLevelClassName(<@untainted> orgName, <@untainted> moduleName, MODULE_INIT_CLASS_NAME);
    map<JavaClass> jvmClassMap = generateClassNameMappings(module, pkgName, typeOwnerClass,
                                                           <@untainted> lambdas, interopValidator);
    if (!isEntry || dlogger.getErrorCount() > 0) {
        return;
    }
    
    injectDefaultParamInits(module);
    injectDefaultParamInitsToAttachedFuncs(module);
    // create dependant modules flat array
    createDependantModuleFlatArray();
    // enrich current package with package initializers
    enrichPkgWithInitializers(jvmClassMap, typeOwnerClass, module, dependentModuleArray);

    // generate the shutdown listener class.
    generateShutdownSignalListener(module, typeOwnerClass, jarFile.pkgEntries);

    boolean serviceEPAvailable = isServiceDefAvailable(module.typeDefs);

    // Desugar the record init function
    rewriteRecordInits(module.typeDefs);

    // generate object/record value classes
    ObjectGenerator objGen = new(module);
    objGen.generateValueClasses(module.typeDefs, jarFile.pkgEntries);
    generateFrameClasses(module, jarFile.pkgEntries);

    foreach var [ moduleClass, v ] in jvmClassMap.entries() {
        jvm:ClassWriter cw = new(COMPUTE_FRAMES);
        currentClass = <@untainted> moduleClass;
        if (moduleClass == typeOwnerClass) {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), VALUE_CREATOR, ());
            generateDefaultConstructor(cw, VALUE_CREATOR);
            generateUserDefinedTypeFields(cw, module.typeDefs);
            generateValueCreatorMethods(cw, module.typeDefs, moduleId);
            // populate global variable to class name mapping and generate them
            foreach var globalVar in module.globalVars {
                if (globalVar is bir:GlobalVariableDcl) {
                    generatePackageVariable(globalVar, cw);
                }
            }

            if (isEntry) {  // TODO: this check seems redundant: check and remove
                bir:Function? mainFunc = getMainFunc(module?.functions);
                string mainClass = "";
                if (mainFunc is bir:Function) {
                    mainClass = getModuleLevelClassName(<@untainted> orgName, <@untainted> moduleName,
                                       <@untainted> cleanupPathSeperators(cleanupBalExt(mainFunc.pos.sourceFileName)));
                }

                generateMainMethod(mainFunc, cw, module, mainClass, moduleClass, serviceEPAvailable);
                if (mainFunc is bir:Function) {
                    generateLambdaForMain(mainFunc, cw, module, mainClass, moduleClass);
                }
                generateLambdaForPackageInits(cw, module, mainClass, moduleClass, dependentModuleArray);
                jarFile.manifestEntries["Main-Class"] = moduleClass;
            }
            generateLockForVariable(cw);
            generateStaticInitializer(cw, moduleClass, serviceEPAvailable);
            generateCreateTypesMethod(cw, module.typeDefs);
            generateModuleInitializer(cw, module);
            generateExecutionStopMethod(cw, typeOwnerClass, module, dependentModuleArray);
        } else {
            cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, (), OBJECT, ());
            generateDefaultConstructor(cw, OBJECT);
        }
        cw.visitSource(v.sourceFileName);
        // generate methods
        foreach var func in v.functions {
            generateMethod(getFunction(func), cw, module, serviceName = getFunction(func).workerName.value);
        }
        // generate lambdas created during generating methods
        foreach var [name, call] in lambdas.entries() {
            generateLambdaMethod(call, cw, name);
        }
        // clear the lambdas
        lambdas = {};
        lambdaIndex = 0;
        cw.visitEnd();

        var result = cw.toByteArray();
        if (result is error) {
            logCompileError(result, module, module);
            jarFile.pkgEntries[moduleClass + ".class"] = [];
        } else {
            jarFile.pkgEntries[moduleClass + ".class"] = result;
        }
    }
}

function createDependantModuleFlatArray() {
    foreach var [k, id] in dependentModules.entries() {
        dependentModuleArray[dependentModuleArray.length()] = id;
    }
}

function generatePackageVariable(bir:GlobalVariableDcl globalVar, jvm:ClassWriter cw) {
    string varName = globalVar.name.value;
    bir:BType bType = globalVar.typeValue;
    generateField(cw, bType, varName, true);
}

function generateLockForVariable(jvm:ClassWriter cw) {
    string lockStoreClass = "L" + LOCK_STORE + ";";
    jvm:FieldVisitor fv;
    fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LOCK_STORE", lockStoreClass);
    fv.visitEnd();
}

function generateStaticInitializer(jvm:ClassWriter cw, string className,
                                    boolean serviceEPAvailable) {
   jvm:MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", (), ());

   string lockStoreClass = "L" + LOCK_STORE + ";";
   mv.visitTypeInsn(NEW, LOCK_STORE);
   mv.visitInsn(DUP);
   mv.visitMethodInsn(INVOKESPECIAL, LOCK_STORE, "<init>", "()V", false);
   mv.visitFieldInsn(PUTSTATIC, className, "LOCK_STORE", lockStoreClass);

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

function lookupModule(bir:ModuleID modId) returns [bir:Package, boolean] {
        string orgName = modId.org;
        string moduleName = modId.name;
        string versionName = modId.modVersion;

        var pkgFromCache = compiledPkgCache[orgName + moduleName];
        if (pkgFromCache is bir:Package) {
            return [pkgFromCache, true];
        }

        var cacheDir = findCacheDirFor(modId);
        var parsedPkg = bir:populateBIRModuleFromBinary(readFileFully(calculateBirCachePath(cacheDir, modId, ".bir")), true);
        var mappingFile = calculateBirCachePath(cacheDir, modId, ".map.json");
        if (file:exists(mappingFile)) {
            var externalMap = readMap(mappingFile);
            foreach var [key,val] in externalMap.entries() {
                externalMapCache[key] = val;
            }
        }
        compiledPkgCache[orgName + moduleName] = parsedPkg;
        return [parsedPkg, false];

}

function findCacheDirFor(bir:ModuleID modId) returns string {
    foreach var birCacheDir in birCacheDirs {
        string birPath = calculateBirCachePath(birCacheDir, modId, ".bir");
        if (file:exists(birPath)) {
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
    string className = cleanupSourceFileName(sourceFileName);
    // handle source file path start with '/'.
    if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
        className = className.substring(1, className.length());
    }
    if (moduleName != ".") {
        className = cleanupName(moduleName) + "/" + className;
    }

    if (!stringutils:equalsIgnoreCase(orgName, "$anon")) {
        className = cleanupName(orgName) + "/" + className;
    }

    return className;
}

function getPackageName(string orgName, string moduleName) returns string {
    string packageName = "";
    if (moduleName != ".") {
        packageName = cleanupName(moduleName) + "/";
    }

    if (!stringutils:equalsIgnoreCase(orgName, "$anon")) {
        packageName = cleanupName(orgName) + "/" + packageName;
    }

    return packageName;
}

function splitPkgName(string key) returns [string, string] {
    int index = stringutils:lastIndexOf(key, "/");
    string pkgName = key.substring(0, index);
    string functionName = key.substring(index + 1, key.length());
    return [pkgName, functionName];
}

function cleanupName(string name) returns string {
    return stringutils:replace(name, ".","_");
}

function cleanupSourceFileName(string name) returns string {
    return stringutils:replace(name, ".", FILE_NAME_PERIOD_SEPERATOR);
}

function cleanupPackageName(string pkgName) returns string {
    int index = stringutils:lastIndexOf(pkgName, "/");
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
                                   map<bir:AsyncCall|bir:FPLoad> lambdaCalls,
                                   jvm:InteropValidator interopValidator) returns map<JavaClass> {
    
    string orgName = module.org.value;
    string moduleName = module.name.value;
    string versionValue = module.versionValue.value;
    map<JavaClass> jvmClassMap = {};

    foreach var globalVar in module.globalVars {
        if (globalVar is bir:GlobalVariableDcl) {
            globalVarClassNames[pkgName + globalVar.name.value] = initClass;
        }
    }

    globalVarClassNames[pkgName + "LOCK_STORE"] = initClass;
    // filter out functions.
    bir:Function?[] functions = module.functions;
    if (functions.length() > 0) {
        int funcSize = functions.length();
        int count  = 0;
        // Generate init class. Init function should be the first function of the package, hence check first
        // function.
        bir:Function initFunc = <bir:Function>functions[0];
        string functionName = initFunc.name.value;
        JavaClass class = { sourceFileName:initFunc.pos.sourceFileName, module'class:initClass };
        class.functions[0] = initFunc;
        addInitAndTypeInitInstructions(module, initFunc);
        jvmClassMap[initClass] = class;
        birFunctionMap[pkgName + functionName] = getFunctionWrapper(initFunc, orgName, moduleName,
                                                                    versionValue, initClass);
        count += 1;

        // Add start function
        bir:Function startFunc = <bir:Function>functions[1];
        functionName = startFunc.name.value;
        birFunctionMap[pkgName + functionName] = getFunctionWrapper(startFunc, orgName, moduleName,
                                                                    versionValue, initClass);
        class.functions[1] = startFunc;
        count += 1;

        // Add stop function
        bir:Function stopFunc = <bir:Function>functions[2];
        functionName = stopFunc.name.value;
        birFunctionMap[pkgName + functionName] = getFunctionWrapper(stopFunc, orgName, moduleName,
                                                                    versionValue, initClass);
        class.functions[2] = stopFunc;
        count += 1;

        // Generate classes for other functions.
        while (count < funcSize) {
            bir:Function birFunc = <bir:Function>functions[count];
            count = count + 1;
            // link the bir function for lookup
            string birFuncName = birFunc.name.value;
            string balFileName = birFunc.pos.sourceFileName;
            string birModuleClassName = getModuleLevelClassName(<@untainted> orgName, <@untainted> moduleName,
                                                        <@untainted> cleanupPathSeperators(cleanupBalExt(balFileName)));

            if !isBallerinaBuiltinModule(orgName, moduleName) {
                var javaClass = jvmClassMap[birModuleClassName];
                if (javaClass is JavaClass) {
                    javaClass.functions[javaClass.functions.length()] = birFunc;
                } else {
                    class = { sourceFileName:balFileName, module'class:birModuleClassName };
                    class.functions[0] = birFunc;
                    jvmClassMap[birModuleClassName] = class;
                }
            }

            BIRFunctionWrapper | error birFuncWrapperOrError;
            if (isExternFunc(getFunction(birFunc))) {
                birFuncWrapperOrError = createExternalFunctionWrapper(interopValidator, birFunc, orgName, moduleName,
                                                    versionValue, birModuleClassName);
            } else {
                addDefaultableBooleanVarsToSignature(birFunc);
                birFuncWrapperOrError = getFunctionWrapper(birFunc, orgName, moduleName, versionValue, birModuleClassName);
            }

            if (birFuncWrapperOrError is error) {
                dlogger.logError(<@untainted> birFuncWrapperOrError, <@untainted> birFunc.pos, <@untainted> module);
                continue;
            } else {
                birFunctionMap[pkgName + birFuncName] = birFuncWrapperOrError;
            }
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

        if ((bType is bir:BObjectType && !bType.isAbstract) || bType is bir:BServiceType) {
            bir:Function?[] attachedFuncs = getFunctions(typeDef.attachedFuncs);
            string typeName = "";
            if (bType is bir:BObjectType) {
                typeName = bType.name.value;
            } else {
                typeName = bType.oType.name.value;
            }
            foreach var func in attachedFuncs {

                // link the bir function for lookup
                bir:Function currentFunc = getFunction(func);
                string functionName = currentFunc.name.value;
                string lookupKey = typeName + "." + functionName;

                if (!isExternFunc(currentFunc)) {
                    string className = getTypeValueClassName(module, typeName);
                    birFunctionMap[pkgName + lookupKey] = getFunctionWrapper(currentFunc, orgName, moduleName,
                                                                        versionValue, className);
                    continue;
                }

                var jClassName = lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
                if (jClassName is string) {
                    birFunctionMap[pkgName + lookupKey] = createOldStyleExternalFunctionWrapper(currentFunc, orgName,
                                                               moduleName, versionValue, jClassName, jClassName);
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
    bir:VariableDcl? receiver = currentFunc.receiver;
    bir:BType? attachedType = receiver is bir:VariableDcl ? receiver.typeValue : ();
    string jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc?.retType,
                                                attachedType = attachedType);
    string jvmMethodDescriptionBString = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc?.retType,
                                                attachedType = attachedType, useBString = true);
    return {
        orgName : orgName,
        moduleName : moduleName,
        versionValue : versionValue,
        func : currentFunc,
        fullQualifiedClassName : moduleClass,
        jvmMethodDescriptionBString : jvmMethodDescriptionBString,
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
    bir:ImportModule annotationsModule = {modOrg : {value:"ballerina"},
                                          modName : {value:"lang.annotations"},
                                          modVersion : {value:""}};

    if (isSameModule(moduleId, annotationsModule)) {
        return;
    }

    module.importModules[module.importModules.length()] = annotationsModule;

    if (isLangModule(moduleId)) {
        return;
    }

    bir:ImportModule internalModule = {modOrg : {value:"ballerina"},
                                          modName : {value:"lang.__internal"},
                                          modVersion : {value:""}};

    if (isSameModule(moduleId, internalModule)) {
        return;
    }

    module.importModules[module.importModules.length()] = internalModule;

    bir:ImportModule langArrayModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.array"},
                                         modVersion : {value:""}};

    bir:ImportModule langDecimalModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.decimal"},
                                         modVersion : {value:""}};

    bir:ImportModule langErrorModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.error"},
                                         modVersion : {value:""}};

    bir:ImportModule langFloatModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.float"},
                                         modVersion : {value:""}};

    bir:ImportModule langFutureModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.future"},
                                         modVersion : {value:""}};

    bir:ImportModule langIntModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.int"},
                                         modVersion : {value:""}};

    bir:ImportModule langMapModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.map"},
                                         modVersion : {value:""}};

    bir:ImportModule langObjectModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.object"},
                                         modVersion : {value:""}};

    bir:ImportModule langStreamModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.stream"},
                                         modVersion : {value:""}};

    bir:ImportModule langStringModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.string"},
                                         modVersion : {value:""}};

    bir:ImportModule langTableModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.table"},
                                         modVersion : {value:""}};

    bir:ImportModule langValueModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.value"},
                                         modVersion : {value:""}};

    bir:ImportModule langXmlModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.xml"},
                                         modVersion : {value:""}};

    bir:ImportModule langTypedescModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.typedesc"},
                                         modVersion : {value:""}};

    bir:ImportModule langBooleanModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.boolean"},
                                         modVersion : {value:""}};

    bir:ImportModule langQueryModule = {modOrg : {value:"ballerina"},
                                         modName : {value:"lang.query"},
                                         modVersion : {value:""}};

    module.importModules[module.importModules.length()] = langArrayModule;
    module.importModules[module.importModules.length()] = langDecimalModule;
    module.importModules[module.importModules.length()] = langErrorModule;
    module.importModules[module.importModules.length()] = langFloatModule;
    module.importModules[module.importModules.length()] = langFutureModule;
    module.importModules[module.importModules.length()] = langIntModule;
    module.importModules[module.importModules.length()] = langMapModule;
    module.importModules[module.importModules.length()] = langObjectModule;
    module.importModules[module.importModules.length()] = langStreamModule;
    module.importModules[module.importModules.length()] = langStringModule;
    module.importModules[module.importModules.length()] = langTableModule;
    module.importModules[module.importModules.length()] = langValueModule;
    module.importModules[module.importModules.length()] = langXmlModule;
    module.importModules[module.importModules.length()] = langTypedescModule;
    module.importModules[module.importModules.length()] = langBooleanModule;
    module.importModules[module.importModules.length()] = langQueryModule;
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

function isLangModule(bir:ModuleID moduleId) returns boolean{
    if (moduleId.org != "ballerina") {
        return false;
    }
    return moduleId.name.indexOf("lang.") == 0;
}

// TODO: this only works for 1000000 byte or less files, get a proper method in stdlib
function readFileFully(string path) returns byte[]  = external;

public function lookupExternClassName(string pkgName, string functionName) returns string? {
    return externalMapCache[cleanupName(pkgName) + "/" + nameOfNonBStringFunc(functionName)];
}

function generateShutdownSignalListener(bir:Package pkg, string initClass, map<byte[]> jarEntries) {
    string innerClassName = initClass + "$SignalListener";
    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_SUPER, innerClassName, (), JAVA_THREAD, ());

    // create constructor
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", (), ());
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, JAVA_THREAD, "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    // implement run() method
    mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", (), ());
    mv.visitCode();

    mv.visitMethodInsn(INVOKESTATIC, initClass, MODULE_STOP, "()V", false);

    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();
    jarEntries[innerClassName + ".class"] = checkpanic cw.toByteArray();
}
