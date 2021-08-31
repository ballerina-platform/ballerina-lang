/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.split;

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPE_INSTANCES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERRORS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_OBJECTS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORDS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REMOTE_METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RESOURCE_METHOD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_CYCLIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_ORIGINAL_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.TYPE_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeDescClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * BIR types to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmCreateTypeGen {

    private final JvmTypeGen jvmTypeGen;
    public static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();
    private final TypeHashVisitor typeHashVisitor;
    private static final int MAX_TYPES_PER_METHOD = 100;
    private final String typesClass;
    private final String anonTypesClass;
    private final String recordsClass;
    private final String objectsClass;
    private final String errorsClass;


    public JvmCreateTypeGen(JvmTypeGen jvmTypeGen, PackageID packageID) {
        this.jvmTypeGen = jvmTypeGen;
        typeHashVisitor = new TypeHashVisitor();
        this.typesClass = getModuleLevelClassName(packageID, MODULE_TYPES_CLASS_NAME);
        this.anonTypesClass = getModuleLevelClassName(packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.recordsClass = getModuleLevelClassName(packageID, MODULE_RECORDS_CLASS_NAME);
        this.objectsClass = getModuleLevelClassName(packageID, MODULE_OBJECTS_CLASS_NAME);
        this.errorsClass = getModuleLevelClassName(packageID, MODULE_ERRORS_CLASS_NAME);
    }

    public void generateTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                  Map<String, byte[]> jarEntries,
                                  String moduleInitClass, SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, typesClass, null, OBJECT, null);
        generateCreateTypesMethod(cw, module.typeDefs, moduleInitClass, symbolTable);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(typesClass + ".class", bytes);
    }

    void generateCreateTypesMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefs,
                                   String moduleInitClass, SymbolTable symbolTable) {

        createTypesInstance(cw, typeDefs, moduleInitClass);
        List<String> populateTypeFuncNames = populateTypes(cw, typeDefs, moduleInitClass, symbolTable);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPES_METHOD, "()V", null, null);
        mv.visitCode();

        // Invoke create-type-instances method
        mv.visitMethodInsn(INVOKESTATIC, typesClass, CREATE_TYPE_INSTANCES_METHOD, "()V", false);

        // Invoke the populate-type functions
        for (String funcName : populateTypeFuncNames) {
            mv.visitMethodInsn(INVOKESTATIC, typesClass, funcName, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createTypesInstance(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String moduleInitClass) {
        int instanceSplits = createTypesInstanceSplits(cw, typeDefs, moduleInitClass);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_INSTANCES_METHOD, "()V", null, null);
        mv.visitCode();
        for (int i = 0; i < instanceSplits; i++) {
            mv.visitMethodInsn(INVOKESTATIC, typesClass, CREATE_TYPE_INSTANCES_METHOD + i, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int createTypesInstanceSplits(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass) {

        if (typeDefs.isEmpty()) {
            return 0;
        }
        MethodVisitor mv = null;
        int bTypesCount = 0;
        int methodCount = 0;

        // Create the type
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            String name = optionalTypeDef.internalName.value;
            BType bType = optionalTypeDef.type;
            if (bType.tag != TypeTags.RECORD && bType.tag != TypeTags.OBJECT && bType.tag != TypeTags.ERROR &&
                    bType.tag != TypeTags.UNION && bType.tag != TypeTags.TUPLE) {
                        // do not generate anything for other types (e.g.: finite type, etc.)
                        continue;
                    } else {
                if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_INSTANCES_METHOD + methodCount++, "()V",
                            null, null);
                    mv.visitCode();
                }
            }
            switch (bType.tag) {
                case TypeTags.RECORD:
                    createRecordType(mv, (BRecordType) bType);
                    mv.visitInsn(DUP);
                    String packageName = JvmCodeGenUtil.getPackageName(bType.tsymbol.pkgID);
                    String className = getTypeDescClassName(packageName, toNameString(bType));
                    mv.visitTypeInsn(NEW, className);
                    mv.visitInsn(DUP_X1);
                    mv.visitInsn(SWAP);
                    mv.visitInsn(ACONST_NULL);
                    String descriptor = String.format("(L%s;[L%s;)V", TYPE, MAP_VALUE);
                    mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, descriptor, false);
                    String fieldType = String.format("L%s;", TYPEDESC_VALUE);
                    mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, jvmTypeGen.getTypedescFieldName(name), fieldType);

                    break;
                case TypeTags.OBJECT:
                    createObjectType(mv, (BObjectType) bType);
                    break;
                case TypeTags.ERROR:
                    createErrorType(mv, (BErrorType) bType, bType.tsymbol.name.value);
                    break;
                case TypeTags.TUPLE:
                    createTupleType(mv, (BTupleType) bType);
                    break;
                default:
                    createUnionType(mv, (BUnionType) bType);
                    break;
            }

            mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, getTypeFieldName(name), String.format("L%s;", TYPE));
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return methodCount;
    }

    private List<String> populateTypes(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass,
                                              SymbolTable symbolTable) {

        List<String> funcNames = new ArrayList<>();
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (!(bType.tag == TypeTags.RECORD || bType.tag == TypeTags.ERROR || bType.tag == TypeTags.OBJECT
                    || bType.tag == TypeTags.UNION || bType.tag == TypeTags.TUPLE)) {
                continue;
            }

            fieldName = getTypeFieldName(optionalTypeDef.internalName.value);
            String methodName = String.format("$populate%s", fieldName);
            funcNames.add(methodName);

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));

            BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
            switch (bType.tag) {
                case TypeTags.RECORD:
                    populateRecord(mv, (BRecordType) bType);
                    break;
                case TypeTags.OBJECT:
                    populateObject(cw, mv, symbolTable, fieldName, (BObjectType) bType, indexMap);
                    break;
                case TypeTags.ERROR:
                    // populate detail field
                    populateError(mv, (BErrorType) bType);
                    break;
                case TypeTags.UNION:
                    populateUnion(mv, (BUnionType) bType);
                    break;
                case TypeTags.TUPLE:
                    populateTuple(mv, (BTupleType) bType);
                    break;
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return funcNames;
    }

    private void populateTuple(MethodVisitor mv, BTupleType bType) {
        mv.visitTypeInsn(CHECKCAST, TUPLE_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);

        addCyclicFlag(mv, bType);
        addTupleMembers(mv, bType);
        addImmutableType(mv, bType);
    }

    private void populateUnion(MethodVisitor mv, BUnionType bType) {
        mv.visitTypeInsn(CHECKCAST, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);

        addCyclicFlag(mv, bType);
        // populate member fields
        addUnionMembers(mv, bType);
        addImmutableType(mv, bType);
    }

    private void populateError(MethodVisitor mv, BErrorType bType) {
        mv.visitTypeInsn(CHECKCAST, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, bType.detailType);
        mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_DETAIL_TYPE_METHOD,
                           String.format("(L%s;)V", TYPE), false);
        BTypeIdSet typeIdSet = bType.typeIdSet;
        if (!typeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
            loadTypeIdSet(mv, typeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                               String.format("(L%s;)V", TYPE_ID_SET), false);
        }
    }

    private void populateObject(ClassWriter cw, MethodVisitor mv, SymbolTable symbolTable, String fieldName,
                                BObjectType bType, BIRVarToJVMIndexMap indexMap) {
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        addObjectFields(mv, bType.fields);
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bType.tsymbol;
        addObjectInitFunction(mv, objectTypeSymbol.generatedInitializerFunc, bType, indexMap,
                              "$init$", "setGeneratedInitializer", symbolTable);
        addObjectInitFunction(mv, objectTypeSymbol.initializerFunc, bType, indexMap, "init",
                              "setInitializer", symbolTable);
        addObjectAttachedFunctions(cw, mv, fieldName, objectTypeSymbol.attachedFuncs, bType,
                                   symbolTable);
        addResourceMethods(cw, mv, fieldName, objectTypeSymbol.attachedFuncs, bType,
                           symbolTable);
        addImmutableType(mv, bType);
        BTypeIdSet objTypeIdSet = bType.typeIdSet;
        if (!objTypeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
            loadTypeIdSet(mv, objTypeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                               String.format("(L%s;)V", TYPE_ID_SET), false);
        }
    }

    private void populateRecord(MethodVisitor mv, BRecordType bType) {
        mv.visitTypeInsn(CHECKCAST, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        addRecordFields(mv, bType.fields);
        addRecordRestField(mv, bType.restFieldType);
        addImmutableType(mv, bType);
    }

    private void addImmutableType(MethodVisitor mv, BType type) {
        BIntersectionType immutableType = ((SelectivelyImmutableReferenceType) type).getImmutableType();
        if (immutableType == null || !(immutableType.tsymbol.pkgID.equals(type.tsymbol.pkgID))) {
            return;
        }

        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, immutableType);
        mv.visitMethodInsn(INVOKEINTERFACE, TYPE, SET_IMMUTABLE_TYPE_METHOD, String.format("(L%s;)V",
                                                                                         INTERSECTION_TYPE), true);
    }

    private void loadTypeIdSet(MethodVisitor mv, BTypeIdSet typeIdSet) {
        // Create TypeIdSet
        mv.visitTypeInsn(NEW, TYPE_ID_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_ID_SET, JVM_INIT_METHOD, "()V", false);

        for (BTypeIdSet.BTypeId typeId : typeIdSet.getPrimary()) {
            addTypeId(mv, typeId, true);
        }

        for (BTypeIdSet.BTypeId typeId : typeIdSet.getSecondary()) {
            addTypeId(mv, typeId, false);
        }
    }

    private void addTypeId(MethodVisitor mv, BTypeIdSet.BTypeId typeId, boolean isPrimaryTypeId) {
        mv.visitInsn(DUP);
        // Load package
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(typeId.packageID.orgName.value);
        mv.visitLdcInsn(typeId.packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(typeId.packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        mv.visitLdcInsn(typeId.name);
        mv.visitInsn(isPrimaryTypeId ? ICONST_1 : ICONST_0);
        // Add to BTypeIdSet
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_ID_SET, "add",
                           String.format("(L%s;L%s;Z)V", MODULE, STRING_VALUE), false);
    }

    public static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex,
                                                    List<? extends NamedNode> nodes, int start,
                                                    int length, Label defaultCaseLabel) {
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        List<Label> labels = new ArrayList<>();
        int[] hashCodes = new int[length];
        for (int j = start; j < (start + length); j++) {
            NamedNode node = nodes.get(j);
            if (node != null) {
                labels.add(i, new Label());
                String name = node.getName().value;
                hashCodes[i] = name.hashCode();
                i += 1;
            }
        }
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels.toArray(new Label[0]));
        return labels;
    }

    static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                 List<? extends NamedNode> nodes, int start, int length,
                                                 List<Label> labels, Label defaultCaseLabel) {
        List<Label> targetLabels = new ArrayList<>();
        int i = 0;
        for (int j = start; j < start + length; j++) {
            NamedNode node = nodes.get(j);
            if (node == null) {
                continue;
            }
            mv.visitLabel(labels.get(i));
            mv.visitVarInsn(ALOAD, nameRegIndex);
            mv.visitLdcInsn(node.getName().value);
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    String.format("(L%s;)Z", OBJECT), false);
            Label targetLabel = new Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels.add(i, targetLabel);
            i += 1;
        }
        return targetLabels;
    }


    // -------------------------------------------------------
    //              getAnonType() generation methods
    // -------------------------------------------------------

    public void generateAnonTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                      String moduleInitClass, Map<String, byte[]> jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, anonTypesClass, null, OBJECT, null);
        generateGetAnonTypeMainMethod(cw, module.typeDefs, moduleInitClass);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(anonTypesClass + ".class", bytes);
    }

    private void generateGetAnonTypeMainMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefinitions,
                                           String moduleInitClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_ANON_TYPE,
                String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), null, null);
        mv.visitCode();
        // filter anon types and sorts them before generating switch case.
        Set<BIRTypeDefinition> typeDefSet = new TreeSet<>(TYPE_HASH_COMPARATOR);
        for (BIRTypeDefinition t : typeDefinitions) {
            if (t.internalName.value.contains(BLangAnonymousModelHelper.ANON_PREFIX)
                    || Symbols.isFlagOn(t.type.flags, Flags.ANONYMOUS)) {
                typeDefSet.add(t);
            }
        }
        AnonTypeHashInfo anonTypeHashSwitch = createLabelsForAnonTypeHashSwitch(typeDefSet);
        if (anonTypeHashSwitch.labelFieldMapping.isEmpty()) {
            Label defaultCaseLabel = new Label();
            createDefaultCase(mv, defaultCaseLabel, 1, "No such type: ");
        } else {
            mv.visitVarInsn(ILOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE + 0,
                               String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), false);
            mv.visitInsn(ARETURN);
            generateGetAnonTypeSplitMethods(cw, anonTypeHashSwitch, moduleInitClass);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateGetAnonTypeSplitMethods(ClassWriter cw,  AnonTypeHashInfo anonTypeHashSwitch,
                                        String typeOwnerClass) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;

        int hashParamRegIndex = 0;
        int shapeParamRegIndex = 1;
        Label defaultCaseLabel = new Label();
        Map<String, Label> labelFieldMapping = anonTypeHashSwitch.labelFieldMapping;
        int i = 0;
        for (Map.Entry<String, Label> labelEntry : labelFieldMapping.entrySet()) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, GET_ANON_TYPE + methodCount++,
                        String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                mv.visitVarInsn(ILOAD, hashParamRegIndex);
                int remainingCases = labelFieldMapping.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                int[] hashes = Arrays.copyOfRange(anonTypeHashSwitch.hashes, bTypesCount, bTypesCount + remainingCases);
                Label[] labels = Arrays.copyOfRange(anonTypeHashSwitch.labels, bTypesCount,
                        bTypesCount + remainingCases);
                mv.visitLookupSwitchInsn(defaultCaseLabel, hashes, labels);
            }
            mv.visitVarInsn(ILOAD, hashParamRegIndex);
            String fieldName = labelEntry.getKey();
            Label targetLabel = labelEntry.getValue();
            mv.visitLabel(targetLabel);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));
            mv.visitInsn(ARETURN);
            i++;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == labelFieldMapping.size()) {
                    createDefaultCase(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ILOAD, hashParamRegIndex);
                    mv.visitVarInsn(ALOAD, shapeParamRegIndex);
                    mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE + methodCount,
                                       String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    private AnonTypeHashInfo createLabelsForAnonTypeHashSwitch(Set<BIRTypeDefinition> nodes) {

        // Create labels for the cases
        Map<String, Label> labelFieldMapping = new LinkedHashMap<>();
        Map<Integer, Label> labelHashMapping = new LinkedHashMap<>();
        for (BIRTypeDefinition node : nodes) {
            if (node != null) {
                BType type = node.type;
                String fieldName = getTypeFieldName(node.internalName.value);
                Integer typeHash = typeHashVisitor.visit(type);
                boolean fieldExists = labelFieldMapping.containsKey(fieldName);
                boolean hashExists = labelHashMapping.containsKey(typeHash);
                if (!fieldExists && !hashExists) {
                    Label label = new Label();
                    labelFieldMapping.put(fieldName, label);
                    labelHashMapping.put(typeHash, label);
                } else {
                    assert fieldExists && hashExists; // hashing issues.
                }
                typeHashVisitor.reset();
            }
        }
        int[] hashes = labelHashMapping.keySet().stream().mapToInt(Integer::intValue).toArray();
        Label[] labels = labelHashMapping.values().toArray(new Label[0]);
        return new AnonTypeHashInfo(hashes, labels, labelFieldMapping);
    }

    static class AnonTypeHashInfo {

        int[] hashes;
        Label[] labels;
        Map<String, Label> labelFieldMapping;

        public AnonTypeHashInfo(int[] hashes, Label[] labels, Map<String, Label> labelFieldMapping) {
            this.hashes = hashes;
            this.labels = labels;
            this.labelFieldMapping = labelFieldMapping;
        }
    }

    // -------------------------------------------------------
    //              Runtime value creation methods
    // -------------------------------------------------------


    public void generateValueCreatorClasses(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     SymbolTable symbolTable) {

        // due to structural type same name can appear twice, need to remove duplicates
        Set<BIRTypeDefinition> recordTypeDefSet = new TreeSet<>(NAME_HASH_COMPARATOR);
        List<BIRTypeDefinition> objectTypeDefList = new ArrayList<>();
        List<BIRTypeDefinition> errorTypeDefList = new ArrayList<>();

        for (BIRTypeDefinition optionalTypeDef : module.typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                recordTypeDefSet.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.OBJECT && Symbols.isFlagOn(bType.tsymbol.flags, Flags.CLASS)) {
                objectTypeDefList.add(optionalTypeDef);
            } else if (bType.tag == TypeTags.ERROR) {
                errorTypeDefList.add(optionalTypeDef);
            }
        }
        ArrayList<BIRTypeDefinition> recordTypeDefList = new ArrayList<>(recordTypeDefSet);
        generateRecordsClass(jvmPackageGen, module, moduleInitClass, jarEntries, recordTypeDefList);
        generateObjectsClass(jvmPackageGen, module, moduleInitClass, jarEntries, objectTypeDefList,
                symbolTable);
        generateErrorsClass(jvmPackageGen, module, moduleInitClass, jarEntries, errorTypeDefList,
                symbolTable);
    }

    public void generateRecordsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     List<BIRTypeDefinition> recordTypeDefList) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, recordsClass, null, OBJECT, null);
        String metadataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_RECORD_VALUE);
        generateStaticInitializer(module, cw, recordsClass, CREATE_RECORD_VALUE, metadataVarName);
        generateCreateRecordMethods(cw, recordTypeDefList, module.packageID, moduleInitClass, recordsClass,
                                    metadataVarName);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(recordsClass + ".class", bytes);
    }

    public void generateObjectsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     List<BIRTypeDefinition> objectTypeDefList,
                                     SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, objectsClass, null, OBJECT, null);
        String metadataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_RECORD_VALUE);
        generateStaticInitializer(module, cw, objectsClass, CREATE_OBJECT_VALUE, metadataVarName);
        generateCreateObjectMethods(cw, objectTypeDefList, module.packageID, moduleInitClass, objectsClass,
                                    symbolTable, metadataVarName);

        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(objectsClass + ".class", bytes);
    }

    public void generateErrorsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                    String moduleInitClass, Map<String, byte[]> jarEntries,
                                    List<BIRTypeDefinition> errorTypeDefList, SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, errorsClass, null, OBJECT, null);
        generateCreateErrorMethods(cw, errorTypeDefList, moduleInitClass, errorsClass, symbolTable);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(errorsClass + ".class", bytes);
    }

    private void generateStaticInitializer(BIRNode.BIRPackage module, ClassWriter cw,
                                           String typeOwnerClass, String varName, String metaDataVarName) {
        FieldVisitor fv = cw.visitField(Opcodes.ACC_STATIC, metaDataVarName, String.format("L%s;", STRAND_METADATA),
                null, null);
        fv.visitEnd();
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        JvmCodeGenUtil.genStrandMetadataField(mv, typeOwnerClass, module.packageID, metaDataVarName,
                new ScheduleFunctionInfo(varName));
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateRecordMethods(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                             PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                             String metadataVarName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_RECORD_VALUE,
                String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);
        mv.visitCode();
        if (recordTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such record: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_RECORD_VALUE + 0,
                    String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE), false);
            mv.visitInsn(ARETURN);
            generateCreateRecordMethodSplits(cw, recordTypeDefList, moduleId, moduleInitClass, typeOwnerClass,
                    metadataVarName);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateRecordMethodSplits(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                                  PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                                  String metadataVarName) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;

        List<Label> targetLabels = new ArrayList<>();

        int fieldNameRegIndex = 0;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        recordTypeDefList.sort(NAME_HASH_COMPARATOR);

        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : recordTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, CREATE_RECORD_VALUE + methodCount++,
                        String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                        String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = recordTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, recordTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, recordTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            String fieldName = getTypeFieldName(optionalTypeDef.internalName.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, optionalTypeDef.internalName.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, String.format("L%s;", TYPE));
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metadataVarName, String.format("L%s;", STRAND_METADATA));
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND_CLASS, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRAND_METADATA, SCHEDULER,
                                             STRAND_CLASS, MAP), false);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKESTATIC, className, JvmConstants.RECORD_INIT_WRAPPER_NAME,
                    String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE), false);

            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == recordTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, "No such record: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_RECORD_VALUE + methodCount,
                            String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, "No such record: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    private void generateCreateObjectMethods(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                             PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                             SymbolTable symbolTable, String metadataVarName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE,
                String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                        STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), null, null);
        mv.visitCode();
        if (objectTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such object: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + 0,
                    String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                            STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), false);
            mv.visitInsn(ARETURN);
            generateCreateObjectMethodSplits(cw, objectTypeDefList, moduleId, moduleInitClass, typeOwnerClass,
                    symbolTable, metadataVarName);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateObjectMethodSplits(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                                  PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                                  SymbolTable symbolTable, String metadataVarName) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        List<Label> targetLabels = new ArrayList<>();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        int var1Index = indexMap.addIfNotExists("var1", symbolTable.stringType);
        int schedulerIndex = indexMap.addIfNotExists("scheduler", symbolTable.anyType);
        int parentIndex = indexMap.addIfNotExists("parent", symbolTable.anyType);
        int propertiesIndex = indexMap.addIfNotExists("properties", symbolTable.anyType);
        int argsIndex = indexMap.addIfNotExists("args", symbolTable.anyType);
        Label defaultCaseLabel = new Label();
        // sort the fields before generating switch case
        objectTypeDefList.sort(NAME_HASH_COMPARATOR);
        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : objectTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE + methodCount++,
                        String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                                STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = objectTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = createLabelsForSwitch(mv, var1Index, objectTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = createLabelsForEqualCheck(mv, var1Index, objectTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            String fieldName = getTypeFieldName(optionalTypeDef.internalName.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, optionalTypeDef.internalName.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, String.format("L%s;", TYPE));
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", OBJECT_TYPE_IMPL),
                               false);

            int tempVarIndex = indexMap.addIfNotExists("tempVar", optionalTypeDef.type);
            mv.visitVarInsn(ASTORE, tempVarIndex);
            int strandVarIndex = indexMap.addIfNotExists("strandVar", symbolTable.anyType);

            mv.visitVarInsn(ALOAD, parentIndex);
            Label parentNonNullLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, parentNonNullLabel);
            Label parentNullLabel = new Label();
            mv.visitLabel(parentNullLabel);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metadataVarName, String.format("L%s;", STRAND_METADATA));
            mv.visitVarInsn(ALOAD, schedulerIndex);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitVarInsn(ALOAD, propertiesIndex);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND_CLASS, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRAND_METADATA, SCHEDULER,
                                             STRAND_CLASS, MAP), false);
            mv.visitVarInsn(ASTORE, strandVarIndex);
            Label endConditionLabel = new Label();
            mv.visitJumpInsn(GOTO, endConditionLabel);
            mv.visitLabel(parentNonNullLabel);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitVarInsn(ASTORE, strandVarIndex);
            mv.visitLabel(endConditionLabel);

            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitVarInsn(ALOAD, strandVarIndex);

            mv.visitLdcInsn("$init$");
            mv.visitVarInsn(ALOAD, argsIndex);

            String methodDesc = String.format("(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT);
            mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", methodDesc, true);

            int tempResultIndex = indexMap.addIfNotExists("tempResult", symbolTable.anyType);
            mv.visitVarInsn(ASTORE, tempResultIndex);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(INSTANCEOF, BERROR);
            Label noErrorLabel = new Label();
            mv.visitJumpInsn(IFEQ, noErrorLabel);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(CHECKCAST, BERROR);
            mv.visitInsn(ATHROW);
            mv.visitLabel(noErrorLabel);
            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitInsn(ARETURN);

            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == objectTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, var1Index, "No such object: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + methodCount,
                            String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                                    STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }

        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, var1Index, "No such object: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    private void generateCreateErrorMethods(ClassWriter cw, List<BIRTypeDefinition> errorTypeDefList,
                                            String moduleInitClass, String typeOwnerClass, SymbolTable symbolTable) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_ERROR_VALUE,
                String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                        OBJECT, BERROR), null, null);
        mv.visitCode();
        if (errorTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such error: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_ERROR_VALUE + 0,
                    String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                            OBJECT, BERROR), false);
            mv.visitInsn(ARETURN);
            generateCreateErrorMethodSplits(cw, errorTypeDefList, moduleInitClass, typeOwnerClass, symbolTable);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateErrorMethodSplits(ClassWriter cw, List<BIRTypeDefinition> errorTypeDefList,
                                                 String moduleInitClass, String typeOwnerClass,
                                                 SymbolTable symbolTable) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        List<Label> targetLabels = new ArrayList<>();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        int errorNameIndex = indexMap.addIfNotExists("errorTypeName", symbolTable.stringType);
        int messageIndex = indexMap.addIfNotExists("message", symbolTable.stringType);
        int causeIndex = indexMap.addIfNotExists("cause", symbolTable.errorType);
        int detailsIndex = indexMap.addIfNotExists("details", symbolTable.anyType);
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        errorTypeDefList.sort(NAME_HASH_COMPARATOR);
        int i = 0;
        for (BIRTypeDefinition errorDefinition : errorTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_ERROR_VALUE + methodCount++,
                        String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR, OBJECT, BERROR)
                        , null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = errorTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = createLabelsForSwitch(mv, errorNameIndex, errorTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = createLabelsForEqualCheck(mv, errorNameIndex, errorTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            String fieldName = getTypeFieldName(errorDefinition.internalName.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitTypeInsn(NEW, ERROR_VALUE);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, String.format("L%s;", TYPE));
            mv.visitVarInsn(ALOAD, messageIndex);
            mv.visitVarInsn(ALOAD, causeIndex);
            mv.visitVarInsn(ALOAD, detailsIndex);
            mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;L%s;L%s;)V", TYPE, B_STRING_VALUE, BERROR, OBJECT), false);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == errorTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, errorNameIndex, "No such error: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_ERROR_VALUE + methodCount,
                            String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                                    OBJECT, BERROR), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, errorNameIndex, "No such error: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    // -------------------------------------------------------
    //              Record type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the record.
     *
     * @param mv         method visitor
     * @param recordType record type
     */
    private void createRecordType(MethodVisitor mv, BRecordType recordType) {
        // Create the record type
        mv.visitTypeInsn(NEW, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        String name = getFullName(recordType);
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(name));

        // Load package path
        // TODO: get it from the type
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = recordType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load flags
        mv.visitLdcInsn(recordType.tsymbol.flags);

        // Load 'sealed' flag
        mv.visitLdcInsn(recordType.sealed);

        // Load type flags
        mv.visitLdcInsn(jvmTypeGen.typeFlag(recordType));

        // initialize the record type
        mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;JZI)V", STRING_VALUE, MODULE), false);
    }

    private String getFullName(BRecordType recordType) {
        String fullName;

        if (recordType.shouldPrintShape()) {
            fullName = recordType.toString();
        } else {
            // for non-shape values toString gives the org name + name, we only need the name
            fullName = recordType.tsymbol.name.value;
        }
        return fullName;
    }

    /**
     * Add the field type information of a record type. The record type is assumed
     * to be at the top of the stack.
     *
     * @param mv     method visitor
     * @param fields record fields to be added
     */
    private void addRecordFields(MethodVisitor mv, Map<String, BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, "()V", false);

        for (BField optionalField : fields.values()) {
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(decodeIdentifier(optionalField.name.value));

            // create and load field type
            createRecordField(mv, optionalField);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE_IMPL, "setFields", String.format("(L%s;)V", MAP), false);
    }

    /**
     * Create a field information for records.
     *
     * @param mv    method visitor
     * @param field field Parameter Description
     */
    private void createRecordField(MethodVisitor mv, BField field) {

        mv.visitTypeInsn(NEW, FIELD_IMPL);
        mv.visitInsn(DUP);

        // Load the field type
        jvmTypeGen.loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(decodeIdentifier(field.name.value));

        // Load flags
        mv.visitLdcInsn(field.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, FIELD_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;J)V", TYPE,
                                                                                     STRING_VALUE), false);
    }

    /**
     * Add the rest field to a record type. The record type is assumed
     * to be at the top of the stack.
     *
     * @param mv            method visitor
     * @param restFieldType type of the rest field
     */
    private void addRecordRestField(MethodVisitor mv, BType restFieldType) {
        // Load the rest field type
        jvmTypeGen.loadType(mv, restFieldType);
        mv.visitFieldInsn(PUTFIELD, RECORD_TYPE_IMPL, "restFieldType", String.format("L%s;", TYPE));
    }

    // -------------------------------------------------------
    //              Object type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the object.
     *
     * @param mv         method visitor
     * @param objectType object type
     */
    private void createObjectType(MethodVisitor mv, BObjectType objectType) {
        // Create the object type
        String objectClassName = Symbols.isService(objectType.tsymbol) ? SERVICE_TYPE_IMPL : OBJECT_TYPE_IMPL;

        mv.visitTypeInsn(NEW, objectClassName);
        mv.visitInsn(DUP);

        // Load type name
        BTypeSymbol typeSymbol = objectType.tsymbol;
        mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));

        // Load package path
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = objectType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load flags
        mv.visitLdcInsn(typeSymbol.flags);

        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, objectClassName, JVM_INIT_METHOD,
                String.format("(L%s;L%s;J)V", STRING_VALUE, MODULE), false);
    }

    /**
     * Create a runtime type instance for union used in type definitions.
     *
     * @param mv        method visitor
     * @param unionType union type
     */
    private void createUnionType(MethodVisitor mv, BUnionType unionType) {
        mv.visitTypeInsn(NEW, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);

        jvmTypeGen.loadUnionName(mv, unionType);

        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = unionType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        mv.visitLdcInsn(jvmTypeGen.typeFlag(unionType));

        jvmTypeGen.loadCyclicFlag(mv, unionType);

        mv.visitLdcInsn(unionType.flags);

        // initialize the union type without the members array
        mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;IZJ)V", STRING_VALUE, MODULE), false);
    }

    /**
     * Create a runtime type instance for tuple used in type definitions.
     *
     * @param mv        method visitor
     * @param tupleType tuple type
     */
    private void createTupleType(MethodVisitor mv, BTupleType tupleType) {
        mv.visitTypeInsn(NEW, TUPLE_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        BTypeSymbol typeSymbol = tupleType.tsymbol;
        mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));

        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        PackageID packageID = tupleType.tsymbol.pkgID;
        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        mv.visitLdcInsn(jvmTypeGen.typeFlag(tupleType));
        jvmTypeGen.loadCyclicFlag(mv, tupleType);
        jvmTypeGen.loadReadonlyFlag(mv, tupleType);

        // initialize the tuple type without the members array
        mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE_IMPL, JVM_INIT_METHOD,
                String.format("(L%s;L%s;IZZ)V", STRING_VALUE, MODULE), false);
    }

    /**
     * Add member type to tuple in a type definition.
     *
     * @param mv        method visitor
     * @param tupleType   tupleType
     */
    private void addTupleMembers(MethodVisitor mv, BTupleType tupleType) {
        createTupleMembersList(mv, tupleType.tupleTypes);

        BType restType = tupleType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            jvmTypeGen.loadType(mv, restType);
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, TUPLE_TYPE_IMPL, SET_MEMBERS_METHOD, String.format("(L%s;L%s;)V", LIST, TYPE),
                false);
    }

    /**
     * Add member type to unions in a type definition.
     *
     * @param mv        method visitor
     * @param unionType   unionType
     */
    private void addUnionMembers(MethodVisitor mv, BUnionType unionType) {
        jvmTypeGen.createUnionMembersArray(mv, unionType.getMemberTypes());
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_MEMBERS_METHOD,
                String.format("([L%s;)V", TYPE), false);

        jvmTypeGen.createUnionMembersArray(mv, unionType.getOriginalMemberTypes());
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_ORIGINAL_MEMBERS_METHOD,
                String.format("([L%s;)V", TYPE), false);
    }

    /**
     * Add cyclic flag to union.
     *
     * @param mv        method visitor
     * @param userDefinedType bType
     */
    private void addCyclicFlag(MethodVisitor mv, BType userDefinedType) {
        jvmTypeGen.loadCyclicFlag(mv, userDefinedType);
        if (userDefinedType.tag == TypeTags.TUPLE) {
            mv.visitMethodInsn(INVOKEVIRTUAL, TUPLE_TYPE_IMPL, SET_CYCLIC_METHOD, "(Z)V", false);
        } else if (userDefinedType.tag == TypeTags.UNION) {
            mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_CYCLIC_METHOD, "(Z)V", false);
        }
    }

    /**
     * Add the field type information to an object type. The object type is assumed
     * to be at the top of the stack.
     *
     * @param mv     method visitor
     * @param fields object fields to be added
     */
    private void addObjectFields(MethodVisitor mv, Map<String, BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, "()V", false);

        for (BField optionalField : fields.values()) {
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(decodeIdentifier(optionalField.name.value));

            // create and load field type
            createObjectField(mv, optionalField);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setFields", String.format("(L%s;)V", MAP), false);
    }

    /**
     * Create a field information for objects.
     *
     * @param mv    method visitor
     * @param field object field
     */
    private void createObjectField(MethodVisitor mv, BField field) {

        mv.visitTypeInsn(NEW, FIELD_IMPL);
        mv.visitInsn(DUP);

        // Load the field type
        jvmTypeGen.loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(decodeIdentifier(field.name.value));

        // Load flags
        mv.visitLdcInsn(field.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, FIELD_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;J)V", TYPE,
                                                                                     STRING_VALUE), false);
    }

    /**
     * Add the attached function information to an object type. The object type is assumed
     * to be at the top of the stack.
     *
     * @param cw                class writer
     * @param mv                method visitor
     * @param fieldName         object name
     * @param attachedFunctions attached functions to be added
     * @param objType           object type to be used to create attached functions
     */
    private void addObjectAttachedFunctions(ClassWriter cw, MethodVisitor mv, String fieldName,
                                            List<BAttachedFunction> attachedFunctions, BObjectType objType,
                                            SymbolTable symbolTable) {
        // Create the attached function array
        mv.visitLdcInsn((long) attachedFunctions.size() - resourceFunctionCount(attachedFunctions));
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, METHOD_TYPE_IMPL);
        String methodName = String.format("$populate%s$%s", fieldName, "attachedFunctions");
        int methodCount = splitObjectAttachedFunctions(cw, methodName, attachedFunctions, objType, symbolTable);
        if (methodCount > 0) {
            mv.visitVarInsn(ASTORE, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, typesClass, methodName + 0, String.format("([L%s;)V",
                                                                                       METHOD_TYPE_IMPL), false);
            mv.visitVarInsn(ALOAD, 0);
        }
        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setMethods",
                String.format("([L%s;)V", METHOD_TYPE), false);
    }

    private int splitObjectAttachedFunctions(ClassWriter cw, String methodName,
                                             List<BAttachedFunction> attachedFunctions,
                                             BObjectType objType, SymbolTable symbolTable) {
        int fTypeCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        List<BAttachedFunction> nonResourceFunctions = new ArrayList<>();
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc != null && !(attachedFunc instanceof BResourceFunction)) {
                nonResourceFunctions.add(attachedFunc);
            }
        }
        int arrayIndex = indexMap.addIfNotExists("$array", symbolTable.anyType);
        for (BAttachedFunction attachedFunc : nonResourceFunctions) {
            if (fTypeCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName + methodCount++,
                        String.format("([L%s;)V", METHOD_TYPE_IMPL), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
            }
            // create and load attached function
            createObjectMemberFunction(mv, attachedFunc, objType);
            int attachedFunctionVarIndex = indexMap.addIfNotExists(toNameString(objType) + attachedFunc.funcName.value,
                    symbolTable.anyType);
            mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);

            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) fTypeCount);
            mv.visitInsn(L2I);

            // Add the member to the array
            mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
            mv.visitInsn(AASTORE);
            fTypeCount++;
            if (fTypeCount % MAX_TYPES_PER_METHOD == 0) {
                if (fTypeCount != nonResourceFunctions.size()) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, typesClass, methodName + methodCount,
                                       String.format("([L%s;)V", METHOD_TYPE_IMPL), false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(fTypeCount + 10, fTypeCount + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && fTypeCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(fTypeCount + 10, fTypeCount + 10);
            mv.visitEnd();
        }
        return methodCount;
    }

    private void addObjectInitFunction(MethodVisitor mv, BAttachedFunction initFunction,
                                              BObjectType objType, BIRVarToJVMIndexMap indexMap, String funcName,
                                              String initializerFuncName, SymbolTable symbolTable) {

        if (initFunction == null || !initFunction.funcName.value.contains(funcName)) {
            return;
        }

        mv.visitInsn(DUP);
        createObjectMemberFunction(mv, initFunction, objType);
        int attachedFunctionVarIndex = indexMap.addIfNotExists(objType.name + initFunction.funcName.value,
                                                               symbolTable.anyType);
        mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
        mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
        mv.visitInsn(DUP);
        mv.visitInsn(POP);
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, initializerFuncName,
                           String.format("(L%s;)V", METHOD_TYPE_IMPL), false);
    }

    private void addResourceMethods(ClassWriter cw, MethodVisitor mv, String fieldName,
                                    List<BAttachedFunction> attachedFunctions, BObjectType objType,
                                    SymbolTable symbolTable) {
        if (!Symbols.isService(objType.tsymbol)) {
            return;
        }
        mv.visitInsn(DUP);
        mv.visitTypeInsn(CHECKCAST, SERVICE_TYPE_IMPL);
        // Create the resource function array
        mv.visitLdcInsn(resourceFunctionCount(attachedFunctions));
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, RESOURCE_METHOD_TYPE);
        String methodName = String.format("$populate%s$%s", fieldName, "resourceFunctions");
        int methodCount = splitResourceMethods(cw, methodName, attachedFunctions, objType, symbolTable);
        if (methodCount > 0) {
            mv.visitVarInsn(ASTORE, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, typesClass, methodName + 0, String.format("([L%s;)V",
                                                                                       RESOURCE_METHOD_TYPE), false);
            mv.visitVarInsn(ALOAD, 0);
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, SERVICE_TYPE_IMPL, "setResourceMethods",
                String.format("([L%s;)V", RESOURCE_METHOD_TYPE), false);
    }

    private int splitResourceMethods(ClassWriter cw, String methodName, List<BAttachedFunction> attachedFunctions,
                                     BObjectType objType, SymbolTable symbolTable) {
        int resourcesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        List<BAttachedFunction> resourceFunctions = new ArrayList<>();
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc instanceof BResourceFunction) {
                resourceFunctions.add(attachedFunc);
            }
        }
        int arrayIndex = indexMap.addIfNotExists("$array", symbolTable.anyType);
        for (BAttachedFunction attachedFunc : resourceFunctions) {
            if (resourcesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName + methodCount++,
                        String.format("([L%s;)V", RESOURCE_METHOD_TYPE), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
            }
            BResourceFunction resourceFunction = (BResourceFunction) attachedFunc;
            createResourceFunction(mv, resourceFunction, objType);

            String varRefName = toNameString(objType) + resourceFunction.funcName.value + "$r$func";
            int rFuncVarIndex = indexMap.addIfNotExists(varRefName, symbolTable.anyType);
            mv.visitVarInsn(ASTORE, rFuncVarIndex);

            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) resourcesCount);
            mv.visitInsn(L2I);

            // Add the member to the array
            mv.visitVarInsn(ALOAD, rFuncVarIndex);
            mv.visitInsn(AASTORE);
            resourcesCount++;
            if (resourcesCount % MAX_TYPES_PER_METHOD == 0) {
                if (resourcesCount != resourceFunctions.size()) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, typesClass, methodName + methodCount,
                                       String.format("([L%s;)V", RESOURCE_METHOD_TYPE), false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(resourcesCount + 10, resourcesCount + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && resourcesCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(resourcesCount + 10, resourcesCount + 10);
            mv.visitEnd();
        }
        return methodCount;
    }

    private static long resourceFunctionCount(List<BAttachedFunction> attachedFunctions) {
        int i = 0;
        for (BAttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction instanceof BResourceFunction) {
                i++;
            }
        }
        return i;
    }

    /**
     * Create a attached function information for objects.
     *
     * @param mv           method visitor
     * @param attachedFunc object attached function
     * @param objType      object type used for creating the attached function
     */
    private void createObjectMemberFunction(MethodVisitor mv, BAttachedFunction attachedFunc,
                                                   BObjectType objType) {

        if (Symbols.isRemote(attachedFunc.symbol)) {
            createRemoteFunction(mv, attachedFunc, objType);
            return;
        }

        mv.visitTypeInsn(NEW, METHOD_TYPE_IMPL);

        mv.visitInsn(DUP);

        // Load function name
        mv.visitLdcInsn(decodeIdentifier(attachedFunc.funcName.value));

        // Load the parent object type
        jvmTypeGen.loadType(mv, objType);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);

        // Load the field type
        jvmTypeGen.loadType(mv, attachedFunc.type);

        // Load flags
        mv.visitLdcInsn(attachedFunc.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, METHOD_TYPE_IMPL, JVM_INIT_METHOD,
                String.format("(L%s;L%s;L%s;J)V", STRING_VALUE, OBJECT_TYPE_IMPL, FUNCTION_TYPE_IMPL),
                false);
    }

    private void createRemoteFunction(MethodVisitor mv, BAttachedFunction attachedFunc, BObjectType objType) {
        mv.visitTypeInsn(NEW, REMOTE_METHOD_TYPE_IMPL);

        mv.visitInsn(DUP);

        // Load function name
        mv.visitLdcInsn(decodeIdentifier(attachedFunc.funcName.value));

        // Load the parent object type
        jvmTypeGen.loadType(mv, objType);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);

        // Load the field type
        jvmTypeGen.loadType(mv, attachedFunc.type);

        // Load flags
        mv.visitLdcInsn(attachedFunc.symbol.flags);
        mv.visitMethodInsn(INVOKESPECIAL, REMOTE_METHOD_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;J)V", STRING_VALUE, OBJECT_TYPE_IMPL,
                                         FUNCTION_TYPE_IMPL), false);

    }

    private void createResourceFunction(MethodVisitor mv, BResourceFunction resourceFunction,
                                               BObjectType objType) {

        mv.visitTypeInsn(NEW, RESOURCE_METHOD_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load function name
        mv.visitLdcInsn(resourceFunction.funcName.value);

        // Load the parent object type
        jvmTypeGen.loadType(mv, objType);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);

        // Load the invokable type
        jvmTypeGen.loadType(mv, resourceFunction.type);

        // Load flags
        mv.visitLdcInsn(resourceFunction.symbol.flags);

        // Load accessor
        mv.visitLdcInsn(decodeIdentifier(resourceFunction.accessor.value));

        // Load resource path
        mv.visitLdcInsn((long) resourceFunction.resourcePath.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        List<Name> resourcePath = resourceFunction.resourcePath;
        for (int i = 0, resourcePathSize = resourcePath.size(); i < resourcePathSize; i++) {
            Name path = resourcePath.get(i);
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // load resource path name
            mv.visitLdcInsn(path.value);

            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKESPECIAL, RESOURCE_METHOD_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;JL%s;[L%s;)V", STRING_VALUE, OBJECT_TYPE_IMPL,
                                         FUNCTION_TYPE_IMPL, STRING_VALUE, STRING_VALUE), false);
    }

    private void createTupleMembersList(MethodVisitor mv, List<BType> members) {
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, "()V", false);

        for (BType tupleType : members) {
            mv.visitInsn(DUP);
            jvmTypeGen.loadType(mv, tupleType);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
        }
    }

    // -------------------------------------------------------
    //              Error type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the error.
     *
     * @param mv        method visitor
     * @param errorType error type
     * @param name      name of the error
     */
    private void createErrorType(MethodVisitor mv, BErrorType errorType, String name) {
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load error type name
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(name));

        // Load package
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        PackageID packageID = errorType.tsymbol.pkgID;
        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;)V", STRING_VALUE, MODULE), false);
    }

}
