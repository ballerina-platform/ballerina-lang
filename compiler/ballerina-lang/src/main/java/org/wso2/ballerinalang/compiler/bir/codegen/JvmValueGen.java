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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.FieldNameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JFieldBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JMethodBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.OldStyleExternalFunctionWrapper;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
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
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ABSTRACT_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATIONS_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_OPTIONAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOCK_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.POPULATE_INITIAL_VALUES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_INIT_WRAPPER_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE_IMPL_CLOSURES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNSUPPORTED_OPERATION_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.computeLockNameFromString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CAST_B_MAPPING_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INSTANTIATE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.OBJECT_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.POPULATE_INITIAL_VALUES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_INIT_WRAPPER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_VALUE_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_DESC_CONSTRUCTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_DESC_CONSTRUCTOR_WITH_ANNOTATIONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VALUE_CLASS_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.desugarOldExternFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;

/**
 * BIR values to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmValueGen {

    static final FieldNameHashComparator FIELD_NAME_HASH_COMPARATOR = new FieldNameHashComparator();
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
            BType bType = JvmCodeGenUtil.getReferredType(optionalTypeDef.type);
            if ((bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(
                    bType.tsymbol.flags, Flags.CLASS)) || bType.tag == TypeTags.RECORD) {
                desugarObjectMethods(module.packageID, bType, optionalTypeDef.attachedFuncs, initMethodGen,
                                     jvmPackageGen);
            }
        }
    }

    private static void desugarObjectMethods(PackageID module, BType bType,
                                             List<BIRNode.BIRFunction> attachedFuncs, InitMethodGen initMethodGen,
                                             JvmPackageGen jvmPackageGen) {
        if (attachedFuncs == null) {
            return;
        }
        for (BIRNode.BIRFunction birFunc : attachedFuncs) {
            if (birFunc == null) {
                continue;
            }
            if (JvmCodeGenUtil.isExternFunc(birFunc)) {
                BIRFunctionWrapper extFuncWrapper = ExternalMethodGen.lookupBIRFunctionWrapper(module, birFunc, bType,
                                                                                               jvmPackageGen);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    desugarOldExternFuncs((OldStyleExternalFunctionWrapper) extFuncWrapper, birFunc, initMethodGen);
                } else if (birFunc instanceof JMethodBIRFunction) {
                    desugarInteropFuncs((JMethodBIRFunction) birFunc, initMethodGen);
                    enrichWithDefaultableParamInits(birFunc, initMethodGen);
                } else if (!(birFunc instanceof JFieldBIRFunction)) {
                    enrichWithDefaultableParamInits(birFunc, initMethodGen);
                }
            } else {
                addDefaultableBooleanVarsToSignature(birFunc, jvmPackageGen.symbolTable.booleanType);
                enrichWithDefaultableParamInits(birFunc, initMethodGen);
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

    void generateValueClasses(Map<String, byte[]> jarEntries, JvmConstantsGen jvmConstantsGen) {
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
                byte[] bytes = this.createObjectValueClass(objectType, className, optionalTypeDef, jvmConstantsGen
                        , asyncDataCollector);
                jarEntries.put(className + ".class", bytes);
            } else if (bType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) bType;
                byte[] bytes = this.createRecordValueClass(recordType, className, optionalTypeDef, jvmConstantsGen
                        , asyncDataCollector);
                jarEntries.put(className + ".class", bytes);
                String typedescClass = getTypeDescClassName(packageName, optionalTypeDef.internalName.value);
                bytes = this.createRecordTypeDescClass(recordType, typedescClass, optionalTypeDef);
                jarEntries.put(typedescClass + ".class", bytes);
            }
        });
    }


    private byte[] createRecordTypeDescClass(BRecordType recordType, String className,
                                             BIRNode.BIRTypeDefinition typeDef) {

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (typeDef.pos != null) {
            cw.visitSource(typeDef.pos.lineRange().filePath(), null);
        } else {
            cw.visitSource(className, null);
        }
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, TYPEDESC_VALUE_IMPL, new String[]{TYPEDESC_VALUE});

        FieldVisitor fv = cw.visitField(0, ANNOTATIONS_FIELD, GET_MAP_VALUE, null, null);
        fv.visitEnd();

        this.createTypeDescConstructor(cw);
        this.createTypeDescConstructorWithAnnotations(cw, className);
        this.createInstantiateMethod(cw, recordType, typeDef);

        cw.visitEnd();
        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createInstantiateMethod(ClassWriter cw, BRecordType recordType,
                                         BIRNode.BIRTypeDefinition typeDef) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "instantiate",
                                          INSTANTIATE,
                                          null, null);
        mv.visitCode();

        String className = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, INIT_TYPEDESC, false);

        BAttachedFunction initializer = ((BRecordTypeSymbol) recordType.tsymbol).initializerFunc;
        StringBuilder closureParamSignature = calcClosureMapSignature(initializer.type.paramTypes.size());

        // Invoke the init-function of this type.
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(SWAP);


        // Invoke the init-functions of referenced types. This is done to initialize the
        // defualt values of the fields coming from the referenced types.
        for (BType bType : typeDef.referencedTypes) {
            BType typeRef = JvmCodeGenUtil.getReferredType(bType);
            if (typeRef.tag == TypeTags.RECORD) {
                String refTypeClassName = getTypeValueClassName(typeRef.tsymbol.pkgID, toNameString(typeRef));
                mv.visitInsn(DUP2);
                mv.visitMethodInsn(INVOKESTATIC, refTypeClassName , RECORD_INIT_WRAPPER_NAME,
                                   RECORD_INIT_WRAPPER, false);
            }
        }


        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, TYPEDESC_VALUE_IMPL, TYPEDESC_VALUE_IMPL_CLOSURES,
                          GET_MAP_ARRAY);

        for (int i = 0; i < initializer.type.paramTypes.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            mv.visitInsn(AALOAD);
            mv.visitInsn(SWAP);
        }
        mv.visitInsn(POP);


        // Invoke the init-function of this type.
        String initFuncName;
        String valueClassName;
        List<BIRFunction> attachedFuncs = typeDef.attachedFuncs;

        // Attached functions are empty for type-labeling. In such cases, call the init() of
        // the original type value;
        if (!attachedFuncs.isEmpty()) {
            initFuncName = attachedFuncs.get(0).name.value;
            valueClassName = className;
        } else {
            // record type is the original record-type of this type-label
            valueClassName = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
            initFuncName = recordType.name + ENCODED_RECORD_INIT;
        }

        mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName,
                "(L" + STRAND_CLASS + ";L" + MAP_VALUE + ";" + closureParamSignature + ")L" + OBJECT + ";", false);
        mv.visitInsn(POP);

        mv.visitInsn(DUP);
        mv.visitTypeInsn(CHECKCAST, valueClassName);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, CAST_B_MAPPING_INITIAL_VALUE_ENTRY);
        mv.visitMethodInsn(INVOKEVIRTUAL, valueClassName, POPULATE_INITIAL_VALUES_METHOD,
                           POPULATE_INITIAL_VALUES, false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static String getTypeValueClassName(PackageID packageID, String typeName) {
        return getTypeValueClassName(JvmCodeGenUtil.getPackageName(packageID), typeName);
    }


    private StringBuilder calcClosureMapSignature(int size) {
        StringBuilder closureParamSignature = new StringBuilder();
        for (int i = 0; i < size; i++) {
            closureParamSignature.append("L").append(MAP_VALUE).append(";");
        }
        return closureParamSignature;
    }

    private byte[] createRecordValueClass(BRecordType recordType, String className, BIRNode.BIRTypeDefinition typeDef,
                                          JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        if (typeDef.pos != null) {
            cw.visitSource(typeDef.pos.lineRange().filePath(), null);
        } else {
            cw.visitSource(className, null);
        }
        JvmTypeGen jvmTypeGen = new JvmTypeGen(jvmConstantsGen, module.packageID, typeHashVisitor,
                                               jvmPackageGen.symbolTable);
        JvmCastGen jvmCastGen = new JvmCastGen(jvmPackageGen.symbolTable, jvmTypeGen, types);
        LambdaGen lambdaGen = new LambdaGen(jvmPackageGen, jvmCastGen);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className,
                RECORD_VALUE_CLASS,
                MAP_VALUE_IMPL, new String[]{MAP_VALUE});

        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs != null) {
            this.createRecordMethods(cw, attachedFuncs, className, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                                     asyncDataCollector);
        }

        Map<String, BField> fields = recordType.fields;
        this.createRecordFields(cw, fields);
        jvmRecordGen.createAndSplitGetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitSetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitEntrySetMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitContainsKeyMethod(cw, fields, className);
        jvmRecordGen.createAndSplitGetValuesMethod(cw, fields, className, jvmCastGen);
        this.createGetSizeMethod(cw, fields, className);
        this.createRecordClearMethod(cw);
        jvmRecordGen.createAndSplitRemoveMethod(cw, fields, className, jvmCastGen);
        jvmRecordGen.createAndSplitGetKeysMethod(cw, fields, className);
        this.createRecordPopulateInitialValuesMethod(cw);

        this.createRecordConstructor(cw, INIT_TYPEDESC);
        this.createRecordConstructor(cw, TYPE_PARAMETER);
        this.createRecordInitWrapper(cw, className, typeDef);
        this.createLambdas(cw, asyncDataCollector, lambdaGen);
        JvmCodeGenUtil.visitStrandMetadataFields(cw, asyncDataCollector.getStrandMetadata());
        this.generateStaticInitializer(cw, className, module.packageID, asyncDataCollector);
        cw.visitEnd();

        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createRecordMethods(ClassWriter cw, List<BIRNode.BIRFunction> attachedFuncs, String moduleClassName,
                                     JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                     JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {

        for (BIRNode.BIRFunction func : attachedFuncs) {
            if (func == null) {
                continue;
            }
            methodGen.generateMethod(func, cw, this.module, null, moduleClassName, jvmTypeGen, jvmCastGen,
                    jvmConstantsGen, asyncDataCollector);
        }
    }

    private void createTypeDescConstructor(ClassWriter cw) {

        String descriptor = TYPE_DESC_CONSTRUCTOR;
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, descriptor, null, null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);

        mv.visitVarInsn(ALOAD, 2);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE_IMPL, JVM_INIT_METHOD, descriptor, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createTypeDescConstructorWithAnnotations(ClassWriter cw, String name) {

        String descriptor = TYPE_DESC_CONSTRUCTOR_WITH_ANNOTATIONS;
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, descriptor, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(PUTFIELD, name, ANNOTATIONS_FIELD, GET_MAP_VALUE);
        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // load closures
        mv.visitVarInsn(ALOAD, 2);
        // load annotations
        mv.visitVarInsn(ALOAD, 3);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE_IMPL, JVM_INIT_METHOD, TYPE_DESC_CONSTRUCTOR_WITH_ANNOTATIONS,
                           false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordConstructor(ClassWriter cw, String argumentClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, argumentClass, null, null);
        mv.visitCode();

        // load super
        mv.visitVarInsn(ALOAD, 0);
        // load type
        mv.visitVarInsn(ALOAD, 1);
        // invoke `super(type)`;
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, JVM_INIT_METHOD, argumentClass, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    // TODO: remove this method, logic moved to createInstantiateMethod, see #23012
    private void createRecordInitWrapper(ClassWriter cw, String className, BIRNode.BIRTypeDefinition typeDef) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC , RECORD_INIT_WRAPPER_NAME, RECORD_INIT_WRAPPER,
                null, null);
        mv.visitCode();
        // load strand
        mv.visitVarInsn(ALOAD, 0);
        // load value
        mv.visitVarInsn(ALOAD, 1);

        // Invoke the init-functions of referenced types. This is done to initialize the
        // defualt values of the fields coming from the referenced types.
        for (BType bType : typeDef.referencedTypes) {
            BType typeRef = JvmCodeGenUtil.getReferredType(bType);
            if (typeRef.tag != TypeTags.RECORD) {
                continue;
            }

            String refTypeClassName = getTypeValueClassName(typeRef.tsymbol.pkgID, toNameString(typeRef));
            mv.visitInsn(DUP2);
            mv.visitMethodInsn(INVOKESTATIC, refTypeClassName , RECORD_INIT_WRAPPER_NAME,
                               RECORD_INIT_WRAPPER, false);
        }

        // Invoke the init-function of this type.
        String initFuncName;
        String valueClassName;
        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;

        // Attached functions are empty for type-labeling. In such cases, call the init() of the original type value
        if (!attachedFuncs.isEmpty()) {
            initFuncName = attachedFuncs.get(0).name.value;
            valueClassName = className;
        } else {
            // record type is the original record-type of this type-label
            BRecordType recordType = (BRecordType) JvmCodeGenUtil.getReferredType(typeDef.type);
            valueClassName = getTypeValueClassName(recordType.tsymbol.pkgID, toNameString(recordType));
            initFuncName = recordType.name + ENCODED_RECORD_INIT;
        }

        mv.visitMethodInsn(INVOKESTATIC, valueClassName, initFuncName, VALUE_CLASS_INIT, false);
        mv.visitInsn(POP);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
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

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordPopulateInitialValuesMethod(ClassWriter cw) {

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, POPULATE_INITIAL_VALUES_METHOD,
                                          POPULATE_INITIAL_VALUES, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, POPULATE_INITIAL_VALUES_METHOD,
                           POPULATE_INITIAL_VALUES, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private byte[] createObjectValueClass(BObjectType objectType, String className, BIRNode.BIRTypeDefinition typeDef,
                                          JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visitSource(typeDef.pos.lineRange().filePath(), null);

        SymbolTable symbolTable = jvmPackageGen.symbolTable;
        JvmTypeGen jvmTypeGen = new JvmTypeGen(jvmConstantsGen, module.packageID, typeHashVisitor, symbolTable);
        JvmCastGen jvmCastGen = new JvmCastGen(symbolTable, jvmTypeGen, types);
        LambdaGen lambdaGen = new LambdaGen(jvmPackageGen, jvmCastGen);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, ABSTRACT_OBJECT_VALUE, new String[]{B_OBJECT});

        Map<String, BField> fields = objectType.fields;
        this.createObjectFields(cw, fields);

        List<BIRNode.BIRFunction> attachedFuncs = typeDef.attachedFuncs;
        if (attachedFuncs != null) {
            this.createObjectMethods(cw, attachedFuncs, className, objectType, jvmTypeGen, jvmCastGen,
                    jvmConstantsGen, asyncDataCollector);
        }

        this.createObjectInit(cw, fields, className);
        jvmObjectGen.createAndSplitCallMethod(cw, attachedFuncs, className, jvmCastGen);
        jvmObjectGen.createAndSplitGetMethod(cw, fields, className, jvmCastGen);
        jvmObjectGen.createAndSplitSetMethod(cw, fields, className, jvmCastGen);
        jvmObjectGen.createAndSplitSetOnInitializationMethod(cw, fields, className);
        this.createLambdas(cw, asyncDataCollector, lambdaGen);
        JvmCodeGenUtil.visitStrandMetadataFields(cw, asyncDataCollector.getStrandMetadata());
        this.generateStaticInitializer(cw, className, module.packageID, asyncDataCollector);

        cw.visitEnd();
        return jvmPackageGen.getBytes(cw, typeDef);
    }

    private void createObjectFields(ClassWriter cw, Map<String, BField> fields) {

        for (BField field : fields.values()) {
            if (field == null) {
                continue;
            }
            FieldVisitor fvb = cw.visitField(0, field.name.value, getTypeDesc(field.type), null, null);
            fvb.visitEnd();
            String lockClass = "L" + LOCK_VALUE + ";";
            FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL, computeLockNameFromString(field.name.value),
                    lockClass, null, null);
            fv.visitEnd();
        }
    }

    private void createObjectMethods(ClassWriter cw, List<BIRNode.BIRFunction> attachedFuncs, String moduleClassName,
                                     BObjectType currentObjectType, JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                     JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {

        for (BIRNode.BIRFunction func : attachedFuncs) {
            if (func == null) {
                continue;
            }
            methodGen.generateMethod(func, cw, module, currentObjectType, moduleClassName,
                    jvmTypeGen, jvmCastGen, jvmConstantsGen, asyncDataCollector);
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
            mv.visitMethodInsn(INVOKESPECIAL, LOCK_VALUE, JVM_INIT_METHOD, "()V", false);
            mv.visitFieldInsn(PUTFIELD, className, computeLockNameFromString(field.name.value), lockClass);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }

    private void createLambdas(ClassWriter cw, AsyncDataCollector asyncDataCollector,
                               LambdaGen lambdaGen) {
        for (Map.Entry<String, BIRInstruction> entry : asyncDataCollector.getLambdas().entrySet()) {
            lambdaGen.generateLambdaMethod(entry.getValue(), cw, entry.getKey());
        }
    }

    private void generateStaticInitializer(ClassWriter cw, String moduleClass, PackageID module,
                                           AsyncDataCollector asyncDataCollector) {

        if (asyncDataCollector.getStrandMetadata().isEmpty()) {
            return;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        JvmCodeGenUtil.generateStrandMetadata(mv, moduleClass, module, asyncDataCollector);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createRecordClearMethod(ClassWriter cw) {
        // throw an UnsupportedOperationException, since clear is not supported by for records.
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "clear", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
