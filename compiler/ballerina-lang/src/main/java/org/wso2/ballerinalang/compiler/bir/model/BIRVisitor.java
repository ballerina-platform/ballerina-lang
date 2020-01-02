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

/**
 * A BIR node visitor.
 *
 * @since 0.980.0
 */
public abstract class BIRVisitor {

    public void visit(BIRNode.BIRPackage birPackage) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRImportModule birImportModule) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRVariableDcl birVariableDcl) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRFunctionParameter birFunctionParameter) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRFunction birFunction) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRParameter birParameter) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRAnnotation birAnnotation) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRConstant birConstant) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRAnnotationAttachment birAnnotAttach) {
        throw new AssertionError();
    }

    public void visit(BIRNode.BIRErrorEntry birErrorEntry) {
        throw new AssertionError();
    }

    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Call birCall) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.AsyncCall birCall) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Return birReturn) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.Branch birBranch) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.FPCall fpCall) {
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

    public void visit(BIRTerminator.Panic birPanic) {
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


    // Non-terminating instructions

    public void visit(BIRNonTerminator.Move birMove) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewStructure birNewStructure) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewArray birNewArray) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.TypeCast birTypeCast) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.IsLike birIsLike) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.TypeTest birTypeTest) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewTable newTable) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewStream newStream) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewStringXMLQName newStringXMLQName) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewXMLProcIns newXMLProcIns) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewXMLComment newXMLComment) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.XMLAccess xmlAccess) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewXMLText newXMLText) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewXMLQName newXMLQName) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.NewXMLElement newXMLElement) {
        throw new AssertionError();
    }

    // Operands
    public void visit(BIROperand birVarRef) {
        throw new AssertionError();
    }
}
