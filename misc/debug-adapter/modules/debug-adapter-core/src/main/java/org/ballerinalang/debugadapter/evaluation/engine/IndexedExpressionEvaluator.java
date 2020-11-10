/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.List;

/**
 * Evaluator implementation for indexed(member access) expressions.
 *
 * @since 2.0.0
 */
public class IndexedExpressionEvaluator extends Evaluator {

    private final IndexedExpressionNode syntaxNode;
    private final Evaluator containerEvaluator;
    private final List<Evaluator> keyEvaluators;

    public IndexedExpressionEvaluator(SuspendedContext context, IndexedExpressionNode indexedExpressionNode,
                                      Evaluator containerEvaluator, List<Evaluator> keyEvaluators) {
        super(context);
        this.syntaxNode = indexedExpressionNode;
        this.containerEvaluator = containerEvaluator;
        this.keyEvaluators = keyEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue containerResult = containerEvaluator.evaluate();
            BVariable containerVar = VariableFactory.getVariable(context, containerResult.getJdiValue());
            // Validates container expression result type.
            if (containerVar.getBType() != BVariableType.STRING && containerVar.getBType() != BVariableType.XML
                    && containerVar.getBType() != BVariableType.ARRAY && containerVar.getBType() != BVariableType.MAP
                    && containerVar.getBType() != BVariableType.JSON) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Type `" +
                        containerVar.getBType().getString() + "' does not support indexing."));
            }
            // Todo - verify
            BExpressionValue keyResult = keyEvaluators.get(0).evaluate();
            BVariable keyVar = VariableFactory.getVariable(context, keyResult.getJdiValue());

            switch (containerVar.getBType()) {
                // Index access of strings
                // If it is string, and index is < 0 or ≥ the length of the string, then the evaluation completes
                // abruptly with a panic;
                // otherwise, the result is a string of length 1 containing the character with index k of the string.
                case STRING: {
                    // Validates key expression result type.
                    if (keyVar.getBType() != BVariableType.INT) {
                        throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                                "Expected key type `" + BVariableType.INT.getString() + "`; found '" +
                                        keyVar.getBType() + "'"));
                    }
                    int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    int strLength = containerVar.getDapVariable().getValue().length();
                    // Validates for IndexOutOfRange errors.
                    if (index < 0 || index >= strLength) {
                        throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                                "String index out of range: index=" + index + ", size=" + strLength));
                    }
                    String substring = containerVar.getDapVariable().getValue().substring(index, index + 1);
                    return EvaluationUtils.make(context, substring);
                }
                // Index access of lists
                // If it is list, and index is < 0 or ≥ the length of the list, then the evaluation completes abruptly
                // with a panic; otherwise, the result is the member of the list with index k.
                case ARRAY: {
                    // Validates key expression result type.
                    if (keyVar.getBType() != BVariableType.INT) {
                        throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                                "Expected key type `" + BVariableType.INT.getString() + "`; found '" +
                                        keyVar.getBType() + "'"));
                    }
                    int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    int childSize = ((BCompoundVariable) containerVar).getChildVariables().size();
                    // Validates for IndexOutOfRange errors.
                    if (index < 0 || index >= childSize) {
                        throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                                "String index out of range: index=" + index + ", size=" + childSize));
                    }
                    String indexAsKey = String.format("[%d]", index);
                    Value child = ((BCompoundVariable) containerVar).getChildByName(indexAsKey);
                    return new BExpressionValue(context, child);
                }
                // Index access of mappings (map, json)
                // If it is mapping, then if the mapping is () or c does not contain a member with key k, then the
                // result is (); otherwise, the result is the member of the mapping with key k.
                case MAP:
                case JSON: {
                    // Validates key expression result type.
                    if (keyVar.getBType() != BVariableType.STRING && keyVar.getBType() != BVariableType.NIL) {
                        throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                                "Expected key type `" + BVariableType.STRING.getString() + "`; found '" +
                                        keyVar.getBType().getString() + "'"));
                    }
                    if (keyVar.getBType() == BVariableType.NIL) {
                        return new BExpressionValue(context, null);
                    }
                    String keyString = keyVar.getDapVariable().getValue();
                    try {
                        Value child = ((BCompoundVariable) containerVar).getChildByName(keyString);
                        return new BExpressionValue(context, child);
                    } catch (DebugVariableException e) {
                        return new BExpressionValue(context, null);
                    }
                }
                // Index access for XMLs
                // If it is xml, and index is < 0, then the evaluation completes abruptly with a panic; if k is ≥ the
                // length of the xml, then the result is an empty xml value; otherwise, the result is a singleton xml
                // value containing the item with index k in the xml.
                // Todo - Enable after new xml changes
                case XML: {
                    // Validates key expression result type.
                    // if (keyVar.getBType() != BVariableType.INT) {
                    //   throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    //                     "Expected key type `" + BVariableType.INT.getString() + "`; found '" +
                    //                        keyVar.getBType() + "'"));
                    // }
                    // int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    // int strLength = containerVar.getDapVariable().getValue().length();
                    // // Validates for IndexOutOfRange errors.
                    // if (index < 0) {
                    //    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    //                  "String index out of range: index=" + index + ", size=" + strLength));
                    // } else if (index >= strLength) {
                    //      new BXmlText(context, "unknown", EvaluationUtils.make(context, "").getJdiValue());
                    // }
                    // String substring = containerVar.getDapVariable().getValue().substring(index, index + 1);
                    // return EvaluationUtils.make(context, substring);
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "Type `" + containerVar.getBType().getString() + "' does not support indexing."));
                }
                default: {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "Type `" + containerVar.getBType().getString() + "' does not support indexing."));
                }
            }
        } catch (DebugVariableException e) {
            throw new EvaluationException(e.getMessage());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
