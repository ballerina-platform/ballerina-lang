/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.model;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRConstant;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRVarRef;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Call;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.GOTO;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;

/**
 * A BIR node visitor.
 *
 * @since 0.980.0
 */
public abstract class BIRVisitor {

    public void visit(BIRPackage birPackage) {
        throw new AssertionError();
    }

    public void visit(BIRVariableDcl birVariableDcl) {
        throw new AssertionError();
    }

    public void visit(BIRFunction birFunction) {
        throw new AssertionError();
    }

    public void visit(BIRBasicBlock birBasicBlock) {
        throw new AssertionError();
    }


    // Terminating instructions
    public void visit(GOTO birGoto) {
        throw new AssertionError();
    }

    public void visit(Call birCall) {
        throw new AssertionError();
    }

    public void visit(Return birReturn) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Branch birBranch) {
        throw new AssertionError();
    }

    // Non-terminating instructions
    public void visit(Move birMove) {
        throw new AssertionError();
    }

    public void visit(BinaryOp birBinaryOp) {
        throw new AssertionError();
    }

    public void visit(UnaryOP birUnaryOp) {
        throw new AssertionError();
    }

    public void visit(ConstantLoad birConstantLoad) {
        throw new AssertionError();
    }

    // Operands
    public void visit(BIRVarRef birVarRef) {
        throw new AssertionError();
    }

    public void visit(BIRConstant birConstant) {
        throw new AssertionError();
    }
}
