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

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.JVMValueType;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.ballerinalang.debugadapter.variable.types.BXmlItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CUSTOM_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INDEX_OUT_OF_RANGE_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INVALID_KEY_TYPE_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_STRING_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_XML_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_STRING_AT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Evaluator implementation for member access expressions.
 *
 * @since 2.0.0
 */
public class MemberAccessExpressionEvaluator extends Evaluator {

    private final IndexedExpressionNode syntaxNode;
    private final Evaluator containerEvaluator;
    private final List<Evaluator> keyEvaluators;

    private static final String FIELD_CHILDREN = "children";

    public MemberAccessExpressionEvaluator(EvaluationContext context, IndexedExpressionNode indexedExpressionNode,
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
                throw createEvaluationException(CUSTOM_ERROR, "Type `" + containerVar.getBType().getString() +
                        "' does not support member access.");
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
                        throw createEvaluationException(INVALID_KEY_TYPE_ERROR, BVariableType.INT.getString(),
                                keyVar.getBType().getString(), syntaxNode.toSourceCode());
                    }
                    int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    List<String> argTypeNames = new ArrayList<>();
                    argTypeNames.add(B_STRING_CLASS);
                    argTypeNames.add(JVMValueType.LONG.getString());
                    RuntimeStaticMethod getCodePointMethod = getRuntimeMethod(context, B_STRING_UTILS_CLASS,
                            GET_STRING_AT_METHOD, argTypeNames);

                    List<Value> argValues = new ArrayList<>();
                    argValues.add(containerResult.getJdiValue());
                    argValues.add(VMUtils.make(context, index).getJdiValue());
                    getCodePointMethod.setArgValues(argValues);
                    return new BExpressionValue(context, getCodePointMethod.invokeSafely());
                }
                // Index access of lists
                // If it is list, and index is < 0 or ≥ the length of the list, then the evaluation completes abruptly
                // with a panic; otherwise, the result is the member of the list with index k.
                case ARRAY: {
                    // Validates key expression result type.
                    if (keyVar.getBType() != BVariableType.INT) {
                        throw createEvaluationException(INVALID_KEY_TYPE_ERROR, BVariableType.INT.getString(),
                                keyVar.getBType().getString(), syntaxNode.toSourceCode());
                    }
                    int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    int childSize = ((BCompoundVariable) containerVar).getChildrenCount();
                    // Validates for IndexOutOfRange errors.
                    if (index < 0 || index >= childSize) {
                        throw createEvaluationException(INDEX_OUT_OF_RANGE_ERROR, containerVar.getBType().getString(),
                                index, childSize);
                    }
                    Value child = ((IndexedCompoundVariable) containerVar).getChildByIndex(index);
                    return new BExpressionValue(context, child);
                }
                // Index access of mappings (map, json)
                // If it is mapping, then if the mapping is () or c does not contain a member with key k, then the
                // result is (); otherwise, the result is the member of the mapping with key k.
                case MAP:
                case JSON: {
                    // Validates key expression result type.
                    if (keyVar.getBType() != BVariableType.STRING && keyVar.getBType() != BVariableType.NIL) {
                        throw createEvaluationException(INVALID_KEY_TYPE_ERROR, BVariableType.STRING.getString(),
                                keyVar.getBType().getString(), syntaxNode.toSourceCode());
                    }
                    if (keyVar.getBType() == BVariableType.NIL) {
                        return new BExpressionValue(context, null);
                    }
                    String keyString = keyVar.getDapVariable().getValue();
                    try {
                        keyString = VariableUtils.removeRedundantQuotes(keyString);
                        Value child = ((IndexedCompoundVariable) containerVar).getChildByName(keyString);
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
                    if (keyVar.getBType() != BVariableType.INT) {
                        throw createEvaluationException(INVALID_KEY_TYPE_ERROR, BVariableType.INT.getString(),
                                keyVar.getBType().getString(), syntaxNode.toSourceCode());
                    }
                    int index = Integer.parseInt(keyVar.getDapVariable().getValue());
                    int childCount = getXmlChildVarCount(containerVar);

                    // Validates for IndexOutOfRange errors.
                    if (index < 0) {
                        throw createEvaluationException(INDEX_OUT_OF_RANGE_ERROR, containerVar.getBType().getString(),
                                index, childCount);
                    } else if (index >= childCount) {
                        List<String> argTypeNames = new ArrayList<>();
                        argTypeNames.add(EvaluationUtils.JAVA_STRING_CLASS);
                        RuntimeStaticMethod createXmlValueMethod = getRuntimeMethod(context, B_VALUE_CREATOR_CLASS,
                                CREATE_XML_VALUE_METHOD, argTypeNames);
                        Value emptyXmlStringValue = EvaluationUtils.getAsJString(context, "");
                        createXmlValueMethod.setArgValues(Collections.singletonList(emptyXmlStringValue));
                        return new BExpressionValue(context, createXmlValueMethod.invokeSafely());
                    }

                    try {
                        Value child;
                        if (containerVar instanceof BXmlItem xmlItem) {
                            Value childrenValue = xmlItem.getChildByName(FIELD_CHILDREN);
                            BVariable childrenVar = VariableFactory.getVariable(context, childrenValue);
                            if (childrenVar instanceof IndexedCompoundVariable indexedCompoundVariable) {
                                child = indexedCompoundVariable.getChildByIndex(index);
                            } else if (childrenVar instanceof NamedCompoundVariable namedCompoundVariable) {
                                child = namedCompoundVariable.getChildByName(String.valueOf(index));
                            } else {
                                child = containerVar.getJvmValue();
                            }
                        } else if (containerVar instanceof IndexedCompoundVariable indexedCompoundVariable) {
                            child = indexedCompoundVariable.getChildByIndex(index);
                        } else if (containerVar instanceof NamedCompoundVariable namedCompoundVariable) {
                            child = namedCompoundVariable.getChildByName(String.valueOf(index));
                        } else {
                            child = containerVar.getJvmValue();
                        }
                        return new BExpressionValue(context, child);
                    } catch (DebugVariableException e) {
                        return new BExpressionValue(context, null);
                    }
                }
                default: {
                    throw createEvaluationException(String.format("type `%s' does not support member access.",
                            containerVar.getBType().getString()));
                }
            }
        } catch (DebugVariableException e) {
            throw createEvaluationException(e.getMessage());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    /**
     * Returns the child variable count for a given parent XML value.
     */
    private int getXmlChildVarCount(BVariable containerVar) {
        try {
            if (containerVar instanceof BXmlItem xmlItem) {
                Value childrenValue = xmlItem.getChildByName(FIELD_CHILDREN);
                BVariable childrenVar = VariableFactory.getVariable(context, childrenValue);
                return childrenVar instanceof BCompoundVariable bCompoundVariable ?
                        bCompoundVariable.getChildrenCount() : 1;
            } else {
                return containerVar instanceof BCompoundVariable bCompoundVariable ?
                        bCompoundVariable.getChildrenCount() : 1;
            }
        } catch (DebugVariableException e) {
            return 1;
        }
    }
}
