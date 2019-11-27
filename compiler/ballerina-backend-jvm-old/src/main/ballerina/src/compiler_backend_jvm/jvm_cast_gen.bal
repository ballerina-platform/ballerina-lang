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

// ------------------------------------------------------------------
//                  Generate Check Cast Methods
// ------------------------------------------------------------------

import ballerina/bir;
import ballerina/io;
import ballerina/jvm;

function generatePlatformCheckCast(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, bir:BType sourceType, bir:BType targetType) {
    if (sourceType is jvm:JType) {
        // If a target type is bir type, then we can guarantee source type is a jvm type, hence the cast
        generateJToBCheckCast(mv, indexMap, sourceType, targetType);
    } else {
        // else target type is jvm and source type is bir
        generateBToJCheckCast(mv, sourceType, <jvm:JType>targetType);
    }
}

function generateBToJCheckCast(jvm:MethodVisitor mv, bir:BType sourceType, jvm:JType targetType) {
    if (targetType is jvm:JByte) {
        generateCheckCastBToJByte(mv, sourceType);
        return;
    } else if (targetType is jvm:JChar) {
        generateCheckCastBToJChar(mv, sourceType);
        return;
    } else if (targetType is jvm:JShort) {
        generateCheckCastBToJShort(mv, sourceType);
        return;
    } else if (targetType is jvm:JInt) {
        generateCheckCastBToJInt(mv, sourceType);
        return;
    } else if (targetType is jvm:JLong) {
        generateCheckCastBToJLong(mv, sourceType);
        return;
    } else if (targetType is jvm:JFloat) {
        generateCheckCastBToJFloat(mv, sourceType);
        return;
    } else if (targetType is jvm:JDouble) {
        generateCheckCastBToJDouble(mv, sourceType);
        return;
    } else if (targetType is jvm:JRefType|jvm:JArrayType) {
        generateCheckCastBToJRef(mv, sourceType, targetType);
        return;
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java %s'", sourceType, targetType));
        panic err;
    }
}

function generateCheckCastBToJByte(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2I);
        mv.visitInsn(I2B);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2I);
        mv.visitInsn(I2B);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java byte'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJChar(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2C);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2I);
        mv.visitInsn(I2C);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2I);
        mv.visitInsn(I2C);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java char'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJShort(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2S);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2I);
        mv.visitInsn(I2S);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2I);
        mv.visitInsn(I2S);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java short'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJInt(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2I);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2I);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java int'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJLong(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2L);
    } else if (sourceType is bir:BTypeInt) {
        // do nothing
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2L);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java long'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJFloat(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2F);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2F);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2F);
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java float'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJDouble(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2D);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2D);
    } else if (sourceType is bir:BTypeFloat) {
        // do nothing
    } else if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'java double'", sourceType));
        panic err;
    }
}

function generateCheckCastBToJRef(jvm:MethodVisitor mv, bir:BType sourceType, jvm:JRefType | jvm:JArrayType targetType) {
    if (sourceType is bir:BTypeHandle) {
        mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, "getValue", "()Ljava/lang/Object;", false);
	string sig = getSignatureForJType(targetType);
        mv.visitTypeInsn(CHECKCAST, sig);
    } else if (sourceType is bir:BTypeDecimal) {
        // In this case we should send a BigDecimal to java side 
        mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "decimalValue", "()Ljava/math/BigDecimal;", false);
    } else {
        if (targetType is jvm:JRefType) {
            addBoxInsn(mv, sourceType);
            mv.visitTypeInsn(CHECKCAST, targetType.typeValue);
        } else {
            error err = error(io:sprintf("Casting is not supported from '%s' to '%s'", sourceType, targetType));
            panic err;
        }
    }
}

function generateJToBCheckCast(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, jvm:JType sourceType, bir:BType targetType) {
    if (targetType is bir:BTypeInt) {
        generateCheckCastJToBInt(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeFloat) {
        generateCheckCastJToBFloat(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeString) {
        generateCheckCastJToBString(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeDecimal) {
        generateCheckCastJToBDecimal(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeBoolean) {
        generateCheckCastJToBBoolean(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeByte) {
        generateCheckCastJToBByte(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeNil) {
        // Do nothing
        return;
    } else if (targetType is bir:BUnionType) {
        generateCheckCastJToBUnionType(mv, indexMap, sourceType, targetType);
        return;
    } else if (targetType is bir:BTypeAnyData) {
        generateCheckCastJToBAnyData(mv, indexMap, sourceType);
        return;
    } else if (targetType is bir:BTypeHandle) {
        generateJCastToBHandle(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeAny) {
        generateJCastToBAny(mv, indexMap, sourceType);
        return;
    } else if (targetType is bir:BJSONType) {
        generateCheckCastJToBJSON(mv, indexMap, sourceType);
        return;
    // TODO fix below properly - rajith
    //} else if (sourceType is bir:BXMLType && targetType is bir:BMapType) {
    //    generateXMLToAttributesMap(mv, sourceType);
    //    return;
    //} else if (targetType is bir:BFiniteType) {
    //    generateCheckCastToFiniteType(mv, sourceType, targetType);
    //    return;
    //} else if (sourceType is bir:BRecordType && (targetType is bir:BMapType && targetType.constraint is bir:BTypeAny)) {
    //    // do nothing
    } else {
        string? targetTypeClass = getTargetClass(sourceType, targetType);
        if (targetTypeClass is string) {
            mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        }
    }
}

function generateCheckCastJToBInt(jvm:MethodVisitor mv, jvm:JType sourceType) {
    if (sourceType is jvm:JByte) {
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        mv.visitInsn(I2L);
    } else if (sourceType is jvm:JChar) {
        mv.visitInsn(I2L);
    } else if (sourceType is jvm:JShort) {
        mv.visitInsn(I2L);
    } else if (sourceType is jvm:JInt) {
        mv.visitInsn(I2L);
    } else if (sourceType is jvm:JLong) {
        // do nothing
    // According to the spec doc, below two are not needed
    // } else if (sourceType is jvm:JFloat) {
    //     mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "jFloatToBInt", "(F)J", false);
    // } else if (sourceType is jvm:JDouble) {
    //     mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "jDoubleToBInt", "(D)J", false);
    } else if (sourceType is jvm:JRefType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", io:sprintf("(L%s;)J", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBFloat(jvm:MethodVisitor mv, jvm:JType sourceType) {
    if (sourceType is jvm:JByte) {
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        mv.visitInsn(I2D);
    } else if (sourceType is jvm:JChar) {
        mv.visitInsn(I2D);
    } else if (sourceType is jvm:JShort) {
        mv.visitInsn(I2D);
    } else if (sourceType is jvm:JInt) {
        mv.visitInsn(I2D);
    } else if (sourceType is jvm:JLong) {
        mv.visitInsn(L2D);
    } else if (sourceType is jvm:JFloat) {
        mv.visitInsn(F2D);
    } else if (sourceType is jvm:JDouble) {
        // do nothing
    } else if (sourceType is jvm:JRefType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", io:sprintf("(L%s;)D", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'float'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBString(jvm:MethodVisitor mv, jvm:JType sourceType) {
    // TODO fill for jvm:JString later
}

function generateCheckCastJToBDecimal(jvm:MethodVisitor mv, jvm:JType sourceType) {
    if (sourceType is jvm:JByte) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(B)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JChar) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(C)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JShort) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(S)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JInt) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(I)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JLong) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(J)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(F)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JDouble) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(D)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is jvm:JRefType) { 
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOfJ", io:sprintf("(Ljava/math/BigDecimal;)L%s;", DECIMAL_VALUE), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'decimal'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBBoolean(jvm:MethodVisitor mv, jvm:JType sourceType) {
    if (sourceType is jvm:JBoolean) {
        // do nothing
    } else if (sourceType is jvm:JRefType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToBoolean", io:sprintf("(L%s;)Z", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'boolean'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBByte(jvm:MethodVisitor mv, jvm:JType sourceType) {
    if (sourceType is jvm:JByte) {
        // do nothing
    } else if (sourceType is jvm:JRefType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", io:sprintf("(L%s;)I", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'byte'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBUnionType(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, jvm:JType sourceType, bir:BUnionType targetType) {
    generateJCastToBAny(mv, indexMap, sourceType);
    // TODO implement check cast from java types - rajith
    //checkCast(mv, targetType);
}

function generateCheckCastJToBAnyData(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, jvm:JType sourceType) {
    if (sourceType is jvm:JRefType || sourceType is jvm:JArrayType) {
    	// TODO fix properly - rajith
        //checkCast(mv, bir:TYPE_ANYDATA);
    } else {
        // if value types, then ad box instruction
        generateJCastToBAny(mv, indexMap, sourceType);
    }
}

function generateJCastToBHandle(jvm:MethodVisitor mv, jvm:JType sourceType) {
    //  TODO do we need to support below? - rajith
    //if (sourceType is jvm:JByte) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(B)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JChar) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(C)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JShort) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(S)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JInt) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(I)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JLong) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(J)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JFloat) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(F)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JDouble) {
    //    mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(D)L%s;", HANDLE_VALUE), false);
    //} else if (sourceType is jvm:JRefType || sourceType is jvm:JArrayType) {
    // Here the corresponding Java method parameter type is 'jvm:JRefType'. This has been verified before
        mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, "valueOfJ", io:sprintf("(L%s;)L%s;", OBJECT, HANDLE_VALUE), false);
    //} else {
    //    error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
    //    panic err;
    //}
}

function generateJCastToBAny(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, jvm:JType sourceType) {
    if (sourceType is jvm:JBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", io:sprintf("(Z)L%s;", BOOLEAN_VALUE), false);
    } else if (sourceType is jvm:JByte) {
        mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", io:sprintf("(I)L%s;", INT_VALUE), false);
    } else if (sourceType is jvm:JChar) {
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is jvm:JShort) {
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is jvm:JInt) {
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is jvm:JLong) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is jvm:JFloat) {
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
    } else if (sourceType is jvm:JDouble) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
    } else if (sourceType is jvm:JRefType) {
        jvm:Label afterHandle = new;
        if (sourceType.typeValue == "java/lang/Object") {
            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, ERROR_VALUE);
            mv.visitJumpInsn(IFNE, afterHandle);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Number");
            mv.visitJumpInsn(IFNE, afterHandle);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Boolean");
            mv.visitJumpInsn(IFNE, afterHandle);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, REF_VALUE);
            mv.visitJumpInsn(IFNE, afterHandle);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Byte");
            mv.visitJumpInsn(IFNE, afterHandle);
        }

        mv.visitInsn(DUP);
        mv.visitTypeInsn(INSTANCEOF, REF_VALUE);
        mv.visitJumpInsn(IFNE, afterHandle);

        bir:VariableDcl retJObjectVarDcl = { typeValue: "any", name: { value: "$_ret_jobject_val_$" }, kind: "LOCAL" };
        int returnJObjectVarRefIndex = indexMap.getIndex(retJObjectVarDcl);
        mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
        mv.visitTypeInsn(NEW, HANDLE_VALUE);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
        mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, "<init>", "(Ljava/lang/Object;)V", false);
        mv.visitLabel(afterHandle);
    } else if (sourceType is jvm:JArrayType) {
        // do nothing
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'any'", sourceType));
        panic err;
    }
}

function generateCheckCastJToBJSON(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, jvm:JType sourceType) {
    if (sourceType is jvm:JRefType || sourceType is jvm:JArrayType) {
    	// TODO fix properly - rajith
        //checkCast(mv, bir:TYPE_JSON);
    } else {
        // if value types, then ad box instruction
        generateJCastToBAny(mv, indexMap, sourceType);
    }
}

function generateCheckCast(jvm:MethodVisitor mv, bir:BType sourceType, bir:BType targetType) {
    if (targetType is bir:BTypeInt) {
        generateCheckCastToInt(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeFloat) {
        generateCheckCastToFloat(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeString) {
        generateCheckCastToString(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeDecimal) {
        generateCheckCastToDecimal(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeBoolean) {
        generateCheckCastToBoolean(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeByte) {
        generateCheckCastToByte(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeNil) {
        checkCast(mv, targetType);
        return;
    } else if (targetType is bir:BUnionType) {
        generateCheckCastToUnionType(mv, sourceType, targetType);
        return;
    } else if (targetType is bir:BTypeAnyData) {
        generateCheckCastToAnyData(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeAny) {
        generateCastToAny(mv, sourceType);
        return;
    } else if (targetType is bir:BJSONType) {
        generateCheckCastToJSON(mv, sourceType);
        return;
    } else if (sourceType is bir:BXMLType && targetType is bir:BMapType) {
        generateXMLToAttributesMap(mv, sourceType);
        return;
    } else if (targetType is bir:BFiniteType) {
        generateCheckCastToFiniteType(mv, sourceType, targetType);
        return;
    } else if (sourceType is bir:BRecordType && (targetType is bir:BMapType && targetType.constraint is bir:BTypeAny)) {
        // do nothing
    } else {
        // do the ballerina checkcast
        checkCast(mv, targetType);
    }

    // cast to the specific java class
    string? targetTypeClass = getTargetClass(sourceType, targetType);
    if (targetTypeClass is string) {
        mv.visitTypeInsn(CHECKCAST, targetTypeClass);
    }
}

function generateCheckCastToInt(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        // do nothing
    } else if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        mv.visitInsn(I2L);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToInt", "(D)J", false);
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BTypeDecimal ||
            sourceType is bir:BJSONType ||
            sourceType is bir:BFiniteType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", io:sprintf("(L%s;)J", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
        panic err;
    }
}

function generateCheckCastToFloat(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeFloat) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2D);
    } else if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2D);
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BTypeDecimal ||
            sourceType is bir:BJSONType ||
            sourceType is bir:BFiniteType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", io:sprintf("(L%s;)D", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'float'", sourceType));
        panic err;
    }
}

function generateCheckCastToDecimal(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeDecimal) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType ||
            sourceType is bir:BFiniteType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToDecimal", 
                io:sprintf("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", io:sprintf("(J)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", io:sprintf("(D)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", io:sprintf("(I)L%s;", DECIMAL_VALUE), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'decimal'", sourceType));
        panic err;
    }
}

function generateCheckCastToString(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeString) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType ||
            sourceType is bir:BFiniteType) {
        checkCast(mv, bir:TYPE_STRING);
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "toString", io:sprintf("(J)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "toString", io:sprintf("(D)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "toString", io:sprintf("(Z)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeDecimal) {
        mv.visitMethodInsn(INVOKESTATIC, STRING_VALUE, "valueOf", io:sprintf("(L%s;)L%s;", OBJECT, STRING_VALUE),
            false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'string'", sourceType));
        panic err;
    }
}

function generateCheckCastToBoolean(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeBoolean) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType ||
            sourceType is bir:BFiniteType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToBoolean", io:sprintf("(L%s;)Z", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'boolean'", sourceType));
        panic err;
    }
}

function generateCheckCastToByte(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToByte", "(J)I", false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToByte", "(D)I", false);
    } else if (sourceType is bir:BTypeDecimal) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", io:sprintf("(L%s;)I", OBJECT), false);
    } else if (sourceType is bir:BTypeByte) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
                sourceType is bir:BTypeAnyData ||
                sourceType is bir:BUnionType ||
                sourceType is bir:BFiniteType ||
                sourceType is bir:BJSONType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", io:sprintf("(L%s;)I", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'byte'", sourceType));
        panic err;
    }
}

function generateCheckCastToAnyData(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny || sourceType is bir:BUnionType) {
        checkCast(mv, bir:TYPE_ANYDATA);
    } else {
        // if value types, then ad box instruction
        generateCastToAny(mv, sourceType);
    }
}

function generateCheckCastToJSON(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeAny ||
        sourceType is bir:BUnionType ||
        sourceType is bir:BMapType) {
        checkCast(mv, bir:TYPE_JSON);
    } else {
        // if value types, then ad box instruction
        generateCastToAny(mv, sourceType);
    }
}

function generateCheckCastToUnionType(jvm:MethodVisitor mv, bir:BType sourceType, bir:BUnionType targetType) {
    generateCastToAny(mv, sourceType);
    checkCast(mv, targetType);
}

function checkCast(jvm:MethodVisitor mv, bir:BType targetType) {
    loadType(mv, targetType);
    mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkCast",
            io:sprintf("(L%s;L%s;)L%s;", OBJECT, BTYPE, OBJECT), false);
}

function getTargetClass(bir:BType sourceType, bir:BType targetType) returns string? {
    string targetTypeClass;
    if (targetType is bir:BArrayType || targetType is bir:BTupleType) {
        targetTypeClass = ARRAY_VALUE;
    } else if (targetType is bir:BMapType) {
        targetTypeClass = MAP_VALUE;
    } else if (targetType is bir:BRecordType) {
        targetTypeClass = MAP_VALUE;
    } else if (targetType is bir:BTableType) {
        targetTypeClass = TABLE_VALUE;
    } else if (targetType is bir:BStreamType) {
        targetTypeClass = STREAM_VALUE;
    } else if (targetType is bir:BObjectType || targetType is bir:BServiceType) {
        targetTypeClass = OBJECT_VALUE;
    } else if (targetType is bir:BErrorType) {
        targetTypeClass = ERROR_VALUE;
    } else if (targetType is bir:BXMLType) {
        targetTypeClass = XML_VALUE;
    } else if (targetType is bir:BTypeDesc) {
        targetTypeClass = TYPEDESC_VALUE;
    } else if (targetType is bir:BInvokableType) {
        targetTypeClass = FUNCTION_POINTER;
    } else if (targetType is bir:BFutureType) {
        targetTypeClass = FUTURE_VALUE;
    } else if (targetType is bir:BTypeHandle) {
        targetTypeClass = HANDLE_VALUE;
    } else {
        return;
    }

    return targetTypeClass;
}

function generateCheckCastToFiniteType(jvm:MethodVisitor mv, bir:BType sourceType, bir:BFiniteType targetType) {
    generateCastToAny(mv, sourceType);
    checkCast(mv, targetType);
}


// ------------------------------------------------------------------
//   Generate Cast Methods - Performs cast without type checking
// ------------------------------------------------------------------

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
    } else if (targetType is bir:BTypeDecimal) {
        generateCastToDecimal(mv, sourceType);
        return;
    } else if (targetType is bir:BTypeNil) {
        // do nothing
        return;
    } else if (targetType is bir:BUnionType ||
               targetType is bir:BTypeAnyData ||
               targetType is bir:BTypeAny ||
               targetType is bir:BJSONType ||
               targetType is bir:BFiniteType) {
        generateCastToAny(mv, sourceType);
        return;
    }

    // cast to the specific java class
    string? targetTypeClass = getTargetClass(sourceType, targetType);
    if (targetTypeClass is string) {
        mv.visitTypeInsn(CHECKCAST, targetTypeClass);
    }
}

function generateCastToInt(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        // do nothing
    } else if (sourceType is bir:BTypeByte) {
        mv.visitInsn(I2L);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitInsn(D2L);
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToInt", io:sprintf("(L%s;)J", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'int'", sourceType));
        panic err;
    }
}

function generateCastToFloat(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeFloat) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2D);
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToFloat", io:sprintf("(L%s;)D", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'float'", sourceType));
        panic err;
    }
}

function generateCastToString(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeString) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "toString", io:sprintf("(J)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "toString", io:sprintf("(D)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "toString", io:sprintf("(Z)L%s;", STRING_VALUE), false);
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType) {
        mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'string'", sourceType));
        panic err;
    }
}

function generateCastToDecimal(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeDecimal) {
        // do nothing
    } else if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", io:sprintf("(J)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, "valueOf", io:sprintf("(D)L%s;", DECIMAL_VALUE), false);
    } else if (sourceType is bir:BTypeAny ||
        sourceType is bir:BTypeAnyData ||
        sourceType is bir:BUnionType ||
        sourceType is bir:BJSONType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToDecimal", 
                io:sprintf("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'decimal'", sourceType));
        panic err;
    }
}

function generateCastToBoolean(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeBoolean) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
            sourceType is bir:BTypeAnyData ||
            sourceType is bir:BUnionType ||
            sourceType is bir:BJSONType) {
        mv.visitTypeInsn(CHECKCAST, BOOLEAN_VALUE);
        mv.visitMethodInsn(INVOKEVIRTUAL, BOOLEAN_VALUE, "booleanValue", "()Z", false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'boolean'", sourceType));
        panic err;
    }
}

function generateCastToByte(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        mv.visitInsn(L2I);
    } else if (sourceType is bir:BTypeByte) {
        // do nothing
    } else if (sourceType is bir:BTypeAny ||
                sourceType is bir:BTypeAnyData ||
                sourceType is bir:BUnionType ||
                sourceType is bir:BJSONType) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToByte", io:sprintf("(L%s;)I", OBJECT), false);
    } else {
        error err = error(io:sprintf("Casting is not supported from '%s' to 'byte'", sourceType));
        panic err;
    }
}

function generateCastToAny(jvm:MethodVisitor mv, bir:BType sourceType) {
    if (sourceType is bir:BTypeInt) {
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, "valueOf", io:sprintf("(J)L%s;", LONG_VALUE), false);
    } else if (sourceType is bir:BTypeByte) {
        mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "valueOf", io:sprintf("(I)L%s;", INT_VALUE), false);
    } else if (sourceType is bir:BTypeFloat) {
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, "valueOf", io:sprintf("(D)L%s;", DOUBLE_VALUE), false);
    } else if (sourceType is bir:BTypeBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, "valueOf", io:sprintf("(Z)L%s;", BOOLEAN_VALUE), false);
    } else {
        // do nothing
        return;
    }
}

function generateXMLToAttributesMap(jvm:MethodVisitor mv, bir:BType sourceType) {
    mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttributesMap", 
            io:sprintf("()L%s;", MAP_VALUE), false);
}
