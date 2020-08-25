/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.optimizer;

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;

/**
 * Util functions used in the BIR optimization related classes.
 */
public class BirOptimizerUtil {
    static BIROperand getDef(BIRAbstractInstruction instruction) {
        BIROperand def = null;
        if (!(instruction.lhsOp == null || instruction instanceof BIRNonTerminator.FieldAccess ||
                instruction instanceof BIRNonTerminator.XMLAccess || instruction instanceof BIRTerminator.WaitAll)) {
            BIROperand lhsOp = instruction.lhsOp;
            if (lhsOp.variableDcl.kind != VarKind.GLOBAL) {
                def = lhsOp;
            }
        }
        return def;
    }

    static BIROperand[] getUse(BIRAbstractInstruction instruction) {
        BIROperand[] rhsOperands = instruction.getRhsOperands();
        if (instruction instanceof BIRNonTerminator.FieldAccess || instruction instanceof BIRNonTerminator.XMLAccess ||
                instruction instanceof BIRTerminator.WaitAll) {
            BIROperand[] operands = new BIROperand[rhsOperands.length + 1];
            System.arraycopy(rhsOperands, 0, operands, 0, rhsOperands.length);
            operands[rhsOperands.length] = instruction.lhsOp;
            return operands;
        }
        return rhsOperands;
    }

    private BirOptimizerUtil() {
    }
}
