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
        if (bType is bir:BRecordType || bType is bir:BObjectType || bType is bir:BErrorType) {
            jvm:FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, io:sprintf("L%s;", BTYPE));
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
# + typeDefs - array of type definitions
public function generateUserDefinedTypes(jvm:MethodVisitor mv, bir:TypeDef?[] typeDefs, BalToJVMIndexMap indexMap,
                                            string pkgName) {
    string fieldName;
    string typePkgName = ".";
    if (pkgName != "") {
        typePkgName = typePkgName;
    }

    // Create the type
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        fieldName = getTypeFieldName(typeDef.name.value);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType) {
            createRecordType(mv, bType, typeDef, typePkgName);
        } else if (bType is bir:BObjectType) {
            createObjectType(mv, bType, typeDef, typePkgName);
        } else if (bType is bir:BErrorType) {
            createErrorType(mv, bType, typeDef.name.value, typePkgName);
        } else {
            // do not generate anything for other types (e.g.: finite type, unions, etc.)
            continue;
        }

        mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
    }

    // Populate the field types
    foreach var optionalTypeDef in typeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        bir:BType bType = typeDef.typeValue;
        if !(bType is bir:BRecordType || bType is bir:BObjectType) {
            continue;
        }

        fieldName = getTypeFieldName(typeDef.name.value);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));

        if (bType is bir:BRecordType) {
            mv.visitTypeInsn(CHECKCAST, RECORD_TYPE);
            mv.visitInsn(DUP);
            addRecordFields(mv, bType.fields);
            addRecordRestField(mv, bType.restFieldType);
        } else if (bType is bir:BObjectType) {
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            mv.visitInsn(DUP);
            addObjectFields(mv, bType.fields);
            addObjectAtatchedFunctions(mv, bType.attachedFunctions, bType, indexMap);
        }
    }
}

// -------------------------------------------------------
//              Runtime value creation methods
// -------------------------------------------------------

public function generateValueCreatorMethods(jvm:ClassWriter cw, bir:TypeDef?[] typeDefs, string pkgName) {
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
        if (bType is bir:BObjectType) {
            objectTypeDefs[i] = typeDef;
            i += 1;
        }
    }

    generateRecordValueCreateMethod(cw, recordTypeDefs, pkgName);
    generateObjectValueCreateMethod(cw, objectTypeDefs, pkgName);
}

function generateRecordValueCreateMethod(jvm:ClassWriter cw, bir:TypeDef?[] recordTypeDefs, string pkgName) {
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
        string className = pkgName + cleanupTypeName(typeDef.name.value);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        mv.visitInsn(ARETURN);
        i += 1;
    }

    createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
    mv.visitMaxs(recordTypeDefs.length() + 10, recordTypeDefs.length() + 10);
    mv.visitEnd();
}

function generateObjectValueCreateMethod(jvm:ClassWriter cw, bir:TypeDef?[] objectTypeDefs, string pkgName) {
    jvm:MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "createObjectValue",
        io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT_VALUE), (), ());

    mv.visitCode();

    int fieldNameRegIndex = 1;
    jvm:Label defaultCaseLabel = new jvm:Label();

    // sort the fields before generating switch case
    NodeSorter sorter = new();
    sorter.sortByHash(objectTypeDefs);

    jvm:Label[] labels = createLabelsforSwitch(mv, fieldNameRegIndex, objectTypeDefs, defaultCaseLabel);
    jvm:Label[] targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, objectTypeDefs, labels,
            defaultCaseLabel);

    int i = 0;

    foreach var optionalTypeDef in objectTypeDefs {
        bir:TypeDef typeDef = getTypeDef(optionalTypeDef);
        string fieldName = getTypeFieldName(typeDef.name.value);
        jvm:Label targetLabel = targetLabels[i];
        mv.visitLabel(targetLabel);
        mv.visitVarInsn(ALOAD, 0);
        string className = pkgName + cleanupTypeName(typeDef.name.value);
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), false);
        mv.visitInsn(ARETURN);
        i += 1;
    }

    createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
    mv.visitMaxs(objectTypeDefs.length() + 10, objectTypeDefs.length() + 10);
    mv.visitEnd();
}


// -------------------------------------------------------
//              Record type generation methods
// -------------------------------------------------------

# Create a runtime type instance for the record.
#
# + mv - method visitor
# + recordType - record type
# + name - name of the record
function createRecordType(jvm:MethodVisitor mv, bir:BRecordType recordType, bir:TypeDef typeDef, string pkgName) {
    // Create the record type
    mv.visitTypeInsn(NEW, RECORD_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = typeDef.name.value;
    mv.visitLdcInsn(name);

    // Load package path
    // TODO: get it from the type
    mv.visitLdcInsn(pkgName);

    // Load flags
    int flag = getVisibilityFlag(typeDef.visibility);
    mv.visitLdcInsn(flag);
    mv.visitInsn(L2I);

    // Load 'sealed' flag
    mv.visitLdcInsn(recordType.sealed);

    // initialize the record type
    mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE, "<init>", 
            io:sprintf("(L%s;L%s;IZ)V", STRING_VALUE, STRING_VALUE), 
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
    int flag = getVisibilityFlag(field.visibility);
    mv.visitLdcInsn(flag);
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
# + name - name of the object
function createObjectType(jvm:MethodVisitor mv, bir:BObjectType objectType, bir:TypeDef typeDef, string pkgName) {
    // Create the object type
    mv.visitTypeInsn(NEW, OBJECT_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    string name = typeDef.name.value;
    mv.visitLdcInsn(name);

    // Load package path
    mv.visitLdcInsn(pkgName);

    // Load flags
    int flag = getVisibilityFlag(typeDef.visibility);
    mv.visitLdcInsn(flag);
    mv.visitInsn(L2I);

    // initialize the object
    mv.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE, "<init>",
            io:sprintf("(L%s;L%s;I)V", STRING_VALUE, STRING_VALUE),
            false);
    return;
}

function getVisibilityFlag(bir:Visibility visibility) returns int {
    if (visibility == bir:VISIBILITY_PUBLIC) {
        return BAL_PUBLIC;
    } else if (visibility == bir:VISIBILITY_OPTIONAL) {
        return BAL_OPTIONAL;
    } else {
        return 0;
    }
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
    // TODO: get the flags
    int visibility = 1;
    if (field.visibility == "PACKAGE_PRIVATE") {
        visibility = 0;
    }
    mv.visitLdcInsn(visibility);
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
function addObjectAtatchedFunctions(jvm:MethodVisitor mv, bir:BAttachedFunction?[] attachedFunctions,
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
            if (attachedFunc.name.value.contains("__init")) {
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
    // TODO: get the flags
    int visibility = 1;
    if (attachedFunc.visibility == "PACKAGE_PRIVATE") {
        visibility = 0;
    }
    mv.visitLdcInsn(visibility);
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
function createErrorType(jvm:MethodVisitor mv, bir:BErrorType errorType, string name, string pkgName) {
    // Create the error type
    mv.visitTypeInsn(NEW, ERROR_TYPE);
    mv.visitInsn(DUP);

    // Load error type name
    mv.visitLdcInsn(name);

    // Load package path
    mv.visitLdcInsn(pkgName);
    
    // Load reason and details type
    loadType(mv, errorType.reasonType);
    loadType(mv, errorType.detailType);

    // initialize the error type
    mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE, "<init>", io:sprintf("(L%s;L%s;L%s;L%s;)V", STRING_VALUE, 
            STRING_VALUE, BTYPE, BTYPE), false);
}

// -------------------------------------------------------
//              Type loading methods
// -------------------------------------------------------

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
        typeFieldName = "typeTypedesc";
    }  else if (bType is bir:BServiceType) {
        typeFieldName = "typeAnyService";
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
        loadUserDefinedType(mv, bType.name);
        return;
    } else if (bType is bir:BObjectType) {
        loadUserDefinedType(mv, bType.name);
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
        if (bType.bType is bir:BErrorType) {
            // Todo: Handle for recursive user defined error types.
            mv.visitFieldInsn(GETSTATIC, BTYPES, TYPES_ERROR, io:sprintf("L%s;", ERROR_TYPE));
        } else {
            loadType(mv, bType.bType);
        }
        return;
    } else if (bType is bir:BFiniteType) {
        loadFiniteType(mv, bType);
        return;
    } else {
        loadFutureType(mv, bType);
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
    // Create an new error type
    mv.visitTypeInsn(NEW, ERROR_TYPE);
    mv.visitInsn(DUP);

    // Load reason and details type
    loadType(mv, errorType.reasonType);
    loadType(mv, errorType.detailType);
    
    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE, "<init>", io:sprintf("(L%s;L%s;)V", BTYPE, BTYPE), false);
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

    // initialize the union type using the members array
    mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE, "<init>", io:sprintf("([L%s;)V", BTYPE), false);
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
    mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE, "<init>", io:sprintf("(L%s;)V",LIST), false);
    return;
}

# Load a user defined type instance to the top of the stack.
#
# + mv - method visitor
# + typeName - type to be loaded
function loadUserDefinedType(jvm:MethodVisitor mv, bir:Name typeName) {
    string fieldName = getTypeFieldName(typeName.value);
    mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
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
    loadType(mv, bType.retType);

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
    } else if (bType is bir:BMapType || bType is bir:BRecordType) {
        return io:sprintf("L%s;", MAP_VALUE);
    } else if (bType is bir:BTypeDesc) {
        return io:sprintf("L%s;", TYPEDESC_TYPE);
    } else if (bType is bir:BTableType) {
        return io:sprintf("L%s;", TABLE_VALUE);
    } else if (bType is bir:BStreamType) {
        return io:sprintf("L%s;", STREAM_VALUE);
    } else if (bType is bir:BTypeDecimal) {
        return io:sprintf("L%s;", DECIMAL_VALUE);
    } else if (bType is bir:BObjectType) {
        return io:sprintf("L%s;", OBJECT_VALUE);
    } else if (bType is bir:BTypeAny ||
               bType is bir:BTypeAnyData ||
               bType is bir:BUnionType ||
               bType is bir:BJSONType ||
               bType is bir:BXMLType) {
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

    foreach var value in finiteType.values {
        mv.visitInsn(DUP);
        mv.visitLdcInsn(value);

        if (value is int) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
        } else if (value is boolean) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", io:sprintf("(Z)L%s;", BOOLEAN_VALUE), false);
        } else if (value is float) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
        } else if (value is byte) {
            mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "valueOf", io:sprintf("(B)L%s;", BYTE_VALUE), false);
        } else {
            // if value is string or (), then do nothing
        }

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", io:sprintf("(L%s;)Z", OBJECT), true);
        mv.visitInsn(POP);
    }

    // initialize the finite type using the value space
    mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE, "<init>", io:sprintf("(L%s;L%s;)V", STRING_VALUE, SET), false);
}
