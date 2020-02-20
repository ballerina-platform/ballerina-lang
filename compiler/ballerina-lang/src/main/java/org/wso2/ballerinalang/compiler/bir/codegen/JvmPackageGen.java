/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRImportModule;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FILE_NAME_PERIOD_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_PACKAGE_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CREATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.rewriteRecordInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen.DiagnosticLogger;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.IS_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.addInitAndTypeInitInstructions;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupBalExt;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.cleanupPathSeperators;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.enrichPkgWithInitializers;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateDefaultConstructor;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateExecutionStopMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateFrameClasses;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateLambdaForMain;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateLambdaForPackageInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateLambdaMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateMainMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.generateModuleInitializer;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunction;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getFunctions;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getMainFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getMethodDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfNonBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateCreateTypesMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateUserDefinedTypeFields;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateValueCreatorMethods;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.isServiceDefAvailable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.typeOwnerClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.typeRefToClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.ObjectGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.injectDefaultParamInitsToAttachedFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.createExternalFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.createOldStyleExternalFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.injectDefaultParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.isBallerinaBuiltinModule;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewInstance;

/**
 * BIR module to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmPackageGen {

    public static DiagnosticLogger dlogger = new DiagnosticLogger();

    public static Map<String, BIRFunctionWrapper> birFunctionMap = new HashMap<>();
    public static Map<String, BIRTypeDefinition> typeDefMap = new HashMap<>();
    public static Map<String, String> globalVarClassNames = new HashMap<>();
    public static Map<String, BIRInstruction> lambdas = new HashMap<>();
    public static Map<String, BIRPackage> compiledPkgCache = new HashMap<>();
    public static Map<String, String> externalMapCache = new HashMap<>();
    public static Map<String, PackageID> dependentModules = new HashMap<>();
    public static List<PackageID> dependentModuleArray = new ArrayList<>();
    public static String currentClass = "";
    public static int lambdaIndex = 0;
    public static SymbolTable symbolTable = null;

    static class JarFile {

        Map<String, String> manifestEntries = new HashMap<>();
        Map<String, byte[]> pkgEntries = new HashMap<>();
    }

    static class JavaClass {

        String sourceFileName;
        String moduleClass;
        @Nilable
        List<BIRFunction> functions = new ArrayList<>();

        JavaClass(String sourceFileName, String moduleClass) {

            this.sourceFileName = sourceFileName;
            this.moduleClass = moduleClass;
        }
    }

    String[] splitPkgName(String key) {

        int index = key.lastIndexOf("/");
        String pkgName = key.substring(0, index);
        String functionName = key.substring(index + 1);
        return new String[]{pkgName, functionName};
    }

    static String lookupFullQualifiedClassName(String key) {

        if (birFunctionMap.containsKey(key)) {
            BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap.get(key));
            return functionWrapper.fullQualifiedClassName;
        } else {
            throw new BLangCompilerException("cannot find full qualified class for : " + key);
        }
    }

    static BIRTypeDefinition lookupTypeDef(NewInstance objectNewIns) {

        if (!objectNewIns.isExternalDef) {
            return objectNewIns.def;
        } else {
            String className = typeRefToClassName(objectNewIns.externalPackageId, objectNewIns.objectName);
            BIRTypeDefinition typeDef = typeDefMap.get(className);
            if (typeDef != null) {
                return typeDef;
            }

            throw new BLangCompilerException("Reference to unknown type " + className);
        }
    }

    static String lookupJavaMethodDescription(String key, boolean useBString) {

        if (birFunctionMap.containsKey(key)) {
            BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap.get(key));
            return useBString ? (functionWrapper.jvmMethodDescriptionBString != null ?
                    functionWrapper.jvmMethodDescriptionBString : "<error>") : functionWrapper.jvmMethodDescription;
        } else {
            throw new BLangCompilerException("cannot find jvm method description for : " + key);
        }
    }

    static boolean isBIRFunctionExtern(String key) {

        if (birFunctionMap.containsKey(key)) {
            BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap.get(key));
            return isExternFunc(functionWrapper.func);
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + key);
        }
    }

    static BIRFunctionWrapper getBIRFunctionWrapper(@Nilable BIRFunctionWrapper wrapper) {

        if (wrapper != null) {
            return wrapper;
        } else {
            throw new BLangCompilerException("invalid bir function linking");
        }
    }

    static String lookupGlobalVarClassName(String key) {

        String result = globalVarClassNames.get(key);
        if (result != null) {
            return result;
        } else {
            throw new BLangCompilerException("cannot find full qualified class for global variable : " + key);
        }
    }

    private static void generateDependencyList(BIRPackage moduleId, JarFile jarFile,
                                               InteropValidator interopValidator) {

        generatePackage(moduleId, jarFile, interopValidator, false);
        String pkgName = getPackageName(moduleId.org, moduleId.name);
        if (!dependentModules.containsKey(pkgName)) {
            dependentModules.put(pkgName, packageToModuleId(moduleId));
        }
    }

    static void injectBStringFunctions(BIRPackage module) {

        @Nilable List<BIRFunction> functions = module.functions;
        if (functions.size() > 0) {
            int funcSize = functions.size();
            int count = 3;

            List<BIRFunction> bStringFuncs = new ArrayList<>();
            while (count < funcSize) {
                BIRFunction birFunc = functions.get(count);
                count = count + 1;
                if (IS_BSTRING) {
                    BIRFunction bStringFunc = birFunc.duplicate();
                    bStringFunc.name = new Name(nameOfBStringFunc(birFunc.name.value));
                    bStringFuncs.add(bStringFunc);
                }
            }

            functions.addAll(bStringFuncs);
        }

    }

    static void generatePackage(BIRNode.BIRPackage moduleId, JarFile jarFile,
                                InteropValidator interopValidator, boolean isEntry) {

        String orgName = moduleId.org.value;
        String moduleName = moduleId.name.value;
        String pkgName = getPackageName(orgName, moduleName);

        Map.Entry<BIRPackage, Boolean> pair = lookupModule(moduleId);
        boolean isFromCache = pair.getValue();
        BIRPackage module = pair.getKey();
        if (!isEntry && isFromCache) {
            return;
        }
//
//        addBuiltinImports(moduleId, module);

        // generate imported modules recursively
//        for (BIRImportModule mod : module.importModules) {
        // TODO fix imported module's linking
//            generateDependencyList(importModuleToModuleId(mod), jarFile, interopValidator);
//            if (DiagnosticLogger.getErrorCount() > 0) {
//                return;
//            }
//        }

        injectBStringFunctions(module);

        typeOwnerClass = getModuleLevelClassName(orgName, moduleName, MODULE_INIT_CLASS_NAME);
        Map<String, JavaClass> jvmClassMap = generateClassNameMappings(module, pkgName, typeOwnerClass,
                interopValidator);
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
        ObjectGenerator objGen = new ObjectGenerator(module);
        objGen.generateValueClasses(module.typeDefs, jarFile.pkgEntries);
        generateFrameClasses(module, jarFile.pkgEntries);
        for (Map.Entry<String, JavaClass> entry : jvmClassMap.entrySet()) {
            String moduleClass = entry.getKey();
            JavaClass v = entry.getValue();
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            currentClass = moduleClass;
            if (Objects.equals(moduleClass, typeOwnerClass)) {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, VALUE_CREATOR, null);
                generateDefaultConstructor(cw, VALUE_CREATOR);
                generateUserDefinedTypeFields(cw, module.typeDefs);
                generateValueCreatorMethods(cw, module.typeDefs, moduleId);
                // populate global variable to class name mapping and generate them
                for (BIRGlobalVariableDcl globalVar : module.globalVars) {
                    if (globalVar != null) {
                        generatePackageVariable(globalVar, cw);
                        generateLockForVariable(globalVar, cw);
                    }
                }

                if (isEntry) {  // TODO: this check seems redundant: check and remove
                    @Nilable BIRFunction mainFunc = getMainFunc(module.functions);
                    String mainClass = "";
                    if (mainFunc != null) {
                        mainClass = getModuleLevelClassName(orgName, moduleName,
                                cleanupPathSeperators(cleanupBalExt(mainFunc.pos.getSource().cUnitName)));
                    }

                    generateMainMethod(mainFunc, cw, module, mainClass, moduleClass, serviceEPAvailable);
                    if (mainFunc != null) {
                        generateLambdaForMain(mainFunc, cw, module, mainClass, moduleClass);
                    }
                    generateLambdaForPackageInits(cw, module, mainClass, moduleClass, dependentModuleArray);
                    jarFile.manifestEntries.put("Main-Class", moduleClass);
                }
                generateStaticInitializer(module.globalVars, cw, moduleClass, serviceEPAvailable);
                generateCreateTypesMethod(cw, module.typeDefs);
                generateModuleInitializer(cw, module);
                generateExecutionStopMethod(cw, typeOwnerClass, module, dependentModuleArray);
            } else {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, OBJECT, null);
                generateDefaultConstructor(cw, OBJECT);
            }
            cw.visitSource(v.sourceFileName, null);
            // generate methods
            for (BIRFunction func : v.functions) {
                String workerName = getFunction(func).workerName == null ? null : func.workerName.value;
                generateMethod(getFunction(func), cw, module, null, false, workerName);
            }
            // generate lambdas created during generating methods
            for (Map.Entry<String, BIRInstruction> l : lambdas.entrySet()) {
                String name = l.getKey();
                BIRInstruction call = l.getValue();
                generateLambdaMethod(call, cw, name);
            }
//        foreach var [name, call] in lambdas.entries() {
//        }
            // clear the lambdas
            lambdas = new HashMap<>();
            lambdaIndex = 0;
            cw.visitEnd();

            byte[] result = cw.toByteArray();
            if (result == null) {
//                logCompileError(result, module, module);
                // TODO log error
                jarFile.pkgEntries.put(moduleClass + ".class", new byte[0]);
            } else {
                jarFile.pkgEntries.put(moduleClass + ".class", result);
            }
        }

    }

    private static void createDependantModuleFlatArray() {

        for (Map.Entry<String, PackageID> entry : dependentModules.entrySet()) {
            String k = entry.getKey();
            PackageID id = entry.getValue();
            dependentModuleArray.add(id);
        }
    }

    private static void generatePackageVariable(BIRGlobalVariableDcl globalVar, ClassWriter cw) {

        String varName = globalVar.name.value;
        BType bType = globalVar.type;
        generateField(cw, bType, varName, true);
    }

    private static void generateLockForVariable(BIRGlobalVariableDcl globalVar, ClassWriter cw) {

        String lockClass = "L" + LOCK_VALUE + ";";
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, computeLockName(globalVar), lockClass, null, null);
        fv.visitEnd();
    }

    private static void generateStaticInitializer(@Nilable List<BIRGlobalVariableDcl> globalVars, ClassWriter cw,
                                                  String className, boolean serviceEPAvailable) {

        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);

        String lockClass = "L" + LOCK_VALUE + ";";
        for (BIRGlobalVariableDcl globalVar : globalVars) {
            if (globalVar != null) {
                mv.visitTypeInsn(NEW, LOCK_VALUE);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, "<init>", "()V", false);
                mv.visitFieldInsn(PUTSTATIC, className, computeLockName(globalVar), lockClass);
            }
        }

        setServiceEPAvailableField(cw, mv, serviceEPAvailable, className);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void setServiceEPAvailableField(ClassWriter cw, MethodVisitor mv, boolean serviceEPAvailable,
                                                   String initClass) {

        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, "serviceEPAvailable", "Z", null, null);
        fv.visitEnd();

        if (serviceEPAvailable) {
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, initClass, "serviceEPAvailable", "Z");
        } else {
            mv.visitInsn(ICONST_0);
            mv.visitFieldInsn(PUTSTATIC, initClass, "serviceEPAvailable", "Z");
        }
    }

    private static String computeLockName(BIRGlobalVariableDcl globalVar) {

        String varName = globalVar.name.value;
        return computeLockNameFromString(varName);
    }

    static String computeLockNameFromString(String varName) {

        return "$lock" + varName;
    }

    private static Map.Entry<BIRPackage, Boolean> lookupModule(BIRPackage modId) {

        String orgName = modId.org.value;
        String moduleName = modId.name.value;
        String versionName = modId.version.value;

        BIRPackage pkgFromCache = compiledPkgCache.get(orgName + moduleName);
        if (pkgFromCache != null) {
            return new AbstractMap.SimpleEntry<>(pkgFromCache, true);
        }

//        var cacheDir = findCacheDirFor(modId);
//        BIRPackage parsedPkg = BIRpopulateBIRModuleFromBinary(readFileFully(calculateBirCachePath(cacheDir,
//        modId, ".bir")), true);
//        var mappingFile = calculateBirCachePath(cacheDir, modId, ".map.json");
//        if (file:exists(mappingFile)){
//            var externalMap = readMap(mappingFile);
//            foreach[] var key, val]in externalMap.entries() {
//                externalMapCache.add(key, val);
//            }
//        }
//        compiledPkgCache[orgName + moduleName] = parsedPkg;
////        return [parsedPkg, false]
//        return new AbstractMap.SimpleEntry<>(parsedPkg, false);
        return null;
    }

//    static String findCacheDirFor(PackageID modId) {
//        for (String birCacheDir : birCacheDirs) {
//            String birPath = calculateBirCachePath(birCacheDir, modId, ".bir");
//            if (file:exists(birPath)){
//                return birCacheDir;
//            }
//        }
//        BLangCompilerException e = new BLangCompilerException(String.format("%s not found in %s",
//        calculateBirCachePath("", modId, ".bir"), birCacheDirs));
//        throw e;
//    }

//    static String calculateBirCachePath(String birCacheDir, PackageID modId, String ext) {
//        String nonEmptyVersion = modId.version.value;
//        if (nonEmptyVersion.equals("")) {
//            nonEmptyVersion = "0.0.0";
//        }
//
//        return birCacheDir + "/" + modId.orgName.value + "/" + modId.name.value + "/" + nonEmptyVersion + "/" +
//                modId.name.value + ext;
//    }

    static String getModuleLevelClassName(String orgName, String moduleName, String sourceFileName) {

        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }
        if (!moduleName.equals(".")) {
            className = cleanupName(moduleName) + "/" + className;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            className = cleanupName(orgName) + "/" + className;
        }

        return className;
    }

    static String getPackageName(Name orgName, Name moduleName) {

        return getPackageName(orgName.getValue(), moduleName.getValue());
    }

    public static String getPackageName(String orgName, String moduleName) {

        String packageName = "";
        if (!moduleName.equals(".")) {
            packageName = cleanupName(moduleName) + "/";
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            packageName = cleanupName(orgName) + "/" + packageName;
        }

        return packageName;
    }

    private static String cleanupName(String name) {

        return name.replace(".", "_");
    }

    private static String cleanupSourceFileName(String name) {

        return name.replace(".", FILE_NAME_PERIOD_SEPERATOR);
    }

    public static String cleanupPackageName(String pkgName) {

        int index = pkgName.lastIndexOf("/");
        if (index > 0) {
            return pkgName.substring(0, index);
        } else {
            return pkgName;
        }
    }

    //# Java Class will be generate for each source file. This method add class mappings to globalVar and filters the
//# functions based on their source file name and then returns map of associated java class contents.
//#
//# + module - The module
//# + pkgName - The module package Name
//# + initClass - The module init class
//# + lambdaCalls - The lambdas
//# + return - The map of javaClass records on given source file name
    private static Map<String, JavaClass> generateClassNameMappings(BIRPackage module, String pkgName, String initClass,
                                                                    InteropValidator interopValidator) {

        String orgName = module.org.value;
        String moduleName = module.name.value;
        String version = module.version.value;
        Map<String, JavaClass> jvmClassMap = new HashMap<>();

        for (BIRGlobalVariableDcl globalVar : module.globalVars) {
            if (globalVar != null) {
                globalVarClassNames.put(pkgName + globalVar.name.value, initClass);
            }
        }
        // filter out functions.
        @Nilable List<BIRFunction> functions = module.functions;
        if (functions.size() > 0) {
            int funcSize = functions.size();
            int count = 0;
            // Generate init class. Init function should be the first function of the package, hence check first
            // function.
            BIRFunction initFunc = functions.get(0);
            String functionName = initFunc.name.value;
            JavaClass klass = new JavaClass(initFunc.pos.src.cUnitName, initClass);
            klass.functions.add(0, initFunc);
            addInitAndTypeInitInstructions(module, initFunc);
            jvmClassMap.put(initClass, klass);
            birFunctionMap.put(pkgName + functionName, getFunctionWrapper(initFunc, orgName, moduleName,
                    version, initClass));
            count += 1;

            // Add start function
            BIRFunction startFunc = functions.get(1);
            functionName = startFunc.name.value;
            birFunctionMap.put(pkgName + functionName, getFunctionWrapper(startFunc, orgName, moduleName,
                    version, initClass));
            klass.functions.add(1, startFunc);
            count += 1;

            // Add stop function
            BIRFunction stopFunc = functions.get(2);
            functionName = stopFunc.name.value;
            birFunctionMap.put(pkgName + functionName, getFunctionWrapper(stopFunc, orgName, moduleName,
                    version, initClass));
            klass.functions.add(2, stopFunc);
            count += 1;

            // Generate classes for other functions.
            while (count < funcSize) {
                BIRFunction birFunc = functions.get(count);
                count = count + 1;
                // link the bir function for lookup
                String birFuncName = birFunc.name.value;
                String balFileName = birFunc.pos.src.cUnitName;
                String birModuleClassName = getModuleLevelClassName(orgName, moduleName,
                        cleanupPathSeperators(cleanupBalExt(balFileName)));

                if (!isBallerinaBuiltinModule(orgName, moduleName)) {
                    JavaClass javaClass = jvmClassMap.get(birModuleClassName);
                    if (javaClass != null) {
                        javaClass.functions.add(birFunc);
                    } else {
                        klass = new JavaClass(balFileName, birModuleClassName);
                        klass.functions.add(0, birFunc);
                        jvmClassMap.put(birModuleClassName, klass);
                    }
                }

                BIRFunctionWrapper birFuncWrapperOrError;
                if (isExternFunc(getFunction(birFunc))) {
                    birFuncWrapperOrError = createExternalFunctionWrapper(interopValidator, birFunc, orgName,
                            moduleName, version, birModuleClassName);
                } else {
                    addDefaultableBooleanVarsToSignature(birFunc);
                    birFuncWrapperOrError = getFunctionWrapper(birFunc, orgName, moduleName, version,
                            birModuleClassName);
                }

                if (birFuncWrapperOrError == null) {
                    // todo handle error
//                    dlogger.logError(birFuncWrapperOrError, birFunc.pos, module);
//                    continue;
                } else {
                    birFunctionMap.put(pkgName + birFuncName, birFuncWrapperOrError);
                }
            }
        }

        // link typedef - object attached native functions
        @Nilable List<BIRTypeDefinition> typeDefs = module.typeDefs;

        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;

            if (bType.tag == TypeTags.OBJECT || bType.tag == TypeTags.RECORD) {
                String key = getModuleLevelClassName(orgName, moduleName, typeDef.name.value);
                typeDefMap.put(key, typeDef);
            }

            if ((bType.tag == TypeTags.OBJECT &&
                    !Symbols.isFlagOn(((BObjectType) bType).tsymbol.flags, Flags.ABSTRACT)) ||
                    bType.tag == TypeTags.SERVICE) {
                @Nilable List<BIRFunction> attachedFuncs = getFunctions(typeDef.attachedFuncs);
                String typeName = typeName = ((BObjectType) bType).name.getValue();
//                if (bType.tag == TypeTags.OBJECT) {
//                    typeName = ((BObjectType)bType).name.getValue();
//                } else {
//                    typeName = bType.oType.name.value;
//                }
                for (BIRFunction func : attachedFuncs) {

                    // link the bir function for lookup
                    BIRFunction currentFunc = getFunction(func);
                    String functionName = currentFunc.name.value;
                    String lookupKey = typeName + "." + functionName;

                    if (!isExternFunc(currentFunc)) {
                        String className = getTypeValueClassName(module, typeName);
                        birFunctionMap.put(pkgName + lookupKey, getFunctionWrapper(currentFunc, orgName, moduleName,
                                version, className));
                        continue;
                    }

                    String jClassName = lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
                    if (jClassName != null) {
                        birFunctionMap.put(pkgName + lookupKey, createOldStyleExternalFunctionWrapper(currentFunc,
                                orgName, moduleName, version, jClassName, jClassName));
                    } else {
                        throw new BLangCompilerException("native function not available: " +
                                pkgName + lookupKey);
                    }
                }
            }
        }
        return jvmClassMap;
    }

    public static BIRFunctionWrapper getFunctionWrapper(BIRFunction currentFunc, String orgName, String moduleName,
                                                        String version, String moduleClass) {

        BInvokableType functionTypeDesc = currentFunc.type;
        @Nilable BIRVariableDcl receiver = currentFunc.receiver;
        @Nilable BType attachedType = receiver != null ? receiver.type : null;
        String jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType,
                attachedType, false, false);
        String jvmMethodDescriptionBString = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType,
                attachedType, false, true);

        return new BIRFunctionWrapper(orgName, moduleName, version, currentFunc, moduleClass,jvmMethodDescription,
                                      jvmMethodDescriptionBString);
    }

    // TODO: remove name/org form Package and replace with ModuleID
    static PackageID packageToModuleId(BIRPackage mod) {
//        return {org :module.org.value, name :module.name.value, modVersion :module.version.value}
        return new PackageID(mod.org, mod.name, mod.version);
    }

    // TODO: remove ImportModule type replace with ModuleID
    private static PackageID importModuleToModuleId(BIRImportModule mod) {
//        return {org:mod.modOrg.value, name:mod.modName.value, modVersion:mod.modVersion.value}
        return new PackageID(mod.org, mod.name, mod.version);
    }

    private static void addBuiltinImports(BIRPackage moduleId, BIRPackage module) {
        // Add the builtin and utils modules to the imported list of modules
//        BIRImportModule annotationsModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.annotations"),
//        modVersion:
//        new (value:""))
//
//        if (isSameModule(moduleId, annotationsModule)) {
//            return;
//        }
//
//        module.importModules.add(annotationsModule);
//
//        if (isLangModule(moduleId)) {
//            return;
//        }
//
//        BIRImportModule internalModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.__internal"),
//        modVersion:
//        new (value:""))
//
//        if (isSameModule(moduleId, internalModule)) {
//            return;
//        }
//
//        module.importModules.add(internalModule);
//
//        BIRImportModule langArrayModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.array"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langDecimalModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.decimal"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langErrorModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.error"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langFloatModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.float"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langFutureModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.future"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langIntModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.int"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langMapModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.map"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langObjectModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.object"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langStringModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.string"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langTableModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.table"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langValueModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.value"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langXmlModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.xml"),
//        modVersion:
//        new (value:""))
//
//        BIRImportModule langTypedescModule = new BIRImportModule(modOrg :new (value:"ballerina"),
//        modName:
//        new (value:"lang.typedesc"),
//        modVersion:
//        new (value:""))
//        module.importModules.add(langArrayModule);
//        module.importModules.add(langDecimalModule);
//        module.importModules.add(langErrorModule);
//        module.importModules.add(langFloatModule);
//        module.importModules.add(langFutureModule);
//        module.importModules.add(langIntModule);
//        module.importModules.add(langMapModule);
//        module.importModules.add(langObjectModule);
//        module.importModules.add(langStringModule);
//        module.importModules.add(langTableModule);
//        module.importModules.add(langValueModule);
//        module.importModules.add(langXmlModule);
//        module.importModules.add(langTypedescModule);
    }

//    static boolean isSameModule(PackageID moduleId, BIRImportModule importModule) {
//        if (moduleId.org != importModule.modOrg.value) {
//            return false;
//        } else if (moduleId.name != importModule.modName.value) {
//            return false;
//        } else {
//            return moduleId.modVersion == importModule.modVersion.value;
//        }
//    }
//
//    static boolean isLangModule(PackageID moduleId) {
//        if (!moduleId.orgName.value.equals("ballerina")) {
//            return false;
//        }
//        return moduleId.name.value.indexOf("lang.") == 0;
//    }

//    // TODO: this only works for 1000000 byte or less files, get a proper method in stdlib
//    static void readFileFully(String path) returns

    public static @Nilable
    String lookupExternClassName(String pkgName, String functionName) {

        return externalMapCache.get(cleanupName(pkgName) + "/" + nameOfNonBStringFunc(functionName));
    }

    static void generateShutdownSignalListener(BIRPackage pkg, String initClass, Map<String, byte[]> jarEntries) {

        String innerClassName = initClass + "$SignalListener";
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_SUPER, innerClassName, null, JAVA_THREAD, null);

        // create constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_THREAD, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // implement run() method
        mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();

        mv.visitMethodInsn(INVOKESTATIC, initClass, MODULE_STOP, "()V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        jarEntries.put(innerClassName + ".class", cw.toByteArray());
    }

    public static class BIRFunctionWrapper {

        public String orgName;
        public String moduleName;
        public String version;
        public BIRFunction func;
        public String fullQualifiedClassName;
        public String jvmMethodDescription;
        @Nilable
        public String jvmMethodDescriptionBString;

        protected BIRFunctionWrapper(String orgName, String moduleName, String version, BIRFunction func,
                                     String fullQualifiedClassName, String jvmMethodDescription,
                                     String jvmMethodDescriptionBString) {

            this.orgName = orgName;
            this.moduleName = moduleName;
            this.version = version;
            this.func = func;
            this.fullQualifiedClassName = fullQualifiedClassName;
            this.jvmMethodDescription = jvmMethodDescription;
            this.jvmMethodDescriptionBString = jvmMethodDescriptionBString;
        }
    }
}
