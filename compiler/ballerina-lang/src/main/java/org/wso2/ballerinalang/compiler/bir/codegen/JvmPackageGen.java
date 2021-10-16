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

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
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
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JInteropException;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.ConfigMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.FrameClassGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.LambdaGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MainMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGenUtils;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.ModuleStopMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmMethodsSplitter;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.isExternFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONSTANT_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ENCODED_DOT_CHARACTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_STORE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_EP_AVAILABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CREATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.rewriteRecordInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.injectDefaultParamInitsToAttachedFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.createExternalFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.injectDefaultParamInits;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * BIR module to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmPackageGen {

    private static Unifier unifier;

    public final SymbolTable symbolTable;
    public final PackageCache packageCache;
    private final MethodGen methodGen;
    private final FrameClassGen frameClassGen;
    private final InitMethodGen initMethodGen;
    private final ConfigMethodGen configMethodGen;
    private final Map<String, BIRFunctionWrapper> birFunctionMap;
    private final Map<String, String> externClassMap;
    private final Map<String, String> globalVarClassMap;
    private final Set<PackageID> dependentModules;
    private final BLangDiagnosticLog dlog;

    JvmPackageGen(SymbolTable symbolTable, PackageCache packageCache, BLangDiagnosticLog dlog,
                  CompilerContext compilerContext) {
        birFunctionMap = new HashMap<>();
        globalVarClassMap = new HashMap<>();
        externClassMap = new HashMap<>();
        dependentModules = new LinkedHashSet<>();
        this.symbolTable = symbolTable;
        this.packageCache = packageCache;
        this.dlog = dlog;
        methodGen = new MethodGen(this, compilerContext);
        initMethodGen = new InitMethodGen(symbolTable);
        configMethodGen = new ConfigMethodGen();
        frameClassGen = new FrameClassGen();
        unifier = new Unifier();

        JvmInstructionGen.anyType = symbolTable.anyType;
    }

    private static String getBvmAlias(String orgName, String moduleName) {

        if (Names.ANON_ORG.value.equals(orgName)) {
            return moduleName;
        }
        return orgName + "/" + moduleName;
    }

    private static void addBuiltinImports(PackageID currentModule, Set<PackageID> dependentModuleArray) {
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
        dependentModuleArray.add(PackageID.VALUE);
        dependentModuleArray.add(PackageID.ERROR);
        dependentModuleArray.add(PackageID.FLOAT);
        dependentModuleArray.add(PackageID.FUTURE);
        dependentModuleArray.add(PackageID.INT);
        dependentModuleArray.add(PackageID.MAP);
        dependentModuleArray.add(PackageID.OBJECT);
        dependentModuleArray.add(PackageID.STREAM);
        dependentModuleArray.add(PackageID.STRING);
        dependentModuleArray.add(PackageID.TABLE);
        dependentModuleArray.add(PackageID.XML);
        dependentModuleArray.add(PackageID.TYPEDESC);
        dependentModuleArray.add(PackageID.BOOLEAN);
        dependentModuleArray.add(PackageID.QUERY);
        dependentModuleArray.add(PackageID.TRANSACTION);
    }

    private static boolean isSameModule(PackageID moduleId, PackageID importModule) {
        PackageID cleanedPkg = JvmCodeGenUtil.cleanupPackageID(importModule);
        if (!moduleId.orgName.value.equals(cleanedPkg.orgName.value)) {
            return false;
        } else if (!moduleId.name.value.equals(cleanedPkg.name.value)) {
            return false;
        } else {
            return getMajorVersion(moduleId.version.value).equals(getMajorVersion(cleanedPkg.version.value));
        }
    }

    private static boolean isLangModule(PackageID moduleId) {

        if (!BALLERINA.equals(moduleId.orgName.value)) {
            return false;
        }
        return moduleId.name.value.indexOf("lang" + ENCODED_DOT_CHARACTER) == 0 || moduleId.name.equals(Names.JAVA);
    }

    private static void generatePackageVariable(BIRGlobalVariableDcl globalVar, ClassWriter cw) {

        String varName = globalVar.name.value;
        BType bType = globalVar.type;
        String descriptor = JvmCodeGenUtil.getFieldTypeSignature(bType);
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, varName, descriptor, null, null);
        fv.visitEnd();
    }

    private static void generateLockForVariable(ClassWriter cw) {

        String lockStoreClass = "L" + LOCK_STORE + ";";
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, LOCK_STORE_VAR_NAME, lockStoreClass, null, null);
        fv.visitEnd();
    }

    private static void generateStaticInitializer(ClassWriter cw, String className, BIRPackage birPackage,
                                                  boolean isInitClass, boolean serviceEPAvailable,
                                                  AsyncDataCollector asyncDataCollector,
                                                  JvmConstantsGen jvmConstantsGen) {
        if (!isInitClass && asyncDataCollector.getStrandMetadata().isEmpty()) {
            return;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        if (isInitClass) {
            setConstantFields(mv, birPackage, jvmConstantsGen);
            setLockStoreField(mv, className);
            setServiceEPAvailableField(cw, mv, serviceEPAvailable, className);
            setModuleStatusField(cw, mv, className);
            setCurrentModuleField(cw, mv, jvmConstantsGen, birPackage.packageID, className);
        }
        JvmCodeGenUtil.generateStrandMetadata(mv, className, birPackage.packageID, asyncDataCollector);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void setConstantFields(MethodVisitor mv, BIRPackage birPackage,
                                          JvmConstantsGen jvmConstantsGen) {
        if (birPackage.constants.isEmpty()) {
            return;
        }
        mv.visitMethodInsn(INVOKESTATIC, jvmConstantsGen.getConstantClass(), CONSTANT_INIT_METHOD_PREFIX, "()V",
                           false);
    }

    private static void setLockStoreField(MethodVisitor mv, String className) {
        String lockStoreClass = "L" + LOCK_STORE + ";";
        mv.visitTypeInsn(NEW, LOCK_STORE);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LOCK_STORE, JVM_INIT_METHOD, "()V", false);
        mv.visitFieldInsn(PUTSTATIC, className, LOCK_STORE_VAR_NAME, lockStoreClass);
    }

    private static void setServiceEPAvailableField(ClassWriter cw, MethodVisitor mv, boolean serviceEPAvailable,
                                                   String initClass) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, SERVICE_EP_AVAILABLE, "Z", null, null);
        fv.visitEnd();

        if (serviceEPAvailable) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
        mv.visitFieldInsn(PUTSTATIC, initClass, SERVICE_EP_AVAILABLE, "Z");
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

    private static void setCurrentModuleField(ClassWriter cw, MethodVisitor mv, JvmConstantsGen jvmConstantsGen,
                                              PackageID packageID, String moduleInitClass) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, CURRENT_MODULE_VAR_NAME,
                                        GET_MODULE, null, null);
        fv.visitEnd();
        String varName = jvmConstantsGen.getModuleConstantVar(packageID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName,
                          GET_MODULE);
        mv.visitFieldInsn(PUTSTATIC, moduleInitClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
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

    public static BIRFunctionWrapper getFunctionWrapper(BIRFunction currentFunc, PackageID packageID,
                                                        String moduleClass) {

        BInvokableType functionTypeDesc = currentFunc.type;
        BIRVariableDcl receiver = currentFunc.receiver;

        BType retType = functionTypeDesc.retType;
        if (isExternFunc(currentFunc) && Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = unifier.build(retType);
        }

        String jvmMethodDescription;
        if (receiver == null) {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes, retType);
        } else {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes, retType, receiver.type);
        }

        return new BIRFunctionWrapper(packageID, currentFunc, moduleClass, jvmMethodDescription);
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
            // some generated functions will not have bir function
            return null;
        }

        return func;
    }

    private static BIRFunction findFunction(List<BIRFunction> functions, String funcName) {

        for (BIRFunction func : functions) {
            if (func.name.value.equals(funcName)) {
                return func;
            }
        }
        return null;
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

    private void generateModuleClasses(BIRPackage module, Map<String, byte[]> jarEntries,
                                       String moduleInitClass, String typesClass,
                                       JvmConstantsGen jvmConstantsGen,
                                       Map<String, JavaClass> jvmClassMapping, List<PackageID> moduleImports,
                                       boolean serviceEPAvailable) {
        jvmClassMapping.entrySet().forEach(entry -> {
            String moduleClass = entry.getKey();
            JavaClass javaClass = entry.getValue();
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            AsyncDataCollector asyncDataCollector = new AsyncDataCollector(moduleClass);
            boolean isInitClass = Objects.equals(moduleClass, moduleInitClass);
            JvmTypeGen jvmTypeGen = new JvmTypeGen(jvmConstantsGen, module.packageID);
            JvmCastGen jvmCastGen = new JvmCastGen(symbolTable, jvmTypeGen);
            LambdaGen lambdaGen = new LambdaGen(this, jvmCastGen);
            if (isInitClass) {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, VALUE_CREATOR, null);
                JvmCodeGenUtil.generateDefaultConstructor(cw, VALUE_CREATOR);
                jvmTypeGen.generateUserDefinedTypeFields(cw, module.typeDefs);
                jvmTypeGen.generateGetAnonTypeMethod(cw);
                jvmTypeGen.generateValueCreatorMethods(cw);
                // populate global variable to class name mapping and generate them
                for (BIRGlobalVariableDcl globalVar : module.globalVars) {
                    if (globalVar != null) {
                        generatePackageVariable(globalVar, cw);
                    }
                }

                BIRFunction mainFunc = getMainFunc(module.functions);
                String mainClass = "";
                if (mainFunc != null) {
                    mainClass = getModuleLevelClassName(module.packageID, JvmCodeGenUtil
                            .cleanupPathSeparators(mainFunc.pos.lineRange().filePath()));
                }

                MainMethodGen mainMethodGen = new MainMethodGen(symbolTable, jvmTypeGen, jvmCastGen,
                                                                asyncDataCollector);
                mainMethodGen.generateMainMethod(mainFunc, cw, module, moduleClass, serviceEPAvailable);
                if (mainFunc != null) {
                    mainMethodGen.generateLambdaForMain(mainFunc, cw, mainClass);
                }
                initMethodGen.generateLambdaForPackageInits(cw, module, moduleClass, moduleImports, jvmCastGen);

                generateLockForVariable(cw);
                initMethodGen.generateModuleInitializer(cw, module, moduleInitClass, typesClass);
                ModuleStopMethodGen moduleStopMethodGen = new ModuleStopMethodGen(symbolTable, jvmTypeGen);
                moduleStopMethodGen.generateExecutionStopMethod(cw, moduleInitClass, module, moduleImports,
                                                                asyncDataCollector);
            } else {
                cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, moduleClass, null, OBJECT, null);
                JvmCodeGenUtil.generateDefaultConstructor(cw, OBJECT);
            }
            cw.visitSource(javaClass.sourceFileName, null);
            // generate methods
            for (BIRFunction func : javaClass.functions) {
                methodGen.generateMethod(func, cw, module, null, moduleClass, jvmTypeGen, jvmCastGen,
                        jvmConstantsGen, asyncDataCollector);
            }
            // generate lambdas created during generating methods
            for (Map.Entry<String, BIRInstruction> lambda : asyncDataCollector.getLambdas().entrySet()) {
                String name = lambda.getKey();
                BIRInstruction call = lambda.getValue();
                lambdaGen.generateLambdaMethod(call, cw, name);
            }
            JvmCodeGenUtil.visitStrandMetadataFields(cw, asyncDataCollector.getStrandMetadata());
            generateStaticInitializer(cw, moduleClass, module, isInitClass, serviceEPAvailable,
                    asyncDataCollector, jvmConstantsGen);
            cw.visitEnd();

            byte[] bytes = getBytes(cw, module);
            jarEntries.put(moduleClass + ".class", bytes);
        });
    }

    private List<PackageID> flattenModuleImports(Set<PackageID> dependentModuleArray) {
        dependentModuleArray.addAll(dependentModules);
        return new ArrayList<>(dependentModuleArray);
    }

    /**
     * Java Class will be generate for each source file. This method add class mappings to globalVar and filters the
     * functions based on their source file name and then returns map of associated java class contents.
     *
     * @param module           bir module
     * @param initClass        module init class name
     * @param isEntry          is entry module flag
     * @return The map of javaClass records on given source file name
     */
    private Map<String, JavaClass> generateClassNameLinking(BIRPackage module, String initClass, boolean isEntry) {

        Map<String, JavaClass> jvmClassMap = new HashMap<>();

        // link global variables with class names
        linkGlobalVars(module, initClass, isEntry);

        // link module functions with class names

        linkModuleFunctions(module, initClass, isEntry, jvmClassMap);

        // link module stop function that will be generated
        linkModuleFunction(module.packageID, initClass, MODULE_STOP_METHOD);

        // link typedef - object attached native functions
        linkTypeDefinitions(module, isEntry);

        return jvmClassMap;
    }

    private void linkGlobalVars(BIRPackage module, String initClass, boolean isEntry) {

        if (isEntry) {
            for (BIRNode.BIRConstant constant : module.constants) {
                module.globalVars.add(new BIRGlobalVariableDcl(constant.pos, constant.flags, constant.constValue.type,
                        null, constant.name, constant.originalName, VarScope.GLOBAL, VarKind.CONSTANT, "",
                        constant.origin));
            }
        }
        String pkgName = JvmCodeGenUtil.getPackageName(module.packageID);
        for (BIRGlobalVariableDcl globalVar : module.globalVars) {
            if (globalVar != null) {
                globalVarClassMap.put(pkgName + globalVar.name.value, initClass);
            }
        }

        globalVarClassMap.put(pkgName + LOCK_STORE_VAR_NAME, initClass);
    }


    private void linkTypeDefinitions(BIRPackage module, boolean isEntry) {
        List<BIRTypeDefinition> typeDefs = module.typeDefs;

        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;

            if ((bType.tag != TypeTags.OBJECT || !Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS))) {
                continue;
            }

            List<BIRFunction> attachedFuncs = optionalTypeDef.attachedFuncs;
            String typeName = toNameString(bType);
            for (BIRFunction func : attachedFuncs) {

                // link the bir function for lookup
                String functionName = func.name.value;
                String lookupKey = typeName + "." + functionName;
                String pkgName = JvmCodeGenUtil.getPackageName(module.packageID);
                String className = JvmValueGen.getTypeValueClassName(pkgName, typeName);
                try {
                    BIRFunctionWrapper birFuncWrapperOrError =
                            getBirFunctionWrapper(isEntry, module.packageID, func, className, lookupKey);
                    birFunctionMap.put(pkgName + lookupKey, birFuncWrapperOrError);
                } catch (JInteropException e) {
                    dlog.error(func.pos, e.getCode(), e.getMessage());
                }
            }
        }
    }

    private void linkModuleFunction(PackageID packageID, String initClass, String funcName) {
        BInvokableType funcType = new BInvokableType(Collections.emptyList(), null, new BNilType(), null);
        BIRFunction moduleStopFunction = new BIRFunction(null, new Name(funcName), 0, funcType, new Name(""), 0,
                                                        VIRTUAL);
        birFunctionMap.put(JvmCodeGenUtil.getPackageName(packageID) + funcName,
                           getFunctionWrapper(moduleStopFunction, packageID, initClass));
    }

    private void linkModuleFunctions(BIRPackage birPackage, String initClass, boolean isEntry,
                                     Map<String, JavaClass> jvmClassMap) {
        // filter out functions.
        List<BIRFunction> functions = birPackage.functions;
        if (functions.size() <= 0) {
            return;
        }

        int funcSize = functions.size();
        int count = 0;
        // Generate init class. Init function should be the first function of the package, hence check first
        // function.
        BIRFunction initFunc = functions.get(0);
        String functionName = initFunc.name.value;
        JavaClass klass = new JavaClass(initFunc.pos.lineRange().filePath());
        klass.functions.add(0, initFunc);
        PackageID packageID = birPackage.packageID;
        jvmClassMap.put(initClass, klass);
        String pkgName = JvmCodeGenUtil.getPackageName(packageID);
        birFunctionMap.put(pkgName + functionName, getFunctionWrapper(initFunc, packageID, initClass));
        count += 1;

        // Add start function
        BIRFunction startFunc = functions.get(1);
        functionName = startFunc.name.value;
        birFunctionMap.put(pkgName + functionName, getFunctionWrapper(startFunc, packageID, initClass));
        klass.functions.add(1, startFunc);
        count += 1;

        // Add stop function
        BIRFunction stopFunc = functions.get(2);
        functionName = stopFunc.name.value;
        birFunctionMap.put(pkgName + functionName, getFunctionWrapper(stopFunc, packageID, initClass));
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
                balFileName = birFunc.pos.lineRange().filePath();
            }

            String cleanedBalFileName = balFileName;
            if (!birFunc.name.value.startsWith(MethodGenUtils.encodeModuleSpecialFuncName(".<test"))) {
                // skip removing `.bal` from generated file names. otherwise `.<testinit>` brakes because,
                // it's "file name" may end in `.bal` due to module. see #27201
                cleanedBalFileName = JvmCodeGenUtil.cleanupPathSeparators(balFileName);
            }
            String birModuleClassName = getModuleLevelClassName(packageID, cleanedBalFileName);

            if (!JvmCodeGenUtil.isBallerinaBuiltinModule(packageID.orgName.value, packageID.name.value)) {
                JavaClass javaClass = jvmClassMap.get(birModuleClassName);
                if (javaClass != null) {
                    javaClass.functions.add(birFunc);
                } else {
                    klass = new JavaClass(balFileName);
                    klass.functions.add(0, birFunc);
                    jvmClassMap.put(birModuleClassName, klass);
                }
            }
            try {
                BIRFunctionWrapper birFuncWrapperOrError = getBirFunctionWrapper(isEntry, packageID, birFunc,
                                                                                 birModuleClassName,
                                                                                 birFuncName);
                birFunctionMap.put(pkgName + birFuncName, birFuncWrapperOrError);
            } catch (JInteropException e) {
                dlog.error(birFunc.pos, e.getCode(), e.getMessage());
            }
        }
    }

    private BIRFunctionWrapper getBirFunctionWrapper(boolean isEntry, PackageID packageID,
                                                     BIRFunction birFunc, String birModuleClassName, String lookupKey) {
        BIRFunctionWrapper birFuncWrapperOrError;
        if (isExternFunc(birFunc) && isEntry) {
            birFuncWrapperOrError = createExternalFunctionWrapper(isEntry, birFunc, packageID,
                                                                  birModuleClassName, lookupKey, this);
        } else {
            if (isEntry && birFunc.receiver == null) {
                addDefaultableBooleanVarsToSignature(birFunc, symbolTable.booleanType);
            }
            birFuncWrapperOrError = getFunctionWrapper(birFunc, packageID, birModuleClassName);
        }
        return birFuncWrapperOrError;
    }

    public String lookupExternClassName(String pkgName, String functionName) {

        return externClassMap.get(pkgName + "/" + functionName);
    }

    public byte[] getBytes(ClassWriter cw, BIRNode node) {

        byte[] result;
        try {
            return cw.toByteArray();
        } catch (MethodTooLargeException e) {
            String funcName = e.getMethodName();
            BIRFunction func = findFunction(node, funcName);
            if (func != null) {
                dlog.error(func.pos, DiagnosticErrorCode.METHOD_TOO_LARGE,
                        IdentifierUtils.decodeIdentifier(func.name.value));
            } else {
                dlog.error(node.pos, DiagnosticErrorCode.METHOD_TOO_LARGE,
                        IdentifierUtils.decodeIdentifier(funcName));
            }
            result = new byte[0];
        } catch (ClassTooLargeException e) {
            dlog.error(node.pos, DiagnosticErrorCode.FILE_TOO_LARGE,
                    IdentifierUtils.decodeIdentifier(e.getClassName()));
            result = new byte[0];
        } catch (Throwable e) {
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
                Name lookupKey = new Name(IdentifierUtils.decodeIdentifier(objectNewIns.objectName));
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbol.scope.lookup(lookupKey).symbol;
                if (objectTypeSymbol != null) {
                    return objectTypeSymbol.type;
                }
            }

            throw new BLangCompilerException("Reference to unknown type " + objectNewIns.externalPackageId
                    + "/" + objectNewIns.objectName);
        }
    }

    public String lookupGlobalVarClassName(String pkgName, String varName) {
        String key = pkgName + varName;
        if (!globalVarClassMap.containsKey(key)) {
            return pkgName + MODULE_INIT_CLASS_NAME;
        } else {
            return globalVarClassMap.get(key);
        }
    }

    private void generateDependencyList(BPackageSymbol packageSymbol) {
        if (packageSymbol.bir != null) {
            generate(packageSymbol.bir, false);
        } else {
            for (BPackageSymbol importPkgSymbol : packageSymbol.imports) {
                if (importPkgSymbol == null) {
                    continue;
                }
                generateDependencyList(importPkgSymbol);
            }
        }
        dependentModules.add(packageSymbol.pkgID);
    }

    CompiledJarFile generate(BIRNode.BIRPackage module, boolean isEntry) {
        if (dependentModules.contains(module.packageID)) {
            return null;
        }
        Set<PackageID> moduleImports = new LinkedHashSet<>();
        addBuiltinImports(module.packageID, moduleImports);
        boolean serviceEPAvailable = module.isListenerAvailable;
        for (BIRNode.BIRImportModule importModule : module.importModules) {

            BPackageSymbol pkgSymbol = packageCache.getSymbol(
                    getBvmAlias(importModule.packageID.orgName.value, importModule.packageID.name.value));
            generateDependencyList(pkgSymbol);
            if (dlog.errorCount() > 0) {
                return null;
            }
            serviceEPAvailable |= listenerDeclarationFound(pkgSymbol);
        }
        String moduleInitClass = JvmCodeGenUtil.getModuleLevelClassName(module.packageID, MODULE_INIT_CLASS_NAME);
        String typesClass = getModuleLevelClassName(module.packageID, MODULE_TYPES_CLASS_NAME);
        Map<String, JavaClass> jvmClassMapping = generateClassNameLinking(module, moduleInitClass, isEntry);

        if (!isEntry || dlog.errorCount() > 0) {
            return null;
        }

        // using a concurrent hash map to store class byte values, which are generated in parallel
        final Map<String, byte[]> jarEntries = new HashMap<>();

        // desugar parameter initialization
        injectDefaultParamInits(module, initMethodGen, this);
        injectDefaultParamInitsToAttachedFuncs(module, initMethodGen, this);

        // create imported modules flat list
        List<PackageID> flattenedModuleImports = flattenModuleImports(moduleImports);

        // enrich current package with package initializers
        initMethodGen.enrichPkgWithInitializers(jvmClassMapping, moduleInitClass, module, flattenedModuleImports);
        JvmConstantsGen jvmConstantsGen = new JvmConstantsGen(module, moduleInitClass);
        JvmMethodsSplitter jvmMethodsSplitter = new JvmMethodsSplitter(this, jvmConstantsGen, module, moduleInitClass);
        configMethodGen.generateConfigMapper(flattenedModuleImports, module, moduleInitClass, jvmConstantsGen,
                                             jarEntries);

        // generate the shutdown listener class.
        new ShutDownListenerGen().generateShutdownSignalListener(moduleInitClass, jarEntries);

        // desugar the record init function
        rewriteRecordInits(module.typeDefs);

        // generate object/record value classes
        JvmValueGen valueGen = new JvmValueGen(module, this, methodGen);
        valueGen.generateValueClasses(jarEntries, jvmConstantsGen);


        // generate frame classes
        frameClassGen.generateFrameClasses(module, jarEntries);

        // generate module classes
        generateModuleClasses(module, jarEntries, moduleInitClass, typesClass, jvmConstantsGen,
                jvmClassMapping, flattenedModuleImports, serviceEPAvailable);
        jvmMethodsSplitter.generateMethods(jarEntries);
        jvmConstantsGen.generateConstants(jarEntries);

        // clear class name mappings
        clearPackageGenInfo();

        return new CompiledJarFile(getModuleLevelClassName(module.packageID, MODULE_INIT_CLASS_NAME, "."), jarEntries);
    }
    private boolean listenerDeclarationFound(BPackageSymbol packageSymbol) {
        if (packageSymbol.bir != null && packageSymbol.bir.isListenerAvailable) {
            return true;
        } else {
            for (BPackageSymbol importPkgSymbol : packageSymbol.imports) {
                if (importPkgSymbol == null) {
                    continue;
                }
                return listenerDeclarationFound(importPkgSymbol);
            }
        }
        return false;
    }
}
