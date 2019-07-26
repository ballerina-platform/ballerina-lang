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

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotation;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRConstant;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FieldAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.IsLike;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewError;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStringXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewTypeDesc;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLComment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLElement;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLProcIns;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLText;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeCast;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeTest;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.AsyncCall;
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

    public void visit(BIRNode.BIRImportModule birImportModule) {
        throw new AssertionError();
    }

    public void visit(BIRVariableDcl birVariableDcl) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRFunctionParameter birFunctionParameter) {
        throw new AssertionError();
    }

    public void visit(BIRFunction birFunction) {
        throw new AssertionError();
    }

    public void visit(BIRBasicBlock birBasicBlock) {
        throw new AssertionError();
    }

    public void visit(BIRParameter birParameter) {
        throw new AssertionError();
    }

    public void visit(BIRAnnotation birAnnotation) {
        throw new AssertionError();
    }

    public void visit(BIRConstant birConstant) {
        throw new AssertionError();
    }

    public void visit(BIRAnnotationAttachment birAnnotAttach) {
        throw new AssertionError();
    }


    // Terminating instructions

    public void visit(GOTO birGoto) {
        throw new AssertionError();
    }

    public void visit(Call birCall) {
        throw new AssertionError();
    }

    public void visit(AsyncCall birCall) {
        throw new AssertionError();
    }

    public void visit(Return birReturn) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Branch birBranch) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.FPCall fpCall) {
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

    public void visit(NewStructure birNewStructure) {
        throw new AssertionError();
    }

    public void visit(NewArray birNewArray) {
        throw new AssertionError();
    }

    public void visit(FieldAccess birFieldAccess) {
        throw new AssertionError();
    }

    public void visit(NewError birNewError) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Panic birPanic) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRErrorEntry birErrorEntry) {
        throw new AssertionError();
    }

    public void visit(TypeCast birTypeCast) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        throw new AssertionError();
    }

    public void visit(IsLike birIsLike) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Wait birWait) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.WaitAll waitAll) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Flush birFlush) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.WorkerReceive workerReceive) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.WorkerSend workerSend) {
        throw new AssertionError();
    }

    public void visit(TypeTest birTypeTest) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewTable newTable) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewStream newStream) {
        throw new AssertionError();
    }

    // Operands
    public void visit(BIROperand birVarRef) {
        throw new AssertionError();
    }

    public void visit(NewXMLElement newXMLElement) {
        throw new AssertionError();
    }

    public void visit(NewXMLQName newXMLQName) {
        throw new AssertionError();
    }

    public void visit(NewXMLText newXMLText) {
        throw new AssertionError();
    }

    public void visit(XMLAccess xmlAccess) {
        throw new AssertionError();
    }

    public void visit(NewXMLComment newXMLComment) {
        throw new AssertionError();
    }

    public void visit(NewXMLProcIns newXMLProcIns) {
        throw new AssertionError();
    }

    public void visit(NewStringXMLQName newStringXMLQName) {
        throw new AssertionError();
    }

    public void visit(NewTypeDesc newTypeDesc) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Lock lock) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.FieldLock lock) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Unlock unlock) {
        throw new AssertionError();
    }
}
