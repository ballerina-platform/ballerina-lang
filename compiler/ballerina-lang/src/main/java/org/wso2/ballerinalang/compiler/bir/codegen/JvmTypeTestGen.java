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

import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAIN_ARG_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CHECK_IS_TYPE;

/**
 * BIR Type checking instructions to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmTypeTestGen {
    private final JvmInstructionGen jvmInstructionGen;
    private final Types types;
    private final MethodVisitor mv;
    private final JvmTypeGen jvmTypeGen;
    private final JvmCastGen jvmCastGen;

    public JvmTypeTestGen(JvmInstructionGen jvmInstructionGen, Types types, MethodVisitor mv, JvmTypeGen jvmTypeGen,
                          JvmCastGen jvmCastGen) {
        this.jvmInstructionGen = jvmInstructionGen;
        this.types = types;
        this.mv = mv;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmCastGen = jvmCastGen;
    }

    void generateTypeTestIns(BIRNonTerminator.TypeTest typeTestIns) {
        var sourceValue = typeTestIns.rhsOp.variableDcl;
        BType sourceType = sourceValue.type;
        BType targetType = typeTestIns.type;
        // Optimization is done by avoiding the call to the TypeChecker and instead generating instructions with the
        // instanceof operator.
        if (canOptimizeNilCheck(sourceType, targetType) ||
                canOptimizeNilUnionCheck(sourceType, targetType) ||
                sourceValue.name.value.startsWith(MAIN_ARG_VAR_PREFIX)) {
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
                                CHECK_IS_TYPE, false);
        jvmInstructionGen.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    /**
     * Checks if the type tested for is nil. That is the target type is nil. Example instructions include 'a is ()'
     * where 'a' is a variable of type say any or a union with nil.
     *
     * @param sourceType the declared variable type
     * @param targetType the RHS type in the type check instruction. Type to be tested for
     * @return whether instruction could be optimized using 'instanceof` check
     */
    private boolean canOptimizeNilCheck(BType sourceType, BType targetType) {
        return JvmCodeGenUtil.getImpliedType(targetType).tag == TypeTags.NIL &&
                types.isAssignable(targetType, sourceType);
    }

    /**
     * This checks for any variable declaration containing a nil in a union of two types. Examples include string? or
     * error? or int?.
     *
     * @param sourceType the declared variable type
     * @param targetType the RHS type in the type check instruction. Type to be tested for
     * @return whether instruction could be optimized using 'instanceof` check for null
     */
    private boolean canOptimizeNilUnionCheck(BType sourceType, BType targetType) {
        sourceType = JvmCodeGenUtil.getImpliedType(sourceType);
        if (isInValidUnionType(sourceType)) {
            return false;
        }
        boolean foundNil = false;
        SemType otherTy = null;
        for (SemType s : ((BUnionType) sourceType).getMemberSemTypes()) {
            if (PredefinedType.NIL.equals(s)) {
                foundNil = true;
            } else {
                otherTy = s;
            }
        }
        return foundNil && SemTypes.isSameType(types.typeCtx(), otherTy, targetType.semType());
    }

    /**
     * Checks if we have <code>x is E</code> where <code>E</code> is a subtype of <code>error</code> and error part
     * of <code>x</code> is a subtype of <code>E</code>.
     * <br>
     * In that case we can simplify <code>is</code>-check to a <code>x instanceof BError</code> check.
     *
     * @param sourceType the declared variable type
     * @param targetType the RHS type in the type check instruction. Type to be tested for
     * @return whether instruction could be optimized using 'instanceof` check for BError
     */
    private boolean canOptimizeErrorCheck(BType sourceType, BType targetType) {
        SemType targetTy = targetType.semType();
        if (!Core.isSubtypeSimple(targetTy, PredefinedType.ERROR)) {
            return false;
        }

        SemType errIntersect = SemTypes.intersect(sourceType.semType(), PredefinedType.ERROR);
        if (Core.isNever(errIntersect)) {
            return false;
        }
        return SemTypes.isSubtype(types.typeCtx(), errIntersect, targetTy);
    }

    /**
     * Checks if we have <code>x is T</code> in which <code>x</code>'s static type is <code>T'=T|E</code> where
     * <code>E</code> is a non-empty error type.
     * <br>
     * In that case we can simplify <code>is</code>-check to a <code>!(x instanceof BError)</code> check.
     *
     * @param sourceType the declared variable type
     * @param targetType the RHS type in the type check instruction. Type to be tested for
     * @return whether instruction could be optimized using 'instanceof` check for BError
     */
    private boolean canOptimizeErrorUnionCheck(BType sourceType, BType targetType) {
        SemType sourceTy = sourceType.semType();
        if (!SemTypes.containsBasicType(sourceTy, PredefinedType.ERROR)) {
            return false;
        }

        SemType tyButError = Core.diff(sourceTy, PredefinedType.ERROR);
        if (Core.isNever(tyButError)) {
            return false;
        }
        return SemTypes.isSameType(types.typeCtx(), tyButError, targetType.semType());
    }

    private boolean isInValidUnionType(BType rhsType) {
        if (rhsType.tag != TypeTags.UNION) {
            return true;
        }
        return ((BUnionType) rhsType).getMemberSemTypes().size() != 2;
    }

    private void handleNilUnionType(BIRNonTerminator.TypeTest typeTestIns) {
        jvmInstructionGen.loadVar(typeTestIns.rhsOp.variableDcl);
        jvmCastGen.addBoxInsn(this.mv, typeTestIns.rhsOp.variableDcl.type);
        Label ifLabel = new Label();
        if (JvmCodeGenUtil.getImpliedType(typeTestIns.type).tag == TypeTags.NIL) {
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
        if (!Core.isSubtypeSimple(typeTestIns.type.semType(), PredefinedType.ERROR)) {
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
