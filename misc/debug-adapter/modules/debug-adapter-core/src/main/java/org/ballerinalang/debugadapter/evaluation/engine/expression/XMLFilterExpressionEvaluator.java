/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_STRING_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_XML_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_XML_FILTER_RESULT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * XML filter expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class XMLFilterExpressionEvaluator extends Evaluator {

    private final XMLFilterExpressionNode syntaxNode;
    private final Evaluator subExprEvaluator;

    public XMLFilterExpressionEvaluator(EvaluationContext context, XMLFilterExpressionNode filterExpressionNode,
                                        Evaluator subExprEvaluator) {
        super(context);
        this.syntaxNode = filterExpressionNode;
        this.subExprEvaluator = subExprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // An xml filter expression selects constituents of a sequence that are elements with a name matching a
            // specified name pattern. The static type of the expression must be a subtype of xml. The static type of
            // the xml filter expression is xml<xml:Element>.
            BExpressionValue subExprResult = subExprEvaluator.evaluate();
            if (subExprResult.getType() != BVariableType.XML) {
                throw createEvaluationException("filter expressions are not supported on type '" +
                        subExprResult.getType().getString() + "'");
            }

            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_XML_CLASS);
            argTypeNames.add(B_STRING_ARRAY_CLASS);
            RuntimeStaticMethod getFilterResultMethod = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    GET_XML_FILTER_RESULT_METHOD, argTypeNames);

            List<Value> argValues = new ArrayList<>();
            argValues.add(subExprResult.getJdiValue());
            argValues.addAll(getXmlPatternChainAsList());
            getFilterResultMethod.setArgValues(argValues);
            return new BExpressionValue(context, getFilterResultMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    private List<Value> getXmlPatternChainAsList() {
        List<Value> result = new ArrayList<>();
        syntaxNode.xmlPatternChain().xmlNamePattern().stream().forEach(node -> {
            try {
                result.add(VMUtils.make(context, node.toSourceCode().trim()).getJdiValue());
            } catch (EvaluationException ignored) {
            }
        });
        return result;
    }
}
