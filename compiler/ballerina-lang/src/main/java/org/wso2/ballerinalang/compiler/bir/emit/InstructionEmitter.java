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
package org.wso2.ballerinalang.compiler.bir.emit;

import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitBasicBlockRef;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitBinaryOpInstructionKind;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitLBreaks;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitModuleID;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitName;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitSpaces;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitTabs;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitValue;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitVarRef;
import static org.wso2.ballerinalang.compiler.bir.emit.EmitterUtils.emitVarRefs;
import static org.wso2.ballerinalang.compiler.bir.emit.TypeEmitter.emitTypeRef;

/**
 * BIR instruction emitter class.
 *
 * @since 1.2.0
 */
class InstructionEmitter {

    private static final byte INITIAL_VALUE_COUNT = 10;

    private InstructionEmitter() {}

    static String emitInstructions(List<? extends BIRInstruction> instructions, int tabs) {

        StringBuilder insStr = new StringBuilder();
        for (BIRInstruction ins : instructions) {
            insStr.append(emitInstruction(ins, tabs));
            insStr.append(emitLBreaks(1));
        }
        return insStr.toString();
    }

    private static String emitInstruction(BIRInstruction ins, int tabs) {

        InstructionKind kind = ins.getKind();

        if (EmitterUtils.isBinaryInstructionKind(kind)) {
            return emitInsBinaryOp((BIRNonTerminator.BinaryOp) ins, tabs);
        } else if (EmitterUtils.isUnaryInstructionKind(kind)) {
            return emitInsUnaryOp((BIRNonTerminator.UnaryOP) ins, tabs);
        }

        return switch (kind) {
            case CONST_LOAD -> emitInsConstantLoad((BIRNonTerminator.ConstantLoad) ins, tabs);
            case NEW_STRUCTURE -> emitInsNewMap((BIRNonTerminator.NewStructure) ins, tabs);
            case NEW_INSTANCE -> emitInsNewInstance((BIRNonTerminator.NewInstance) ins, tabs);
            case NEW_ARRAY -> emitInsNewArray((BIRNonTerminator.NewArray) ins, tabs);
            case NEW_ERROR -> emitInsNewError((BIRNonTerminator.NewError) ins, tabs);
            case FP_LOAD -> emitInsFPLoad((BIRNonTerminator.FPLoad) ins, tabs);
            case RECORD_DEFAULT_FP_LOAD -> emitInsRecordDefaultFpLoad((BIRNonTerminator.RecordDefaultFPLoad) ins, tabs);
            case MAP_LOAD, MAP_STORE, ARRAY_LOAD, ARRAY_STORE, OBJECT_LOAD, OBJECT_STORE, XML_ATTRIBUTE_LOAD,
                    XML_ATTRIBUTE_STORE, XML_SEQ_LOAD, STRING_LOAD, TABLE_LOAD, TABLE_STORE ->
                    emitInsFieldAccess((BIRNonTerminator.FieldAccess) ins, tabs);
            case TYPE_CAST -> emitInsTypeCast((BIRNonTerminator.TypeCast) ins, tabs);
            case IS_LIKE -> emitInsIsLike((BIRNonTerminator.IsLike) ins, tabs);
            case TYPE_TEST -> emitInsTypeTest((BIRNonTerminator.TypeTest) ins, tabs);
            case MOVE -> emitInsMove((BIRNonTerminator.Move) ins, tabs);
            case NEW_XML_ELEMENT -> emitInsNewXMLElement((BIRNonTerminator.NewXMLElement) ins, tabs);
            case NEW_XML_SEQUENCE -> emitInsNewXMLSequence((BIRNonTerminator.NewXMLSequence) ins, tabs);
            case NEW_XML_QNAME -> emitInsNewXMLQName((BIRNonTerminator.NewXMLQName) ins, tabs);
            case NEW_STRING_XML_QNAME -> emitInsNewStringXMLQName((BIRNonTerminator.NewStringXMLQName) ins, tabs);
            case XML_LOAD_ALL, XML_SEQ_STORE -> emitInsXMLAccess((BIRNonTerminator.XMLAccess) ins, tabs);
            case NEW_XML_TEXT -> emitInsNewXMLText((BIRNonTerminator.NewXMLText) ins, tabs);
            case NEW_XML_COMMENT -> emitInsNewXMLComment((BIRNonTerminator.NewXMLComment) ins, tabs);
            case NEW_XML_PI -> emitInsNewXMLPI((BIRNonTerminator.NewXMLProcIns) ins, tabs);
            case NEW_TYPEDESC -> emitInsNewTypeDesc((BIRNonTerminator.NewTypeDesc) ins, tabs);
            case NEW_TABLE -> emitInsNewTable((BIRNonTerminator.NewTable) ins, tabs);
            default -> emitBIRInstruction(ins.getClass().getSimpleName(), ((BIRAbstractInstruction) ins).lhsOp,
                    ((BIRAbstractInstruction) ins).getRhsOperands(), tabs);
        };
    }

    private static String emitInsRecordDefaultFpLoad(BIRNonTerminator.RecordDefaultFPLoad ins, int tabs) {
        String anonLoadIns = "";
        anonLoadIns += emitTabs(tabs);
        anonLoadIns += "_";
        anonLoadIns += emitSpaces(1);
        anonLoadIns += "=";
        anonLoadIns += emitSpaces(1);
        anonLoadIns += emitTypeRef(ins.enclosedType, tabs);
        anonLoadIns += " Default => ";
        anonLoadIns += ins.fieldName;
        anonLoadIns += emitSpaces(1);
        anonLoadIns += "<";
        anonLoadIns += emitVarRef(ins.lhsOp);
        anonLoadIns += ">";
        return anonLoadIns;
    }

    private static String emitBIRInstruction(String ins, BIROperand lhsOp, BIROperand[] rhsOperands, int tabs) {
        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += ins;
        str += emitSpaces(1);
        str += emitVarRefs(rhsOperands);
        str += ";";
        return str;
    }

    private static String emitInsConstantLoad(BIRNonTerminator.ConstantLoad ins, int tabs) {

        String cnstLStr = "";
        cnstLStr += emitTabs(tabs);
        cnstLStr += emitVarRef(ins.lhsOp);
        cnstLStr += emitSpaces(1);
        cnstLStr += "=";
        cnstLStr += emitSpaces(1);
        cnstLStr += "ConstLoad";
        cnstLStr += emitSpaces(1);
        cnstLStr += emitValue(ins.value, ins.type);
        cnstLStr += ";";
        return cnstLStr;
    }

    private static String emitInsNewMap(BIRNonTerminator.NewStructure ins, int tabs) {

        String nMapStr = "";
        nMapStr += emitTabs(tabs);
        nMapStr += emitVarRef(ins.lhsOp);
        nMapStr += emitSpaces(1);
        nMapStr += "=";
        nMapStr += emitSpaces(1);
        nMapStr += "NewMap";
        nMapStr += emitSpaces(1);
        nMapStr += emitVarRef(ins.rhsOp);
        nMapStr += "{";
        nMapStr += emitMapValues(ins.initialValues);
        nMapStr += "}";
        nMapStr += ";";
        return nMapStr;
    }

    private static String emitMapValues(List<BIRNode.BIRMappingConstructorEntry> initialValues) {
        if (initialValues.isEmpty()) {
            return "";
        }
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < Math.min(initialValues.size(), INITIAL_VALUE_COUNT); i++) {
            BIRNode.BIRMappingConstructorEntry mappingEntry = initialValues.get(i);
            if (mappingEntry instanceof BIRNode.BIRMappingConstructorKeyValueEntry entry) {
                outStr.append(emitVarRef(entry.keyOp)).append(":").append(emitVarRef(entry.valueOp)).append(",");
            } else {
                outStr.append(emitVarRef(((BIRNode.BIRMappingConstructorSpreadFieldEntry) mappingEntry).exprOp))
                        .append(",");
            }
        }
        return initialValues.size() > INITIAL_VALUE_COUNT ? outStr.append("...").toString()
                : outStr.substring(0, outStr.length() - 1);
    }

    private static String emitInsNewTable(BIRNonTerminator.NewTable ins, int tabs) {
        String nMapStr = "";
        nMapStr += emitTabs(tabs);
        nMapStr += emitVarRef(ins.lhsOp);
        nMapStr += emitSpaces(1);
        nMapStr += "=";
        nMapStr += emitSpaces(1);
        nMapStr += "NewTable";
        nMapStr += emitSpaces(1);
        nMapStr += "<";
        nMapStr += emitVarRef(ins.dataOp);
        nMapStr += ">";
        nMapStr += emitSpaces(1);
        nMapStr += "key(";
        nMapStr += emitVarRef(ins.dataOp);
        nMapStr += ");";
        return nMapStr;
    }

    private static String emitInsNewInstance(BIRNonTerminator.NewInstance ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new";
        str += emitSpaces(1);
        if (ins.isExternalDef) {
            str += emitModuleID(ins.externalPackageId);
            str += ":";
            str += ins.objectName;
        } else {
            str += emitName(ins.def.internalName);
        }
        str += ";";
        return str;
    }

    private static String emitInsNewArray(BIRNonTerminator.NewArray ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "newArray";
        str += emitSpaces(1);
        BType type = Types.getImpliedType(ins.type);
        if (type.tag == TypeTags.TUPLE) {
            str += emitVarRef(ins.typedescOp);
        } else {
            str += emitTypeRef(ins.type, 0);
        }
        str += "[";
        str += emitVarRef(ins.sizeOp);
        str += "]";
        str += "{";
        str += emitArrayValues(ins.values);
        str += "}";
        str += ";";
        return str;
    }

    private static String emitArrayValues(List<BIRNode.BIRListConstructorEntry> values) {
        int operandArraySize = Math.min(INITIAL_VALUE_COUNT, values.size());
        BIROperand[] valueOperands = new BIROperand[operandArraySize];
        for (int i = 0; i < operandArraySize; i++) {
            valueOperands[i] = values.get(i).exprOp;
        }
        String result = emitVarRefs(valueOperands);
        return values.size() > INITIAL_VALUE_COUNT ? result + ",..." : result;
    }

    private static String emitInsNewError(BIRNonTerminator.NewError ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "error";
        str += emitSpaces(1);
        str += emitTypeRef(ins.type, 0);
        str += "(";
        str += emitVarRef(ins.messageOp);
        str += ",";
        str += emitSpaces(1);
        str += emitVarRef(ins.causeOp);
        str += ",";
        str += emitSpaces(1);
        str += emitVarRef(ins.detailOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsFPLoad(BIRNonTerminator.FPLoad ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "fp";
        str += emitSpaces(1);
        str += emitModuleID(ins.pkgId);
        str += "::";
        str += emitName(ins.funcName);
        str += emitClosureParams(ins.closureMaps);
        // TODO add params and closure maps
        str += ";";
        return str;
    }

    private static String emitClosureParams(List<BIROperand> closureVars) {
        String str = "";
        if (!closureVars.isEmpty()) {
            str += "(";
            str += closureVars.stream().map(EmitterUtils::emitVarRef).collect(Collectors.joining(","));
            str += ")";
        }
        return str;
    }

    private static String emitInsFieldAccess(BIRNonTerminator.FieldAccess ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        if (ins.kind == InstructionKind.MAP_LOAD || ins.kind == InstructionKind.ARRAY_LOAD) {
            str += emitSpaces(1);
            str += "=";
            str += emitSpaces(1);
            str += emitVarRef(ins.rhsOp);
            str += "[";
            str += emitVarRef(ins.keyOp);
            str += "]";
        } else if (ins.kind == InstructionKind.MAP_STORE || ins.kind == InstructionKind.ARRAY_STORE ||
                ins.kind == InstructionKind.OBJECT_STORE) {
            str += "[";
            str += emitVarRef(ins.keyOp);
            str += "]";
            str += emitSpaces(1);
            str += "=";
            str += emitSpaces(1);
            str += emitVarRef(ins.rhsOp);
        }
        str += ";";
        return str;
    }

    private static String emitInsTypeCast(BIRNonTerminator.TypeCast ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "<";
        str += emitTypeRef(ins.type, 0);
        str += ">";
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp);
        str += ";";
        return str;
    }

    private static String emitInsIsLike(BIRNonTerminator.IsLike ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp);
        str += emitSpaces(1);
        str += "isLike";
        str += emitSpaces(1);
        str += emitTypeRef(ins.type, 0);
        str += ";";
        return str;
    }

    private static String emitInsTypeTest(BIRNonTerminator.TypeTest ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp);
        str += emitSpaces(1);
        str += "is";
        str += emitSpaces(1);
        str += emitTypeRef(ins.type, 0);
        str += ";";
        return str;
    }

    private static String emitInsMove(BIRNonTerminator.Move ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp);
        str += ";";
        return str;
    }

    private static String emitInsBinaryOp(BIRNonTerminator.BinaryOp ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp1);
        str += emitSpaces(1);
        str += emitBinaryOpInstructionKind(ins.kind);
        str += emitSpaces(1);
        str += emitVarRef(ins.rhsOp2);
        str += ";";
        return str;
    }

    private static String emitInsNewXMLElement(BIRNonTerminator.NewXMLElement ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:Element";
        str += "(";
        str += emitVarRef(ins.startTagOp);
        str += ",";
        str += emitVarRef(ins.defaultNsURIOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsNewXMLSequence(BIRNonTerminator.NewXMLSequence ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:Sequence";
        str += "()";
        str += ";";
        return str;
    }

    private static String emitInsNewXMLQName(BIRNonTerminator.NewXMLQName ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:QName";
        str += "(";
        str += emitVarRef(ins.localnameOp);
        str += ",";
        str += "{";
        str += emitVarRef(ins.nsURIOp);
        str += "}";
        str += ",";
        str += emitVarRef(ins.prefixOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsNewStringXMLQName(BIRNonTerminator.NewStringXMLQName ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:StringQName";
        str += "(";
        str += emitVarRef(ins.stringQNameOP);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsXMLAccess(BIRNonTerminator.XMLAccess ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        if (ins.kind == InstructionKind.XML_LOAD_ALL) {
            str += "new xml:Load";
        } else if (ins.kind == InstructionKind.XML_SEQ_STORE) {
            str += "new xml:SequenceStore";
        }
        str += "(";
        str += emitVarRef(ins.rhsOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsNewXMLText(BIRNonTerminator.NewXMLText ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:Text";
        str += "(";
        str += emitVarRef(ins.textOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsNewXMLComment(BIRNonTerminator.NewXMLComment ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:Comment";
        str += "(";
        str += emitVarRef(ins.textOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsNewXMLPI(BIRNonTerminator.NewXMLProcIns ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "new xml:ProcessingInstruction";
        str += "(";
        str += emitVarRef(ins.targetOp);
        str += ",";
        str += emitVarRef(ins.dataOp);
        str += ")";
        str += ";";
        return str;
    }

    private static String emitInsUnaryOp(BIRNonTerminator.UnaryOP ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += ins.kind.toString().toLowerCase();
        str += " ";
        // TODO emit unary op kind
        str += emitVarRef(ins.rhsOp);
        str += ";";
        return str;
    }

    private static String emitInsNewTypeDesc(BIRNonTerminator.NewTypeDesc ins, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(ins.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "newType";
        str += emitSpaces(1);
        str += emitTypeRef(ins.type, 0);
        str += emitClosureParams(ins.closureVars);
        str += ";";
        return str;
    }

    /////////////// Emit Terminator instructions ////////////////////
    static String emitTerminator(BIRTerminator term, int tabs) {

        return switch (term.kind) {
            case WAIT -> emitWait((BIRTerminator.Wait) term, tabs);
            case FLUSH -> emitFlush((BIRTerminator.Flush) term, tabs);
            case WK_RECEIVE -> emitWorkerReceive((BIRTerminator.WorkerReceive) term, tabs);
            case WK_SEND -> emitWorkerSend((BIRTerminator.WorkerSend) term, tabs);
            case CALL -> emitCall((BIRTerminator.Call) term, tabs);
            case ASYNC_CALL -> emitAsyncCall((BIRTerminator.AsyncCall) term, tabs);
            case BRANCH -> emitBranch((BIRTerminator.Branch) term, tabs);
            case GOTO -> emitGOTO((BIRTerminator.GOTO) term, tabs);
            case LOCK -> emitLock((BIRTerminator.Lock) term, tabs);
            case FIELD_LOCK -> emitFieldLock((BIRTerminator.FieldLock) term, tabs);
            case UNLOCK -> emitUnlock((BIRTerminator.Unlock) term, tabs);
            case RETURN -> emitReturn(tabs);
            case PANIC -> emitPanic((BIRTerminator.Panic) term, tabs);
            case FP_CALL -> emitFPCall((BIRTerminator.FPCall) term, tabs);
            case WAIT_ALL -> emitWaitAll((BIRTerminator.WaitAll) term, tabs);
            default -> emitBIRTerminator(term.getClass().getSimpleName(), term.lhsOp, term.getRhsOperands(), tabs,
                    term.thenBB);
        };
    }

    private static String emitBIRTerminator(String ins, BIROperand lhsOp, BIROperand[] rhsOperands, int tabs,
                                            BIRNode.BIRBasicBlock thenBB) {
        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += ins;
        str += emitSpaces(1);
        str += emitVarRefs(rhsOperands);
        str += emitSpaces(1);
        str += "->";
        str += emitSpaces(1);
        str += emitBasicBlockRef(thenBB);
        str += ";";
        return str;
    }

    private static String emitWait(BIRTerminator.Wait term, int tabs) {

        StringBuilder str = new StringBuilder();
        str.append(emitTabs(tabs));
        str.append(emitVarRef(term.lhsOp));
        str.append(emitSpaces(1));
        str.append("=");
        str.append(emitSpaces(1));
        str.append("wait");
        str.append(emitSpaces(1));
        int i = 0;
        int argLength = term.exprList.size();
        while (i < argLength) {
            BIROperand ref = term.exprList.get(i);
            if (ref != null) {
                str.append(emitVarRef(ref));
                i += 1;
                if (i < argLength) {
                    str.append(emitSpaces(1));
                    str.append("|");
                    str.append(emitSpaces(1));
                }
            }
        }
        str.append(emitSpaces(1));
        str.append("->");
        str.append(emitSpaces(1));
        str.append(emitBasicBlockRef(term.thenBB));
        str.append(";");
        return str.toString();
    }

    private static String emitFlush(BIRTerminator.Flush term, int tabs) {

        StringBuilder str = new StringBuilder();
        str.append(emitTabs(tabs));
        str.append(emitVarRef(term.lhsOp));
        str.append(emitSpaces(1));
        str.append("=");
        str.append(emitSpaces(1));
        str.append("flush");
        str.append(emitSpaces(1));
        int i = 0;
        int argLength = term.channels.length;
        while (i < argLength) {
            BIRNode.ChannelDetails ref = term.channels[i];
            str.append(ref.name);
            i += 1;
            if (i < argLength) {
                str.append(",");
                str.append(emitSpaces(1));
            }
        }
        str.append(emitSpaces(1));
        str.append("->");
        str.append(emitSpaces(1));
        str.append(emitBasicBlockRef(term.thenBB));
        str.append(";");
        return str.toString();
    }

    private static String emitWorkerReceive(BIRTerminator.WorkerReceive term, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(term.lhsOp);
        str += emitSpaces(1);
        str += "=";
        str += emitSpaces(1);
        str += "<=";
        str += emitSpaces(1);
        str += emitName(term.workerName);
        str += emitSpaces(1);
        str += "->";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.thenBB);
        str += ";";
        return str;
    }

    private static String emitWorkerSend(BIRTerminator.WorkerSend term, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        BIROperand lhsOp = term.lhsOp;
        if (lhsOp != null) {
            str += emitVarRef(lhsOp);
            str += emitSpaces(1);
            str += "=";
            str += emitSpaces(1);
        }
        str += emitVarRef(term.data);
        str += emitSpaces(1);
        if (term.isSync) {
            str += "=>";
        } else {
            str += "=>>";
        }
        str += emitSpaces(1);
        str += emitName(term.channel);
        str += emitSpaces(1);
        str += "->";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.thenBB);
        str += ";";
        return str;
    }

    private static String emitCall(BIRTerminator.Call term, int tabs) {

        StringBuilder callStr = new StringBuilder();
        callStr.append(emitTabs(tabs));
        BIROperand lhsOp = term.lhsOp;
        if (lhsOp != null) {
            callStr.append(emitVarRef(lhsOp));
            callStr.append(emitSpaces(1));
            callStr.append("=");
            callStr.append(emitSpaces(1));
        }
        callStr.append(emitName(term.name));
        callStr.append("(");
        int i = 0;
        int argLength = term.args.size();
        for (BIROperand ref : term.args) {
            if (ref != null) {
                callStr.append(emitVarRef(ref));
                i += 1;
                if (i < argLength) {
                    callStr.append(",");
                    callStr.append(emitSpaces(1));
                }
            }
        }
        callStr.append(")");
        callStr.append(emitSpaces(1));
        callStr.append("->");
        callStr.append(emitSpaces(1));
        callStr.append(emitBasicBlockRef(term.thenBB));
        callStr.append(";");
        return callStr.toString();
    }

    private static String emitAsyncCall(BIRTerminator.AsyncCall term, int tabs) {

        StringBuilder str = new StringBuilder();
        str.append(emitTabs(tabs));
        BIROperand lhsOp = term.lhsOp;
        if (lhsOp != null) {
            str.append(emitVarRef(lhsOp));
            str.append(emitSpaces(1));
            str.append("=");
            str.append(emitSpaces(1));
        }
        str.append("start");
        str.append(emitSpaces(1));
        str.append(emitName(term.name));
        str.append("(");
        int i = 0;
        int argLength = term.args.size();
        for (BIROperand ref : term.args) {
            if (ref != null) {
                str.append(emitVarRef(ref));
                i += 1;
                if (i < argLength) {
                    str.append(",");
                    str.append(emitSpaces(1));
                }
            }
        }
        str.append(")");
        str.append(emitSpaces(1));
        str.append("->");
        str.append(emitSpaces(1));
        str.append(emitBasicBlockRef(term.thenBB));
        str.append(";");
        return str.toString();
    }

    private static String emitBranch(BIRTerminator.Branch term, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += emitVarRef(term.op);
        str += "?";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.trueBB);
        str += emitSpaces(1);
        str += ":";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.falseBB);
        str += ";";
        return str;
    }

    private static String emitGOTO(BIRTerminator.GOTO term, int tabs) {

        String retStr = "";
        retStr += emitTabs(tabs);
        retStr += "GOTO";
        retStr += emitSpaces(1);
        retStr += emitBasicBlockRef(term.targetBB);
        retStr += ";";
        return retStr;
    }

    private static String emitLock(BIRTerminator.Lock term, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += "lock";
        str += emitSpaces(1);
        str += "->";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.lockedBB);
        str += ";";
        return str;
    }

    private static String emitFieldLock(BIRTerminator.FieldLock term, int tabs) {

        String str = "";
        str += emitTabs(tabs);
        str += "lock";
        str += emitSpaces(1);
        str += term.field;
        str += "[\"";
        str += term.field;
        str += "\"]";
        str += emitSpaces(1);
        str += "->";
        str += emitSpaces(1);
        str += emitBasicBlockRef(term.lockedBB);
        str += ";";
        return str;
    }

    private static String emitUnlock(BIRTerminator.Unlock term, int tabs) {

        return emitTabs(tabs) +
                "unlock" +
                emitSpaces(1) +
                "->" +
                emitSpaces(1) +
                emitBasicBlockRef(term.unlockBB) +
                ";";
    }

    private static String emitReturn(int tabs) {

        String retStr = "";
        retStr += emitTabs(tabs);
        retStr += "return";
        retStr += ";";
        return retStr;
    }

    private static String emitPanic(BIRTerminator.Panic term, int tabs) {

        String retStr = "";
        retStr += emitTabs(tabs);
        retStr += "panic";
        retStr += emitSpaces(1);
        retStr += emitVarRef(term.errorOp);
        retStr += ";";
        return retStr;
    }

    private static String emitFPCall(BIRTerminator.FPCall term, int tabs) {

        StringBuilder callStr = new StringBuilder();
        callStr.append(emitTabs(tabs));
        BIROperand lhsOp = term.lhsOp;
        if (lhsOp != null) {
            callStr.append(emitVarRef(lhsOp));
            callStr.append(emitSpaces(1));
            callStr.append("=");
            callStr.append(emitSpaces(1));
        }
        callStr.append("FPCall");
        callStr.append(emitSpaces(1));
        callStr.append(emitVarRef(term.fp));
        callStr.append("(");
        int i = 0;
        int argLength = term.args.size();
        for (BIROperand ref : term.args) {
            if (ref != null) {
                callStr.append(emitVarRef(ref));
                i += 1;
                if (i < argLength) {
                    callStr.append(",");
                    callStr.append(emitSpaces(1));
                }
            }
        }
        callStr.append(")");
        callStr.append(emitSpaces(1));
        callStr.append("->");
        callStr.append(emitSpaces(1));
        callStr.append(emitBasicBlockRef(term.thenBB));
        callStr.append(";");
        return callStr.toString();
    }

    private static String emitWaitAll(BIRTerminator.WaitAll term, int tabs) {

        StringBuilder str = new StringBuilder();
        str.append(emitTabs(tabs));
        str.append(emitVarRef(term.lhsOp));
        str.append(emitSpaces(1));
        str.append("=");
        str.append(emitSpaces(1));
        str.append("waitAll");
        str.append(emitSpaces(1));
        str.append("{");
        int i = 0;
        int argLength = term.valueExprs.size();
        while (i < argLength) {
            BIROperand ref = term.valueExprs.get(i);
            String key = term.keys.get(i);
            if (ref != null) {
                str.append("\"");
                str.append(key);
                str.append("\"");
                str.append(":");
                str.append(emitSpaces(1));
                str.append(emitVarRef(ref));
                i += 1;
                if (i < argLength) {
                    str.append(",");
                    str.append(emitSpaces(1));
                }
            }
        }
        str.append("}");
        str.append(emitSpaces(1));
        str.append("->");
        str.append(emitSpaces(1));
        str.append(emitBasicBlockRef(term.thenBB));
        str.append(";");
        return str.toString();
    }
}


