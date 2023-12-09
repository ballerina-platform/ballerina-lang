/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JFieldBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JMethodBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.LambdaGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.values.JvmObjectGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.values.JvmRecordGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ABSTRACT_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_OPTIONAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_METHOD_COUNT_PER_BALLERINA_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_INITIAL_VALUES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_INIT_WRAPPER_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SPLIT_CLASS_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNSUPPORTED_OPERATION_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.computeLockNameFromString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.OBJECT_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.POPULATE_INITIAL_VALUES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_VALUE_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;

/**
 * BIR values to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmValueGen {

    static final String ENCODED_RECORD_INIT =
            Utils.encodeFunctionIdentifier(Names.INIT_FUNCTION_SUFFIX.value);
    private final BIRNode.BIRPackage module;
    private final JvmPackageGen jvmPackageGen;
    private final MethodGen methodGen;
    private final BType booleanType;
    private final JvmObjectGen jvmObjectGen;
    private final JvmRecordGen jvmRecordGen;
    private final TypeHashVisitor typeHashVisitor;
    private final Types types;

    JvmValueGen(BIRNode.BIRPackage module, JvmPackageGen jvmPackageGen, MethodGen methodGen,
                TypeHashVisitor typeHashVisitor, Types types) {
        this.module = module;
        this.jvmPackageGen = jvmPackageGen;
        this.methodGen = methodGen;
        this.booleanType = jvmPackageGen.symbolTable.booleanType;
        this.jvmRecordGen = new JvmRecordGen(jvmPackageGen.symbolTable);
        this.jvmObjectGen = new JvmObjectGen();
        this.typeHashVisitor = typeHashVisitor;
        this.types = types;
    }

    static void injectDefaultParamInitsToAttachedFuncs(BIRNode.BIRPackage module, InitMethodGen initMethodGen,
                                                       JvmPackageGen jvmPackageGen) {
        List<BIRNode.BIRTypeDefinition> typeDefs = module.typeDefs;
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = JvmCodeGenUtil.getImpliedType(optionalTypeDef.type);
            if ((bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(
                    bType.tsymbol.flags, Flags.CLASS)) || bType.tag == TypeTags.RECORD) {
                desugarObjectMethods(optionalTypeDef.attachedFuncs, initMethodGen,
                                     jvmPackageGen);
            }
        }
    }

    private static void desugarObjectMethods(List<BIRFunction> attachedFuncs, InitMethodGen initMethodGen,
                                             JvmPackageGen jvmPackageGen) {
        for (BIRNode.BIRFunction birFunc : attachedFuncs) {
            if (JvmCodeGenUtil.isExternFunc(birFunc)) {
                if (birFunc instanceof JMethodBIRFunction) {
                    desugarInteropFuncs((JMethodBIRFunction) birFunc, initMethodGen);
                    initMethodGen.resetIds();
                } else if (!(birFunc instanceof JFieldBIRFunction)) {
                    initMethodGen.resetIds();
                }
            } else {
                addDefaultableBooleanVarsToSignature(birFunc);
                initMethodGen.resetIds();
            }
        }
    }

    public static String getTypeDescClassName(String packageName, String typeName) {
        return packageName + TYPEDESC_CLASS_PREFIX + typeName;
    }

    public static String getTypeValueClassName(String packageName, String typeName) {
        return packageName + VALUE_CLASS_PREFIX + typeName;
    }

    public static String getFieldIsPresentFlagName(String fieldName) {
        return "$" + fieldName + "$isPresent";
    }

    public static boolean isOptionalRecordField(BField field) {
        return (field.symbol.flags & BAL_OPTIONAL) == BAL_OPTIONAL;
    }

    void generateValueClasses(Map<String, byte[]> jarEntries, JvmConstantsGen jvmConstantsGen, JvmTypeGen jvmTypeGen) {
        String packageName = JvmCodeGenUtil.getPackageName(module.packageID);
        module.typeDefs.forEach(optionalTypeDef -> {
            if (optionalTypeDef.type.tag == TypeTags.TYPEREFDESC) {
                return;
            }
            BType bType = optionalTypeDef.type;
            String className = getTypeValueClassName(packageName, optionalTypeDef.internalName.value);
            AsyncDataCollector asyncDataCollector = new AsyncDataCollector(className);
            if (optionalTypeDef.type.tag == TypeTags.OBJECT &&
                    Symbols.isFlagOn(optionalTypeDef.type.tsymbol.flags, Flags.CLASS)) {
                BObjectType objectType = (BObjectType) optionalTypeDef.type;
                this.createObjectValueClasses(objectType, className, optionalTypeDef, jvmConstantsGen
                        , asyncDataCollector, jarEntries);
            } else if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                byte[] bytes = this.createRecordValueClass(recordType, className, optionalTypeDef, jvmConstantsGen
                        , asyncDataCollector, jvmTypeGen);
                jarEntries.put(className + CLASS_FILE_SUFFIX, bytes);
            }
        });
    }

    public static String getTypeValueClassName(PackageID packageID, String typeName) {
        return getTypeValueClassName(JvmCodeGenUtil.getPackageName(packageID), typeName);
    }

    private byte[] createRecordValueClass(BRecordType recordType, String className, BIRNode.BIRTypeDefinition typeDef,
                                          JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector,
                                          JvmTypeGen jvmTypeGen) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (typeDef.pos != null) {
            cw.visitSource(typeDef.pos.lineRange().fileName(), null);
        } else {
            cw.visitSource(className, null);
        }
        JvmCastGen jvmCastGen = new JvmCastGen(jvmPackageGen.symbolTable, jvmTypeGen, types);
        LambdaGen lambdaGen = new LambdaGen(jvmPackageGen, jvmCastGen);
        cw.visit(V17, ACC_PUBLIC + ACC_SUPER, className, RECORD_VALUE_CLASS, MAP_VALUE_IMPL, new String[]{MAP_VALUE});

        Map<String, BField> fields = recordType.fields;
        this.createRecordFields(cw, fields);
        jvmRecordGen.createAndSplitGetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitSetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitEntrySetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitContainsKeyMethod(cw, fields, className);
        jvmRecordGen.createAndSplitGetValuesMethod(cw, fields, className, jvmCastGen);
        this.createGetSizeMethod(cw, fields, className);
        this.createRecordClearMethod(cw, typeDef.name.value);
        jvmRecordGen.createAndSplitRemoveMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitGetKeysMethod(cw, fields, className);
        this.createRecordPopulateInitialValuesMethod(cw, className);

        this.createRecordConstructor(cw, INIT_TYPEDESC, className, typeDef, recordType);
        this.createRecordConstructor(cw, TYPE_PARAMETER, className, typeDef, recordType);
        this.createLambdas(cw, asyncDataCollector, lambdaGen, className);
        JvmCodeGenUtil.visitStrandMetadataFields(cw, asyncDataCollector.getStrandMetadata());
        this.generateStaticInitializer(cw, className, module.packageID, asyncDataCollector);
        cw.visitEnd();

        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createRecordConstructor(ClassWriter cw, String argumentClass, String className,
                                         BIRNode.BIRTypeDefinition typedef, BRecordType recordType) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, argumentClass, null, null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, JVM_INIT_METHOD, argumentClass, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, RECORD_INIT_WRAPPER_NAME, className);
        mv.visitEnd();
    }

    private void createRecordFields(ClassWriter cw, Map<String, BField> fields) {
        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }
            String fieldName = field.name.value;
            FieldVisitor fv = cw.visitField(0, fieldName, getTypeDesc(field.type), null, null);
            fv.visitEnd();

            if (isOptionalRecordField(field)) {
                fv = cw.visitField(0, getFieldIsPresentFlagName(fieldName), getTypeDesc(booleanType),
                        null, null);
                fv.visitEnd();
            }
        }
    }


    private void createGetSizeMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "size", "()I", null, null);
        mv.visitCode();
        int sizeVarIndex = 1;

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "size", "()I", false);
        mv.visitVarInsn(ISTORE, sizeVarIndex);

        int requiredFieldsCount = 0;
        for (BField optionalField : fields.values()) {
            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                Label l3 = new Label();
                mv.visitJumpInsn(IFEQ, l3);
                mv.visitIincInsn(sizeVarIndex, 1);
                mv.visitLabel(l3);
            } else {
                requiredFieldsCount += 1;
            }
        }

        mv.visitIincInsn(sizeVarIndex, requiredFieldsCount);
        mv.visitVarInsn(ILOAD, sizeVarIndex);
        mv.visitInsn(IRETURN);

        JvmCodeGenUtil.visitMaxStackForMethod(mv, "size", className);
        mv.visitEnd();
    }

    private void createRecordPopulateInitialValuesMethod(ClassWriter cw, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, POPULATE_INITIAL_VALUES_METHOD,
                                          POPULATE_INITIAL_VALUES, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, POPULATE_INITIAL_VALUES_METHOD,
                           POPULATE_INITIAL_VALUES, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, POPULATE_INITIAL_VALUES_METHOD, className);
        mv.visitEnd();
    }

    private void createObjectValueClasses(BObjectType objectType, String className, BIRNode.BIRTypeDefinition typeDef,
                                          JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector,
                                          Map<String, byte[]> jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.lineRange().fileName(), null);

        SymbolTable symbolTable = jvmPackageGen.symbolTable;
        JvmTypeGen jvmTypeGen = new JvmTypeGen(jvmConstantsGen, module.packageID, typeHashVisitor, symbolTable);
        JvmCastGen jvmCastGen = new JvmCastGen(symbolTable, jvmTypeGen, types);
        LambdaGen lambdaGen = new LambdaGen(jvmPackageGen, jvmCastGen);
        cw.visit(V17, ACC_PUBLIC + ACC_SUPER, className, null, ABSTRACT_OBJECT_VALUE, new String[]{B_OBJECT});

        Map<String, BField> fields = objectType.fields;
        this.createObjectFields(cw, fields);

        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs.size() > MAX_METHOD_COUNT_PER_BALLERINA_OBJECT) {
            this.createObjectMethodsWithSplitClasses(cw, attachedFuncs, className, objectType, jvmTypeGen,
                    jvmCastGen, jvmConstantsGen, asyncDataCollector, typeDef, jarEntries);
        } else {
            this.createObjectMethods(cw, attachedFuncs, className, objectType, jvmTypeGen, jvmCastGen,
                    jvmConstantsGen, asyncDataCollector);
        }

        this.createObjectInit(cw, fields, className);
        jvmObjectGen.createAndSplitCallMethod(cw, attachedFuncs, className, jvmCastGen);
        jvmObjectGen.createAndSplitGetMethod(cw, fields, className, jvmCastGen);
        jvmObjectGen.createAndSplitSetMethod(cw, fields, className, jvmCastGen);
        jvmObjectGen.createAndSplitSetOnInitializationMethod(cw, fields, className);
        this.createLambdas(cw, asyncDataCollector, lambdaGen, className);
        JvmCodeGenUtil.visitStrandMetadataFields(cw, asyncDataCollector.getStrandMetadata());
        this.generateStaticInitializer(cw, className, module.packageID, asyncDataCollector);
        cw.visitEnd();
        jarEntries.put(className + CLASS_FILE_SUFFIX, jvmPackageGen.getBytes(cw, typeDef));
    }

    private void createObjectFields(ClassWriter cw, Map<String, BField> fields) {
        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }
            FieldVisitor fvb = cw.visitField(0, field.name.value, getTypeDesc(field.type), null, null);
            fvb.visitEnd();
            String lockClass = "L" + LOCK_VALUE + ";";
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, computeLockNameFromString(field.name.value),
                    lockClass, null, null);
            fv.visitEnd();
        }
    }

    private void createObjectMethods(ClassWriter cw, List<BIRFunction> attachedFuncs, String moduleClassName,
                                     BObjectType currentObjectType, JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                     JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {
        for (BIRNode.BIRFunction func : attachedFuncs) {
            methodGen.generateMethod(func, cw, module, currentObjectType, moduleClassName, jvmTypeGen, jvmCastGen,
                    jvmConstantsGen, asyncDataCollector);
        }
    }

    private void createObjectMethodsWithSplitClasses(ClassWriter cw, List<BIRFunction> attachedFuncs,
                                                     String moduleClassName, BObjectType currentObjectType,
                                                     JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                                     JvmConstantsGen jvmConstantsGen,
                                                     AsyncDataCollector asyncDataCollector,
                                                     BIRNode.BIRTypeDefinition typeDef,
                                                     Map<String, byte[]> jarEntries) {
        int splitClassNum = 1;
        ClassWriter splitCW = new BallerinaClassWriter(COMPUTE_FRAMES);
        splitCW.visitSource(typeDef.pos.lineRange().fileName(), null);
        String splitClassName = moduleClassName + SPLIT_CLASS_SUFFIX + splitClassNum;
        splitCW.visit(V17, ACC_PUBLIC + ACC_SUPER, splitClassName, null, OBJECT, null);
        JvmCodeGenUtil.generateDefaultConstructor(splitCW, OBJECT);
        int methodCountPerSplitClass = 0;

        for (BIRNode.BIRFunction func : attachedFuncs) {
            if (func.name.value.contains("$init$")) {
                methodGen.generateMethod(func, cw, module, currentObjectType, moduleClassName,
                        jvmTypeGen, jvmCastGen, jvmConstantsGen, asyncDataCollector);
                continue;
            }
            methodGen.genJMethodWithBObjectMethodCall(func, cw, module, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                    moduleClassName, asyncDataCollector, splitClassName);
            methodGen.genJMethodForBFunc(func, splitCW, module, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                    moduleClassName, currentObjectType, asyncDataCollector, true);
            methodCountPerSplitClass++;
            if (methodCountPerSplitClass == MAX_METHOD_COUNT_PER_BALLERINA_OBJECT) {
                splitCW.visitEnd();
                byte[] splitBytes = jvmPackageGen.getBytes(splitCW, typeDef);
                jarEntries.put(splitClassName + CLASS_FILE_SUFFIX, splitBytes);
                splitClassNum++;
                splitCW = new BallerinaClassWriter(COMPUTE_FRAMES);
                splitCW.visitSource(typeDef.pos.lineRange().fileName(), null);
                splitClassName = moduleClassName + SPLIT_CLASS_SUFFIX + splitClassNum;
                splitCW.visit(V17, ACC_PUBLIC + ACC_SUPER, splitClassName, null, OBJECT, null);
                JvmCodeGenUtil.generateDefaultConstructor(splitCW, OBJECT);
                methodCountPerSplitClass = 0;
            }
        }
        if (methodCountPerSplitClass != 0) {
            splitCW.visitEnd();
            byte[] splitBytes = jvmPackageGen.getBytes(splitCW, typeDef);
            jarEntries.put(splitClassName + CLASS_FILE_SUFFIX, splitBytes);
        }
    }

    private void createObjectInit(ClassWriter cw, Map<String, BField> fields, String className) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, OBJECT_TYPE_IMPL_INIT, null,
                null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke super(type);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_OBJECT_VALUE, JVM_INIT_METHOD, OBJECT_TYPE_IMPL_INIT, false);

        String lockClass = "L" + LOCK_VALUE + ";";
        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }

            Label fLabel = new Label();
            mv.visitLabel(fLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, LOCK_VALUE);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
            mv.visitFieldInsn(PUTFIELD, className, computeLockNameFromString(field.name.value), lockClass);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    private void createLambdas(ClassWriter cw, AsyncDataCollector asyncDataCollector,
                               LambdaGen lambdaGen, String className) {
        for (Map.Entry<String, BIRInstruction> entry : asyncDataCollector.getLambdas().entrySet()) {
            lambdaGen.generateLambdaMethod(entry.getValue(), cw, entry.getKey(), className);
        }
    }

    private void generateStaticInitializer(ClassWriter cw, String moduleClass, PackageID module,
                                           AsyncDataCollector asyncDataCollector) {
        if (asyncDataCollector.getStrandMetadata().isEmpty()) {
            return;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        JvmCodeGenUtil.generateStrandMetadata(mv, moduleClass, module, asyncDataCollector);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, JVM_STATIC_INIT_METHOD, moduleClass);
        mv.visitEnd();
    }

    private void createRecordClearMethod(ClassWriter cw, String className) {
        // throw an UnsupportedOperationException, since clear is not supported by for records.
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "clear", VOID_METHOD_DESC, null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitInsn(ATHROW);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "clear", className);
        mv.visitEnd();
    }
}
