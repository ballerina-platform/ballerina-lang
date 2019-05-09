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
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FieldAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.IsLike;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
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
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * This class emits the text version of the BIR.
 *
 * TODO improve implementation of this class. This is a WIP implementation.
 * TODO this implementation is just a quick hack to see the generated BIR.
 * TODO Explore the possibility of using a templating engine here.
 *
 * @since 0.980.0
 */
public class BIREmitter extends BIRVisitor {

    private StringBuilder sb = new StringBuilder();

    public String emit(BIRNode birNode) {
        birNode.accept(this);
        String value = sb.toString();
        sb = new StringBuilder();
        return value;
    }

    public void visit(BIRNode.BIRPackage birPackage) {
        sb.append("module ").append(birPackage.name).append(";").append("\n");
        sb.append("fileName :").append(birPackage.sourceFileName).append("\n\n");
        birPackage.importModules.forEach(birImpModule -> birImpModule.accept(this));
        sb.append("\n");
        birPackage.functions.forEach(birFunction -> birFunction.accept(this));
    }

    public void visit(BIRNode.BIRImportModule birImpModule) {
        sb.append("import ").append(birImpModule.org).append("/");
        sb.append(birImpModule.name).append(":").append(birImpModule.version).append(";");
        sb.append("\n");
    }

    public void visit(BIRNode.BIRVariableDcl birVariableDcl) {
        sb.append("\t\t").append(birVariableDcl.type).append(" ").append(birVariableDcl.name).append(";\t\t// ");
        sb.append(birVariableDcl.kind.name().toLowerCase(Locale.ENGLISH)).append("\n");
    }

    public void visit(BIRNode.BIRFunction birFunction) {
        writePosition(birFunction.pos);
        sb.append(" ");
        sb.append(" function ").append(birFunction.name).append("(");
        StringJoiner sj = new StringJoiner(",");
        birFunction.type.paramTypes.forEach(paramType -> sj.add(paramType.toString()));
        sb.append(sj.toString()).append(")").append(" -> ").append(birFunction.type.retType);
        sb.append(" {");
        sb.append("\n");

        birFunction.localVars.forEach(birVariableDcl -> birVariableDcl.accept(this));
        sb.append("\n");
        birFunction.basicBlocks.forEach(birBasicBlock -> birBasicBlock.accept(this));
        if (!birFunction.errorTable.isEmpty()) {
            sb.append("\t\tError Table \n\t\t\tBB\t|errorOp\n");
            birFunction.errorTable.forEach(entry -> {
                entry.accept(this);
            });
        }
        sb.append("}\n\n");
    }

    public void visit(BIRNode.BIRErrorEntry errorEntry) {
        sb.append("\t\t\t").append(errorEntry.trapBB.id).append("\t|");
        errorEntry.errorOp.accept(this);
        sb.append("\n");
    }

    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        sb.append("\t\t");
        sb.append(birBasicBlock.id).append(" {\n");
        birBasicBlock.instructions.forEach(instruction -> ((BIRNode) instruction).accept(this));
        if (birBasicBlock.terminator == null) {
            throw new BLangCompilerException("Basic block without a terminator : " + birBasicBlock.id);
        }
        birBasicBlock.terminator.accept(this);
        sb.append("\t\t}\n\n");
    }

    public void visit(BIRTerminator.Call birCall) {
        writePosition(birCall.pos);
        sb.append("\t\t");
        if (birCall.lhsOp != null) {
            birCall.lhsOp.accept(this);
            sb.append(" = ");
        }
        sb.append(birCall.name.getValue()).append("(");
        List<BIROperand> args = birCall.args;
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            BIROperand arg = args.get(i);
            arg.accept(this);
        }
        sb.append(") -> ");
        sb.append(birCall.thenBB.id);

        sb.append(";\n");
    }

    public void visit(BIRTerminator.AsyncCall birAsyncCall) {
        writePosition(birAsyncCall.pos);
        sb.append("\t\t");
        if (birAsyncCall.lhsOp != null) {
            birAsyncCall.lhsOp.accept(this);
            sb.append(" = ");
        }
        sb.append(birAsyncCall.name.getValue()).append("(");
        List<BIROperand> args = birAsyncCall.args;
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            BIROperand arg = args.get(i);
            arg.accept(this);
        }
        sb.append(") ->> ");
        sb.append(birAsyncCall.thenBB.id);

        sb.append(";\n");
    }

    // Non-terminating instructions
    public void visit(BIRNonTerminator.Move birMove) {
        writePosition(birMove.pos);
        sb.append("\t\t");
        birMove.lhsOp.accept(this);
        sb.append(" = ");
        birMove.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        writePosition(birBinaryOp.pos);
        sb.append("\t\t");
        birBinaryOp.lhsOp.accept(this);
        sb.append(" = ").append(birBinaryOp.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birBinaryOp.rhsOp1.accept(this);
        sb.append(" ");
        birBinaryOp.rhsOp2.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        writePosition(birUnaryOp.pos);
        sb.append("\t\t");
        birUnaryOp.lhsOp.accept(this);
        sb.append(" = ").append(birUnaryOp.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birUnaryOp.rhsOp.accept(this);
        sb.append(" ");
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        writePosition(birConstantLoad.pos);
        sb.append("\t\t");
        birConstantLoad.lhsOp.accept(this);
        sb.append(" = ").append(birConstantLoad.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        sb.append(birConstantLoad.value).append(";\n");
    }

    public void visit(NewStructure birNewStructure) {
        writePosition(birNewStructure.pos);
        sb.append("\t\t");
        birNewStructure.lhsOp.accept(this);
        sb.append(" = ").append(birNewStructure.kind.name().toLowerCase(Locale.ENGLISH)).append(";\n");
    }

    public void visit(NewArray birNewArray) {
        writePosition(birNewArray.pos);
        sb.append("\t\t");
        birNewArray.lhsOp.accept(this);
        sb.append(" = ").append(birNewArray.kind.name().toLowerCase(Locale.ENGLISH)).append(" [");
        birNewArray.sizeOp.accept(this);
        sb.append("];\n");
    }

    @Override
    public void visit(BIRNonTerminator.NewTable newTable) {
        writePosition(newTable.pos);
        sb.append("\t\t");
        newTable.lhsOp.accept(this);
        sb.append(" = ").append(newTable.kind.name().toLowerCase(Locale.ENGLISH)).append(" [");
        newTable.columnsOp.accept(this);
        sb.append(", ");
        newTable.indexColOp.accept(this);
        sb.append(", ");
        newTable.keyColOp.accept(this);
        sb.append(", ");
        newTable.dataOp.accept(this);
        sb.append("];\n");
    }

    public void visit(FieldAccess birFieldAccess) {
        writePosition(birFieldAccess.pos);
        sb.append("\t\t");
        birFieldAccess.lhsOp.accept(this);
        sb.append("[");
        birFieldAccess.keyOp.accept(this);
        sb.append("] = ").append(birFieldAccess.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birFieldAccess.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(TypeCast birTypeCast) {
        writePosition(birTypeCast.pos);
        sb.append("\t\t");
        birTypeCast.lhsOp.accept(this);
        sb.append(" = ").append(birTypeCast.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birTypeCast.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(IsLike birIsLike) {
        writePosition(birIsLike.pos);
        sb.append("\t\t");
        birIsLike.lhsOp.accept(this);
        sb.append(" = ");
        birIsLike.rhsOp.accept(this);
        sb.append(" ").append(birIsLike.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        //TODO emit type
        sb.append(";\n");
    }

    public void visit(TypeTest birTypeTest) {
        writePosition(birTypeTest.pos);
        sb.append("\t\t");
        birTypeTest.lhsOp.accept(this);
        sb.append(" = ");
        birTypeTest.rhsOp.accept(this);
        sb.append(" ").append(birTypeTest.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        //TODO emit type
        sb.append(";\n");
    }

    // Terminating instructions

    public void visit(BIRTerminator.Return birReturn) {
        writePosition(birReturn.pos);
        sb.append("\t\t");
        sb.append("return;\n");
    }

    public void visit(BIRTerminator.GOTO birGoto) {
        writePosition(birGoto.pos);
        sb.append("\t\t");
        sb.append("goto ").append(birGoto.targetBB.id).append(";\n");
    }

    public void visit(BIRTerminator.Branch birBranch) {
        writePosition(birBranch.pos);
        sb.append("\t\t");
        sb.append("branch ");
        birBranch.op.accept(this);
        sb.append(" [true:").append(birBranch.trueBB.id).append(", false:");
        sb.append(birBranch.falseBB.id).append("];\n");
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        writePosition(birNewError.pos);
        sb.append("\t\t");
        birNewError.lhsOp.accept(this);
        sb.append(" = ").append(birNewError.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birNewError.reasonOp.accept(this);
        sb.append(" ");
        birNewError.detailOp.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        sb.append("\t\t");
        fpLoad.lhsOp.accept(this);
        sb.append(" = ").append(fpLoad.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        sb.append(fpLoad.funcName.getValue()).append("()");
        sb.append(";");
    }

    public void visit(BIRTerminator.Panic birPanic) {
        writePosition(birPanic.pos);
        sb.append("\t\t");
        sb.append(birPanic.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birPanic.errorOp.accept(this);
        sb.append(";\n");
    }

    @Override
    public void visit(NewXMLElement newXMLElement) {
        sb.append("\t\t");
        newXMLElement.lhsOp.accept(this);
        sb.append(" = ").append(newXMLElement.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newXMLElement.startTagOp.accept(this);
        sb.append(" ");
        newXMLElement.endTagOp.accept(this);
        sb.append(" ");
        newXMLElement.defaultNsURIOp.accept(this);
        sb.append(";\n");
    }

    @Override
    public void visit(NewXMLQName newXMLQName) {
        sb.append("\t\t");
        newXMLQName.lhsOp.accept(this);
        sb.append(" = ").append(newXMLQName.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newXMLQName.localnameOp.accept(this);
        sb.append(" ");
        newXMLQName.nsURIOp.accept(this);
        sb.append(" ");
        newXMLQName.prefixOp.accept(this);
        sb.append(";\n");
    }

    @Override
    public void visit(NewXMLText newXMLQText) {
        sb.append("\t\t");
        newXMLQText.lhsOp.accept(this);
        sb.append(" = ").append(newXMLQText.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newXMLQText.textOp.accept(this);
        sb.append(";\n");
    }

    public void visit(XMLAccess xmlAddChild) {
        sb.append("\t\t");
        xmlAddChild.lhsOp.accept(this);
        sb.append(" = ").append(xmlAddChild.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        xmlAddChild.rhsOp.accept(this);
        sb.append(";\n");
    }

    @Override
    public void visit(NewXMLComment newXMLComment) {
        sb.append("\t\t");
        newXMLComment.lhsOp.accept(this);
        sb.append(" = ").append(newXMLComment.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newXMLComment.textOp.accept(this);
        sb.append(";\n");
    }

    @Override
    public void visit(NewXMLProcIns newXMLProcIns) {
        sb.append("\t\t");
        newXMLProcIns.lhsOp.accept(this);
        sb.append(" = ").append(newXMLProcIns.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newXMLProcIns.dataOp.accept(this);
        sb.append(" ");
        newXMLProcIns.targetOp.accept(this);
        sb.append(";\n");
    }

    public void visit(NewStringXMLQName newStringXMLQName) {
        sb.append("\t\t");
        newStringXMLQName.lhsOp.accept(this);
        sb.append(" = ").append(newStringXMLQName.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        newStringXMLQName.stringQNameOP.accept(this);
        sb.append(";\n");
    }

    public void visit(NewTypeDesc newTypeDesc) {
        sb.append("\t\t");
        newTypeDesc.lhsOp.accept(this);
        sb.append(" = ").append(newTypeDesc.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        sb.append(newTypeDesc.type.tsymbol);
        sb.append(";\n");
    }

    // Operands
    public void visit(BIROperand birOp) {
        sb.append(birOp.variableDcl.name);
    }

    // Positions

    private void writePosition(DiagnosticPos pos) {
        if (pos != null) {
            appendPos(sb, pos.sLine);
            sb.append("-");
            appendPos(sb, pos.eLine);
            sb.append(":");
            appendPos(sb, pos.sCol);
            sb.append("-");
            appendPos(sb, pos.eCol);
        }
    }

    private void appendPos(StringBuilder sb, int line) {
        if (line < 10) {
            sb.append("0");
        }
        sb.append(line);
    }

    public void visit(BIRTerminator.Wait wait) {
        sb.append("\t\t");
        wait.lhsOp.accept(this);
        sb.append(" = ");
        sb.append(wait.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        int i = 0;
        for (BIROperand expr : wait.exprList) {
            if (i != 0) {
                sb.append("|");
            }
            expr.accept(this);
            i++;
        }
        sb.append(";\n");
    }
}
