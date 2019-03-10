# Name of the class to which the types will be added as static fields.
string typeOwnerClass = "";

# # Create static fields to hold the user defined types.
#
# + cw - class writer
# + typeDefs - array of type definitions
public function generateUserDefinedTypeFields(jvm:ClassWriter cw, bir:TypeDef[] typeDefs) {
    string fieldName;
    // create the type
    foreach var typeDef in typeDefs {
        fieldName = getTypeFieldName(typeDef.name.value);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType || bType is bir:BObjectType || bType is bir:BErrorType) {
            jvm:FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, io:sprintf("L%s;", BTYPE));
            fv.visitEnd();
        } else {
            error err = error("Type definition is not yet supported for " + io:sprintf("%s", bType));
            panic err;
        }
    }
}

# Create instances of runtime types. This will create one instance from each 
# runtime type and populate the static fields.
#
# + mv - method visitor
# + typeDefs - array of type definitions
public function generateUserDefinedTypes(jvm:MethodVisitor mv, bir:TypeDef[] typeDefs) {
    string fieldName;

    // Create the type
    foreach var typeDef in typeDefs {
        fieldName = getTypeFieldName(typeDef.name.value);
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType) {
            createRecordType(mv, bType, typeDef.name.value);
        } else if (bType is bir:BObjectType) {
            createObjectType(mv, bType, typeDef.name.value);
        } else if (bType is bir:BErrorType) {
            createErrorType(mv, bType, typeDef.name.value);
        } else {
            error err = error("Type definition is not yet supported for " + io:sprintf("%s", bType));
            panic err;
        }

        mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
    }

    // Populate the field types
    foreach var typeDef in typeDefs {
        fieldName = getTypeFieldName(typeDef.name.value);
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, io:sprintf("L%s;", BTYPE));
        bir:BType bType = typeDef.typeValue;
        if (bType is bir:BRecordType) {
            mv.visitTypeInsn(CHECKCAST, RECORD_TYPE);
            mv.visitInsn(DUP);
            addRecordFields(mv, bType.fields);
            addRecordRestField(mv, bType.restFieldType);
        } else if (bType is bir:BObjectType) {
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            mv.visitInsn(DUP);
            addObjectFields(mv, bType.fields);
        }
    }
}

// -------------------------------------------------------
//              Record type generation methods
// -------------------------------------------------------

# Create a runtime type instance for the record.
#
# + mv - method visitor
# + recordType - record type
# + name - name of the record
function createRecordType(jvm:MethodVisitor mv, bir:BRecordType recordType, string name) {
    // Create the record type
    mv.visitTypeInsn(NEW, RECORD_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    mv.visitLdcInsn(name);

    // Load package path
    // TODO: get it from the type
    mv.visitLdcInsn("pkg");

    // Load flags
    mv.visitLdcInsn(0);
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
function addRecordFields(jvm:MethodVisitor mv, bir:BRecordField[] fields) {
    // Create the fields map
    mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

    foreach var field in fields {
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
    // TODO: get the flags
    mv.visitLdcInsn(0);
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
function createObjectType(jvm:MethodVisitor mv, bir:BObjectType objectType, string name) {
    // Create the record type
    mv.visitTypeInsn(NEW, OBJECT_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    mv.visitLdcInsn(name);

    // Load package path
    // TODO: get it from the type
    mv.visitLdcInsn("pkg");

    // Load flags
    mv.visitLdcInsn(0);
    mv.visitInsn(L2I);

    // initialize the object
    mv.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE, "<init>",
            io:sprintf("(L%s;L%s;I)V", STRING_VALUE, STRING_VALUE),
            false);
    return;
}

# Add the field type information of a object type. The object type is assumed
# to be at the top of the stack.
#
# + mv - method visitor
# + fields - object fields to be added
function addObjectFields(jvm:MethodVisitor mv, bir:BObjectField[] fields) {
    // Create the fields map
    mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "<init>", "()V", false);

    foreach var field in fields {
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
    int visibility = 0;
    if (field.visibility == "PACKAGE_PRIVATE") {
        visibility = 1;
    }
    mv.visitLdcInsn(visibility);
    mv.visitInsn(L2I);

    mv.visitMethodInsn(INVOKESPECIAL, BFIELD, "<init>",
            io:sprintf("(L%s;L%s;I)V", BTYPE, STRING_VALUE),
            false);
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
    // Create the record type
    mv.visitTypeInsn(NEW, ERROR_TYPE);
    mv.visitInsn(DUP);

    // Load type name
    mv.visitLdcInsn(name);

    // Load package path
    // TODO: get it from the type
    mv.visitLdcInsn("pkg");
    
    // Load reason and details type
    loadType(mv, errorType.reasonType);
    loadType(mv, errorType.detailType);

    // initialize the record type
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
function loadType(jvm:MethodVisitor mv, bir:BType bType) {
    string typeFieldName = "";
    if (bType is bir:BTypeInt) {
        typeFieldName = "typeInt";
    } else if (bType is bir:BTypeFloat) {
        typeFieldName = "typeFloat";
    } else if (bType is bir:BTypeString) {
        typeFieldName = "typeString";
    } else if (bType is bir:BTypeBoolean) {
        typeFieldName = "typeBoolean";
    } else if (bType is bir:BTypeByte) {
        typeFieldName = "typeByte";
    } else if (bType is bir:BTypeNil) {
        typeFieldName = "typeNull";
    } else if (bType is bir:BTypeAny) {
        typeFieldName = "typeAny";
    } else if (bType is bir:BTypeAnyData) {
        typeFieldName = "typeAnydata";
    } else if (bType is bir:BArrayType) {
        loadArrayType(mv, bType);
        return;
    } else if (bType is bir:BMapType) {
        loadMapType(mv, bType);
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
    } else if (bType is bir:BTypeNone) {
        mv.visitInsn(ACONST_NULL);
        return;
    } else {
        error err = error("JVM generation is not supported for type " + io:sprintf("%s", bType));
        panic err;
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

    // invoke the constructor
    mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
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

# Generate code to load an instance of the given union type
# to the top of the stack.
#
# + bType - union type to load
function loadUnionType(jvm:MethodVisitor mv, bir:BUnionType bType) {
    // Create the union type
    mv.visitTypeInsn(NEW, UNION_TYPE);
    mv.visitInsn(DUP);

    // Create the members array
    bir:BType[] memberTypes = bType.members;
    mv.visitLdcInsn(memberTypes.length());
    mv.visitInsn(L2I);
    mv.visitTypeInsn(ANEWARRAY, BTYPE);
    int i = 0;
    foreach var memberType in memberTypes {
        mv.visitInsn(DUP);
        mv.visitLdcInsn(i);
        mv.visitInsn(L2I);

        // Load the member type
        loadType(mv, memberType);

        // Add the member to the array
        mv.visitInsn(AASTORE);
        i += 1;
    }

    // initialize the union type using the members array
    mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE, "<init>", io:sprintf("([L%s;)V", BTYPE), false);
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
