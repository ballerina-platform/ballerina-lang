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
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.objectweb.asm.ClassTooLargeException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodTooLargeException;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JavaClass;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JInteropException;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewInstance;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_EP_AVAILABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CREATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.rewriteRecordInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateCreateTypesMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateUserDefinedTypeFields;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.generateValueCreatorMethods;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.isServiceDefAvailable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.injectDefaultParamInitsToAttachedFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.createExternalFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.injectDefaultParamInits;

/**
 * BIR module to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmPackageGen {

    private static ResolvedTypeBuilder typeBuilder;

    public final SymbolTable symbolTable;
    public final PackageCache packageCache;
    private final JvmMethodGen jvmMethodGen;
    private Map<String, BIRFunctionWrapper> birFunctionMap;
    private Map<String, String> externClassMap;
    private Map<String, String> globalVarClassMap;
    private Map<String, PackageID> dependentModules;
    private BLangDiagnosticLog dlog;

    JvmPackageGen(SymbolTable symbolTable, PackageCache packageCache, BLangDiagnosticLog dlog) {

        birFunctionMap = new HashMap<>();
        globalVarClassMap = new HashMap<>();
        externClassMap = new HashMap<>();
        dependentModules = new LinkedHashMap<>();
        this.symbolTable = symbolTable;
        this.packageCache = packageCache;
        this.dlog = dlog;
        jvmMethodGen = new JvmMethodGen(this);
        typeBuilder = new ResolvedTypeBuilder();

        JvmCastGen.symbolTable = symbolTable;
        JvmInstructionGen.anyType = symbolTable.anyType;
    }

    private static String getBvmAlias(String orgName, String moduleName) {

        if (Names.ANON_ORG.value.equals(orgName)) {
            return moduleName;
        }
        return orgName + "/" + moduleName;
    }

    private static void addBuiltinImports(BIRPackage currentModule, Set<PackageID> dependentModuleArray) {
        Name ballerinaOrgName = new Name("ballerina");
        Name builtInVersion = new Name("");

        // Add the builtin and utils modules to the imported list of modules
        if (isSameModule(currentModule, PackageID.ANNOTATIONS)) {
            return;
        }

        dependentModuleArray.add(PackageID.ANNOTATIONS);

        if (isSameModule(currentModule, PackageID.JAVA)) {
            return;
        }

        dependentModuleArray.add(PackageID.JAVA);

        if (isLangModule(currentModule)) {
            return;
        }

        if (isSameModule(currentModule, PackageID.INTERNAL)) {
            return;
        }
        dependentModuleArray.add(PackageID.INTERNAL);
        dependentModuleArray.add(PackageID.ARRAY);
        dependentModuleArray.add(PackageID.DECIMAL);
        dependentModuleArray.add(PackageID.ERROR);
        dependentModuleArray.add(PackageID.FLOAT);
        dependentModuleArray.add(PackageID.FUTURE);
        dependentModuleArray.add(PackageID.INT);
        dependentModuleArray.add(PackageID.MAP);
        dependentModuleArray.add(PackageID.OBJECT);
        dependentModuleArray.add(PackageID.STREAM);
        dependentModuleArray.add(PackageID.STRING);
        dependentModuleArray.add(PackageID.TABLE);
        dependentModuleArray.add(PackageID.VALUE);
        dependentModuleArray.add(PackageID.XML);
        dependentModuleArray.add(PackageID.TYPEDESC);
        dependentModuleArray.add(PackageID.BOOLEAN);
        dependentModuleArray.add(PackageID.QUERY);
        dependentModuleArray.add(PackageID.TRANSACTION);
    }

    private static boolean isSameModule(BIRPackage moduleId, PackageID importModule) {

        if (!moduleId.org.value.equals(importModule.orgName.value)) {
            return false;
        } else if (!moduleId.name.value.equals(importModule.name.value)) {
            return false;
        } else {
            return moduleId.version.value.equals(importModule.version.value);
        }
    }

    private static boolean isLangModule(BIRPackage moduleId) {

        if (!BALLERINA.equals(moduleId.org.value)) {
            return false;
        }
        return moduleId.name.value.indexOf("lang.") == 0 || moduleId.name.equals(Names.JAVA);
    }

    private static void generatePackageVariable(BIRGlobalVariableDcl globalVar, ClassWriter cw) {

        String varName = globalVar.name.value;
        BType bType = globalVar.type;
        String typeSig = JvmCodeGenUtil.getFieldTypeSignature(bType);
        cw.visitField(ACC_PUBLIC + ACC_STATIC, varName, typeSig, null, null).visitEnd();

    }

    private static void generateLockForVariable(ClassWriter cw) {

        String lockStoreClass = "L" + LOCK_STORE + ";";
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, LOCK_STORE_VAR_NAME, lockStoreClass, null, null);
        fv.visitEnd();
    }

    private static void generateStaticInitializer(ClassWriter cw, String className,
                                                  BIRPackage module, boolean isInitClass,
                                                  boolean serviceEPAvailable, AsyncDataCollector asyncDataCollector) {

        if (!isInitClass && asyncDataCollector.getStrandMetadata().isEmpty()) {
            return;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        if (isInitClass) {
            String lockStoreClass = "L" + LOCK_STORE + ";";
            mv.visitTypeInsn(NEW, LOCK_STORE);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, LOCK_STORE, JVM_INIT_METHOD, "()V", false);
            mv.visitFieldInsn(PUTSTATIC, className, LOCK_STORE_VAR_NAME, lockStoreClass);
            setServiceEPAvailableField(cw, mv, serviceEPAvailable, className);
            setModuleStatusField(cw, mv, className);
        }
        JvmCodeGenUtil.generateStrandMetadata(mv, className, module, asyncDataCollector);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void setModuleStatusField(ClassWriter cw, MethodVisitor mv, String initClass) {

        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, MODULE_START_ATTEMPTED, "Z", null, null);
        fv.visitEnd();

        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, initClass, MODULE_START_ATTEMPTED, "Z");

        fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, MODULE_STARTED, "Z", null, null);
        fv.visitEnd();

        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, initClass, MODULE_STARTED, "Z");
    }

    private static void setServiceEPAvailableField(ClassWriter cw, MethodVisitor mv, boolean serviceEPAvailable,
                                                   String initClass) {

        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, SERVICE_EP_AVAILABLE, "Z", null, null);
        fv.visitEnd();

        if (serviceEPAvailable) {
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTSTATIC, initClass, SERVICE_EP_AVAILABLE, "Z");
        } else {
            mv.visitInsn(ICONST_0);
            mv.visitFieldInsn(PUTSTATIC, initClass, SERVICE_EP_AVAILABLE, "Z");
        }
    }

    static String computeLockNameFromString(String varName) {

        return "$lock" + varName;
    }

    public static String cleanupPackageName(String pkgName) {

        int index = pkgName.lastIndexOf("/");
        if (index > 0) {
            return pkgName.substring(0, index);
        } else {
            return pkgName;
        }
    }

    public static BIRFunctionWrapper getFunctionWrapper(BIRFunction currentFunc, String orgName, String moduleName,
                                                        String version, String moduleClass) {

        BInvokableType functionTypeDesc = currentFunc.type;
        BIRVariableDcl receiver = currentFunc.receiver;

        BType retType = functionTypeDesc.retType;
        if (isExternFunc(currentFunc) && Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = typeBuilder.build(retType);
        }

        String jvmMethodDescription;
        if (receiver == null) {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes, retType);
        } else {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes, retType, receiver.type);
        }

        return new BIRFunctionWrapper(orgName, moduleName, version, currentFunc, moduleClass, jvmMethodDescription);
    }

    private static void generateShutdownSignalListener(String initClass, Map<String, byte[]> jarEntries) {

        String innerClassName = initClass + "$SignalListener";
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_SUPER, innerClassName, null, JAVA_THREAD, null);

        // create constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_THREAD, JVM_INIT_METHOD, "()V", false);
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

    private static BIRFunction findFunction(BIRNode parentNode, String funcName) {

        BIRFunction func;
        if (parentNode instanceof BIRTypeDefinition) {
            BIRTypeDefinition typeDef = (BIRTypeDefinition) parentNode;
            func = findFunction(typeDef.attachedFuncs, funcName);
        } else if (parentNode instanceof BIRPackage) {
            BIRPackage pkg = (BIRPackage) parentNode;
            func = findFunction(pkg.functions, funcName);
        } else {
            throw new IllegalStateException();
        }

        return func;
    }

    private static BIRFunction findFunction(List<BIRFunction> functions, String funcName) {

        for (BIRFunction func : functions) {
            if (JvmCodeGenUtil.cleanupFunctionName(func.name.value).equals(funcName)) {
                return func;
            }
        }

        throw new IllegalStateException("cannot find function: '" + funcName + "'");
    }

    private BIRFunction getMainFunc(List<BIRFunction> funcs) {
        BIRFunction userMainFunc = null;
        for (BIRFunction func : funcs) {
            if (func != null && func.name.value.equals("main")) {
                userMainFunc = func;
                break;
            }
        }

        return userMainFunc;
    }

    CompiledJarFile generate(BIRNode.BIRPackage module, InteropValidator interopValidator, boolean isEntry) {


        Set<PackageID> moduleImports = new LinkedHashSet<>();

        addBuiltinImports(module, moduleImports);

        for (BIRNode.BIRImportModule importModule : module.importModules) {
            BPackageSymbol pkgSymbol = packageCache.getSymbol(getBvmAlias(importModule.org.value,
                    importModule.name.value));
            generateDependencyList(pkgSymbol, interopValidator);
            if (dlog.errorCount() > 0) {
                return new CompiledJarFile(Collections.emptyMap());
            }
        }

        // Desugar BIR to include the observations
        JvmObservabilityGen jvmObservabilityGen = new JvmObservabilityGen(this);
        jvmObservabilityGen.rewriteObservableFunctions(module);

        String moduleInitClass = JvmCodeGenUtil.getModuleLevelClassName(module, MODULE_INIT_CLASS_NAME);
        String pkgName = JvmCodeGenUtil.getPackageName(module);
        Map<String, JavaClass> jvmClassMapping = generateClassNameLinking(module, pkgName, moduleInitClass,
                                                                          interopValidator, isEntry);
        if (!isEntry || dlog.errorCount() > 0) {
            return new CompiledJarFile(Collections.emptyMap());
        }

        // using a concurrent hash map to store class byte values, which are generated in parallel
        final Map<String, byte[]> jarEntries = new ConcurrentHashMap<>();

        // desugar parameter initialization
        injectDefaultParamInits(module, jvmMethodGen, this);
        injectDefaultParamInitsToAttachedFuncs(module, jvmMethodGen, this);

        // create imported modules flat list
        List<PackageID> flattenedModuleImports = flattenModuleImports(moduleImports);

        // enrich current package with package initializers
        jvmMethodGen.enrichPkgWithInitializers(jvmClassMapping, moduleInitClass, module, flattenedModuleImports);

        // generate the shutdown listener class.
        generateShutdownSignalListener(moduleInitClass, jarEntries);

        // desugar the record init function
        rewriteRecordInits(module.typeDefs);

        // generate object/record value classes
        JvmValueGen valueGen = new JvmValueGen(module, this, jvmMethodGen);
        valueGen.generateValueClasses(jarEntries);

        // generate frame classes
        jvmMethodGen.generateFrameClasses(module, jarEntries);

        // generate module classes
        generateModuleClasses(module, jarEntries, moduleInitClass, jvmClassMapping, flattenedModuleImports);

        // clear class name mappings
        clearPackageGenInfo();

        return new CompiledJarFile(JvmCodeGenUtil.getModuleLevelClassName(
                module.org.value, module.name.value, module.version.value, MODULE_INIT_CLASS_NAME, "."), jarEntries);
    }

    private void generateModuleClasses(BIRPackage module, Map<String, byte[]> jarEntries, String moduleInitClass,
                                       Map<String, JavaClass> jvmClassMapping, List<PackageID> moduleImports) {

        jvmClassMapping.entrySet().parallelStream().forEach(entry -> {
            String moduleClass = entry.getKey();
            JavaClass javaClass = entry.getValue();
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            AsyncDataCollector asyncDataCollector = new AsyncDataCollector(moduleClass);
            boolean serviceEPAvailable = false;
            boolean isInitClass = Objects.equals(moduleClass, moduleInitClass);
            if (isInitClass) {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, VALUE_CREATOR, null);
                JvmCodeGenUtil.generateDefaultConstructor(cw, VALUE_CREATOR);
                generateUserDefinedTypeFields(cw, module.typeDefs);
                generateValueCreatorMethods(cw, module.typeDefs, module, moduleInitClass, symbolTable,
                                            asyncDataCollector);
                // populate global variable to class name mapping and generate them
                for (BIRGlobalVariableDcl globalVar : module.globalVars) {
                    if (globalVar != null) {
                        generatePackageVariable(globalVar, cw);
                    }
                }

                BIRFunction mainFunc = getMainFunc(module.functions);
                String mainClass = "";
                if (mainFunc != null) {
                    mainClass = JvmCodeGenUtil.getModuleLevelClassName(module, JvmCodeGenUtil
                            .cleanupPathSeparators(mainFunc.pos.getSource().cUnitName));
                }

                serviceEPAvailable = isServiceDefAvailable(module.typeDefs);

                jvmMethodGen.generateMainMethod(mainFunc, cw, module, moduleClass, serviceEPAvailable,
                                                asyncDataCollector);
                if (mainFunc != null) {
                    jvmMethodGen.generateLambdaForMain(mainFunc, cw, mainClass);
                }
                jvmMethodGen.generateLambdaForPackageInits(cw, module, moduleClass, moduleImports);

                generateLockForVariable(cw);
                generateCreateTypesMethod(cw, module.typeDefs, moduleInitClass, symbolTable);
                jvmMethodGen.generateModuleInitializer(cw, module, moduleInitClass);
                jvmMethodGen.generateExecutionStopMethod(cw, moduleInitClass, module, moduleImports,
                                                         asyncDataCollector);
            } else {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, OBJECT, null);
                JvmCodeGenUtil.generateDefaultConstructor(cw, OBJECT);
            }
            cw.visitSource(javaClass.sourceFileName, null);
            // generate methods
            for (BIRFunction func : javaClass.functions) {
                jvmMethodGen.generateMethod(func, cw, module, null, moduleClass,
                                            asyncDataCollector);
            }
            // generate lambdas created during generating methods
            for (Map.Entry<String, BIRInstruction> lambda : asyncDataCollector.getLambdas().entrySet()) {
                String name = lambda.getKey();
                BIRInstruction call = lambda.getValue();
                jvmMethodGen.generateLambdaMethod(call, cw, name);
            }
            JvmCodeGenUtil.visitStrandMetadataField(cw, asyncDataCollector);
            generateStaticInitializer(cw, moduleClass, module, isInitClass, serviceEPAvailable,
                                      asyncDataCollector);
            cw.visitEnd();

            byte[] bytes = getBytes(cw, module);
            jarEntries.put(moduleClass + ".class", bytes);
        });
    }

    private List<PackageID> flattenModuleImports(Set<PackageID> dependentModuleArray) {

        for (Map.Entry<String, PackageID> entry : dependentModules.entrySet()) {
            PackageID id = entry.getValue();
            dependentModuleArray.add(id);
        }

        return new ArrayList<>(dependentModuleArray);
    }

    /**
     * Java Class will be generate for each source file. This method add class mappings to globalVar and filters the
     * functions based on their source file name and then returns map of associated java class contents.
     *
     * @param module           bir module
     * @param pkgName          module name
     * @param initClass        module init class name
     * @param interopValidator interop validator instance
     * @param isEntry          is entry module flag
     * @return The map of javaClass records on given source file name
     */
    private Map<String, JavaClass> generateClassNameLinking(BIRPackage module, String pkgName, String initClass,
                                                            InteropValidator interopValidator,
                                                            boolean isEntry) {

        String orgName = module.org.value;
        String moduleName = module.name.value;
        String version = module.version.value;
        Map<String, JavaClass> jvmClassMap = new HashMap<>();

        // link global variables with class names
        linkGlobalVars(module, pkgName, initClass, isEntry);

        // link module functions with class names
        linkModuleFunctions(module, pkgName, initClass, interopValidator, isEntry, orgName, moduleName, version,
                jvmClassMap);

        // link module init function that will be generated
        linkModuleInitFunction(pkgName, initClass, orgName, moduleName, version);

        // link module stop function that will be generated
        linkModuleStopFunction(pkgName, initClass, orgName, moduleName, version);

        // link typedef - object attached native functions
        linkTypeDefinitions(module, pkgName, interopValidator, isEntry, orgName, moduleName, version);

        return jvmClassMap;
    }

    private void linkGlobalVars(BIRPackage module, String pkgName, String initClass, boolean isEntry) {

        if (isEntry) {
            for (BIRNode.BIRConstant constant : module.constants) {
                module.globalVars.add(new BIRGlobalVariableDcl(constant.pos, constant.flags, constant.type, null,
                                                               constant.name, VarScope.GLOBAL, VarKind.CONSTANT, "",
                                                               constant.origin));
            }
        }
        for (BIRGlobalVariableDcl globalVar : module.globalVars) {
            if (globalVar != null) {
                globalVarClassMap.put(pkgName + globalVar.name.value, initClass);
            }
        }

        globalVarClassMap.put(pkgName + LOCK_STORE_VAR_NAME, initClass);
    }

    private void linkTypeDefinitions(BIRPackage module, String pkgName,
                                     InteropValidator interopValidator,
                                     boolean isEntry, String orgName,
                                     String moduleName, String version) {

        List<BIRTypeDefinition> typeDefs = module.typeDefs;

        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;

            if ((bType.tag != TypeTags.OBJECT ||
                    !Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS)) &&
                    !(bType instanceof BServiceType)) {
                continue;
            }

            List<BIRFunction> attachedFuncs = optionalTypeDef.attachedFuncs;
            String typeName = toNameString(bType);
            for (BIRFunction func : attachedFuncs) {

                // link the bir function for lookup
                String functionName = func.name.value;
                String lookupKey = typeName + "." + functionName;

                String className = getTypeValueClassName(module, typeName);
                try {
                    BIRFunctionWrapper birFuncWrapperOrError =
                            getBirFunctionWrapper(interopValidator, isEntry, orgName, moduleName, version, func,
                                                  className, lookupKey);
                    birFunctionMap.put(pkgName + lookupKey, birFuncWrapperOrError);
                } catch (JInteropException e) {
                    dlog.error(func.pos, e.getCode(), e.getMessage());
                }
            }
        }
    }

    private void linkModuleStopFunction(String pkgName, String initClass, String orgName, String moduleName,
                                        String version) {
        BInvokableType funcType = new BInvokableType(Collections.emptyList(), null, new BNilType(), null);
        BIRFunction moduleStopFunction = new BIRFunction(null, new Name(MODULE_STOP), 0, funcType, new Name(""), 0,
                                                         null, VIRTUAL);
        birFunctionMap.put(pkgName + MODULE_STOP, getFunctionWrapper(moduleStopFunction, orgName, moduleName,
                                                                     version, initClass));
    }

    private void linkModuleInitFunction(String pkgName, String initClass, String orgName, String moduleName,
                                        String version) {
        BInvokableType funcType = new BInvokableType(Collections.emptyList(), null, new BNilType(), null);
        BIRFunction moduleInitFunction = new BIRFunction(null, new Name(CURRENT_MODULE_INIT), 0, funcType, new Name(""),
                                                         0, null, VIRTUAL);
        birFunctionMap.put(pkgName + CURRENT_MODULE_INIT, getFunctionWrapper(moduleInitFunction, orgName, moduleName,
                                                                             version, initClass));
    }

    private void linkModuleFunctions(BIRPackage module, String pkgName, String initClass,
                                     InteropValidator interopValidator, boolean isEntry, String orgName,
                                     String moduleName, String version, Map<String, JavaClass> jvmClassMap) {

        // filter out functions.
        List<BIRFunction> functions = module.functions;
        if (functions.size() <= 0) {
            return;
        }

        int funcSize = functions.size();
        int count = 0;
        // Generate init class. Init function should be the first function of the package, hence check first
        // function.
        BIRFunction initFunc = functions.get(0);
        String functionName = initFunc.name.value;
        JavaClass klass = new JavaClass(initFunc.pos.src.cUnitName);
        klass.functions.add(0, initFunc);
        jvmMethodGen.addInitAndTypeInitInstructions(module, initFunc);
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

            String balFileName;

            if (birFunc.pos == null) {
                balFileName = MODULE_INIT_CLASS_NAME;
            } else {
                balFileName = birFunc.pos.src.cUnitName;
            }
            String birModuleClassName = JvmCodeGenUtil.getModuleLevelClassName(
                    orgName, moduleName, version, JvmCodeGenUtil.cleanupPathSeparators(balFileName));

            if (!JvmCodeGenUtil.isBallerinaBuiltinModule(orgName, moduleName)) {
                JavaClass javaClass = jvmClassMap.get(birModuleClassName);
                if (javaClass != null) {
                    javaClass.functions.add(birFunc);
                } else {
                    klass = new JavaClass(balFileName);
                    klass.functions.add(0, birFunc);
                    jvmClassMap.put(birModuleClassName, klass);
                }
            }

            interopValidator.setEntryModuleValidation(isEntry);

            try {
                BIRFunctionWrapper birFuncWrapperOrError =
                        getBirFunctionWrapper(interopValidator, isEntry, orgName, moduleName, version, birFunc,
                                              birModuleClassName, birFuncName);
                birFunctionMap.put(pkgName + birFuncName, birFuncWrapperOrError);
            } catch (JInteropException e) {
                dlog.error(birFunc.pos, e.getCode(), e.getMessage());
            }
        }
    }

    private BIRFunctionWrapper getBirFunctionWrapper(InteropValidator interopValidator, boolean isEntry,
                                                     String orgName, String moduleName, String version,
                                                     BIRFunction birFunc, String birModuleClassName,
                                                     String lookupKey) {
        BIRFunctionWrapper birFuncWrapperOrError;
        if (isExternFunc(birFunc) && isEntry) {
            birFuncWrapperOrError = createExternalFunctionWrapper(interopValidator, birFunc, orgName,
                                                                  moduleName, version, birModuleClassName,
                                                                  lookupKey, this);
        } else {
            if (isEntry && birFunc.receiver == null) {
                addDefaultableBooleanVarsToSignature(birFunc, symbolTable.booleanType);
            }
            birFuncWrapperOrError = getFunctionWrapper(birFunc, orgName, moduleName, version,
                                                       birModuleClassName);
        }
        return birFuncWrapperOrError;
    }

    public String lookupExternClassName(String pkgName, String functionName) {

        return externClassMap.get(JvmCodeGenUtil.cleanupName(pkgName) + "/" + functionName);
    }

    public byte[] getBytes(ClassWriter cw, BIRNode node) {

        byte[] result;
        try {
            return cw.toByteArray();
        } catch (MethodTooLargeException e) {
            String funcName = e.getMethodName();
            BIRFunction func = findFunction(node, funcName);
            dlog.error(func.pos, DiagnosticCode.METHOD_TOO_LARGE, func.name.value);
            result = new byte[0];
        } catch (ClassTooLargeException e) {
            dlog.error(node.pos, DiagnosticCode.FILE_TOO_LARGE, e.getClassName());
            result = new byte[0];
        } catch (Exception e) {
            throw new BLangCompilerException(e.getMessage(), e);
        }

        return result;
    }

    private void clearPackageGenInfo() {

        birFunctionMap.clear();
        globalVarClassMap.clear();
        externClassMap.clear();
        dependentModules.clear();
    }

    public BIRFunctionWrapper lookupBIRFunctionWrapper(String lookupKey) {

        return this.birFunctionMap.get(lookupKey);
    }

    void addExternClassMapping(String key, String value) {

        this.externClassMap.put(key, value);
    }

    BType lookupTypeDef(NewInstance objectNewIns) {

        if (!objectNewIns.isExternalDef) {
            return objectNewIns.def.type;
        } else {
            PackageID id = objectNewIns.externalPackageId;
            assert id != null;
            BPackageSymbol symbol = packageCache.getSymbol(id.orgName + "/" + id.name);
            if (symbol != null) {
                BObjectTypeSymbol objectTypeSymbol =
                        (BObjectTypeSymbol) symbol.scope.lookup(new Name(objectNewIns.objectName)).symbol;
                if (objectTypeSymbol != null) {
                    return objectTypeSymbol.type;
                }
            }

            throw new BLangCompilerException("Reference to unknown type " + objectNewIns.externalPackageId
                    + "/" + objectNewIns.objectName);
        }
    }

    String lookupGlobalVarClassName(String pkgName, String varName) {

        String key = pkgName + varName;
        if (!globalVarClassMap.containsKey(key)) {
            return pkgName + MODULE_INIT_CLASS_NAME;
        } else {
            return globalVarClassMap.get(key);
        }
    }

    private void generateDependencyList(BPackageSymbol packageSymbol, InteropValidator interopValidator) {

        if (packageSymbol.bir != null) {
            generate(packageSymbol.bir, interopValidator, false);
        } else {
            for (BPackageSymbol importPkgSymbol : packageSymbol.imports) {
                if (importPkgSymbol == null) {
                    continue;
                }
                generateDependencyList(importPkgSymbol, interopValidator);
            }
        }

        PackageID moduleId = packageSymbol.pkgID;

        String pkgName = JvmCodeGenUtil.getPackageName(moduleId.orgName, moduleId.name, moduleId.version);
        if (!dependentModules.containsKey(pkgName)) {
            dependentModules.put(pkgName, moduleId);
        }
    }
}
