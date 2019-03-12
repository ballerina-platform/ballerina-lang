function generateCast(jvm:MethodVisitor mv, bir:BType sourceType, bir:BType targetType) {
    if (targetType is bir:BTypeInt) {
        generateCastToInt(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeFloat) {
        generateCastToFloat(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeString) {
        generateCastToString(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeBoolean) {
        generateCastToBoolean(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeByte) {
        generateCastToByte(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeNil) {
        // do nothing
        return;
    } else if (targetType is bir:BUnionType) {
        generateCastToUnionType(mv, sourceType, targetType);
        return;
    } else if (targetType is bir:BTypeAnyData) {
        generateCastToAnyData(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeAny) {
        generateCastToAny(mv, sourceType);
        return;
    } 

    string targetTypeClass = "";
    if (targetType is bir:BArrayType) {
        targetTypeClass = ARRAY_VALUE;
    } else if (targetType is bir:BMapType) {
        targetTypeClass = MAP_VALUE;
    } else if (targetType is bir:BRecordType) {
        targetTypeClass = MAP_VALUE;
    } else if (targetType is bir:BObjectType) {
        targetTypeClass = OBJECT_VALUE;
    } else if (targetType is bir:BInvokableType) {
        error err = error(io:sprintf("Casting is not supported from '%s' to '%s'", sourceType, targetType));
        panic err;
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to '%s'", sourceType, targetType));
        panic err;
    }

    generateCheckCast(mv, sourceType, targetType, targetTypeClass);
}

function generateCastToInt(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2L);
    } else if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", io:sprintf("(L%s;)J", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
        panic err;
    }
}

function generateCastToFloat(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2D);
    } else if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", io:sprintf("(L%s;)D", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'float'", sourceType));
        panic err;
    }
}

function generateCastToString(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'string'", sourceType));
        panic err;
    }
}

function generateCastToBoolean(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToBoolean", io:sprintf("(L%s;)Z", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'boolean'", sourceType));
        panic err;
    }
}

function generateCastToByte(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", io:sprintf("(L%s;)B", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'byte'", sourceType));
        panic err;
    }
}

function generateCastToAny(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
    } else if (sourceType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", io:sprintf("(Z)L%s;", BOOLEAN_VALUE), false);
    } else if (sourceType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "valueOf", io:sprintf("(B)L%s;", BYTE_VALUE), false);
    } else {
        // do nothing
        return;
    }
}

function generateCastToAnyData(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny) {
        checkCast(mv, "anydata");
    } else {
        // if value types, then ad box instruction
        generateCastToAny(mv, sourceType);
    }
}

function generateCastToUnionType(jvm:MethodVisitor mv, bir:BType sourceType, bir:BUnionType targetType) {
    if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        // TODO: add JSON
        checkCast(mv, targetType);
    } else {
        // if value types, then ad box instruction
        generateCastToAny(mv, sourceType);
    }
}

function generateCheckCast(jvm:MethodVisitor mv, bir:BType sourceType, bir:BType targetType, string targetClass) {
    // do the ballerina checkcast
    checkCast(mv, targetType);

    // cast to the specific java class
    if (sourceType is bir:BTypeAny || sourceType is bir:BTypeAnyData) {
        mv.visitTypeInsn(CHECKCAST, targetClass);
    }
}

function checkCast(jvm:MethodVisitor mv, bir:BType targetType) {
    loadType(mv, targetType);
    mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkCast",
            io:sprintf("(L%s;L%s;)L%s;", OBJECT, BTYPE, OBJECT), false);
}
