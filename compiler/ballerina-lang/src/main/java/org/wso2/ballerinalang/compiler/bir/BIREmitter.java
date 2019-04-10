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
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLElement;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLText;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeCast;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeTest;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLSeqStore;
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
        sb.append("module ").append(birPackage.name).append(";").append("\n\n");
        birPackage.importModules.forEach(birImpModule -> birImpModule.accept(this));
        sb.append("\n");
        birPackage.functions.forEach(birFunction -> birFunction.accept(this));
    }

    public void visit(BIRNode.BIRImportModule birImpModule) {
        sb.append("import ").append(birImpModule.org).append("/");
        sb.append(birImpModule.name).append(":").append(birImpModule.version).append(";");
        writePosition(birImpModule.pos);
        sb.append("\n");
    }

    public void visit(BIRNode.BIRVariableDcl birVariableDcl) {
        sb.append("\t").append(birVariableDcl.type).append(" ").append(birVariableDcl.name).append(";\t\t// ");
        sb.append(birVariableDcl.kind.name().toLowerCase(Locale.ENGLISH)).append("\n");
    }

    public void visit(BIRNode.BIRFunction birFunction) {
        sb.append("function ").append(birFunction.name).append("(");
        StringJoiner sj = new StringJoiner(",");
        birFunction.type.paramTypes.forEach(paramType -> sj.add(paramType.toString()));
        sb.append(sj.toString()).append(")").append(" -> ").append(birFunction.type.retType);
        sb.append(" {");
        writePosition(birFunction.pos);
        sb.append("\n");

        birFunction.localVars.forEach(birVariableDcl -> birVariableDcl.accept(this));
        sb.append("\n");
        birFunction.basicBlocks.forEach(birBasicBlock -> birBasicBlock.accept(this));
        sb.deleteCharAt(sb.lastIndexOf("\n"));
        if (!birFunction.errorTable.isEmpty()) {
            sb.append("\tError Table \n\t\tBB\t|errorOp\n");
            birFunction.errorTable.forEach(entry -> {
                entry.accept(this);
            });
        }
        sb.append("}\n\n");
    }

    public void visit(BIRNode.BIRErrorEntry errorEntry) {
        sb.append("\t\t").append(errorEntry.trapBB.id).append("\t|");
        errorEntry.errorOp.accept(this);
        sb.append("\n");
    }

    public void visit(BIRNode.BIRBasicBlock birBasicBlock) {
        sb.append("\t");
        sb.append(birBasicBlock.id).append(" {\n");
        birBasicBlock.instructions.forEach(instruction -> ((BIRNode) instruction).accept(this));
        if (birBasicBlock.terminator == null) {
            throw new BLangCompilerException("Basic block without a terminator : " + birBasicBlock.id);
        }
        birBasicBlock.terminator.accept(this);
        sb.append("\t}\n\n");
    }

    public void visit(BIRTerminator.Call birCall) {
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
        sb.append("\t\t");
        birMove.lhsOp.accept(this);
        sb.append(" = ");
        birMove.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        sb.append("\t\t");
        birBinaryOp.lhsOp.accept(this);
        sb.append(" = ").append(birBinaryOp.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birBinaryOp.rhsOp1.accept(this);
        sb.append(" ");
        birBinaryOp.rhsOp2.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        sb.append("\t\t");
        birConstantLoad.lhsOp.accept(this);
        sb.append(" = ").append(birConstantLoad.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        sb.append(birConstantLoad.value).append(";\n");
    }

    public void visit(NewStructure birNewStructure) {
        sb.append("\t\t");
        birNewStructure.lhsOp.accept(this);
        sb.append(" = ").append(birNewStructure.kind.name().toLowerCase(Locale.ENGLISH)).append(";\n");
    }

    public void visit(NewArray birNewArray) {
        sb.append("\t\t");
        birNewArray.lhsOp.accept(this);
        sb.append(" = ").append(birNewArray.kind.name().toLowerCase(Locale.ENGLISH)).append(" [");
        birNewArray.sizeOp.accept(this);
        sb.append("];\n");
    }

    public void visit(FieldAccess birFieldAccess) {
        sb.append("\t\t");
        birFieldAccess.lhsOp.accept(this);
        sb.append("[");
        birFieldAccess.keyOp.accept(this);
        sb.append("] = ").append(birFieldAccess.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birFieldAccess.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(TypeCast birTypeCast) {
        sb.append("\t\t");
        birTypeCast.lhsOp.accept(this);
        sb.append(" = ").append(birTypeCast.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birTypeCast.rhsOp.accept(this);
        sb.append(";\n");
    }

    public void visit(IsLike birIsLike) {
        sb.append("\t\t");
        birIsLike.lhsOp.accept(this);
        sb.append(" = ");
        birIsLike.rhsOp.accept(this);
        sb.append(" ").append(birIsLike.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        //TODO emit type
        sb.append(";\n");
    }

    public void visit(TypeTest birTypeTest) {
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
        sb.append("\t\treturn;\n");
    }

    public void visit(BIRTerminator.GOTO birGoto) {
        sb.append("\t\tgoto ").append(birGoto.targetBB.id).append(";\n");
    }

    public void visit(BIRTerminator.Branch birBranch) {
        sb.append("\t\tbranch ");
        birBranch.op.accept(this);
        sb.append(" [true:").append(birBranch.trueBB.id).append(", false:");
        sb.append(birBranch.falseBB.id).append("];\n");
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        sb.append("\t\t");
        birNewError.lhsOp.accept(this);
        sb.append(" = ").append(birNewError.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        birNewError.reasonOp.accept(this);
        sb.append(" ");
        birNewError.detailOp.accept(this);
        sb.append(";\n");
    }

    public void visit(BIRTerminator.Panic birPanic) {
        sb.append("\t\t").append(birPanic.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
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

    public void visit(XMLSeqStore xmlAddChild) {
        sb.append("\t\t");
        xmlAddChild.lhsOp.accept(this);
        sb.append(" = ").append(xmlAddChild.kind.name().toLowerCase(Locale.ENGLISH)).append(" ");
        xmlAddChild.rhsOp.accept(this);
        sb.append(";\n");
    }

    // Operands
    public void visit(BIROperand birOp) {
        sb.append(birOp.variableDcl.name);
    }


    private void writePosition(DiagnosticPos pos) {
        sb.append("\t\t// pos:[").append(pos.sLine).append(":").append(pos.sCol).append("-");
        sb.append(pos.eLine).append(":").append(pos.eCol).append("]");
    }
}
