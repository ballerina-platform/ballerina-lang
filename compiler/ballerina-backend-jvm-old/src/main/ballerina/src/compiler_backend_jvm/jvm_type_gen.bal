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
import ballerina/io;
import ballerina/jvm;
import ballerina/stringutils;

# Name of the class to which the types will be added as static fields.
string typeOwnerClass = "";

# # Create static fields to hold the user defined types.
#
# + cw - class writer
# + typeDefs - array of type definitions
public function generateUserDefinedTypeFields(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs) {
    string fieldName;
    // create the type
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        fieldName = getTypeFieldName(typeDef.name.value);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType || bType is bir:BObjectType || bType is bir:BErrorType ||
                bType is bir:BServiceType) {
            jvm:FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, fieldName, io:sprintf("L%s;", BTYPE));
            fv.visitEnd();
        } else {
            // do not generate anything for other types (e.g.: finite type, unions, etc.)
        }
    }
}

# Create instances of runtime types. This will create one instance from each
# runtime type and populate the static fields.
#
# + mv - method visitor
public function generateUserDefinedTypes(jvm:MethodVisitor mv) {
    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, "$createTypes", "()V", false);
}

function generateCreateTypesMethod(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs) {
    createTypesInstance(cw, typeDefs);
    string[] populateTypeFuncNames = populateTypes(cw, typeDefs);

    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$createTypes", "()V", (), ());
    mv.visitCode();

    // Invoke create-type-instances method
    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, "$createTypeInstances", "()V", false);

    // Invoke the populate-type functions
    foreach var funcName in populateTypeFuncNames {
        mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, funcName, "()V", false);
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function createTypesInstance(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "$createTypeInstances", "()V", (), ());
    mv.visitCode();

    // Create the type
    string fieldName;
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        fieldName = getTypeFieldName(typeDef.name.value);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType) {
            createRecordType(mv, bType, typeDef);
        } else if (bType is bir:BObjectType) {
            createObjectType(mv, bType, typeDef);
        } else if (bType is bir:BServiceType) {
            createServiceType(mv, bType.oType, typeDef);
        } else if (bType is bir:BErrorType) {
            createErrorType(mv, bType, typeDef.name.value);
        } else {
            // do not generate anything for other types (e.g.: finite type, unions, etc.)
            continue;
        }

        mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
    }

    mv.visitInsn(RETURN);
    mv.visitMaxs(0,0);
    mv.visitEnd();
}

function populateTypes(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs) returns string[] {
    string[] funcNames = [];
    string fieldName;
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if !(bType is bir:BRecordType || bType is bir:BObjectType ||
                bType is bir:BServiceType || bType is bir:BErrorType) {
            continue;
        }

        fieldName = getTypeFieldName(typeDef.name.value);
        string methodName = io:sprintf("$populate%s", fieldName);
        funcNames[funcNames.length()] = methodName;

        jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", (), ());
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));

        BalToJVMIndexMap indexMap = new;
        if (bType is bir:BRecordType) {
            mv.visitTypeInsn(CHECKCAST, RECORD_TYPE);
            mv.visitInsn(DUP);
            addRecordFields(mv, bType.fields);
            addRecordRestField(mv, bType.restFieldType);
        } else if (bType is bir:BObjectType) {
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            mv.visitInsn(DUP);
            addObjectFields(mv, bType.fields);
            addObjectInitFunction(mv, bType.constructor, bType, indexMap);
            addObjectAttachedFunctions(mv, bType.attachedFunctions, bType, indexMap);
        } else if (bType is bir:BServiceType) {
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            mv.visitInsn(DUP);
            addObjectFields(mv, bType.oType.fields);
            addObjectAttachedFunctions(mv, bType.oType.attachedFunctions, bType.oType, indexMap);
        } else if (bType is bir:BErrorType) {
            // populate detail field
            mv.visitTypeInsn(CHECKCAST, ERROR_TYPE);
            mv.visitInsn(DUP);
            mv.visitInsn(DUP);
            loadType(mv, bType.detailType);
            mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE, SET_DETAIL_TYPE_METHOD, io:sprintf("(L%s;)V", BTYPE), false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();
    }

    return funcNames;
}


// -------------------------------------------------------
//              Runtime value creation methods
// -------------------------------------------------------

public function generateValueCreatorMethods(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs, bir:ModuleID moduleId) {
    bir:TypeDef?[] recordTypeDefs = [];
    bir:TypeDef?[] objectTypeDefs = [];

    int i = 0;
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType) {
            recordTypeDefs[i] = typeDef;
            i += 1;
        }
    }

    i = 0;
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BObjectType && !bType.isAbstract) {
            objectTypeDefs[i] = typeDef;
            i += 1;
        }
    }

    generateRecordValueCreateMethod(cw, recordTypeDefs, moduleId);
    generateObjectValueCreateMethod(cw, objectTypeDefs, moduleId);
}

function generateRecordValueCreateMethod(jvm:ClassWriter cw, bir:TypeDef?[] recordTypeDefs, bir:ModuleID moduleId) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "createRecordValue",
        io:sprintf("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
        io:sprintf("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), ());

    mv.visitCode();

    int fieldNameRegIndex = 1;
    jvm:Label defaultCaseLabel = new jvm:Label();

    // sort the fields before generating switch case
    NodeSorter sorter = new();
    sorter.sortByHash(recordTypeDefs);

    jvm:Label[] labels = createLabelsforSwitch(mv, fieldNameRegIndex, recordTypeDefs, defaultCaseLabel);
    jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, recordTypeDefs, labels,
            defaultCaseLabel);

    int i = 0;

    foreach var optionalTypeDef in recordTypeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        string fieldName = getTypeFieldName(typeDef.name.value);
        jvm:Label targetLabel = targetLabels[i];
        mv.visitLabel(targetLabel);
        mv.visitVarInsn(ALOAD, 0);
        string className = getTypeValueClassName(moduleId, typeDef.name.value);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", BTYPE), false);

        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, STRAND);
        mv.visitInsn(DUP);
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKESPECIAL, STRAND, "<init>", io:sprintf("(L%s;)V", SCHEDULER) , false);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESTATIC, className, "$init", io:sprintf("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
        
        mv.visitInsn(ARETURN);
        i += 1;
    }

    createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
    mv.visitMaxs(recordTypeDefs.length() + 10, recordTypeDefs.length() + 10);
    mv.visitEnd();
}

function generateObjectValueCreateMethod(jvm:ClassWriter cw, bir:TypeDef?[] objectTypeDefs, bir:ModuleID moduleId) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "createObjectValue",
        io:sprintf("(L%s;L%s;L%s;L%s;[L%s;)L%s;", STRING_VALUE, SCHEDULER, STRAND, MAP, OBJECT, OBJECT_VALUE), (), ());

    BalToJVMIndexMap indexMap = new;

    bir:VariableDcl selfVar = { typeValue: "any",
                                    name: { value: "self" },
                                    kind: "ARG" };
    bir:VariableDcl var1 = { typeValue: "string",
                                    name: { value: "var1" },
                                    kind: "ARG" };
    bir:VariableDcl scheduler = { typeValue: "any",
                                    name: { value: "scheduler" },
                                    kind: "ARG" };
    bir:VariableDcl parent = { typeValue: "any",
                                    name: { value: "parent" },
                                    kind: "ARG" };
    bir:VariableDcl properties = { typeValue: "any",
                                    name: { value: "properties" },
                                    kind: "ARG" };
    bir:VariableDcl args = { typeValue: "any",
                                    name: { value: "args" },
                                    kind: "ARG" };
    _ = indexMap.getIndex(selfVar);
    int var1Index = indexMap.getIndex(var1);
    int schedulerIndex = indexMap.getIndex(scheduler);
    int parentIndex = indexMap.getIndex(parent);
    int propertiesIndex = indexMap.getIndex(properties);
    int argsIndex = indexMap.getIndex(args);

    mv.visitCode();

    jvm:Label defaultCaseLabel = new jvm:Label();

    // sort the fields before generating switch case
    NodeSorter sorter = new();
    sorter.sortByHash(objectTypeDefs);

    jvm:Label[] labels = createLabelsforSwitch(mv, var1Index, objectTypeDefs, defaultCaseLabel);
    jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, var1Index, objectTypeDefs, labels,
            defaultCaseLabel);

    int i = 0;

    foreach var optionalTypeDef in objectTypeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        string fieldName = getTypeFieldName(typeDef.name.value);
        jvm:Label targetLabel = targetLabels[i];
        mv.visitLabel(targetLabel);
        mv.visitVarInsn(ALOAD, 0);
        string className = getTypeValueClassName(moduleId, typeDef.name.value);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), false);

        bir:VariableDcl tempVar = { typeValue: typeDef.typeValue,
                                    name: { value: "tempVar" },
                                    kind: "LOCAL" };
        int tempVarIndex = indexMap.getIndex(tempVar);
        mv.visitVarInsn(ASTORE, tempVarIndex);

        mv.visitTypeInsn(NEW, STRAND);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, schedulerIndex);
        mv.visitVarInsn(ALOAD, parentIndex);
        mv.visitVarInsn(ALOAD, propertiesIndex);
        mv.visitMethodInsn(INVOKESPECIAL, STRAND, "<init>", io:sprintf("(L%s;L%s;L%s;)V", SCHEDULER, STRAND, MAP), false);
        bir:VariableDcl strandVar = { typeValue: "any",
                                    name: { value: "strandVar" },
                                    kind: "LOCAL" };
        int strandVarIndex = indexMap.getIndex(strandVar);
        mv.visitVarInsn(ASTORE, strandVarIndex);

        mv.visitVarInsn(ALOAD, tempVarIndex);
        mv.visitVarInsn(ALOAD, strandVarIndex);

        mv.visitLdcInsn("__init");
        mv.visitVarInsn(ALOAD, argsIndex);

        string methodDesc = io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
        mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

        bir:VariableDcl tempResult = { typeValue: "any",
                                    name: { value: "tempResult" },
                                    kind: "LOCAL" };
        int tempResultIndex = indexMap.getIndex(tempResult);
        mv.visitVarInsn(ASTORE, tempResultIndex);
        mv.visitVarInsn(ALOAD, tempResultIndex);
        mv.visitTypeInsn(INSTANCEOF, ERROR_VALUE);
        jvm:Label noErrorLabel = new jvm:Label();
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
    mv.visitMaxs(objectTypeDefs.length() + 100, objectTypeDefs.length() + 100);
    mv.visitEnd();
}


// -------------------------------------------------------
//              Record type generation methods
// -------------------------------------------------------

# Create a runtime type instance for the record.
#
# + mv - method visitor
# + recordType - record type
function createRecordType(jvm:MethodVisitor mv, bir:BRecordType recordType, bir:TypeDef typeDef) {
    // Create the record type
    mv.visitTypeInsn(NEW, RECORD_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = typeDef.name.value;
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
            io:sprintf("(L%s;L%s;IZI)V", STRING_VALUE, PACKAGE_TYPE),
            false);

    return;
}

# Add the field type information of a record type. The record type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + fields - record fields to be added
function addRecordFields(jvm:MethodVisitor mv, bir:BRecordField?[] fields) {
    // Create the fields map
    mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

    foreach var optionalField in fields {
        bir:BRecordField field = getRecordField(optionalField);
        mv.visitInsn(DUP);

        // Load field name
        mv.visitLdcInsn(field.name.value);

        // create and load field type
        createRecordField(mv, field);

        // Add the field to the map
        mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
            io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
            true);

        // emit a pop, since we are not using the return value from the map.put()
        mv.visitInsn(POP);
    }

    // Set the fields of the record
    mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE, "setFields", io:sprintf("(L%s;)V", MAP), false);
}

# Create a field information for records.
#
# + mv - method visitor
# + field - field Parameter Description
function createRecordField(jvm:MethodVisitor mv, bir:BRecordField field) {
    mv.visitTypeInsn(NEW, BFIELD);
    mv.visitInsn(DUP);

    // Load the field type
    loadType(mv, field.typeValue);

    // Load field name
    mv.visitLdcInsn(field.name.value);

    // Load flags
    mv.visitLdcInsn(field.flags);
    mv.visitInsn(L2I);

    mv.visitMethodInsn(INVOKESPECIAL, BFIELD, "<init>",
            io:sprintf("(L%s;L%s;I)V", BTYPE, STRING_VALUE),
            false);
}

# Add the rest field to a record type. The record type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + restFieldType - type of the rest field
function addRecordRestField(jvm:MethodVisitor mv, bir:BType restFieldType) {
    // Load the rest field type
    loadType(mv, restFieldType);
    mv.visitFieldInsn(PUTFIELD, RECORD_TYPE, "restFieldType", io:sprintf("L%s;", BTYPE));
}

// -------------------------------------------------------
//              Object type generation methods
// -------------------------------------------------------

# Create a runtime type instance for the object.
#
# + mv - method visitor
# + objectType - object type
function createObjectType(jvm:MethodVisitor mv, bir:BObjectType objectType, bir:TypeDef typeDef) {
    // Create the object type
    mv.visitTypeInsn(NEW, OBJECT_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = typeDef.name.value;
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
        io:sprintf("(L%s;L%s;I)V", STRING_VALUE, PACKAGE_TYPE),
        false);
}

# Create a runtime type instance for the service.
#
# + mv - method visitor
# + objectType - object type
# + typeDef - type definition of the service
function createServiceType(jvm:MethodVisitor mv, bir:BObjectType objectType, bir:TypeDef typeDef) {
    // Create the object type
    mv.visitTypeInsn(NEW, SERVICE_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = typeDef.name.value;
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
    mv.visitMethodInsn(INVOKESPECIAL, SERVICE_TYPE, "<init>", io:sprintf("(L%s;L%s;I)V", STRING_VALUE, PACKAGE_TYPE),
                       false);
}

function duplicateServiceTypeWithAnnots(jvm:MethodVisitor mv, bir:BObjectType objectType, bir:TypeDef typeDef,
                                        string pkgName, int strandIndex) {
    createServiceType(mv, objectType, typeDef);
    mv.visitInsn(DUP);

    string pkgClassName = pkgName == "." || pkgName == "" ? MODULE_INIT_CLASS_NAME :
                            lookupGlobalVarClassName(pkgName + ANNOTATION_MAP_NAME);
    mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, io:sprintf("L%s;", MAP_VALUE));

    mv.visitVarInsn(ALOAD, strandIndex);

    loadExternalOrLocalType(mv, typeDef);
    mv.visitTypeInsn(CHECKCAST, SERVICE_TYPE);

    bir:BAttachedFunction?[] attachedFunctions = objectType.attachedFunctions;
    mv.visitLdcInsn(attachedFunctions.length());
    mv.visitInsn(L2I);
    mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION);
    int i = 0;
    foreach var attachedFunc in attachedFunctions {
        if (attachedFunc is bir:BAttachedFunction) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitInsn(L2I);

            createObjectAttachedFunction(mv, attachedFunc, objectType);
            mv.visitInsn(AASTORE);
            i += 1;
        }
    }
    mv.visitMethodInsn(INVOKEVIRTUAL, SERVICE_TYPE, "setAttachedFuncsAndProcessAnnots",
                       io:sprintf("(L%s;L%s;L%s;[L%s;)V", MAP_VALUE, STRAND, SERVICE_TYPE, ATTACHED_FUNCTION), false);
}

# Add the field type information to an object type. The object type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + fields - object fields to be added
function addObjectFields(jvm:MethodVisitor mv, bir:BObjectField?[] fields) {
    // Create the fields map
    mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

    foreach var optionalField in fields {
        bir:BObjectField field = getObjectField(optionalField);
        mv.visitInsn(DUP);

        // Load field name
        mv.visitLdcInsn(field.name.value);

        // create and load field type
        createObjectField(mv, field);

        // Add the field to the map
        mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
            io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
            true);

        // emit a pop, since we are not using the return value from the map.put()
        mv.visitInsn(POP);
    }

    // Set the fields of the object
    mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, "setFields", io:sprintf("(L%s;)V", MAP), false);
}

# Create a field information for objects.
#
# + mv - method visitor
# + field - object field
function createObjectField(jvm:MethodVisitor mv, bir:BObjectField field) {
    mv.visitTypeInsn(NEW, BFIELD);
    mv.visitInsn(DUP);

    // Load the field type
    loadType(mv, field.typeValue);

    // Load field name
    mv.visitLdcInsn(field.name.value);

    // Load flags
    mv.visitLdcInsn(field.flags);
    mv.visitInsn(L2I);

    mv.visitMethodInsn(INVOKESPECIAL, BFIELD, "<init>",
            io:sprintf("(L%s;L%s;I)V", BTYPE, STRING_VALUE),
            false);
}

# Add the attached function information to an object type. The object type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + attachedFunctions - attached functions to be added
function addObjectAttachedFunctions(jvm:MethodVisitor mv, bir:BAttachedFunction?[] attachedFunctions,
                                    bir:BObjectType objType, BalToJVMIndexMap indexMap) {
    // Create the attached function array
    mv.visitLdcInsn(attachedFunctions.length());
    mv.visitInsn(L2I);
    mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION);
    int i = 0;
    foreach var attachedFunc in attachedFunctions {
        if (attachedFunc is bir:BAttachedFunction) {
            // create and load attached function
            createObjectAttachedFunction(mv, attachedFunc, objType);
            bir:VariableDcl attachedFuncVar = { typeValue: "any",
                                                name: { value: objType.name.value + attachedFunc.name.value},
                                                kind: "LOCAL" };
            int attachedFunctionVarIndex = indexMap.getIndex(attachedFuncVar);
            mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);

            // if this initializer function, set it to the object type
            if (stringutils:contains(attachedFunc.name.value, "__init")) {
                mv.visitInsn(DUP2);
                mv.visitInsn(POP);
                mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
                mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, "setInitializer",
                                    io:sprintf("(L%s;)V", ATTACHED_FUNCTION), false);
            }

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
            io:sprintf("([L%s;)V", ATTACHED_FUNCTION), false);
}

# Add the init function information to an object type. The object type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + initFunction - init functions to be added
function addObjectInitFunction(jvm:MethodVisitor mv, bir:BAttachedFunction? initFunction,
                                    bir:BObjectType objType, BalToJVMIndexMap indexMap) {
    if (initFunction is bir:BAttachedFunction && stringutils:contains(initFunction.name.value, "__init")) {
        mv.visitInsn(DUP);
        createObjectAttachedFunction(mv, initFunction, objType);
        bir:VariableDcl attachedFuncVar = { typeValue: "any",
            name: { value: objType.name.value + initFunction.name.value},
            kind: "LOCAL" };
        int attachedFunctionVarIndex = indexMap.getIndex(attachedFuncVar);
        mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
        mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
        mv.visitInsn(DUP);
        mv.visitInsn(POP);
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE, "setInitializer",
            io:sprintf("(L%s;)V", ATTACHED_FUNCTION), false);
    }
}

# Create a attached function information for objects.
#
# + mv - method visitor
# + attachedFunc - object attached function
function createObjectAttachedFunction(jvm:MethodVisitor mv, bir:BAttachedFunction attachedFunc,
                                      bir:BObjectType objType) {
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
                        io:sprintf("(L%s;L%s;L%s;I)V", STRING_VALUE, OBJECT_TYPE, FUNCTION_TYPE), false);
}

// -------------------------------------------------------
//              Error type generation methods
// -------------------------------------------------------

# Create a runtime type instance for the error.
#
# + mv - method visitor
# + errorType - error type
# + name - name of the error
function createErrorType(jvm:MethodVisitor mv, bir:BErrorType errorType, string name) {
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
                        io:sprintf("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

    // Load reason and details type
    loadType(mv, errorType.reasonType);

    // initialize the error type
    mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE, "<init>",
                            io:sprintf("(L%s;L%s;L%s;)V", STRING_VALUE, PACKAGE_TYPE, BTYPE), false);
}

function typeRefToClassName(bir:TypeRef typeRef, string className) returns string {
    return getModuleLevelClassName(typeRef.externalPkg.org, typeRef.externalPkg.name, className);
}


// -------------------------------------------------------
//              Type loading methods
// -------------------------------------------------------

function loadExternalOrLocalType(jvm:MethodVisitor mv,  bir:TypeDef|bir:TypeRef typeRef) {
    if (typeRef is bir:TypeRef) {
        string fieldName = getTypeFieldName(typeRef.name.value);
        string externlTypeOwner =  typeRefToClassName(typeRef, MODULE_INIT_CLASS_NAME);
        mv.visitFieldInsn(GETSTATIC, externlTypeOwner, fieldName, io:sprintf("L%s;", BTYPE));
    } else {
        bir:BType bType = typeRef.typeValue;
        if (bType is bir:BServiceType) {
            loadType(mv, bType.oType);
        } else {
            loadType(mv, bType);
        }
    }
}

# Generate code to load an instance of the given type
# to the top of the stack.
#
# + bType - type to load
function loadType(jvm:MethodVisitor mv, bir:BType? bType) {
    string typeFieldName = "";
    if (bType is bir:BTypeInt) {
        typeFieldName = "typeInt";
    } else if (bType is bir:BTypeFloat) {
        typeFieldName = "typeFloat";
    } else if (bType is bir:BTypeString) {
        typeFieldName = "typeString";
    } else if (bType is bir:BTypeDecimal) {
        typeFieldName = "typeDecimal";
    } else if (bType is bir:BTypeBoolean) {
        typeFieldName = "typeBoolean";
    } else if (bType is bir:BTypeByte) {
        typeFieldName = "typeByte";
    } else if (bType is bir:BTypeNil || bType is ()) {
        typeFieldName = "typeNull";
    } else if (bType is bir:BTypeAny) {
        typeFieldName = "typeAny";
    } else if (bType is bir:BTypeAnyData) {
        typeFieldName = "typeAnydata";
    } else if (bType is bir:BJSONType) {
        typeFieldName = "typeJSON";
    } else if (bType is bir:BXMLType) {
        typeFieldName = "typeXML";
    } else if (bType is bir:BTypeDesc) {
        loadTypedescType(mv, bType);
        return;
    }  else if (bType is bir:BServiceType) {
        if (getTypeFieldName(bType.oType.name.value) != "$type$service") {
            loadUserDefinedType(mv, bType);
            return;
        }
        typeFieldName = "typeAnyService";
    } else if (bType is bir:BTypeHandle) {
        typeFieldName = "typeHandle";
    } else if (bType is bir:BArrayType) {
        loadArrayType(mv, bType);
        return;
    } else if (bType is bir:BMapType) {
        loadMapType(mv, bType);
        return;
    } else if (bType is bir:BTableType) {
        loadTableType(mv, bType);
        return;
    } else if (bType is bir:BStreamType) {
        loadStreamType(mv, bType);
        return;
    } else if (bType is bir:BErrorType) {
        loadErrorType(mv, bType);
        return;
    } else if (bType is bir:BUnionType) {
        loadUnionType(mv, bType);
        return;
    } else if (bType is bir:BRecordType) {
        loadUserDefinedType(mv, bType);
        return;
    } else if (bType is bir:BObjectType) {
        loadUserDefinedType(mv, bType);
        return;
    } else if (bType is bir:BInvokableType) {
        loadInvokableType(mv, bType);
        return;
    } else if (bType is bir:BTypeNone) {
        mv.visitInsn(ACONST_NULL);
        return;
    } else if (bType is bir:BTupleType) {
        loadTupleType(mv, bType);
        return;
    } else if (bType is bir:Self) {
        loadType(mv, bType.bType);
        return;
    } else if (bType is bir:BFiniteType) {
        loadFiniteType(mv, bType);
        return;
    } else if (bType is bir:BFutureType) {
        loadFutureType(mv, bType);
        return;
    } else {
    	// TODO Fix properly - rajith
        return;
    }


    mv.visitFieldInsn(GETSTATIC, BTYPES, typeFieldName, io:sprintf("L%s;", BTYPE));
}

# Generate code to load an instance of the given array type
# to the top of the stack.
#
# + bType - array type to load
function loadArrayType(jvm:MethodVisitor mv, bir:BArrayType bType) {
    // Create an new array type
    mv.visitTypeInsn(NEW, ARRAY_TYPE);
    mv.visitInsn(DUP);

    // Load the element type
    loadType(mv, bType.eType);

    int arraySize = bType.size;
    mv.visitLdcInsn(arraySize);
    mv.visitInsn(L2I);

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE, "<init>", io:sprintf("(L%s;I)V", BTYPE), false);
}

# Generate code to load an instance of the given typedesc type
# to the top of the stack.
#
# + bType - typedesc type to load
function loadTypedescType(jvm:MethodVisitor mv, bir:BTypeDesc bType) {
    // Create an new map type
    mv.visitTypeInsn(NEW, TYPEDESC_TYPE);
    mv.visitInsn(DUP);

    // Load the constraint type
    loadType(mv, bType.typeConstraint);

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
}

# Generate code to load an instance of the given map type
# to the top of the stack.
#
# + bType - map type to load
function loadMapType(jvm:MethodVisitor mv, bir:BMapType bType) {
    // Create an new map type
    mv.visitTypeInsn(NEW, MAP_TYPE);
    mv.visitInsn(DUP);

    // Load the constraint type
    loadType(mv, bType.constraint);

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, MAP_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
}

# Generate code to load an instance of the given table type
# to the top of the stack.
#
# + bType - table type to load
function loadTableType(jvm:MethodVisitor mv, bir:BTableType bType) {
    // Create an new table type
    mv.visitTypeInsn(NEW, TABLE_TYPE);
    mv.visitInsn(DUP);

    // Load the constraint type
    loadType(mv, bType.tConstraint);

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
}

# Generate code to load an instance of the given stream type
# to the top of the stack.
#
# + bType - stream type to load
function loadStreamType(jvm:MethodVisitor mv, bir:BStreamType bType) {
    // Create an new stream type
    mv.visitTypeInsn(NEW, STREAM_TYPE);
    mv.visitInsn(DUP);

    // Load the constraint type
    loadType(mv, bType.sConstraint);

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, STREAM_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
}

# Generate code to load an instance of the given error type
# to the top of the stack.
#
# + errorType - error type to load
function loadErrorType(jvm:MethodVisitor mv, bir:BErrorType errorType) {
    // TODO: Builtin error type will be loaded from BTypes java class. Need to handle this properly.
    if (errorType.moduleId.org == BALLERINA && errorType.moduleId.name ==  BUILT_IN_PACKAGE_NAME) {
        mv.visitFieldInsn(GETSTATIC, BTYPES, TYPES_ERROR, io:sprintf("L%s;", ERROR_TYPE));
        return;
    }
    string typeOwner = getPackageName(errorType.moduleId.org, errorType.moduleId.name) + MODULE_INIT_CLASS_NAME;
    string fieldName = getTypeFieldName(errorType.name.value);
    mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, io:sprintf("L%s;", BTYPE));
}

# Generate code to load an instance of the given union type
# to the top of the stack.
#
# + bType - union type to load
function loadUnionType(jvm:MethodVisitor mv, bir:BUnionType bType) {
    // Create the union type
    mv.visitTypeInsn(NEW, UNION_TYPE);
    mv.visitInsn(DUP);

    // Create the members array
    bir:BType?[] memberTypes = bType.members;
    mv.visitLdcInsn(memberTypes.length());
    mv.visitInsn(L2I);
    mv.visitTypeInsn(ANEWARRAY, BTYPE);
    int i = 0;
    foreach var memberType in memberTypes {
        bir:BType mType = getType(memberType);
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
    mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE, "<init>", io:sprintf("([L%s;I)V", BTYPE), false);
    return;
}

# Load a Tuple type instance to the top of the stack.
#
# + mv - method visitor
# + bType - tuple type to be loaded
function loadTupleType(jvm:MethodVisitor mv, bir:BTupleType bType) {
    mv.visitTypeInsn(NEW, TUPLE_TYPE);
    mv.visitInsn(DUP);
    //new arraylist
    mv.visitTypeInsn(NEW, ARRAY_LIST);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);

    bir:BType?[] tupleTypes = bType.tupleTypes;
    foreach var tupleType in tupleTypes {
        bir:BType tType = getType(tupleType);
        mv.visitInsn(DUP);
        loadType(mv, tType);
        mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", io:sprintf("(L%s;)Z", OBJECT), true);
        mv.visitInsn(POP);
    }

    bir:BType? restType = bType.restType;
    if (restType is ()) {
        mv.visitInsn(ACONST_NULL);
    } else {
        loadType(mv, restType);
    }

    // Load type flags
    mv.visitLdcInsn(bType.typeFlags);
    mv.visitInsn(L2I);

    mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE, "<init>", io:sprintf("(L%s;L%s;I)V", LIST, BTYPE), false);
    return;
}

# Load a user defined type instance to the top of the stack.
#
# + mv - method visitor
function loadUserDefinedType(jvm:MethodVisitor mv, bir:BObjectType|bir:BRecordType|bir:BServiceType bType) {
    string fieldName = "";
    string typeOwner = "";

    if (bType is bir:BObjectType) {
        typeOwner = getPackageName(bType.moduleId.org, bType.moduleId.name) + MODULE_INIT_CLASS_NAME;
        fieldName = getTypeFieldName(bType.name.value);
    } else if (bType is bir:BServiceType) {
        typeOwner = getPackageName(bType.oType.moduleId.org, bType.oType.moduleId.name) + MODULE_INIT_CLASS_NAME;
        fieldName = getTypeFieldName(bType.oType.name.value);
    } else {
        typeOwner = getPackageName(bType.moduleId.org, bType.moduleId.name) + MODULE_INIT_CLASS_NAME;
        fieldName = getTypeFieldName(bType.name.value);
    }

    mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, io:sprintf("L%s;", BTYPE));
}

# Return the name of the field that holds the instance of a given type.
#
# + typeName - type name
# + return - name of the field that holds the type instance
function getTypeFieldName(string typeName) returns string {
    return io:sprintf("$type$%s", typeName);
}

function loadFutureType(jvm:MethodVisitor mv, bir:BFutureType bType) {
    mv.visitTypeInsn(NEW, FUTURE_TYPE);
    mv.visitInsn(DUP);

    loadType(mv, bType.returnType);
    mv.visitMethodInsn(INVOKESPECIAL, FUTURE_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
}

# Create and load an invokable type.
#
# + mv - method visitor
# + bType - invokable type to be created
function loadInvokableType(jvm:MethodVisitor mv, bir:BInvokableType bType) {
    mv.visitTypeInsn(NEW, FUNCTION_TYPE);
    mv.visitInsn(DUP);

    // Create param types array
    mv.visitLdcInsn(bType.paramTypes.length());
    mv.visitInsn(L2I);
    mv.visitTypeInsn(ANEWARRAY, BTYPE);
    int i = 0;
    foreach var paramType in bType.paramTypes {
        mv.visitInsn(DUP);
        mv.visitLdcInsn(i);
        mv.visitInsn(L2I);

        // load param type
        loadType(mv, paramType);

        // Add the member to the array
        mv.visitInsn(AASTORE);
        i += 1;
    }

    // load return type type
    loadType(mv, bType?.retType);

    // initialize the function type using the param types array and the return type
    mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE, "<init>", io:sprintf("([L%s;L%s;)V", BTYPE, BTYPE), false);
}

function getTypeDesc(bir:BType bType) returns string {
    if (bType is bir:BTypeInt) {
        return "J";
    } else if (bType is bir:BTypeByte) {
        return "I";
    } else if (bType is bir:BTypeFloat) {
        return "D";
    } else if (bType is bir:BTypeString) {
        return io:sprintf("L%s;", STRING_VALUE);
    } else if (bType is bir:BTypeBoolean) {
        return "Z";
    } else if (bType is bir:BTypeNil) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BArrayType || bType is bir:BTupleType) {
        return io:sprintf("L%s;", ARRAY_VALUE );
    } else if (bType is bir:BErrorType) {
        return io:sprintf("L%s;", ERROR_VALUE);
    } else if (bType is bir:BFutureType) {
        return io:sprintf("L%s;", FUTURE_VALUE);
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BTypeDesc) {
        return io:sprintf("L%s;", TYPEDESC_VALUE);
    } else if (bType is bir:BTableType) {
        return io:sprintf("L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        return io:sprintf("L%s;", STREAM_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        return io:sprintf("L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BObjectType || bType is bir:BServiceType) {
        return io:sprintf("L%s;", OBJECT_VALUE);
    }  else if (bType is bir:BXMLType) {
        return io:sprintf("L%s;", XML_VALUE);
    }  else if (bType is bir:BTypeHandle) {
        return io:sprintf("L%s;", HANDLE_VALUE);
    } else if (bType is bir:BTypeAny ||
               bType is bir:BTypeAnyData ||
               bType is bir:BUnionType ||
               bType is bir:BJSONType ||
               bType is bir:BFiniteType) {
        return io:sprintf("L%s;", OBJECT);
    } else if (bType is bir:BInvokableType) {
        return io:sprintf("L%s;", FUNCTION_POINTER);
    } else {
        error err = error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
    }
}

function loadFiniteType(jvm:MethodVisitor mv, bir:BFiniteType finiteType) {
    mv.visitTypeInsn(NEW, FINITE_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = finiteType.name.value;
    mv.visitLdcInsn(name);

    mv.visitTypeInsn(NEW, LINKED_HASH_SET);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, "<init>", "()V", false);

    foreach var valueTypePair in finiteType.values {
        var value = valueTypePair[0];
        bir:BType valueType = valueTypePair[1];
        mv.visitInsn(DUP);

        if (valueType is bir:BTypeNil) {
            mv.visitInsn(ACONST_NULL);
        } else if (value is bir:Decimal) {
            // do nothing
        } else {
            mv.visitLdcInsn(value);
        }

        if (valueType is bir:BTypeInt) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
        } else if (valueType is bir:BTypeBoolean) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", io:sprintf("(Z)L%s;", BOOLEAN_VALUE), false);
        } else if (valueType is bir:BTypeFloat) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
        } else if (valueType is bir:BTypeByte) {
            mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", io:sprintf("(I)L%s;", INT_VALUE), false);
        } else if (value is bir:Decimal) {
            mv.visitTypeInsn(NEW, DECIMAL_VALUE);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(value.value);
            mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", io:sprintf("(L%s;)V", STRING_VALUE), false);
        } else {
            // if value is string or (), then do nothing
        }

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", io:sprintf("(L%s;)Z", OBJECT), true);
        mv.visitInsn(POP);
    }

    // Load type flags
    mv.visitLdcInsn(finiteType.typeFlags);
    mv.visitInsn(L2I);

    // initialize the finite type using the value space
    mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE, "<init>", io:sprintf("(L%s;L%s;I)V", STRING_VALUE, SET), false);
}

function isServiceDefAvailable(bir:TypeDef?[] typeDefs) returns boolean {
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BServiceType) {
            return true;
        }
    }
    return false;
}
