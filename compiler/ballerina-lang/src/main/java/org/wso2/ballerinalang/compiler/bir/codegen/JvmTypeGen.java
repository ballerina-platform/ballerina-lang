/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFEQ;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ATTACHED_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BFIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BUILT_IN_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FINITE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PACKAGE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPES_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.I_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getObjectField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getRecordField;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupGlobalVarClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.NodeSorter;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createLabelsForEqualCheck;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createLabelsForSwitch;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;


//import ballerina/bir;
//import ballerina/io;
//import ballerina/jvm;
//import ballerina/stringutils;

public class JvmTypeGen {
    //# Name of the class to which the types will be added as static fields.
    public static String typeOwnerClass = "";

    //# # Create static fields to hold the user defined types.
//#
//# + cw - class writer
//# + typeDefs - array of type definitions
    public static void generateUserDefinedTypeFields(ClassWriter cw, @Nilable List<BIRTypeDefinition> typeDefs) {
        String fieldName;
        // create the type
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            fieldName = getTypeFieldName(typeDef.name.value);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.RECORD || bType.tag == TypeTags.OBJECT || bType.tag == TypeTags.ERROR ||
                    bType.tag == TypeTags.SERVICE) {
                FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, fieldName, String.format("L%s;", BTYPE), null, null);
                fv.visitEnd();
            } else {
                // do not generate anything for other types (e.g.: finite type, unions, etc.)
            }
        }
    }

    //# Create instances of runtime types. This will create one instance from each
//# runtime type and populate the static fields.
//#
//# + mv - method visitor
    public static void generateUserDefinedTypes(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, "$createTypes", "()V", false);
    }

    static void generateCreateTypesMethod(ClassWriter cw, @Nilable List<BIRTypeDefinition> typeDefs) {
        createTypesInstance(cw, typeDefs);
        List<String> populateTypeFuncNames = populateTypes(cw, typeDefs);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$createTypes", "()V", null, null);
        mv.visitCode();

        // Invoke create-type-instances method
        mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, "$createTypeInstances", "()V", false);

        // Invoke the populate-type functions
        for (String funcName : populateTypeFuncNames) {
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, funcName, "()V", false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    static void createTypesInstance(ClassWriter cw, @Nilable List<BIRTypeDefinition> typeDefs) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$createTypeInstances", "()V", null, null);
        mv.visitCode();

        // Create the type
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            fieldName = getTypeFieldName(typeDef.name.value);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                createRecordType(mv, bType, typeDef);
            } else if (bType.tag == TypeTags.OBJECT) {
                createObjectType(mv, bType, typeDef);
            } else if (bType.tag == TypeTags.SERVICE) {
                createServiceType(mv, bType.oType, typeDef);
            } else if (bType.tag == TypeTags.ERROR) {
                createErrorType(mv, bType, typeDef.name.value);
            } else {
                // do not generate anything for other types (e.g.: finite type, unions, etc.)
                continue;
            }

            mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, fieldName, String.format("L%s;", BTYPE));
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    static List<String> populateTypes(ClassWriter cw, @Nilable List<BIRTypeDefinition> typeDefs) {
        List<String> funcNames = new ArrayList<>();
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if !(bType.tag == TypeTags.RECORD || bType.tag == TypeTags.OBJECT ||
                    bType.tag == TypeTags.SERVICE || bType.tag == TypeTags.ERROR) {
                continue;
            }

            fieldName = getTypeFieldName(typeDef.name.value);
            String methodName = String.format("$populate%s", fieldName);
            funcNames.add(methodName);

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", BTYPE));

            BalToJVMIndexMap indexMap = new;
            if (bType.tag == TypeTags.RECORD) {
                mv.visitTypeInsn(CHECKCAST, RECORD_TYPE);
                mv.visitInsn(DUP);
                addRecordFields(mv, bType.fields);
                addRecordRestField(mv, bType.restFieldType);
            } else if (bType.tag == TypeTags.OBJECT) {
                mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
                mv.visitInsn(DUP);
                addObjectFields(mv, bType.fields);
                addObjectInitFunction(mv, bType.generatedConstructor, bType, indexMap, "$__init$", "setGeneratedInitializer");
                addObjectInitFunction(mv, bType.constructor, bType, indexMap, "__init", "setInitializer");
                addObjectAttachedFunctions(mv, bType.attachedFunctions, bType, indexMap);
            } else if (bType.tag == TypeTags.SERVICE) {
                mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
                mv.visitInsn(DUP);
                addObjectFields(mv, bType.oType.fields);
                addObjectAttachedFunctions(mv, bType.oType.attachedFunctions, bType.oType, indexMap);
            } else if (bType.tag == TypeTags.ERROR) {
                // populate detail field
                mv.visitTypeInsn(CHECKCAST, ERROR_TYPE);
                mv.visitInsn(DUP);
                mv.visitInsn(DUP);
                loadType(mv, bType.detailType);
                mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE, SET_DETAIL_TYPE_METHOD, String.format("(L%s;)V", BTYPE), false);
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return funcNames;
    }


    // -------------------------------------------------------
    //              Runtime value creation methods
    // -------------------------------------------------------

    public static void generateValueCreatorMethods(ClassWriter cw, @Nilable List<BIRTypeDefinition> typeDefs, PackageID moduleId) {
        @Nilable List<BIRTypeDefinition> recordTypeDefs = new ArrayList<>();
        @Nilable List<BIRTypeDefinition> objectTypeDefs = new ArrayList<>();

        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                recordTypeDefs.add(i, typeDef);
                i += 1;
            }
        }

        i = 0;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.OBJECT && !bType.isAbstract) {
                objectTypeDefs.add(i, typeDef);
                i += 1;
            }
        }

        generateRecordValueCreateMethod(cw, recordTypeDefs, moduleId);
        generateObjectValueCreateMethod(cw, objectTypeDefs, moduleId);
    }

    static void generateRecordValueCreateMethod(ClassWriter cw, @Nilable List<BIRTypeDefinition> recordTypeDefs, PackageID moduleId) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "createRecordValue",
                String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);

        mv.visitCode();

        int fieldNameRegIndex = 1;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        NodeSorter sorter = new NodeSorter();
        sorter.sortByHash(recordTypeDefs);

        List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, recordTypeDefs, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, recordTypeDefs, labels,
                defaultCaseLabel);

        int i = 0;

        for (BIRTypeDefinition optionalTypeDef : recordTypeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            String fieldName = getTypeFieldName(typeDef.name.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, typeDef.name.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", BTYPE));
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", BTYPE), false);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, STRAND);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND, "<init>", String.format("(L%s;)V", SCHEDULER), false);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKESTATIC, className, "$init", String.format("(L%s;L%s;)V", STRAND, MAP_VALUE), false);

            mv.visitInsn(ARETURN);
            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(recordTypeDefs.size() + 10, recordTypeDefs.size() + 10);
        mv.visitEnd();
    }

    static void generateObjectValueCreateMethod(ClassWriter cw, @Nilable List<BIRTypeDefinition> objectTypeDefs, PackageID moduleId) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "createObjectValue",
                String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;", STRING_VALUE, SCHEDULER, STRAND, MAP, OBJECT, OBJECT_VALUE), null, null);

        BalToJVMIndexMap indexMap = new;

        BIRVariableDcl selfVar = new BIRVariableDcl(type:"any",
                name:new (value:"self" ),
        kind:
        "ARG" );
        BIRVariableDcl var1 = new BIRVariableDcl(type:"string",
                name:new (value:"var1" ),
        kind:
        "ARG" );
        BIRVariableDcl scheduler = new BIRVariableDcl(type:"any",
                name:new (value:"scheduler" ),
        kind:
        "ARG" );
        BIRVariableDcl parent = new BIRVariableDcl(type:"any",
                name:new (value:"parent" ),
        kind:
        "ARG" );
        BIRVariableDcl properties = new BIRVariableDcl(type:"any",
                name:new (value:"properties" ),
        kind:
        "ARG" );
        BIRVariableDcl args = new BIRVariableDcl(type:"any",
                name:new (value:"args" ),
        kind:
        "ARG" );
        _ = indexMap.getIndex(selfVar);
        int var1Index = indexMap.getIndex(var1);
        int schedulerIndex = indexMap.getIndex(scheduler);
        int parentIndex = indexMap.getIndex(parent);
        int propertiesIndex = indexMap.getIndex(properties);
        int argsIndex = indexMap.getIndex(args);

        mv.visitCode();

        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        NodeSorter sorter = new NodeSorter();
        sorter.sortByHash(objectTypeDefs);

        List<Label> labels = createLabelsForSwitch(mv, var1Index, objectTypeDefs, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, var1Index, objectTypeDefs, labels,
                defaultCaseLabel);

        int i = 0;

        for (BIRTypeDefinition optionalTypeDef : objectTypeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            String fieldName = getTypeFieldName(typeDef.name.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, typeDef.name.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", BTYPE));
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", OBJECT_TYPE), false);

            BIRVariableDcl tempVar = new BIRVariableDcl(type:typeDef.type,
                    name:new (value:"tempVar" ),
            kind:
            "LOCAL" );
            int tempVarIndex = indexMap.getIndex(tempVar);
            mv.visitVarInsn(ASTORE, tempVarIndex);

            mv.visitTypeInsn(NEW, STRAND);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, schedulerIndex);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitVarInsn(ALOAD, propertiesIndex);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND, "<init>", String.format("(L%s;L%s;L%s;)V", SCHEDULER, STRAND, MAP), false);
            BIRVariableDcl strandVar = new BIRVariableDcl(type:"any",
                    name:new (value:"strandVar" ),
            kind:
            "LOCAL" );
            int strandVarIndex = indexMap.getIndex(strandVar);
            mv.visitVarInsn(ASTORE, strandVarIndex);

            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitVarInsn(ALOAD, strandVarIndex);

            mv.visitLdcInsn("$__init$");
            mv.visitVarInsn(ALOAD, argsIndex);

            String methodDesc = String.format("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
            mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

            BIRVariableDcl tempResult = new BIRVariableDcl(type:"any",
                    name:new (value:"tempResult" ),
            kind:
            "LOCAL" );
            int tempResultIndex = indexMap.getIndex(tempResult);
            mv.visitVarInsn(ASTORE, tempResultIndex);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(INSTANCEOF, ERROR_VALUE);
            Label noErrorLabel = new Label();
            mv.visitJumpInsn(IFEQ, noErrorLabel);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(CHECKCAST, ERROR_VALUE);
            mv.visitInsn(ATHROW);
            mv.visitLabel(noErrorLabel);
            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitInsn(ARETURN);

            i += 1;
        }

        createDefaultCase(mv, defaultCaseLabel, var1Index);
        mv.visitMaxs(objectTypeDefs.size() + 100, objectTypeDefs.size() + 100);
        mv.visitEnd();
    }


    // -------------------------------------------------------
    //              Record type generation methods
    // -------------------------------------------------------

    //# Create a runtime type instance for the record.
//#
//# + mv - method visitor
//# + recordType - record type
    static void createRecordType(MethodVisitor mv, BRecordType recordType, BIRTypeDefinition typeDef) {
        // Create the record type
        mv.visitTypeInsn(NEW, RECORD_TYPE);
        mv.visitInsn(DUP);

        // Load type name
        String name = typeDef.name.value;
        mv.visitLdcInsn(name);

        // Load package path
        // TODO: get it from the type
        mv.visitTypeInsn(NEW, PACKAGE_TYPE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(recordType.moduleId.org);
        mv.visitLdcInsn(recordType.moduleId.name);
        mv.visitLdcInsn(recordType.moduleId.modVersion);
        mv.visitMethodInsn(INVOKESPECIAL, PACKAGE_TYPE, "<init>",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

        // Load flags
        mv.visitLdcInsn(typeDef.flags);
        mv.visitInsn(L2I);

        // Load 'sealed' flag
        mv.visitLdcInsn(recordType.sealed);

        // Load type flags
        mv.visitLdcInsn(recordType.typeFlags);
        mv.visitInsn(L2I);

        // initialize the record type
        mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE, "<init>",
                String.format("(L%s;L%s;IZI)V", STRING_VALUE, PACKAGE_TYPE),
                false);

        return;
    }

    //# Add the field type information of a record type. The record type is assumed
//# to be at the top of the stack.
//#
//# + mv - method visitor
//# + fields - record fields to be added
    static void addRecordFields(MethodVisitor mv, @Nilable List<BIRBRecordField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

        for (BIRBRecordField optionalField : fields) {
            BIRBRecordField field = getRecordField(optionalField);
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(field.name.value);

            // create and load field type
            createRecordField(mv, field);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE, "setFields", String.format("(L%s;)V", MAP), false);
    }

    //# Create a field information for records.
//#
//# + mv - method visitor
//# + field - field Parameter Description
    static void createRecordField(MethodVisitor mv, BIRBRecordField field) {
        mv.visitTypeInsn(NEW, BFIELD);
        mv.visitInsn(DUP);

        // Load the field type
        loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(field.name.value);

        // Load flags
        mv.visitLdcInsn(field.flags);
        mv.visitInsn(L2I);

        mv.visitMethodInsn(INVOKESPECIAL, BFIELD, "<init>",
                String.format("(L%s;L%s;I)V", BTYPE, STRING_VALUE),
                false);
    }

    //# Add the rest field to a record type. The record type is assumed
//# to be at the top of the stack.
//#
//# + mv - method visitor
//# + restFieldType - type of the rest field
    static void addRecordRestField(MethodVisitor mv, BType restFieldType) {
        // Load the rest field type
        loadType(mv, restFieldType);
        mv.visitFieldInsn(PUTFIELD, RECORD_TYPE, "restFieldType", String.format("L%s;", BTYPE));
    }

    // -------------------------------------------------------
    //              Object type generation methods
    // -------------------------------------------------------

    //# Create a runtime type instance for the object.
//#
//# + mv - method visitor
//# + objectType - object type
    static void createObjectType(MethodVisitor mv, BObjectType objectType, BIRTypeDefinition typeDef) {
        // Create the object type
        mv.visitTypeInsn(NEW, OBJECT_TYPE);
        mv.visitInsn(DUP);

        // Load type name
        String name = typeDef.name.value;
        mv.visitLdcInsn(name);

        // Load package path
        mv.visitTypeInsn(NEW, PACKAGE_TYPE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(objectType.moduleId.org);
        mv.visitLdcInsn(objectType.moduleId.name);
        mv.visitLdcInsn(objectType.moduleId.modVersion);
        mv.visitMethodInsn(INVOKESPECIAL, PACKAGE_TYPE, "<init>",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

        // Load flags
        mv.visitLdcInsn(typeDef.flags);
        mv.visitInsn(L2I);

        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE, "<init>",
                String.format("(L%s;L%s;I)V", STRING_VALUE, PACKAGE_TYPE),
                false);
    }

    //# Create a runtime type instance for the service.
//#
//# + mv - method visitor
//# + objectType - object type
//# + typeDef - type definition of the service
    static void createServiceType(MethodVisitor mv, BObjectType objectType, BIRTypeDefinition typeDef) {
        // Create the object type
        mv.visitTypeInsn(NEW, SERVICE_TYPE);
        mv.visitInsn(DUP);

        // Load type name
        String name = typeDef.name.value;
        mv.visitLdcInsn(name);

        // Load package path
        mv.visitTypeInsn(NEW, PACKAGE_TYPE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(objectType.moduleId.org);
        mv.visitLdcInsn(objectType.moduleId.name);
        mv.visitLdcInsn(objectType.moduleId.modVersion);
        mv.visitMethodInsn(INVOKESPECIAL, PACKAGE_TYPE, "<init>",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

        // Load flags
        mv.visitLdcInsn(typeDef.flags);
        mv.visitInsn(L2I);

        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, SERVICE_TYPE, "<init>", String.format("(L%s;L%s;I)V", STRING_VALUE, PACKAGE_TYPE),
                false);
    }

    static void duplicateServiceTypeWithAnnots(MethodVisitor mv, BObjectType objectType, BIRTypeDefinition typeDef,
                                               String pkgName, int strandIndex) {
        createServiceType(mv, objectType, typeDef);
        mv.visitInsn(DUP);

        String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                lookupGlobalVarClassName(pkgName + ANNOTATION_MAP_NAME);
        mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));

        mv.visitVarInsn(ALOAD, strandIndex);

        loadExternalOrLocalType(mv, typeDef);
        mv.visitTypeInsn(CHECKCAST, SERVICE_TYPE);

        @Nilable List<BIRFunction> attachedFunctions = objectType.attachedFunctions;
        mv.visitLdcInsn(attachedFunctions.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION);
        int i = 0;
        for (BIRFunction attachedFunc : attachedFunctions) {
            if (attachedFunc instanceof BIRFunction) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                mv.visitInsn(L2I);

                createObjectAttachedFunction(mv, attachedFunc, objectType);
                mv.visitInsn(AASTORE);
                i += 1;
            }
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, SERVICE_TYPE, "setAttachedFuncsAndProcessAnnots",
                String.format("(L%s;L%s;L%s;[L%s;)V", MAP_VALUE, STRAND, SERVICE_TYPE, ATTACHED_FUNCTION), false);
    }

    //# Add the field type information to an object type. The object type is assumed
//# to be at the top of the stack.
//#
//# + mv - method visitor
//# + fields - object fields to be added
    static void addObjectFields(MethodVisitor mv, @Nilable List<BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

        for (BField optionalField : fields) {
            BField field = getObjectField(optionalField);
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(field.name.value);

            // create and load field type
            createObjectField(mv, field);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, "setFields", String.format("(L%s;)V", MAP), false);
    }

    //# Create a field information for objects.
//#
//# + mv - method visitor
//# + field - object field
    static void createObjectField(MethodVisitor mv, BField field) {
        mv.visitTypeInsn(NEW, BFIELD);
        mv.visitInsn(DUP);

        // Load the field type
        loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(field.name.value);

        // Load flags
        mv.visitLdcInsn(field.flags);
        mv.visitInsn(L2I);

        mv.visitMethodInsn(INVOKESPECIAL, BFIELD, "<init>",
                String.format("(L%s;L%s;I)V", BTYPE, STRING_VALUE),
                false);
    }

    //# Add the attached function information to an object type. The object type is assumed
//# to be at the top of the stack.
//#
//# + mv - method visitor
//# + attachedFunctions - attached functions to be added
    static void addObjectAttachedFunctions(MethodVisitor mv, @Nilable List<BIRFunction> attachedFunctions,
                                           BObjectType objType, BalToJVMIndexMap indexMap) {
        // Create the attached function array
        mv.visitLdcInsn(attachedFunctions.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION);
        int i = 0;
        for (T attachedFunc : attachedFunctions) {
            if (attachedFunc instanceof BIRFunction) {
                // create and load attached function
                createObjectAttachedFunction(mv, attachedFunc, objType);
                BIRVariableDcl attachedFuncVar = new BIRVariableDcl(type:"any",
                        name:new (value:objType.name.value + attachedFunc.name.value),
                kind:
                "LOCAL" );
                int attachedFunctionVarIndex = indexMap.getIndex(attachedFuncVar);
                mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);

                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                mv.visitInsn(L2I);

                // Add the member to the array
                mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
                mv.visitInsn(AASTORE);
                i += 1;
            }
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, "setAttachedFunctions",
                String.format("([L%s;)V", ATTACHED_FUNCTION), false);
    }

    //# Add the init function information to an object type. The object type is assumed
//# to be at the top of the stack.
//#
//# + mv - method visitor
//# + initFunction - init functions to be added
    static void addObjectInitFunction(MethodVisitor mv, @Nilable BIRFunction initFunction,
                                      BObjectType objType, BalToJVMIndexMap indexMap, String funcName,
                                      String initializerFuncSetter) {
        if (initFunction instanceof BIRFunction && initFunction.name.value.contains(funcName)) {
            mv.visitInsn(DUP);
            createObjectAttachedFunction(mv, initFunction, objType);
            BIRVariableDcl attachedFuncVar = new BIRVariableDcl(type:"any",
                    name:new (value:objType.name.value + initFunction.name.value),
            kind:
            "LOCAL" );
            int attachedFunctionVarIndex = indexMap.getIndex(attachedFuncVar);
            mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
            mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
            mv.visitInsn(DUP);
            mv.visitInsn(POP);
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, initializerFuncSetter,
                    String.format("(L%s;)V", ATTACHED_FUNCTION), false);
        }
    }

    //# Create a attached function information for objects.
//#
//# + mv - method visitor
//# + attachedFunc - object attached function
    static void createObjectAttachedFunction(MethodVisitor mv, BIRFunction attachedFunc,
                                             BObjectType objType) {
        mv.visitTypeInsn(NEW, ATTACHED_FUNCTION);
        mv.visitInsn(DUP);

        // Load function name
        mv.visitLdcInsn(attachedFunc.name.value);

        // Load the parent object type
        loadType(mv, objType);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);

        // Load the field type
        loadType(mv, attachedFunc.funcType);

        // Load flags
        mv.visitLdcInsn(attachedFunc.flags);
        mv.visitInsn(L2I);

        mv.visitMethodInsn(INVOKESPECIAL, ATTACHED_FUNCTION, "<init>",
                String.format("(L%s;L%s;L%s;I)V", STRING_VALUE, OBJECT_TYPE, FUNCTION_TYPE), false);
    }

    // -------------------------------------------------------
    //              Error type generation methods
    // -------------------------------------------------------

    //# Create a runtime type instance for the error.
//#
//# + mv - method visitor
//# + errorType - error type
//# + name - name of the error
    static void createErrorType(MethodVisitor mv, BIRBErrorType errorType, String name) {
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE);
        mv.visitInsn(DUP);

        // Load error type name
        mv.visitLdcInsn(name);

        // Load package
        mv.visitTypeInsn(NEW, PACKAGE_TYPE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(errorType.moduleId.org);
        mv.visitLdcInsn(errorType.moduleId.name);
        mv.visitLdcInsn(errorType.moduleId.modVersion);
        mv.visitMethodInsn(INVOKESPECIAL, PACKAGE_TYPE, "<init>",
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load reason and details type
        loadType(mv, errorType.reasonType);

        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE, "<init>",
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, PACKAGE_TYPE, BTYPE), false);
    }

    static String typeRefToClassName(PackageID typeRef, String className) {
        return getModuleLevelClassName(typeRef.orgName.value, typeRef.name.value, className);
    }


    // -------------------------------------------------------
    //              Type loading methods
    // -------------------------------------------------------

    static void loadExternalOrLocalType(MethodVisitor mv, BIRTypeDefinition|BIRTypeRef typeRef) {
        if (typeRef instanceof BIRTypeRef) {
            String fieldName = getTypeFieldName(typeRef.name.value);
            String externlTypeOwner = typeRefToClassName(typeRef, MODULE_INIT_CLASS_NAME);
            mv.visitFieldInsn(GETSTATIC, externlTypeOwner, fieldName, String.format("L%s;", BTYPE));
        } else {
            BType bType = typeRef.type;
            if (bType.tag == TypeTags.SERVICE) {
                loadType(mv, bType.oType);
            } else {
                loadType(mv, bType);
            }
        }
    }

    //# Generate code to load an instance of the given type
//# to the top of the stack.
//#
//# + bType - type to load
    static void loadType(MethodVisitor mv, @Nilable BType bType) {
        String typeFieldName = "";
        if (bType.tag == TypeTags.INT) {
            typeFieldName = "typeInt";
        } else if (bType.tag == TypeTags.FLOAT) {
            typeFieldName = "typeFloat";
        } else if (bType.tag == TypeTags.STRING) {
            typeFieldName = "typeString";
        } else if (bType.tag == TypeTags.DECIMAL) {
            typeFieldName = "typeDecimal";
        } else if (bType.tag == TypeTags.BOOLEAN) {
            typeFieldName = "typeBoolean";
        } else if (bType.tag == TypeTags.BYTE) {
            typeFieldName = "typeByte";
        } else if (bType.tag == TypeTags.NIL || bType == null) {
            typeFieldName = "typeNull";
        } else if (bType.tag == TypeTags.ANY) {
            typeFieldName = "typeAny";
        } else if (bType.tag == TypeTags.ANYDATA) {
            typeFieldName = "typeAnydata";
        } else if (bType.tag == TypeTags.JSON) {
            typeFieldName = "typeJSON";
        } else if (bType.tag == TypeTags.XML) {
            typeFieldName = "typeXML";
        } else if (bType.tag == TypeTags.TYPEDESC) {
            loadTypedescType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.SERVICE) {
            if (getTypeFieldName(bType.oType.name.value) != "$type$service") {
                loadUserDefinedType(mv, bType);
                return;
            }
            typeFieldName = "typeAnyService";
        } else if (bType.tag == TypeTags.HANDLE) {
            typeFieldName = "typeHandle";
        } else if (bType.tag == TypeTags.ARRAY) {
            loadArrayType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.MAP) {
            loadMapType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.TABLE) {
            loadTableType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.ERROR) {
            loadErrorType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.UNION) {
            loadUnionType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.RECORD) {
            loadUserDefinedType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.OBJECT) {
            loadUserDefinedType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.INVOKABLE) {
            loadInvokableType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.NONE) {
            mv.visitInsn(ACONST_NULL);
            return;
        } else if (bType.tag == TypeTags.TUPLE) {
            loadTupleType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.SELF) {
            loadType(mv, bType.bType);
            return;
        } else if (bType.tag == TypeTags.FINITE) {
            loadFiniteType(mv, bType);
            return;
        } else if (bType.tag == TypeTags.FUTURE) {
            loadFutureType(mv, bType);
            return;
        } else {
            // TODO Fix properly - rajith
            return;
        }


        mv.visitFieldInsn(GETSTATIC, BTYPES, typeFieldName, String.format("L%s;", BTYPE));
    }

    //# Generate code to load an instance of the given array type
//# to the top of the stack.
//#
//# + bType - array type to load
    static void loadArrayType(MethodVisitor mv, BIRBArrayType bType) {
        // Create an new array type
        mv.visitTypeInsn(NEW, ARRAY_TYPE);
        mv.visitInsn(DUP);

        // Load the element type
        loadType(mv, bType.eType);

        int arraySize = bType.size;
        mv.visitLdcInsn(arraySize);
        mv.visitInsn(L2I);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE, "<init>", String.format("(L%s;I)V", BTYPE), false);
    }

    //# Generate code to load an instance of the given typedesc type
//# to the top of the stack.
//#
//# + bType - typedesc type to load
    static void loadTypedescType(MethodVisitor mv, BTypeDesc bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, TYPEDESC_TYPE);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.typeConstraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_TYPE, "<init>", String.format("(L%s;)V", BTYPE), false);
    }

    //# Generate code to load an instance of the given map type
//# to the top of the stack.
//#
//# + bType - map type to load
    static void loadMapType(MethodVisitor mv, BIRBMapType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, MAP_TYPE);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, MAP_TYPE, "<init>", String.format("(L%s;)V", BTYPE), false);
    }

    //# Generate code to load an instance of the given table type
//# to the top of the stack.
//#
//# + bType - table type to load
    static void loadTableType(MethodVisitor mv, BIRBTableType bType) {
        // Create an new table type
        mv.visitTypeInsn(NEW, TABLE_TYPE);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.tConstraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE, "<init>", String.format("(L%s;)V", BTYPE), false);
    }

    //# Generate code to load an instance of the given error type
//# to the top of the stack.
//#
//# + errorType - error type to load
    static void loadErrorType(MethodVisitor mv, BIRBErrorType errorType) {
        // TODO: Builtin error type will be loaded from BTypes java class. Need to handle this properly.
        if (errorType.moduleId.org == BALLERINA && errorType.moduleId.name == BUILT_IN_PACKAGE_NAME) {
            mv.visitFieldInsn(GETSTATIC, BTYPES, TYPES_ERROR, String.format("L%s;", ERROR_TYPE));
            return;
        }
        String typeOwner = getPackageName(errorType.moduleId.org, errorType.moduleId.name) + MODULE_INIT_CLASS_NAME;
        String fieldName = getTypeFieldName(errorType.name.value);
        mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, String.format("L%s;", BTYPE));
    }

    //# Generate code to load an instance of the given union type
//# to the top of the stack.
//#
//# + bType - union type to load
    static void loadUnionType(MethodVisitor mv, BIRBUnionType bType) {
        // Create the union type
        mv.visitTypeInsn(NEW, UNION_TYPE);
        mv.visitInsn(DUP);

        // Create the members array
        @Nilable List<BType> memberTypes = bType.members;
        mv.visitLdcInsn(memberTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, BTYPE);
        int i = 0;
        for (BType memberType : memberTypes) {
            BType mType = getType(memberType);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitInsn(L2I);

            // Load the member type
            loadType(mv, mType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            i += 1;
        }

        // Load type flags
        mv.visitLdcInsn(bType.typeFlags);
        mv.visitInsn(L2I);

        // initialize the union type using the members array
        mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE, "<init>", String.format("([L%s;I)V", BTYPE), false);
        return;
    }

    //# Load a Tuple type instance to the top of the stack.
//#
//# + mv - method visitor
//# + bType - tuple type to be loaded
    static void loadTupleType(MethodVisitor mv, BIRBTupleType bType) {
        mv.visitTypeInsn(NEW, TUPLE_TYPE);
        mv.visitInsn(DUP);
        //new arraylist
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);

        @Nilable List<BType> tupleTypes = bType.tupleTypes;
        for (BType tupleType : tupleTypes) {
            BType tType = getType(tupleType);
            mv.visitInsn(DUP);
            loadType(mv, tType);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
        }

        @Nilable BType restType = bType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            loadType(mv, restType);
        }

        // Load type flags
        mv.visitLdcInsn(bType.typeFlags);
        mv.visitInsn(L2I);

        mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE, "<init>", String.format("(L%s;L%s;I)V", LIST, BTYPE), false);
        return;
    }

    //# Load a user defined type instance to the top of the stack.
//#
//# + mv - method visitor
    static void loadUserDefinedType(MethodVisitor mv, BObjectType|BRecordType|BIRBServiceType bType) {
        String fieldName = "";
        String typeOwner = "";

        if (bType.tag == TypeTags.OBJECT) {
            typeOwner = getPackageName(bType.moduleId.org, bType.moduleId.name) + MODULE_INIT_CLASS_NAME;
            fieldName = getTypeFieldName(bType.name.value);
        } else if (bType.tag == TypeTags.SERVICE) {
            typeOwner = getPackageName(bType.oType.moduleId.org, bType.oType.moduleId.name) + MODULE_INIT_CLASS_NAME;
            fieldName = getTypeFieldName(bType.oType.name.value);
        } else {
            typeOwner = getPackageName(bType.moduleId.org, bType.moduleId.name) + MODULE_INIT_CLASS_NAME;
            fieldName = getTypeFieldName(bType.name.value);
        }

        mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, String.format("L%s;", BTYPE));
    }

    //# Return the name of the field that holds the instance of a given type.
//#
//# + typeName - type name
//# + return - name of the field that holds the type instance
    static String getTypeFieldName(String typeName) {
        return String.format("$type$%s", typeName);
    }

    static void loadFutureType(MethodVisitor mv, BIRBFutureType bType) {
        mv.visitTypeInsn(NEW, FUTURE_TYPE);
        mv.visitInsn(DUP);

        loadType(mv, bType.returnType);
        mv.visitMethodInsn(INVOKESPECIAL, FUTURE_TYPE, "<init>", String.format("(L%s;)V", BTYPE), false);
    }

    //# Create and load an invokable type.
//#
//# + mv - method visitor
//# + bType - invokable type to be created
    static void loadInvokableType(MethodVisitor mv, BIRBInvokableType bType) {
        mv.visitTypeInsn(NEW, FUNCTION_TYPE);
        mv.visitInsn(DUP);

        // Create param types array
        mv.visitLdcInsn(bType.paramTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, BTYPE);
        int i = 0;
        for (T paramType : bType.paramTypes) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitInsn(L2I);

            // load param type
            loadType(mv, paramType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            i += 1;
        }

        @Nilable BType restType = bType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            loadType(mv, restType);
        }

        // load return type type
        loadType(mv, bType ?.retType);

        // initialize the function type using the param types array and the return type
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE, "<init>", String.format("([L%s;L%s;L%s;)V", BTYPE, BTYPE, BTYPE),
                false);
    }

    static String getTypeDesc(BType bType, boolean useBString /* = false */) {
        if (bType.tag == TypeTags.INT) {
            return "J";
        } else if (bType.tag == TypeTags.BYTE) {
            return "I";
        } else if (bType.tag == TypeTags.FLOAT) {
            return "D";
        } else if (bType.tag == TypeTags.STRING) {
            return String.format("L%s;", useBString ? I_STRING_VALUE : STRING_VALUE);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            return "Z";
        } else if (bType.tag == TypeTags.NIL) {
            return String.format("L%s;", OBJECT);
        } else if (bType.tag == TypeTags.ARRAY || bType.tag == TypeTags.TUPLE) {
            return String.format("L%s;", ARRAY_VALUE);
        } else if (bType.tag == TypeTags.ERROR) {
            return String.format("L%s;", ERROR_VALUE);
        } else if (bType.tag == TypeTags.FUTURE) {
            return String.format("L%s;", FUTURE_VALUE);
        } else if (bType.tag == TypeTags.MAP || bType.tag == TypeTags.RECORD) {
            return String.format("L%s;", MAP_VALUE);
        } else if (bType.tag == TypeTags.TYPEDESC) {
            return String.format("L%s;", TYPEDESC_VALUE);
        } else if (bType.tag == TypeTags.TABLE) {
            return String.format("L%s;", TABLE_VALUE);
        } else if (bType.tag == TypeTags.DECIMAL) {
            return String.format("L%s;", DECIMAL_VALUE);
        } else if (bType.tag == TypeTags.OBJECT || bType.tag == TypeTags.SERVICE) {
            return String.format("L%s;", OBJECT_VALUE);
        } else if (bType.tag == TypeTags.XML) {
            return String.format("L%s;", XML_VALUE);
        } else if (bType.tag == TypeTags.HANDLE) {
            return String.format("L%s;", HANDLE_VALUE);
        } else if (bType.tag == TypeTags.ANY ||
                bType.tag == TypeTags.ANYDATA ||
                bType.tag == TypeTags.UNION ||
                bType.tag == TypeTags.JSON ||
                bType.tag == TypeTags.FINITE) {
            return String.format("L%s;", OBJECT);
        } else if (bType.tag == TypeTags.INVOKABLE) {
            return String.format("L%s;", FUNCTION_POINTER);
        } else {
            BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
            throw err;
        }
    }

    static void loadFiniteType(MethodVisitor mv, BIRBFiniteType finiteType) {
        mv.visitTypeInsn(NEW, FINITE_TYPE);
        mv.visitInsn(DUP);

        // Load type name
        String name = finiteType.name.value;
        mv.visitLdcInsn(name);

        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);

        for (T valueTypePair : finiteType.values) {
            var value = valueTypePair.get(0);
            BType valueType = valueTypePair.get(1);
            mv.visitInsn(DUP);

            if (valueType.tag == TypeTags.NIL) {
                mv.visitInsn(ACONST_NULL);
            } else if (value instanceof BIRDecimal) {
                // do nothing
            } else {
                mv.visitLdcInsn(value);
            }

            if (valueType.tag == TypeTags.INT) {
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", String.format("(J)L%s;", LONG_VALUE), false);
            } else if (valueType.tag == TypeTags.BOOLEAN) {
                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", String.format("(Z)L%s;", BOOLEAN_VALUE), false);
            } else if (valueType.tag == TypeTags.FLOAT) {
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", String.format("(D)L%s;", DOUBLE_VALUE), false);
            } else if (valueType.tag == TypeTags.BYTE) {
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", String.format("(I)L%s;", INT_VALUE), false);
            } else if (value instanceof BIRDecimal) {
                mv.visitTypeInsn(NEW, DECIMAL_VALUE);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(value.value);
                mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", String.format("(L%s;)V", STRING_VALUE), false);
            } else {
                // if value is string or (), then do nothing
            }

            // Add the value to the set
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
        }

        // Load type flags
        mv.visitLdcInsn(finiteType.typeFlags);
        mv.visitInsn(L2I);

        // initialize the finite type using the value space
        mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE, "<init>", String.format("(L%s;L%s;I)V", STRING_VALUE, SET), false);
    }

    static boolean isServiceDefAvailable(@Nilable List<BIRTypeDefinition> typeDefs) {
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BIRTypeDefinition typeDef = getTypeDef(optionalTypeDef);
            BType bType = typeDef.type;
            if (bType.tag == TypeTags.SERVICE) {
                return true;
            }
        }
        return false;
    }
}