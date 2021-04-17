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
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final BLangDiagnosticLog dlog;

    public MainFunctionValidator(Types types, BLangDiagnosticLog dlog) {
        this.types = types;
        this.dlog = dlog;
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
        Set<Integer> foundArrayTypes = new HashSet<>();
        MainParameterVisitor mainParameterVisitor = new MainParameterVisitor(false);
        for (int i = 0; i < requiredParams.size(); i++) {
            BLangSimpleVariable param = requiredParams.get(i);
            if (!mainParameterVisitor.visit(param.type)) {
                if (i == requiredParams.size() - 1 && isIncludedRecord(param)) {
                    break;
                }
                dlog.error(param.pos, DiagnosticErrorCode.INVALID_MAIN_PARAMS_TYPE, param.name, param.type);
                continue;
            }
            validateWithFoundArrayTypes(foundArrayTypes, param);
        }

        if (funcNode.restParam != null) {
            validateRestParams(funcNode.restParam, mainParameterVisitor, foundArrayTypes);
        }
    }

    private void validateRestParams(BLangSimpleVariable param, MainParameterVisitor mainParameterVisitor,
                                    Set<Integer> foundArrayTypes) {
        if (!mainParameterVisitor.visit(param.type)) {
            dlog.error(param.pos, DiagnosticErrorCode.INVALID_MAIN_PARAMS_TYPE, param.name, param.type);
        } else {
            validateWithFoundArrayTypes(foundArrayTypes, param);
        }
    }

    private void validatePublicFunction(BLangFunction funcNode) {
        if (!Symbols.isPublic(funcNode.symbol)) {
            dlog.error(funcNode.pos, DiagnosticErrorCode.MAIN_SHOULD_BE_PUBLIC);
        }
    }

    private void validateWithFoundArrayTypes(Set<Integer> foundArrayTypes, BLangSimpleVariable param) {
        if (param.type.tag == TypeTags.ARRAY) {
            BType elementType = ((BArrayType) param.type).eType;
            if (foundArrayTypes.contains(elementType.tag)) {
                dlog.error(param.pos, DiagnosticErrorCode.SAME_ARRAY_TYPE_AS_MAIN_PARAMETER, elementType);
            } else {
                foundArrayTypes.add(elementType.tag);
            }
        } else if (param.expr != null) { // defaultable
            if (foundArrayTypes.contains(param.type.tag)) {
                dlog.error(param.pos, DiagnosticErrorCode.VARIABLE_AND_ARRAY_TYPE_AS_MAIN_PARAM, param.name,
                           "defaultable " + param.type, param.type);
            }
        } else if (param.type.tag == TypeTags.UNION) { // optional
            LinkedHashSet<BType> memberTypes = ((BUnionType) param.type).getMemberTypes();
            memberTypes.forEach(type -> {
                if (foundArrayTypes.contains(type.tag)) {
                    dlog.error(param.pos, DiagnosticErrorCode.VARIABLE_AND_ARRAY_TYPE_AS_MAIN_PARAM, param.name,
                               param.type, type);
                }
            });
        }
    }

    private boolean isIncludedRecord(BLangSimpleVariable param) {
        return param.type.tag == TypeTags.RECORD && param.getFlags().contains(Flag.INCLUDED);
    }

    private void validateOptionParams(BLangSimpleVariable lastVar) {
        MainParameterVisitor mainParameterVisitor = new MainParameterVisitor(true);
        BRecordType recordType = (BRecordType) lastVar.type;
        recordType.getFields().forEach(
                (name, field) -> {
                    if (!mainParameterVisitor.visit(field.type)) {
                        dlog.error(field.pos, DiagnosticErrorCode.INVALID_MAIN_OPTION_PARAMS_TYPE, field.name,
                                   lastVar.name, field.type);
                    }
                }
        );
    }

}
