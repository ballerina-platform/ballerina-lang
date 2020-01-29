/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import static org.ballerinalang.jvm.util.BLangConstants.ANON_ORG;
import static org.ballerinalang.jvm.util.BLangConstants.DOT;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.jvm.util.BLangConstants.UNDERSCORE;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator extends BIRVisitor {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    private CodeGenerator(CompilerContext context) {

        context.put(CODE_GEN, this);
    }

    public void generate(BIRNode.BIRPackage birPackage) {

        birPackage.accept(this);
    }

    @Override
    public void visit(BIRNode.BIRPackage birPackage) {

        String orgName = birPackage.org.value;
        String moduleName = birPackage.name.value;
        String pkgName = getPackageName(orgName, moduleName);

        // TODO
    }

    private String getPackageName(String orgName, String moduleName) {

        String packageName = "";
        if (!DOT.equals(moduleName)) {
            packageName = cleanupName(moduleName) + ORG_NAME_SEPARATOR;
        }

        if (!ANON_ORG.equalsIgnoreCase(orgName)) {
            packageName = cleanupName(orgName) + ORG_NAME_SEPARATOR + packageName;
        }

        return packageName;
    }

    private String cleanupName(String name) {

        return name.replace(DOT, UNDERSCORE);
    }

    @Override
    public void visit(BIRNode.BIRFunction birFunction) {

        birFunction.basicBlocks.forEach(bb -> bb.accept(this));
    }

    @Override
    public void visit(BIRNode.BIRImportModule birImportModule) {

        super.visit(birImportModule);
    }

    @Override
    public void visit(BIRNode.BIRTypeDefinition birTypeDefinition) {

        super.visit(birTypeDefinition);
    }

    @Override
    public void visit(BIRNode.BIRVariableDcl birVariableDcl) {

        super.visit(birVariableDcl);
    }

    @Override
    public void visit(BIRNode.BIRFunctionParameter birFunctionParameter) {

        super.visit(birFunctionParameter);
    }

    @Override
    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {

        super.visit(birBasicBlock);
    }

    @Override
    public void visit(BIRNode.BIRParameter birParameter) {

        super.visit(birParameter);
    }

    @Override
    public void visit(BIRNode.BIRAnnotation birAnnotation) {

        super.visit(birAnnotation);
    }

    @Override
    public void visit(BIRNode.BIRConstant birConstant) {

        super.visit(birConstant);
    }

    @Override
    public void visit(BIRNode.BIRAnnotationAttachment birAnnotAttach) {

        super.visit(birAnnotAttach);
    }

    @Override
    public void visit(BIRNode.BIRErrorEntry birErrorEntry) {

        super.visit(birErrorEntry);
    }

    @Override
    public void visit(BIRTerminator.GOTO birGoto) {

        super.visit(birGoto);
    }

    @Override
    public void visit(BIRTerminator.Call birCall) {

        super.visit(birCall);
    }

    @Override
    public void visit(BIRTerminator.AsyncCall birCall) {

        super.visit(birCall);
    }

    @Override
    public void visit(BIRTerminator.Return birReturn) {

        super.visit(birReturn);
    }

    @Override
    public void visit(BIRTerminator.Branch birBranch) {

        super.visit(birBranch);
    }

    @Override
    public void visit(BIRTerminator.FPCall fpCall) {

        super.visit(fpCall);
    }

    @Override
    public void visit(BIRTerminator.Lock lock) {

        super.visit(lock);
    }

    @Override
    public void visit(BIRTerminator.FieldLock lock) {

        super.visit(lock);
    }

    @Override
    public void visit(BIRTerminator.Unlock unlock) {

        super.visit(unlock);
    }

    @Override
    public void visit(BIRTerminator.Panic birPanic) {

        super.visit(birPanic);
    }

    @Override
    public void visit(BIRTerminator.Wait birWait) {

        super.visit(birWait);
    }

    @Override
    public void visit(BIRTerminator.WaitAll waitAll) {

        super.visit(waitAll);
    }

    @Override
    public void visit(BIRTerminator.Flush birFlush) {

        super.visit(birFlush);
    }

    @Override
    public void visit(BIRTerminator.WorkerReceive workerReceive) {

        super.visit(workerReceive);
    }

    @Override
    public void visit(BIRTerminator.WorkerSend workerSend) {

        super.visit(workerSend);
    }

    @Override
    public void visit(BIRNonTerminator.Move birMove) {

        super.visit(birMove);
    }

    @Override
    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {

        super.visit(birBinaryOp);
    }

    @Override
    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {

        super.visit(birUnaryOp);
    }

    @Override
    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {

        super.visit(birConstantLoad);
    }

    @Override
    public void visit(BIRNonTerminator.NewStructure birNewStructure) {

        super.visit(birNewStructure);
    }

    @Override
    public void visit(BIRNonTerminator.NewArray birNewArray) {

        super.visit(birNewArray);
    }

    @Override
    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {

        super.visit(birFieldAccess);
    }

    @Override
    public void visit(BIRNonTerminator.NewError birNewError) {

        super.visit(birNewError);
    }

    @Override
    public void visit(BIRNonTerminator.FPLoad fpLoad) {

        super.visit(fpLoad);
    }

    @Override
    public void visit(BIRNonTerminator.TypeCast birTypeCast) {

        super.visit(birTypeCast);
    }

    @Override
    public void visit(BIRNonTerminator.NewInstance newInstance) {

        super.visit(newInstance);
    }

    @Override
    public void visit(BIRNonTerminator.IsLike birIsLike) {

        super.visit(birIsLike);
    }

    @Override
    public void visit(BIRNonTerminator.TypeTest birTypeTest) {

        super.visit(birTypeTest);
    }

    @Override
    public void visit(BIRNonTerminator.NewTable newTable) {

        super.visit(newTable);
    }

    @Override
    public void visit(BIRNonTerminator.NewTypeDesc newTypeDesc) {

        super.visit(newTypeDesc);
    }

    @Override
    public void visit(BIRNonTerminator.NewStringXMLQName newStringXMLQName) {

        super.visit(newStringXMLQName);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLProcIns newXMLProcIns) {

        super.visit(newXMLProcIns);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLComment newXMLComment) {

        super.visit(newXMLComment);
    }

    @Override
    public void visit(BIRNonTerminator.XMLAccess xmlAccess) {

        super.visit(xmlAccess);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLText newXMLText) {

        super.visit(newXMLText);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLQName newXMLQName) {

        super.visit(newXMLQName);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLElement newXMLElement) {

        super.visit(newXMLElement);
    }

    @Override
    public void visit(BIROperand birVarRef) {

        super.visit(birVarRef);
    }
}
