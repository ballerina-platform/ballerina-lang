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
import ballerina/bir;
import ballerina/jvm;
import ballerina/runtime;

boolean IS_BSTRING = runtime:getProperty("ballerina.bstring") != "";
string BSTRING_VALUE = runtime:getProperty("ballerina.bstring") == "" ? STRING_VALUE : B_STRING_VALUE;

const string B_STRING_VALUE = "io/ballerina/runtime/values/api/BString";
const string I_STRING_VALUE = "io/ballerina/runtime/values/StringValue";
const string NON_BMP_STRING_VALUE = "io/ballerina/runtime/values/NonBmpStringValue";

type InstructionGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;
    bir:Package currentPackage;

    function init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, bir:Package moduleId) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.currentPackage = moduleId;
        self.currentPackageName = getPackageName(moduleId.org.value, moduleId.name.value);
    }

    function generateConstantLoadIns(bir:ConstantLoad loadIns, boolean useBString) {
        bir:BType bType = loadIns.typeValue;

        if (bType is bir:BTypeInt || bType is bir:BTypeByte) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeFloat) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeString) {
            string val = <string> loadIns.value;
            if (useBString) {
                int[] highSurrogates = listHighSurrogates(val);

                self.mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
                self.mv.visitInsn(DUP);
                self.mv.visitLdcInsn(val);
                self.mv.visitIntInsn(BIPUSH, highSurrogates.length());
                self.mv.visitIntInsn(NEWARRAY, T_INT);

                int i = 0;
                foreach var char in highSurrogates {
                    self.mv.visitInsn(DUP);
                    self.mv.visitIntInsn(BIPUSH, i);
                    self.mv.visitIntInsn(BIPUSH, char);
                    i = i + 1;
                    self.mv.visitInsn(IASTORE);
                }
                self.mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, "<init>", io:sprintf("(L%s;[I)V", STRING_VALUE), false);
            } else {
                self.mv.visitLdcInsn(val);
            }
        } else if (bType is bir:BTypeDecimal) {
            any val = loadIns.value;
            self.mv.visitTypeInsn(NEW, DECIMAL_VALUE);
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(val);
            self.mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", io:sprintf("(L%s;)V", STRING_VALUE), false);
        } else if (bType is bir:BTypeBoolean) {
            any val = loadIns.value;
            self.mv.visitLdcInsn(val);
        } else if (bType is bir:BTypeNil) {
            self.mv.visitInsn(ACONST_NULL);
        } else {
            error err = error( "JVM generation is not supported for type : " + io:sprintf("%s", bType));
            panic err;
        }

        self.storeToVar(loadIns.lhsOp.variableDcl);
    }

    function generatePlatformIns(JInstruction ins) {
        if (ins.jKind == JCAST) {
            JCast castIns = <JCast> ins;
            bir:BType targetType = castIns.targetType;
            self.loadVar(castIns.rhsOp.variableDcl);
            generatePlatformCheckCast(self.mv, self.indexMap, castIns.rhsOp.typeValue, targetType);
            self.storeToVar(castIns.lhsOp.variableDcl);
        }
    }

    function generateMoveIns(bir:Move moveIns) {
        self.loadVar(moveIns.rhsOp.variableDcl);
        self.storeToVar(moveIns.lhsOp.variableDcl);
    }


    function generateBinaryOpIns(bir:BinaryOp binaryIns) {
        int insKind = binaryIns.kind;
        if (insKind <= bir:BINARY_LESS_EQUAL) {
            if (insKind == bir:BINARY_ADD) {
                self.generateAddIns(binaryIns);
            } else if (insKind == bir:BINARY_SUB) {
                self.generateSubIns(binaryIns);
            } else if (insKind == bir:BINARY_MUL) {
                self.generateMulIns(binaryIns);
            } else if (insKind == bir:BINARY_DIV) {
                self.generateDivIns(binaryIns);
            } else if (insKind == bir:BINARY_MOD) {
                self.generateRemIns(binaryIns);
            } else if (insKind == bir:BINARY_EQUAL) {
                self.generateEqualIns(binaryIns);
            } else if (insKind == bir:BINARY_NOT_EQUAL) {
                self.generateNotEqualIns(binaryIns);
            }  else if (insKind == bir:BINARY_GREATER_THAN) {
                self.generateGreaterThanIns(binaryIns);
            }  else if (insKind == bir:BINARY_GREATER_EQUAL) {
                self.generateGreaterEqualIns(binaryIns);
            } else if (insKind == bir:BINARY_LESS_THAN) {
                self.generateLessThanIns(binaryIns);
            } else {
                self.generateLessEqualIns(binaryIns);
            } 
        } else if (insKind <= bir:BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT) {
            if (insKind == bir:BINARY_REF_EQUAL) {
                self.generateRefEqualIns(binaryIns);
            } else if (insKind == bir:BINARY_REF_NOT_EQUAL) {
                self.generateRefNotEqualIns(binaryIns);
            } else if (insKind == bir:BINARY_CLOSED_RANGE) {
                self.generateClosedRangeIns(binaryIns);
            } else if (insKind == bir:BINARY_HALF_OPEN_RANGE) {
                self.generateClosedRangeIns(binaryIns);
            } else if (insKind == bir:BINARY_ANNOT_ACCESS) {
                self.generateAnnotAccessIns(binaryIns);
            } else if (insKind == bir:BINARY_BITWISE_AND) {
                self.generateBitwiseAndIns(binaryIns);
            } else if (insKind == bir:BINARY_BITWISE_OR) {
                self.generateBitwiseOrIns(binaryIns);
            } else if (insKind == bir:BINARY_BITWISE_XOR) {
                self.generateBitwiseXorIns(binaryIns);
            } else if (insKind == bir:BINARY_BITWISE_LEFT_SHIFT) {
                self.generateBitwiseLeftShiftIns(binaryIns);
            } else if (insKind == bir:BINARY_BITWISE_RIGHT_SHIFT) {
                self.generateBitwiseRightShiftIns(binaryIns);
            } else {
                self.generateBitwiseUnsignedRightShiftIns(binaryIns);
            }
        } else {
            error err = error("JVM generation is not supported for type : " + io:sprintf("%s", insKind));
            panic err;
        }
    }

    function generateBinaryRhsAndLhsLoad(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);
    }

    private function generateLessThanIns(bir:BinaryOp binaryIns) {
        self.generateBinaryCompareIns(binaryIns, IFLT);
    }

    private function generateGreaterThanIns(bir:BinaryOp binaryIns) {
        self.generateBinaryCompareIns(binaryIns, IFGT);
    }

    private function generateLessEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryCompareIns(binaryIns, IFLE);

    }

    private function generateGreaterEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryCompareIns(binaryIns, IFGE);
    }

    private function generateBinaryCompareIns(bir:BinaryOp binaryIns, int opcode) {
        if (opcode != IFLT && opcode != IFGT && opcode != IFLE && opcode != IFGE) {
            error err = error(io:sprintf("Unsupported opcode '%s' for binary operator.", opcode));
            panic err;
        }

        self.generateBinaryRhsAndLhsLoad(binaryIns);
        jvm:Label label1 = new;
        jvm:Label label2 = new;

        bir:BType lhsOpType = binaryIns.rhsOp1.variableDcl.typeValue;
        bir:BType rhsOpType = binaryIns.rhsOp2.variableDcl.typeValue;

        if (lhsOpType is bir:BTypeInt && rhsOpType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(opcode, label1);
        } else if (lhsOpType is bir:BTypeByte && rhsOpType is bir:BTypeByte) {
            if (opcode == IFLT) {
                self.mv.visitJumpInsn(IF_ICMPLT, label1);
            } else if (opcode != IFGT) {
                self.mv.visitJumpInsn(IF_ICMPGT, label1);
            } else if (opcode != IFLE) {
                self.mv.visitJumpInsn(IF_ICMPLE, label1);
            } else if (opcode == IFGE) {
                self.mv.visitJumpInsn(IF_ICMPGE, label1);
            }
        } else if (lhsOpType is bir:BTypeFloat && rhsOpType is bir:BTypeFloat) {
            self.mv.visitInsn(DCMPL);
            self.mv.visitJumpInsn(opcode, label1);
        } else if (lhsOpType is bir:BTypeDecimal && rhsOpType is bir:BTypeDecimal) {
            string compareFuncName = self.getDecimalCompareFuncName(opcode);
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, compareFuncName,
                io:sprintf("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            self.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        self.mv.visitInsn(ICONST_0);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_1);

        self.mv.visitLabel(label2);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private function getDecimalCompareFuncName(int opcode) returns string {
        if (opcode == IFGT) {
            return "checkDecimalGreaterThan";
        } else if (opcode == IFGE) {
            return "checkDecimalGreaterThanOrEqual";
        } else if (opcode == IFLT) {
            return "checkDecimalLessThan";
        } else if (opcode == IFLE) {
            return "checkDecimalLessThanOrEqual";
        } else {
            error err = error(io:sprintf("Opcode: '%s' is not a comparison opcode.", opcode));
            panic err;
        }
    }

    function generateEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        bir:BType lhsOpType = binaryIns.rhsOp1.variableDcl.typeValue;
        bir:BType rhsOpType = binaryIns.rhsOp2.variableDcl.typeValue;

        if (lhsOpType is bir:BTypeInt && rhsOpType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType is bir:BTypeByte && rhsOpType is bir:BTypeByte) {
            self.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType is bir:BTypeFloat && rhsOpType is bir:BTypeFloat) {
            self.mv.visitInsn(DCMPL);
            self.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType is bir:BTypeBoolean && rhsOpType is bir:BTypeBoolean) {
            self.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType is bir:BTypeDecimal && rhsOpType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                io:sprintf("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            self.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        } else {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                    io:sprintf("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            self.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateNotEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        // It is assumed that both operands are of same type
        bir:BType lhsOpType = binaryIns.rhsOp1.variableDcl.typeValue;
        bir:BType rhsOpType = binaryIns.rhsOp2.variableDcl.typeValue;
        if (lhsOpType is bir:BTypeInt && rhsOpType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType is bir:BTypeByte && rhsOpType is bir:BTypeByte) {
            self.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType is bir:BTypeFloat && rhsOpType is bir:BTypeFloat) {
            self.mv.visitInsn(DCMPL);
            self.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType is bir:BTypeBoolean && rhsOpType is bir:BTypeBoolean) {
            self.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType is bir:BTypeDecimal && rhsOpType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                io:sprintf("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            self.mv.visitJumpInsn(IFNE, label1);
        } else {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                    io:sprintf("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            self.mv.visitJumpInsn(IFNE, label1);
        }

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateRefEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        bir:BType lhsOpType = binaryIns.rhsOp1.variableDcl.typeValue;
        bir:BType rhsOpType = binaryIns.rhsOp2.variableDcl.typeValue;
        if (lhsOpType is bir:BTypeInt && rhsOpType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType is bir:BTypeByte && rhsOpType is bir:BTypeByte) {
            self.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType is bir:BTypeFloat && rhsOpType is bir:BTypeFloat) {
            self.mv.visitInsn(DCMPL);
            self.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType is bir:BTypeBoolean && rhsOpType is bir:BTypeBoolean) {
            self.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                    io:sprintf("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            self.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateRefNotEqualIns(bir:BinaryOp binaryIns) {
        self.generateBinaryRhsAndLhsLoad(binaryIns);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        // It is assumed that both operands are of same type
        bir:BType lhsOpType = binaryIns.rhsOp1.variableDcl.typeValue;
        bir:BType rhsOpType = binaryIns.rhsOp2.variableDcl.typeValue;
        if (lhsOpType is bir:BTypeInt && rhsOpType is bir:BTypeInt) {
            self.mv.visitInsn(LCMP);
            self.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType is bir:BTypeByte && rhsOpType is bir:BTypeByte) {
            self.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType is bir:BTypeFloat && rhsOpType is bir:BTypeFloat) {
            self.mv.visitInsn(DCMPL);
            self.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType is bir:BTypeBoolean && rhsOpType is bir:BTypeBoolean) {
            self.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else {
            self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                    io:sprintf("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            self.mv.visitJumpInsn(IFNE, label1);
        }

        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);

        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);

        self.mv.visitLabel(label2);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateClosedRangeIns(bir:BinaryOp binaryIns) {
        self.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
        self.mv.visitInsn(DUP);
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        self.mv.visitMethodInsn(INVOKESTATIC, LONG_STREAM, "rangeClosed", io:sprintf("(JJ)L%s;", LONG_STREAM), true);
        self.mv.visitMethodInsn(INVOKEINTERFACE, LONG_STREAM, "toArray", "()[J", true);
        self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", "([J)V", false);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateAnnotAccessIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getAnnotValue",
            io:sprintf("(L%s;L%s;)L%s;", TYPEDESC_VALUE, STRING_VALUE, OBJECT), false);

        bir:BType targetType = binaryIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateAddIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LADD);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitInsn(IADD);
        } else if (bType is bir:BTypeString) {
            if(IS_BSTRING){
                self.mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, "concat",
                                        io:sprintf("(L%s;)L%s;", B_STRING_VALUE, B_STRING_VALUE) , true);
            } else {
                self.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                                        io:sprintf("(L%s;)L%s;", STRING_VALUE, STRING_VALUE) , false);
            }
        } else if (bType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "add",
                io:sprintf("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE) , false);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DADD);
        } else if (bType is bir:BXMLType) {
            self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "concatenate", 
                                    io:sprintf("(L%s;L%s;)L%s;", XML_VALUE, XML_VALUE, XML_VALUE), false);
        } else {
            error err = error("JVM generation is not supported for type " +
                              io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }

        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateSubIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LSUB);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DSUB);
        } else if (bType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "subtract",
                io:sprintf("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE) , false);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateDivIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "divide", "(JJ)J", false);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DDIV);
        } else if (bType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "divide",
                io:sprintf("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE) , false);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateMulIns(bir:BinaryOp binaryIns) {
        bir:BType bType = binaryIns.lhsOp.typeValue;
        self.generateBinaryRhsAndLhsLoad(binaryIns);
        if (bType is bir:BTypeInt) {
            self.mv.visitInsn(LMUL);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitInsn(DMUL);
        } else if (bType is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "multiply",
                io:sprintf("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE) , false);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", binaryIns.lhsOp.typeValue));
            panic err;
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateRemIns(bir:BinaryOp binaryIns) {
            bir:BType bType = binaryIns.lhsOp.typeValue;
            self.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType is bir:BTypeInt) {
                self.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "remainder", "(JJ)J", false);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitInsn(DREM);
            } else if (bType is bir:BTypeDecimal) {
                self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "remainder",
                    io:sprintf("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE) , false);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                io:sprintf("%s", binaryIns.lhsOp.typeValue));
                panic err;
            }
            self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseAndIns(bir:BinaryOp binaryIns) {
        bir:BType opType1 = binaryIns.rhsOp1.typeValue;
        bir:BType opType2 = binaryIns.rhsOp2.typeValue;
        
        if (opType1 is bir:BTypeInt && opType2 is bir:BTypeInt) {
            self.loadVar(binaryIns.rhsOp1.variableDcl);
            self.loadVar(binaryIns.rhsOp2.variableDcl);
            self.mv.visitInsn(LAND);
        } else {
            self.loadVar(binaryIns.rhsOp1.variableDcl);
            generateCheckCastToByte(self.mv, opType1);
            
            self.loadVar(binaryIns.rhsOp2.variableDcl);
            generateCheckCastToByte(self.mv, opType2);
            
            self.mv.visitInsn(IAND);
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseOrIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);

        bir:BType opType = binaryIns.rhsOp1.typeValue;
        if (opType is bir:BTypeInt) {
            self.mv.visitInsn(LOR);
        } else {
            self.mv.visitInsn(IOR);
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseXorIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);

        bir:BType opType = binaryIns.rhsOp1.typeValue;
        if (opType is bir:BTypeInt) {
            self.mv.visitInsn(LXOR);
        } else {
            self.mv.visitInsn(IXOR);
        }
        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseLeftShiftIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);

        bir:BType secondOpType = binaryIns.rhsOp2.typeValue;
        if (secondOpType is bir:BTypeInt) {
            self.mv.visitInsn(L2I);
        }

        bir:BType firstOpType = binaryIns.rhsOp1.typeValue;
        if (firstOpType is bir:BTypeInt) {
            self.mv.visitInsn(LSHL);
        } else {
            self.mv.visitInsn(ISHL);
            self.mv.visitInsn(I2L);
        }

        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseRightShiftIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);

        bir:BType secondOpType = binaryIns.rhsOp2.typeValue;
        if (secondOpType is bir:BTypeInt) {
            self.mv.visitInsn(L2I);
        }

        bir:BType firstOpType = binaryIns.rhsOp1.typeValue;
        if (firstOpType is bir:BTypeInt) {
            self.mv.visitInsn(LSHR);
        } else {
            self.mv.visitInsn(ISHR);
        }

        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function generateBitwiseUnsignedRightShiftIns(bir:BinaryOp binaryIns) {
        self.loadVar(binaryIns.rhsOp1.variableDcl);
        self.loadVar(binaryIns.rhsOp2.variableDcl);

        bir:BType secondOpType = binaryIns.rhsOp2.typeValue;
        if (secondOpType is bir:BTypeInt) {
            self.mv.visitInsn(L2I);
        }

        bir:BType firstOpType = binaryIns.rhsOp1.typeValue;
        if (firstOpType is bir:BTypeInt) {
            self.mv.visitInsn(LUSHR);
        } else {
            self.mv.visitInsn(IUSHR);
        }

        self.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }

    function generateMapNewIns(bir:NewMap mapNewIns, int localVarOffset) {
        bir:BType typeOfMapNewIns = mapNewIns.bType;
        string className = MAP_VALUE_IMPL;

        if (typeOfMapNewIns is bir:BRecordType) {
            var typeRef = mapNewIns.typeRef;
            if (typeRef is bir:TypeRef) {
                className = getTypeValueClassName(typeRef.externalPkg, typeOfMapNewIns.name.value);
            } else {
                className = getTypeValueClassName(self.currentPackage, typeOfMapNewIns.name.value);
            }

            self.mv.visitTypeInsn(NEW, className);
            self.mv.visitInsn(DUP);
            self.mv.visitInsn(DUP);
            if (typeRef is bir:TypeRef) {
                loadExternalOrLocalType(self.mv, typeRef);
            } else {
                loadType(self.mv, mapNewIns.bType);
            }
            self.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", BTYPE), false);

            // Invoke the init-function of this type.
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitInsn(SWAP);
            self.mv.visitMethodInsn(INVOKESTATIC, className, "$init", io:sprintf("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
        } else {
            self.mv.visitTypeInsn(NEW, className);
            self.mv.visitInsn(DUP);
            loadType(self.mv, mapNewIns.bType);
            self.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", BTYPE), false);
        }
        self.storeToVar(mapNewIns.lhsOp.variableDcl);
    }

    function generateTableNewIns(bir:NewTable tableNewIns) {
        self.mv.visitTypeInsn(NEW, TABLE_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, tableNewIns.typeValue);
        self.loadVar(tableNewIns.keyColOp.variableDcl);
        self.loadVar(tableNewIns.dataOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, TABLE_VALUE, "<init>", io:sprintf("(L%s;L%s;L%s;)V", BTYPE,
                ARRAY_VALUE, ARRAY_VALUE), false);
        self.storeToVar(tableNewIns.lhsOp.variableDcl);
    }

    function generateMapStoreIns(bir:FieldAccess mapStoreIns) {
        // visit map_ref
        self.loadVar(mapStoreIns.lhsOp.variableDcl);
        bir:BType varRefType = mapStoreIns.lhsOp.variableDcl.typeValue;

        // visit key_expr
        self.loadVar(mapStoreIns.keyOp.variableDcl);

        // visit value_expr
        bir:BType valueType = mapStoreIns.rhsOp.variableDcl.typeValue;
        self.loadVar(mapStoreIns.rhsOp.variableDcl);
        addBoxInsn(self.mv, valueType);

        if (varRefType is bir:BJSONType) {
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setElement",
                    io:sprintf("(L%s;L%s;L%s;)V", OBJECT, STRING_VALUE, OBJECT), false);
        } else {
            self.mv.visitMethodInsn(INVOKESTATIC, MAP_UTILS, "handleMapStore",
                                        io:sprintf("(L%s;L%s;L%s;)V",
                                        MAP_VALUE, IS_BSTRING ? I_STRING_VALUE : STRING_VALUE, OBJECT), false);
        }
    }

    function generateMapLoadIns(bir:FieldAccess mapLoadIns, boolean useBString) {
        // visit map_ref
        self.loadVar(mapLoadIns.rhsOp.variableDcl);
        bir:BType varRefType = mapLoadIns.rhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, varRefType);

        // visit key_expr
        self.loadVar(mapLoadIns.keyOp.variableDcl);

        if (varRefType is bir:BJSONType) {
            if (mapLoadIns.optionalFieldAccess) {
                self.mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
                self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElementOrNil",
                        io:sprintf("(L%s;L%s;)L%s;", OBJECT, STRING_VALUE, OBJECT), false);
            } else {
                self.mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
                self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElement",
                        io:sprintf("(L%s;L%s;)L%s;", OBJECT, STRING_VALUE, OBJECT), false);
            }
        } else {
            if (mapLoadIns.fillingRead) {
                self.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "fillAndGet",
                                        io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), true);
            } else {
                self.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "get",
                                        io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), true);
            }
        }

        // store in the target reg
        bir:BType targetType = mapLoadIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType, useBString);
        self.storeToVar(mapLoadIns.lhsOp.variableDcl);
    }

    function generateObjectLoadIns(bir:FieldAccess objectLoadIns) {
        // visit object_ref
        self.loadVar(objectLoadIns.rhsOp.variableDcl);
        bir:BType varRefType = objectLoadIns.rhsOp.variableDcl.typeValue;

        // visit key_expr
        self.loadVar(objectLoadIns.keyOp.variableDcl);

        // invoke get() method, and unbox if needed
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "get",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, OBJECT), true);
        bir:BType targetType = objectLoadIns.lhsOp.variableDcl.typeValue;
        addUnboxInsn(self.mv, targetType);

        // store in the target reg
        self.storeToVar(objectLoadIns.lhsOp.variableDcl);
    }

    function generateObjectStoreIns(bir:FieldAccess objectStoreIns, boolean useBString) {
        // visit object_ref
        self.loadVar(objectStoreIns.lhsOp.variableDcl);
        bir:BType varRefType = objectStoreIns.lhsOp.variableDcl.typeValue;

        // visit key_expr
        self.loadVar(objectStoreIns.keyOp.variableDcl);

        // visit value_expr
        bir:BType valueType = objectStoreIns.rhsOp.variableDcl.typeValue;
        self.loadVar(objectStoreIns.rhsOp.variableDcl);
        addBoxInsn(self.mv, valueType);

        // invoke set() method
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "set",
                io:sprintf("(L%s;L%s;)V", useBString ? B_STRING_VALUE : STRING_VALUE, OBJECT), true);
    }

    function generateStringLoadIns(bir:FieldAccess stringLoadIns, boolean useBString) {
        // visit the string
        self.loadVar(stringLoadIns.rhsOp.variableDcl);

        // visit the key expr
        self.loadVar(stringLoadIns.keyOp.variableDcl);

        string consVal = useBString ? B_STRING_VALUE : STRING_VALUE;
        // invoke the `getStringAt()` method
        self.mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "getStringAt",
                                io:sprintf("(L%s;J)L%s;", consVal, consVal), false);

        // store in the target reg
        self.storeToVar(stringLoadIns.lhsOp.variableDcl);
    }

    # Generate a new instance of an array value
    # 
    # + inst - the new array instruction
    function generateArrayNewIns(bir:NewArray inst, boolean useBString) {
        if (inst.typeValue is bir:BArrayType) {
            self.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
            self.mv.visitInsn(DUP);
            loadType(self.mv, inst.typeValue);
            self.loadVar(inst.sizeOp.variableDcl);
            if (useBString) {
                self.mv.visitInsn(ICONST_1);
                self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", io:sprintf("(L%s;JZ)V", ARRAY_TYPE), false);
            } else {
                self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", io:sprintf("(L%s;J)V", ARRAY_TYPE), false);
            }
            self.storeToVar(inst.lhsOp.variableDcl);
        } else {
            self.mv.visitTypeInsn(NEW, TUPLE_VALUE_IMPL);
            self.mv.visitInsn(DUP);
            loadType(self.mv, inst.typeValue);
            self.loadVar(inst.sizeOp.variableDcl);
            self.mv.visitMethodInsn(INVOKESPECIAL, TUPLE_VALUE_IMPL, "<init>", io:sprintf("(L%s;J)V", TUPLE_TYPE), false);
            self.storeToVar(inst.lhsOp.variableDcl);
        }
    }

    # Generate adding a new value to an array
    # 
    # + inst - array store instruction
    function generateArrayStoreIns(bir:FieldAccess inst, boolean useBString) {
        self.loadVar(inst.lhsOp.variableDcl);
        self.loadVar(inst.keyOp.variableDcl);
        self.loadVar(inst.rhsOp.variableDcl);

        bir:BType valueType = inst.rhsOp.variableDcl.typeValue;
        bir:BType varRefType = inst.lhsOp.variableDcl.typeValue;
        if (varRefType is bir:BJSONType ||
                (varRefType is bir:BArrayType && varRefType.eType  is bir:BJSONType)) {
            addBoxInsn(self.mv, valueType);
            self.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setArrayElement",
                    io:sprintf("(L%s;JL%s;)V", OBJECT, OBJECT), false);
            return;
        }

        if (valueType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JJ)V", true);
        } else if (valueType is bir:BTypeFloat) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JD)V", true);
        } else if (valueType is bir:BTypeString) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", io:sprintf("(JL%s;)V", useBString ? 
            B_STRING_VALUE : STRING_VALUE), true);
        } else if (valueType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JZ)V", true);
        } else if (valueType is bir:BTypeByte) {
            self.mv.visitInsn(I2B);
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JB)V", true);
        } else {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", io:sprintf("(JL%s;)V", OBJECT), true);
        }
    }

    # Generating loading a new value from an array to the top of the stack
    # 
    # + inst - field access instruction
    function generateArrayValueLoad(bir:FieldAccess inst, boolean useBString) {
        self.loadVar(inst.rhsOp.variableDcl);
        self.mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
        self.loadVar(inst.keyOp.variableDcl);
        bir:BType bType = inst.lhsOp.variableDcl.typeValue;

        bir:BType varRefType = inst.rhsOp.variableDcl.typeValue;
        if (varRefType is bir:BTupleType) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), true);
            addUnboxInsn(self.mv, bType);
        } else if (bType is bir:BTypeInt) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
        } else if (bType is bir:BTypeString) {
            if (useBString) {
                self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBString", io:sprintf("(J)L%s;", B_STRING_VALUE),
                                        true);
            } else {
                self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getString", io:sprintf("(J)L%s;", STRING_VALUE),
                                        true);   
            }
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
            self.mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
        } else {
            self.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", io:sprintf("(J)L%s;", OBJECT), true);
            string? targetTypeClass = getTargetClass(bType);
            if (targetTypeClass is string) {
                self.mv.visitTypeInsn(CHECKCAST, targetTypeClass);
            } else {
                addUnboxInsn(self.mv, bType);
            }
        }
        self.storeToVar(inst.lhsOp.variableDcl);
    }

    function generateNewErrorIns(bir:NewError newErrorIns, boolean useBString) {
        self.mv.visitTypeInsn(NEW, ERROR_VALUE);
        self.mv.visitInsn(DUP);
        // load errorType
        loadType(self.mv, newErrorIns.typeValue);
        self.loadVar(newErrorIns.reasonOp.variableDcl);
        self.loadVar(newErrorIns.detailsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                           io:sprintf("(L%s;L%s;L%s;)V", BTYPE, useBString ? B_STRING_VALUE : STRING_VALUE, OBJECT), false);
        self.storeToVar(newErrorIns.lhsOp.variableDcl);
    }

    function generateCastIns(bir:TypeCast typeCastIns, boolean useBString) {
        // load source value
        self.loadVar(typeCastIns.rhsOp.variableDcl);
        if (typeCastIns.checkType) {
            generateCheckCast(self.mv, typeCastIns.rhsOp.typeValue, typeCastIns.castType, self.indexMap, useBString);
        } else {
            generateCast(self.mv, typeCastIns.rhsOp.typeValue, typeCastIns.castType);
        }
        self.storeToVar(typeCastIns.lhsOp.variableDcl);
    }

    function generateTypeTestIns(bir:TypeTest typeTestIns) {
        // load source value
        self.loadVar(typeTestIns.rhsOp.variableDcl);

        // load targetType
        loadType(self.mv, typeTestIns.typeValue);

        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                io:sprintf("(L%s;L%s;)Z", OBJECT, BTYPE), false);
        self.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    function generateIsLikeIns(bir:IsLike isLike) {
        // load source value
        self.loadVar(isLike.rhsOp.variableDcl);

        // load targetType
        loadType(self.mv, isLike.typeVal);

        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsLikeType",
            io:sprintf("(L%s;L%s;)Z", OBJECT, BTYPE), false);
        self.storeToVar(isLike.lhsOp.variableDcl);
    }

    function generateObjectNewIns(bir:NewInstance objectNewIns, int strandIndex) {
        var typeDefRef = objectNewIns.typeDefRef;
        bir:TypeDef typeDef = lookupTypeDef(typeDefRef);
        string className;
        if (typeDefRef is bir:TypeRef) {
            className = getTypeValueClassName(typeDefRef.externalPkg, typeDefRef.name.value);
        } else {
            className = getTypeValueClassName(self.currentPackage, typeDefRef.name.value);
        }

        self.mv.visitTypeInsn(NEW, className);
        self.mv.visitInsn(DUP);

        bir:BType typeValue = typeDef.typeValue;
        if (typeValue is bir:BServiceType) {
            // For services, create a new type for each new service value. TODO: do only for local vars
            duplicateServiceTypeWithAnnots(self.mv, typeValue.oType, typeDef, self.currentPackageName, strandIndex);
        } else {
            loadExternalOrLocalType(self.mv, typeDefRef);
        }
        self.mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
        self.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", io:sprintf("(L%s;)V", OBJECT_TYPE), false);
        self.storeToVar(objectNewIns.lhsOp.variableDcl);
    }

    function generateFPLoadIns(bir:FPLoad inst) {
        self.mv.visitTypeInsn(NEW, FUNCTION_POINTER);
        self.mv.visitInsn(DUP);

        string lambdaName = inst.name.value + "$lambda" + lambdaIndex.toString() + "$";
        lambdaIndex += 1;
        string pkgName = getPackageName(inst.pkgID.org, inst.pkgID.name);
        string lookupKey = pkgName + inst.name.value;
        string methodClass = lookupFullQualifiedClassName(lookupKey);

        bir:BType returnType = inst.lhsOp.typeValue;
        if !(returnType is bir:BInvokableType) {
            error err = error( "Expected BInvokableType, found " + io:sprintf("%s", returnType));
            panic err;
        }

        foreach var v in inst.closureMaps {
            if (v is bir:VarRef) {
                self.loadVar(v.variableDcl);
            }
        }

        self.mv.visitInvokeDynamicInsn(currentClass, lambdaName, inst.closureMaps.length());
        loadType(self.mv, returnType);
        self.mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                                io:sprintf("(L%s;L%s;)V", FUNCTION, BTYPE), false);

        // Set annotations if available.
        self.mv.visitInsn(DUP);
        string pkgClassName = pkgName == "." || pkgName == "" ? MODULE_INIT_CLASS_NAME :
                                    lookupGlobalVarClassName(pkgName + ANNOTATION_MAP_NAME);
        self.mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, io:sprintf("L%s;", MAP_VALUE));
        self.mv.visitLdcInsn(inst.name.value);
        self.mv.visitMethodInsn(INVOKESTATIC, io:sprintf("%s", ANNOTATION_UTILS), "processFPValueAnnotations",
            io:sprintf("(L%s;L%s;L%s;)V", FUNCTION_POINTER, MAP_VALUE, STRING_VALUE), false);

        self.storeToVar(inst.lhsOp.variableDcl);
        lambdas[lambdaName] = inst;
    }

    function generateNewXMLElementIns(bir:NewXMLElement newXMLElement, boolean useBString) {
        self.loadVar(newXMLElement.startTagOp.variableDcl);
        self.mv.visitTypeInsn(CHECKCAST, XML_QNAME);
        self.loadVar(newXMLElement.defaultNsURIOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLElement",
                io:sprintf("(L%s;L%s;)L%s;", XML_QNAME, useBString ? B_STRING_VALUE : STRING_VALUE,
                XML_VALUE), false);
        self.storeToVar(newXMLElement.lhsOp.variableDcl);
    }

    function generateNewXMLQNameIns(bir:NewXMLQName newXMLQName, boolean useBString) {
        self.mv.visitTypeInsn(NEW, XML_QNAME);
        self.mv.visitInsn(DUP);
        self.loadVar(newXMLQName.localnameOp.variableDcl);
        self.loadVar(newXMLQName.nsURIOp.variableDcl);
        self.loadVar(newXMLQName.prefixOp.variableDcl);
        string consVal = useBString ? B_STRING_VALUE : STRING_VALUE;
        self.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                io:sprintf("(L%s;L%s;L%s;)V", consVal, consVal, consVal), false);
        self.storeToVar(newXMLQName.lhsOp.variableDcl);
    }

    function generateNewStringXMLQNameIns(bir:NewStringXMLQName newStringXMLQName) {
        self.mv.visitTypeInsn(NEW, XML_QNAME);
        self.mv.visitInsn(DUP);
        self.loadVar(newStringXMLQName.stringQNameOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                io:sprintf("(L%s;)V", STRING_VALUE), false);
        self.storeToVar(newStringXMLQName.lhsOp.variableDcl);
    }

    function generateNewXMLTextIns(bir:NewXMLText newXMLText, boolean useBString) {
        self.loadVar(newXMLText.textOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLText",
                io:sprintf("(L%s;)L%s;", useBString ? B_STRING_VALUE : STRING_VALUE, XML_VALUE), false);
        self.storeToVar(newXMLText.lhsOp.variableDcl);
    }

    function generateNewXMLCommentIns(bir:NewXMLComment newXMLComment, boolean useBString) {
        self.loadVar(newXMLComment.textOp.variableDcl);
        self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLComment",
                io:sprintf("(L%s;)L%s;", useBString ? B_STRING_VALUE : STRING_VALUE, XML_VALUE), false);
        self.storeToVar(newXMLComment.lhsOp.variableDcl);
    }

    function generateNewXMLProcIns(bir:NewXMLPI newXMLPI, boolean useBString) {
        self.loadVar(newXMLPI.targetOp.variableDcl);
        self.loadVar(newXMLPI.dataOp.variableDcl);
          string consVal = useBString ? B_STRING_VALUE : STRING_VALUE;
      self.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLProcessingInstruction",
                io:sprintf("(L%s;L%s;)L%s;", consVal, consVal, XML_VALUE), false);
        self.storeToVar(newXMLPI.lhsOp.variableDcl);
    }

    function generateXMLStoreIns(bir:XMLAccess xmlStoreIns) {
        self.loadVar(xmlStoreIns.lhsOp.variableDcl);
        self.loadVar(xmlStoreIns.rhsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "addChildren", io:sprintf("(L%s;)V", XML_VALUE),
                                        false);
    }

    function generateXMLLoadAllIns(bir:XMLAccess xmlLoadAllIns) {
        self.loadVar(xmlLoadAllIns.rhsOp.variableDcl);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children", io:sprintf("()L%s;", XML_VALUE),
                                        false);
        self.storeToVar(xmlLoadAllIns.lhsOp.variableDcl);
    }

    function generateXMLAttrLoadIns(bir:FieldAccess xmlAttrStoreIns) {
        // visit xml_ref
        self.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

        // visit attribute name expr
        self.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
        self.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

        // invoke getAttribute() method
        self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttribute",
                io:sprintf("(L%s;)L%s;", BXML_QNAME, STRING_VALUE), false);

        // store in the target reg
        bir:BType targetType = xmlAttrStoreIns.lhsOp.variableDcl.typeValue;
        self.storeToVar(xmlAttrStoreIns.lhsOp.variableDcl);
    }

    function generateXMLAttrStoreIns(bir:FieldAccess xmlAttrStoreIns, boolean useBString) {
        // visit xml_ref
        self.loadVar(xmlAttrStoreIns.lhsOp.variableDcl);

        // visit attribute name expr
        self.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
        self.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

        // visit attribute value expr
        self.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

        // invoke setAttribute() method
        self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "setAttribute",
                io:sprintf("(L%s;L%s;)V", BXML_QNAME, useBString ? B_STRING_VALUE : STRING_VALUE), false);
    }

    function generateXMLLoadIns(bir:FieldAccess xmlLoadIns) {
        // visit xml_ref
        self.loadVar(xmlLoadIns.rhsOp.variableDcl);

        // visit element name/index expr
        self.loadVar(xmlLoadIns.keyOp.variableDcl);

        if (xmlLoadIns.keyOp.variableDcl.typeValue is bir:BTypeString) {
            // invoke `children(name)` method
            self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children",
                io:sprintf("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
        } else {
            // invoke `getItem(index)` method
            self.mv.visitInsn(L2I);
            self.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getItem",
                io:sprintf("(I)L%s;", XML_VALUE), false);
        }

        // store in the target reg
        bir:BType targetType = xmlLoadIns.lhsOp.variableDcl.typeValue;
        self.storeToVar(xmlLoadIns.lhsOp.variableDcl);
    }

    function generateTypeofIns(bir:UnaryOp unaryOp) {
        self.loadVar(unaryOp.rhsOp.variableDcl);
        addBoxInsn(self.mv, unaryOp.rhsOp.variableDcl.typeValue);
        self.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getTypedesc",
                io:sprintf("(L%s;)L%s;", OBJECT, TYPEDESC_VALUE), false);
        self.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    function generateNotIns(bir:UnaryOp unaryOp) {
        self.loadVar(unaryOp.rhsOp.variableDcl);

        jvm:Label label1 = new;
        jvm:Label label2 = new;

        self.mv.visitJumpInsn(IFNE, label1);
        self.mv.visitInsn(ICONST_1);
        self.mv.visitJumpInsn(GOTO, label2);
        self.mv.visitLabel(label1);
        self.mv.visitInsn(ICONST_0);
        self.mv.visitLabel(label2);

        self.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    function generateNegateIns(bir:UnaryOp unaryOp) {
        self.loadVar(unaryOp.rhsOp.variableDcl);

        bir:BType btype = unaryOp.rhsOp.variableDcl.typeValue;
        if (btype is bir:BTypeInt) {
            self.mv.visitInsn(LNEG);
        } else if (btype is bir:BTypeByte) {
            self.mv.visitInsn(INEG);
        } else if (btype is bir:BTypeFloat) {
            self.mv.visitInsn(DNEG);
        } else if (btype is bir:BTypeDecimal) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "negate",
                io:sprintf("()L%s;", DECIMAL_VALUE), false);
        } else {
            error err = error(io:sprintf("Negation is not supported for type: %s", btype));
            panic err;
        }

        self.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    function generateNewTypedescIns(bir:NewTypeDesc newTypeDesc) {
        self.mv.visitTypeInsn(NEW, TYPEDESC_VALUE);
        self.mv.visitInsn(DUP);
        loadType(self.mv, newTypeDesc.typeValue);
        self.mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE, "<init>",
                io:sprintf("(L%s;)V", BTYPE), false);
        self.storeToVar(newTypeDesc.lhsOp.variableDcl);
    }

    private function loadVar(bir:VariableDcl varDcl) {
        generateVarLoad(self.mv, varDcl, self.currentPackageName, self.getJVMIndexOfVarRef(varDcl));
    }

    private function storeToVar(bir:VariableDcl varDcl) {
        generateVarStore(self.mv, varDcl, self.currentPackageName, self.getJVMIndexOfVarRef(varDcl));
    }
};

function addBoxInsn(jvm:MethodVisitor mv, bir:BType? bType) {
    if (bType is ()) {
        return;
    } else {
        generateCast(mv, bType, "any");
    }
}

function addUnboxInsn(jvm:MethodVisitor mv, bir:BType? bType, boolean useBString = false) {
    if (bType is ()) {
        return;
    } else {
        generateCast(mv, "any", bType, useBString = useBString);
    }
}

function addJUnboxInsn(jvm:MethodVisitor mv, bir:BType? bType) {
    if (bType is ()) {
        return;
    } else if (bType is jvm:JByte) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJByte", io:sprintf("(L%s;)B", OBJECT), false);
    } else if (bType is jvm:JChar) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJChar", io:sprintf("(L%s;)C", OBJECT), false);
    } else if (bType is jvm:JShort) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJShort", io:sprintf("(L%s;)S", OBJECT), false);
    } else if (bType is jvm:JInt) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJInt", io:sprintf("(L%s;)I", OBJECT), false);
    } else if (bType is jvm:JLong) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", io:sprintf("(L%s;)J", OBJECT), false);
    } else if (bType is jvm:JFloat) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJFloat", io:sprintf("(L%s;)F", OBJECT), false);
    } else if (bType is jvm:JDouble) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", io:sprintf("(L%s;)D", OBJECT), false);
    } else if (bType is jvm:JBoolean) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJBoolean", io:sprintf("(L%s;)Z", OBJECT), false);
    } else if (bType is jvm:JRefType) {
        mv.visitTypeInsn(CHECKCAST, bType.typeValue);
    //} else {
    //    error err = error(io:sprintf("Unboxing is not supported for '%s'", bType));
    //    panic err;
    }
}

function generateVarLoad(jvm:MethodVisitor mv, bir:VariableDcl varDcl, string currentPackageName, int valueIndex) {
    bir:BType bType = varDcl.typeValue;

    if (varDcl.kind == bir:VAR_KIND_GLOBAL) {
        bir:GlobalVariableDcl globalVar = <bir:GlobalVariableDcl> varDcl;
        bir:ModuleID modId = <bir:ModuleID> globalVar?.moduleId;
        string moduleName = getPackageName(modId.org, modId.name);

        string varName = varDcl.name.value;
        string className = lookupGlobalVarClassName(moduleName + varName);

        string typeSig = getTypeDesc(bType);
        mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
        return;
    } else if (varDcl.kind == bir:VAR_KIND_SELF) {
        mv.visitVarInsn(ALOAD, 0);
        return;
    } else if (varDcl.kind == bir:VAR_KIND_CONSTANT) {
        string varName = varDcl.name.value;
        bir:ModuleID moduleId = <bir:ModuleID> varDcl?.moduleId;
        string pkgName = getPackageName(moduleId.org, moduleId.name);
        string className = lookupGlobalVarClassName(pkgName + varName);
        string typeSig = getTypeDesc(bType);
        mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
        return;
    }

    if (bType is bir:BTypeInt) {
        mv.visitVarInsn(LLOAD, valueIndex);
    } else if (bType is bir:BTypeByte) {
        mv.visitVarInsn(ILOAD, valueIndex);
        mv.visitInsn(I2B);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
    } else if (bType is bir:BTypeFloat) {
        mv.visitVarInsn(DLOAD, valueIndex);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (bType is bir:BArrayType ||
                bType is bir:BTypeString ||
                bType is bir:BMapType ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
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
                bType is bir:BServiceType ||
                bType is bir:BTypeDecimal ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeHandle ||
                bType is bir:BTypeDesc) {
        mv.visitVarInsn(ALOAD, valueIndex);
    } else if(bType is jvm:JType) {
        generateJVarLoad(mv, bType, currentPackageName, valueIndex);
    } else {
        error err = error( "JVM generation is not supported for type " +io:sprintf("%s", bType));
        panic err;
    }
}

function generateJVarLoad(jvm:MethodVisitor mv, jvm:JType jType, string currentPackageName, int valueIndex) {

    if (jType is jvm:JByte) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (jType is jvm:JChar) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (jType is jvm:JShort) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (jType is jvm:JInt) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (jType is jvm:JLong) {
        mv.visitVarInsn(LLOAD, valueIndex);
    } else if (jType is jvm:JFloat) {
        mv.visitVarInsn(FLOAD, valueIndex);
    } else if (jType is jvm:JDouble) {
        mv.visitVarInsn(DLOAD, valueIndex);
    } else if (jType is jvm:JBoolean) {
        mv.visitVarInsn(ILOAD, valueIndex);
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        mv.visitVarInsn(ALOAD, valueIndex);
    } else {
        error err = error( "JVM generation is not supported for type " +io:sprintf("%s", jType));
        panic err;
    }
}

function generateVarStore(jvm:MethodVisitor mv, bir:VariableDcl varDcl, string currentPackageName, int valueIndex) {
    bir:BType bType = varDcl.typeValue;

    if (varDcl.kind == bir:VAR_KIND_GLOBAL) {
        string varName = varDcl.name.value;
        string className = lookupGlobalVarClassName(currentPackageName + varName);
        string typeSig = getTypeDesc(bType);
        mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
        return;
    } else if (varDcl.kind == bir:VAR_KIND_CONSTANT) {
        string varName = varDcl.name.value;
        bir:ModuleID moduleId = <bir:ModuleID> varDcl?.moduleId;
        string pkgName = getPackageName(moduleId.org, moduleId.name);
        string className = lookupGlobalVarClassName(pkgName + varName);
        string typeSig = getTypeDesc(bType);
        mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
        return;
    }

    if (bType is bir:BTypeInt) {
        mv.visitVarInsn(LSTORE, valueIndex);
    } else if (bType is bir:BTypeByte) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (bType is bir:BTypeFloat) {
        mv.visitVarInsn(DSTORE, valueIndex);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (bType is bir:BArrayType ||
                    bType is bir:BTypeString ||
                    bType is bir:BMapType ||
                    bType is bir:BTableType ||
                    bType is bir:BStreamType ||
                    bType is bir:BTypeAny ||
                    bType is bir:BTypeAnyData ||
                    bType is bir:BTypeNil ||
                    bType is bir:BUnionType ||
                    bType is bir:BTupleType ||
                    bType is bir:BTypeDecimal ||
                    bType is bir:BRecordType ||
                    bType is bir:BErrorType ||
                    bType is bir:BJSONType ||
                    bType is bir:BFutureType ||
                    bType is bir:BObjectType ||
                    bType is bir:BServiceType ||
                    bType is bir:BXMLType ||
                    bType is bir:BInvokableType ||
                    bType is bir:BFiniteType ||
                    bType is bir:BTypeHandle ||
		    bType is bir:BTypeDesc) {
			    mv.visitVarInsn(ASTORE, valueIndex);
    } else if(bType is jvm:JType) {
        generateJVarStore(mv, bType, currentPackageName, valueIndex);
    } else {
        error err = error("JVM generation is not supported for type " +io:sprintf("%s", bType));
        panic err;
    }
}

function generateJVarStore(jvm:MethodVisitor mv, jvm:JType jType, string currentPackageName, int valueIndex) {
    if (jType is jvm:JByte) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (jType is jvm:JChar) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (jType is jvm:JShort) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (jType is jvm:JInt) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (jType is jvm:JLong) {
        mv.visitVarInsn(LSTORE, valueIndex);
    } else if (jType is jvm:JFloat) {
        mv.visitVarInsn(FSTORE, valueIndex);
    } else if (jType is jvm:JDouble) {
        mv.visitVarInsn(DSTORE, valueIndex);
    } else if (jType is jvm:JBoolean) {
        mv.visitVarInsn(ISTORE, valueIndex);
    } else if (jType is jvm:JArrayType ||
                jType is jvm:JRefType) {
        mv.visitVarInsn(ASTORE, valueIndex);
    } else {
        error err = error("JVM generation is not supported for type " +io:sprintf("%s", jType));
        panic err;
    }
}

function listHighSurrogates(string str) returns int[]  = external;
