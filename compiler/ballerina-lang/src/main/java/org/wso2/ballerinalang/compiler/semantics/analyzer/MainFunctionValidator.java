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

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * This represents the code analyzing pass of semantic analysis.
 * <p>
 * The following validations are done here:-
 * <p>
 * (*) Loop continuation statement validation. (*) Function return path existence and unreachable code validation. (*)
 * Worker send/receive validation. (*) Experimental feature usage.
 */
public class MainFunctionValidator extends BLangNodeVisitor {
    private final Types types;
    private final BLangDiagnosticLog dLog;
    private BType foundArrayType;

    public MainFunctionValidator(Types types, BLangDiagnosticLog dLog) {
        this.types = types;
        this.dLog = dLog;
    }

    void validateMainFunction(BLangFunction funcNode) {
        validatePublicFunction(funcNode);
        List<BLangSimpleVariable> requiredParams = funcNode.requiredParams;
        validateOperandParams(funcNode, requiredParams);
        if (!requiredParams.isEmpty()) {
            BLangSimpleVariable lastVar = requiredParams.get(requiredParams.size() - 1);
            if (isIncludedRecord(lastVar)) {
                validateOptionParams(lastVar);
            }
        }
        types.validateErrorOrNilReturn(funcNode, DiagnosticErrorCode.MAIN_RETURN_SHOULD_BE_ERROR_OR_NIL);
    }

    private void validateOperandParams(BLangFunction funcNode, List<BLangSimpleVariable> requiredParams) {
        MainParameterVisitor mainParameterVisitor = new MainParameterVisitor(false);
        BLangSimpleVariable foundOperand = null;
        boolean foundInvalid = false;
        for (int i = 0; i < requiredParams.size(); i++) {
            BLangSimpleVariable param = requiredParams.get(i);
            if (mainParameterVisitor.visit(param.getBType())) {
                foundOperand = handleSuccessfulVisit(mainParameterVisitor, foundOperand, param);
            } else {
                if (i == requiredParams.size() - 1 && isIncludedRecord(param)) {
                    break;
                }
                dLog.error(param.pos, DiagnosticErrorCode.INVALID_MAIN_PARAMS_TYPE, param.name, param.getBType());
                foundInvalid = true;
            }
        }
        validateRestParams(funcNode, mainParameterVisitor);
        if (foundInvalid) {
            return;
        }
        detectArrayRelatedErrors(requiredParams);
    }

    private void detectArrayRelatedErrors(List<BLangSimpleVariable> requiredParams) {
        boolean firstArrayFound = false;
        for (BLangSimpleVariable param : requiredParams) {
            if (Types.getReferredType(param.getBType()).tag == TypeTags.ARRAY && !firstArrayFound) {
                firstArrayFound = true;
            } else {
                validateWithFoundArrayType(param);
            }
        }
    }

    private BLangSimpleVariable handleSuccessfulVisit(MainParameterVisitor mainParameterVisitor,
                                                      BLangSimpleVariable foundOperand, BLangSimpleVariable param) {
        if (mainParameterVisitor.isOperandType(param.getBType()) && param.expr == null && foundOperand != null) {
            dLog.error(param.pos, DiagnosticErrorCode.OPTIONAL_OPERAND_PRECEDES_OPERAND, foundOperand.name,
                       foundOperand.getBType(), param.name, param.getBType());
        } else if (foundUnion(param)) {
            foundOperand = param;
        } else if (param.getBType().tag == TypeTags.ARRAY && foundArrayType == null) {
            foundArrayType = ((BArrayType) param.getBType()).eType;
        }
        return foundOperand;
    }

    private void validateRestParams(BLangFunction funcNode, MainParameterVisitor mainParameterVisitor) {
        if (funcNode.restParam == null) {
            return;
        }
        BLangSimpleVariable param = funcNode.restParam;
        if (!mainParameterVisitor.visit(param.getBType())) {
            dLog.error(param.pos, DiagnosticErrorCode.INVALID_MAIN_PARAMS_TYPE, param.name, param.getBType());
        } else {
            if (foundArrayType != null) {
                dLog.error(param.pos, DiagnosticErrorCode.SAME_ARRAY_TYPE_AS_MAIN_PARAMETER);
                return;
            }
            foundArrayType = ((BArrayType) param.getBType()).eType;
        }
    }

    private void validatePublicFunction(BLangFunction funcNode) {
        if (!Symbols.isPublic(funcNode.symbol)) {
            dLog.error(funcNode.pos, DiagnosticErrorCode.MAIN_SHOULD_BE_PUBLIC);
        }
    }

    private void validateWithFoundArrayType(BLangSimpleVariable param) {
        if (foundArrayType == null) {
            return;
        }
        if (Types.getReferredType(param.getBType()).tag == TypeTags.ARRAY) {
            dLog.error(param.pos, DiagnosticErrorCode.SAME_ARRAY_TYPE_AS_MAIN_PARAMETER);
        } else if (param.expr != null) { // defaultable
            dLog.error(param.pos, DiagnosticErrorCode.VARIABLE_AND_ARRAY_TYPE_AS_MAIN_PARAM, param.name,
                       "defaultable " + param.getBType(), foundArrayType);
        } else if (foundUnion(param)) { // optional
            dLog.error(param.pos, DiagnosticErrorCode.VARIABLE_AND_ARRAY_TYPE_AS_MAIN_PARAM, param.name,
                       param.getBType(), foundArrayType);
        }
    }

    private boolean foundUnion(BLangSimpleVariable param) {
        return Types.getReferredType(param.getBType()).tag == TypeTags.UNION;
    }

    private boolean isIncludedRecord(BLangSimpleVariable param) {
        return Types.getReferredType(param.getBType()).tag == TypeTags.RECORD
                && param.getFlags().contains(Flag.INCLUDED);
    }

    private void validateOptionParams(BLangSimpleVariable lastVar) {
        MainParameterVisitor mainParameterVisitor = new MainParameterVisitor(true);
        BRecordType recordType = (BRecordType) Types.getReferredType(lastVar.getBType());
        recordType.getFields().forEach(
                (name, field) -> {
                    if (!mainParameterVisitor.visit(field.type)) {
                        dLog.error(field.pos, DiagnosticErrorCode.INVALID_MAIN_OPTION_PARAMS_TYPE, field.name,
                                   lastVar.name, field.type);
                    }
                }
        );
    }

}
