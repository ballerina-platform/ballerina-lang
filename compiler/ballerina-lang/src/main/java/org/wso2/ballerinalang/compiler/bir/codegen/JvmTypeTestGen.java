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

package org.wso2.ballerinalang.compiler.bir.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;

/**
 * BIR function to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmTypeTestGen {
    private final JvmInstructionGen jvmInstructionGen;
    private final Types types;
    private final MethodVisitor mv;
    private final JvmTypeGen jvmTypeGen;

    public JvmTypeTestGen(JvmInstructionGen jvmInstructionGen, CompilerContext compilerContext, MethodVisitor mv,
                          JvmTypeGen jvmTypeGen) {
        this.jvmInstructionGen = jvmInstructionGen;
        types = Types.getInstance(compilerContext);
        this.mv = mv;
        this.jvmTypeGen = jvmTypeGen;
    }

    void generateTypeTestIns(BIRNonTerminator.TypeTest typeTestIns) {
        var sourceValue = typeTestIns.rhsOp.variableDcl;
        BType sourceType = sourceValue.type;
        BType targetType = typeTestIns.type;
        if (canOptimizeNilCheck(sourceType, targetType) ||
                canOptimizeNilUnionCheck(sourceType, targetType)) {
            handleNilUnionType(typeTestIns);
            return;
        }
        if (canOptimizeErrorCheck(sourceType, targetType) ||
                canOptimizeErrorUnionCheck(sourceType, targetType)) {
            handleErrorUnionType(typeTestIns);
            return;
        }
        jvmInstructionGen.loadVar(sourceValue);
        jvmTypeGen.loadType(this.mv, targetType);

        this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                                String.format("(L%s;L%s;)Z", OBJECT, TYPE), false);
        jvmInstructionGen.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    private boolean canOptimizeNilCheck(BType sourceType, BType targetType) {
        return targetType.tag == TypeTags.NIL && types.isAssignable(targetType, sourceType);
    }

    private boolean canOptimizeNilUnionCheck(BType sourceType, BType type) {
        if (isInValidUnionType(sourceType)) {
            return false;
        }
        boolean foundNil = false;
        BType otherType = null;
        for (BType bType : ((BUnionType) sourceType).getMemberTypes()) {
            if (bType.tag == TypeTags.NIL) {
                foundNil = true;
            } else {
                otherType = bType;
            }
        }
        return foundNil && type.equals(otherType);
    }

    private boolean canOptimizeErrorCheck(BType sourceType, BType targetType) {
        if (targetType.tag != TypeTags.ERROR || sourceType.tag != TypeTags.UNION) {
            return false;
        }
        BType errorType = null;
        int foundError = 0;
        for (BType bType : ((BUnionType) sourceType).getMemberTypes()) {
            if (bType.tag == TypeTags.ERROR) {
                foundError++;
                errorType = bType;
            }
        }
        return (foundError == 1 && types.isAssignable(errorType, targetType)) || (foundError > 0 && "error".equals(
                targetType.tsymbol.name.value));
    }

    private boolean canOptimizeErrorUnionCheck(BType sourceType, BType targetType) {
        if (isInValidUnionType(sourceType)) {
            return false;
        }
        BType otherType = null;
        int foundError = 0;
        for (BType bType : ((BUnionType) sourceType).getMemberTypes()) {
            if (bType.tag == TypeTags.ERROR) {
                foundError++;
            } else {
                otherType = bType;
            }
        }
        return foundError == 1 && targetType.equals(otherType);
    }

    private boolean isInValidUnionType(BType rhsType) {
        if (rhsType.tag != TypeTags.UNION) {
            return true;
        }
        return ((BUnionType) rhsType).getMemberTypes().size() != 2;
    }

    private void handleNilUnionType(BIRNonTerminator.TypeTest typeTestIns) {
        jvmInstructionGen.loadVar(typeTestIns.rhsOp.variableDcl);
        Label ifLabel = new Label();
        if (typeTestIns.type.tag == TypeTags.NIL) {
            mv.visitJumpInsn(IFNONNULL, ifLabel);
        } else {
            mv.visitJumpInsn(IFNULL, ifLabel);
        }
        loadBoolean(ifLabel);
        jvmInstructionGen.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    private void loadBoolean(Label ifLabel) {
        mv.visitInsn(ICONST_1);
        Label gotoLabel = new Label();
        mv.visitJumpInsn(GOTO, gotoLabel);
        mv.visitLabel(ifLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(gotoLabel);
    }

    private void handleErrorUnionType(BIRNonTerminator.TypeTest typeTestIns) {
        jvmInstructionGen.loadVar(typeTestIns.rhsOp.variableDcl);
        mv.visitTypeInsn(INSTANCEOF, BERROR);
        if (typeTestIns.type.tag != TypeTags.ERROR) {
            generateNegateBoolean();
        }
        jvmInstructionGen.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    private void generateNegateBoolean() {
        Label ifLabel = new Label();
        mv.visitJumpInsn(IFNE, ifLabel);
        loadBoolean(ifLabel);
    }
}
