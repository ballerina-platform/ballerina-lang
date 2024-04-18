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

import org.wso2.ballerinalang.compiler.bir.codegen.interop.JLargeArrayInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JLargeMapInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JMethodCallInstruction;

/**
 * A BIR node visitor.
 *
 * @since 0.980.0
 */
public abstract class BIRVisitor {

    public void visit(BIRNode.BIRPackage birPackage) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRImportModule birImportModule) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRVariableDcl birVariableDcl) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRFunctionParameter birFunctionParameter) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRFunction birFunction) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRParameter birParameter) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRAnnotation birAnnotation) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRConstant birConstant) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRAnnotationAttachment birAnnotAttach) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRConstAnnotationAttachment birConstAnnotAttach) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRErrorEntry birErrorEntry) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNode.BIRServiceDeclaration birServiceDecl) {
        throw new UnsupportedOperationException();
    }

    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Call birCall) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.AsyncCall birCall) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Return birReturn) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Branch birBranch) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.FPCall fpCall) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Lock lock) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.FieldLock lock) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Unlock unlock) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Panic birPanic) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Wait birWait) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.WaitAll waitAll) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.Flush birFlush) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.WorkerReceive workerReceive) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.WorkerSend workerSend) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRTerminator.WorkerAlternateReceive altReceive) {
        throw new AssertionError();
    }

    public void visit(BIRTerminator.WorkerMultipleReceive multipleReceive) {
        throw new AssertionError();
    }

    // Non-terminating instructions

    public void visit(BIRNonTerminator.Move birMove) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewStructure birNewStructure) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewArray birNewArray) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.TypeCast birTypeCast) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.IsLike birIsLike) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.TypeTest birTypeTest) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewTable newTable) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewStringXMLQName newStringXMLQName) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLProcIns newXMLProcIns) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLComment newXMLComment) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.XMLAccess xmlAccess) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLText newXMLText) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLSequence newXMLSequence) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLQName newXMLQName) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewXMLElement newXMLElement) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewRegExp newRegExp) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReDisjunction reDisjunction) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReSequence reSequence) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReAssertion reAssertion) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReAtomQuantifier reAtomQuantifier) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReLiteralCharOrEscape reLiteralCharOrEscape) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReCharacterClass reCharacterClass) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReCharSet reCharSet) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReCharSetRange reCharSetRange) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReCapturingGroup reCapturingGroup) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReFlagExpression reFlagExpression) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReFlagOnOff reFlagOnOff) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.NewReQuantifier reQuantifier) {
        throw new UnsupportedOperationException();
    }

    public void visit(BIRNonTerminator.RecordDefaultFPLoad recordDefaultFPLoad) {
        throw new UnsupportedOperationException();
    }

    // Operands
    public void visit(BIROperand birVarRef) {
        throw new UnsupportedOperationException();
    }

    public void visit(JMethodCallInstruction methodCallInstruction) {
        throw new UnsupportedOperationException();
    }

    public void visit(JLargeMapInstruction largeMapInstruction) {
        throw new UnsupportedOperationException();
    }

    public void visit(JLargeArrayInstruction jLargeArrayInstruction) {
        throw new UnsupportedOperationException();
    }
}
