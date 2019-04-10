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

import ballerina/io;

type InstructionGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;

    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, string currentPackageName) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.currentPackageName = currentPackageName;
    }

    function generateConstantLoadIns(bir:ConstantLoad loadIns) {
        bir:BType bType = loadIns.typeValue;

        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeFloat) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeString) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeBoolean) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeNil) {
            self.mv.visitInsn(ACONST_NULL);
        } else {
            error err = error( "JVM generation is not supported for type : " + io:sprintf("%s", bType));
            panic err;
        }

        self.generateVarStore(loadIns.lhsOp.variableDcl);
    }

    function generateMoveIns(bir:Move moveIns) {
        self.generateVarLoad(moveIns.rhsOp.variableDcl);
        self.generateVarStore(moveIns.lhsOp.variableDcl);
    }


    function generateBinaryOpIns(bir:BinaryOp binaryIns) {
        if (binaryIns.kind == bir:BINARY_LESS_THAN) {
            self.generateLessThanIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_ADD) {
            self.generateAddIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_EQUAL) {
            self.generateEqualIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_SUB) {
            self.generateSubIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_DIV) {
            self.generateDivIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_MUL) {
            self.generateMulIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_AND) {
            self.generateAndIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_OR) {
            self.generateOrIns(binaryIns);
        } else if (binaryIns.kind == bir:BINARY_LESS_EQUAL) {
            self.generateLessEqualIns(binaryIns);
        } else {
            error err = error("JVM generation is not supported for type : " + io:sprintf("%s", binaryIns.kind));
            panic err;
        }
    }

    function generateBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);
        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);
    }

    function generateLessThanIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLT, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateLessEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitInsn(LCMP);
        self.mv.visitJumpInsn(IFLE, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        // It is assumed that both operands are of same type
        bir:BType opType = binaryIns.rhsOp1.variableDcl.typeValue;
        if (opType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(IFNE, label1);
        } else if (opType is bir:BTypeFloat ||
                    opType is bir:BTypeString ||
                    opType is bir:BTypeBoolean ||
                    opType is bir:BTypeByte ) {
            error err = error( "equal operator is not supported for type " +
                    io:sprintf("%s", opType));
            panic err;
        } else {
            self.mv.visitJumpInsn(IF_ACMPNE, label1);
        }

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateAddIns(bir:BinaryOp binaryIns) {
        //io:println("ADD Ins " + io:sprintf("%s", binaryIns));

        bir:BType bType = binaryIns.lhsOp.typeValue;

        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            self.generateBinaryRhsAndLhsLoad(binaryIns);

            self.mv.visitInsn(LADD);
            self.generateVarStore(binaryIns.lhsOp.variableDcl);
        } else if (bType is bir:BTypeString) {
            self.generateVarLoad(binaryIns.rhsOp1.variableDcl);
            self.generateVarLoad(binaryIns.rhsOp2.variableDcl);
            self.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                                         "(Ljava/lang/String;)Ljava/lang/String;", false);
            self.generateVarStore(binaryIns.lhsOp.variableDcl);
        } else if (bType is bir:BTypeFloat) {
            self.generateBinaryRhsAndLhsLoad(binaryIns);

            self.mv.visitInsn(DADD);
            self.generateVarStore(binaryIns.lhsOp.variableDcl);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
    }

    function generateSubIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LSUB);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DSUB);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateDivIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LDIV);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DDIV);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateMulIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LMUL);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DMUL);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateAndIns(bir:BinaryOp binaryIns) {
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ICONST_1
        // ISTORE

        //io:println("AND ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPNE, label1);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);

        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function generateOrIns(bir:BinaryOp binaryIns) {
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ILOAD
        // ICONST_1
        // IF_ICMPNE L0
        // ICONST_1
        // ISTORE

        //io:println("OR ins : " + io:sprintf("%s", binaryIns));

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.generateVarLoad(binaryIns.rhsOp1.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        self.generateVarLoad(binaryIns.rhsOp2.variableDcl);

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(IF_ICMPEQ, label1);

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);

        self.generateVarStore(binaryIns.lhsOp.variableDcl);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }

    function generateMapNewIns(bir:NewMap mapNewIns) {
        self.mv.visitTypeInsn(NEW, MAP_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, mapNewIns.typeValue);
        self.mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        self.generateVarStore(mapNewIns.lhsOp.variableDcl);
    }

    function generateMapStoreIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        self.generateVarLoad(mapStoreIns.lhsOp.variableDcl);
        bir:BType varRefType = mapStoreIns.lhsOp.variableDcl.typeValue;

        // visit key_expr
        self.generateVarLoad(mapStoreIns.keyOp.variableDcl);

        // visit value_expr
        bir:BType valueType = mapStoreIns.rhsOp.variableDcl.typeValue;
        self.generateVarLoad(mapStoreIns.rhsOp.variableDcl);
        addBoxInsn(self.mv, valueType);

        if (varRefType is bir:BJSONType) {
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setElement",
                    io:sprintf("(L%s;L%s;L%s;)V", OBJECT, STRING_VALUE, OBJECT), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "put",
                    io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), false);

            // emit a pop, since we are not using the return value from the map.put()
            self.mv.visitInsn(POP);
        }
    }

    function generateMapLoadIns(bir:FieldAccess mapLoadIns) {
        // visit map_ref
        self.generateVarLoad(mapLoadIns.rhsOp.variableDcl);
        bir:BType varRefType = mapLoadIns.rhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, varRefType);

        // visit key_expr
        self.generateVarLoad(mapLoadIns.keyOp.variableDcl);

        if (varRefType is bir:BJSONType) {
            self.mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElement",
                    io:sprintf("(L%s;L%s;)L%s;", OBJECT, STRING_VALUE, OBJECT), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, MAP_VALUE, "get",
                    io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);
        }

        // store in the target reg
        bir:BType targetType = mapLoadIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);
        self.generateVarStore(mapLoadIns.lhsOp.variableDcl);
    }

    function generateObjectLoadIns(bir:FieldAccess objectLoadIns) {
        // visit object_ref
        self.generateVarLoad(objectLoadIns.rhsOp.variableDcl);
        bir:BType varRefType = objectLoadIns.rhsOp.variableDcl.typeValue;

        // visit key_expr
        self.generateVarLoad(objectLoadIns.keyOp.variableDcl);

        // invoke get() method, and unbox if needed
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "get",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT), true);
        bir:BType targetType = objectLoadIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);

        // store in the target reg
        self.generateVarStore(objectLoadIns.lhsOp.variableDcl);
    }

    function generateObjectStoreIns(bir:FieldAccess objectStoreIns) {
        // visit object_ref
        self.generateVarLoad(objectStoreIns.lhsOp.variableDcl);
        bir:BType varRefType = objectStoreIns.lhsOp.variableDcl.typeValue;

        // visit key_expr
        self.generateVarLoad(objectStoreIns.keyOp.variableDcl);

        // visit value_expr
        bir:BType valueType = objectStoreIns.rhsOp.variableDcl.typeValue;
        self.generateVarLoad(objectStoreIns.rhsOp.variableDcl);
        addBoxInsn(self.mv, valueType);

        // invoke set() method
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "set",
                io:sprintf("(L%s;L%s;)V", STRING_VALUE, OBJECT), true);
    }

    # Generate a new instance of an array value
    # 
    # + inst - the new array instruction
    function generateArrayNewIns(bir:NewArray inst) {
        self.mv.visitTypeInsn(NEW, ARRAY_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, inst.typeValue);
        self.generateVarLoad(inst.sizeOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE, "<init>", io:sprintf("(L%s;J)V", BTYPE), false);
        self.generateVarStore(inst.lhsOp.variableDcl);
    }

    # Generate adding a new value to an array
    # 
    # + inst - array store instruction
    function generateArrayStoreIns(bir:FieldAccess inst) {
        self.generateVarLoad(inst.lhsOp.variableDcl);
        self.generateVarLoad(inst.keyOp.variableDcl);
        self.generateVarLoad(inst.rhsOp.variableDcl);

        bir:BType varRefType = inst.lhsOp.variableDcl.typeValue;
        if (varRefType is bir:BJSONType ||
                (varRefType is bir:BArrayType && varRefType.eType  is bir:BJSONType)) {
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setArrayElement",
                    io:sprintf("(L%s;JL%s;)V", OBJECT, OBJECT), false);
            return;
        }

        string valueDesc;
        if (varRefType is bir:BArrayType && varRefType.eType is bir:BTypeByte) {
            self.mv.visitInsn(L2I);
            self.mv.visitInsn(I2B);
            valueDesc = "B";
        } else {
            valueDesc = getTypeDesc(inst.rhsOp.variableDcl.typeValue);
        }
        self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "add", io:sprintf("(J%s)V", valueDesc), false);
    }

    # Generating loading a new value from an array to the top of the stack
    # 
    # + inst - field access instruction
    function generateArrayValueLoad(bir:FieldAccess inst) {
        self.generateVarLoad(inst.rhsOp.variableDcl);
        self.generateVarLoad(inst.keyOp.variableDcl);
        bir:BType bType = inst.lhsOp.variableDcl.typeValue;

        bir:BType varRefType = inst.rhsOp.variableDcl.typeValue;
        if (varRefType is bir:BJSONType ||
                (varRefType is bir:BArrayType && varRefType.eType  is bir:BJSONType)) {
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getArrayElement",
                        io:sprintf("(L%s;J)L%s;", OBJECT, OBJECT), false);
        } else if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getInt", "(J)J", false);
        } else if (bType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE),
                                        false);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getBoolean", "(J)J", false);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getByte", "(J)B", false);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getFloat", "(J)D", false);
        } else if (bType is bir:BRecordType) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
            self.mv.visitTypeInsn(CHECKCAST, MAP_VALUE);
        } else if (bType is bir:BXMLType) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
            self.mv.visitTypeInsn(CHECKCAST, XML_VALUE);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), false);
        }

        self.generateVarStore(inst.lhsOp.variableDcl);
    }

    function generateNewErrorIns(bir:NewError newErrorIns) {
        self.mv.visitTypeInsn(NEW, ERROR_VALUE);
        self.mv.visitInsn(DUP);
        self.generateVarLoad(newErrorIns.reasonOp.variableDcl);
        self.generateVarLoad(newErrorIns.detailsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                           io:sprintf("(L%s;L%s;)V", STRING_VALUE, OBJECT), false);
        self.generateVarStore(newErrorIns.lhsOp.variableDcl);
    }

    private function generateVarLoad(bir:VariableDcl varDcl) {
        bir:BType bType = varDcl.typeValue;

        if (varDcl.kind == bir:VAR_KIND_GLOBAL) {
            string varName = varDcl.name.value;
            string className = lookupFullQualifiedClassName(self.currentPackageName + varName);
            string typeSig = getTypeDesc(bType);
            self.mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == bir:VAR_KIND_SELF) {
            self.mv.visitVarInsn(ALOAD, 0);
            return;
        }

        int valueIndex = self.getJVMIndexOfVarRef(varDcl);
        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            self.mv.visitVarInsn(LLOAD, valueIndex);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DLOAD, valueIndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, valueIndex);
        } else if (bType is bir:BArrayType ||
                    bType is bir:BTypeString ||
                    bType is bir:BMapType ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BTypeNil ||
                    bType is bir:BUnionType ||
                    bType is bir:BTupleType ||
                    bType is bir:BRecordType ||
                    bType is bir:BErrorType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFutureType ||
                    bType is bir:BObjectType ||
                    bType is bir:BXMLType) {
            self.mv.visitVarInsn(ALOAD, valueIndex);
        } else {
            error err = error( "JVM generation is not supported for type " +io:sprintf("%s", bType));
            panic err;
        }
    }

    private function generateVarStore(bir:VariableDcl varDcl) {
        bir:BType bType = varDcl.typeValue;

        if (varDcl.kind == "GLOBAL") {
            string varName = varDcl.name.value;
            string className = lookupFullQualifiedClassName(self.currentPackageName + varName);
            string typeSig = getTypeDesc(bType);
            self.mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        }

        int valueIndex = self.getJVMIndexOfVarRef(varDcl);
        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            self.mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DSTORE, valueIndex);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType is bir:BArrayType ||
                        bType is bir:BTypeString ||
                        bType is bir:BMapType ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BTupleType ||
                        bType is bir:BRecordType ||
                        bType is bir:BErrorType ||
                        bType is bir:BJSONType ||
                        bType is bir:BFutureType ||
                        bType is bir:BObjectType ||
                        bType is bir:BXMLType) {
            self.mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
            panic err;
        }
    }

    function generateCastIns(bir:TypeCast typeCastIns) {
        // load source value
        self.generateVarLoad(typeCastIns.rhsOp.variableDcl);
        generateCheckCast(self.mv, typeCastIns.rhsOp.typeValue, typeCastIns.lhsOp.typeValue);
        self.generateVarStore(typeCastIns.lhsOp.variableDcl);
    }

    function generateTypeTestIns(bir:TypeTest typeTestIns) {
        // load source value
        self.generateVarLoad(typeTestIns.rhsOp.variableDcl);

        // load targetType
        loadType(self.mv, typeTestIns.typeValue);

        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                io:sprintf("(L%s;L%s;)Z", OBJECT, BTYPE, OBJECT), false);
        self.generateVarStore(typeTestIns.lhsOp.variableDcl);
    }

    function generateObjectNewIns(bir:NewInstance objectNewIns) {
        string className = self.currentPackageName + cleanupTypeName(objectNewIns.typeDef.name.value);
        self.mv.visitTypeInsn(NEW, className);
        self.mv.visitInsn(DUP);
        loadType(self.mv, objectNewIns.typeDef.typeValue);
        self.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        self.generateVarStore(objectNewIns.lhsOp.variableDcl);
    }

    function generateNewXMLElementIns(bir:NewXMLElement newXMLElement) {
        self.generateVarLoad(newXMLElement.startTagOp.variableDcl);
        self.generateVarLoad(newXMLElement.endTagOp.variableDcl);
        self.generateVarLoad(newXMLElement.defaultNsURIOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLElement",
                io:sprintf("(L%s;L%s;L%s;)L%s;", XML_QNAME, XML_QNAME, STRING_VALUE, XML_VALUE), false);
        self.generateVarStore(newXMLElement.lhsOp.variableDcl);
    }

    function generateNewXMLQNameIns(bir:NewXMLQName newXMLQName) {
        self.mv.visitTypeInsn(NEW, XML_QNAME);
        self.mv.visitInsn(DUP);
        self.generateVarLoad(newXMLQName.localnameOp.variableDcl);
        self.generateVarLoad(newXMLQName.nsURIOp.variableDcl);
        self.generateVarLoad(newXMLQName.prefixOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                io:sprintf("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);
        self.generateVarStore(newXMLQName.lhsOp.variableDcl);
    }

    function generateNewXMLTextIns(bir:NewXMLText newXMLText) {
        self.generateVarLoad(newXMLText.textOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLText",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
        self.generateVarStore(newXMLText.lhsOp.variableDcl);
    }

    function generateXMLStoreIns(bir:XMLSeqStore xmlStoreIns) {
        self.generateVarLoad(xmlStoreIns.lhsOp.variableDcl);
        self.generateVarLoad(xmlStoreIns.rhsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "addChildren", io:sprintf("(L%s;)V", XML_VALUE),
                                        false);
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, bType, "any");
}

function addUnboxInsn(jvm:MethodVisitor mv, bir:BType bType) {
    generateCast(mv, "any", bType);
}
