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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.types.BXmlSequence;

import java.util.Collections;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CONVERTER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_XML_ITEM;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRING_TO_XML_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * XML template expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class XMLTemplateExpressionEvaluator extends Evaluator {

    private final TemplateExpressionNode syntaxNode;

    public XMLTemplateExpressionEvaluator(EvaluationContext context, TemplateExpressionNode templateExpressionNode) {
        super(context);
        this.syntaxNode = templateExpressionNode;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            StringBuilder xmlStrBuilder = new StringBuilder();
            for (Node memberNode : syntaxNode.content()) {
                xmlStrBuilder.append(memberNode.toSourceCode());
            }
            Value xmlStrValue = EvaluationUtils.getAsJString(context, xmlStrBuilder.toString());
            RuntimeStaticMethod strToXmlMethod = getRuntimeMethod(context, B_TYPE_CONVERTER_CLASS,
                    STRING_TO_XML_METHOD, Collections.singletonList(JAVA_STRING_CLASS));
            strToXmlMethod.setArgValues(Collections.singletonList(xmlStrValue));
            Value result = strToXmlMethod.invokeSafely();
            BVariable xmlVar = VariableFactory.getVariable(context, result);

            if (xmlVar instanceof BXmlSequence xmlSequence) {
                if (xmlSequence.getChildrenCount() == 0) {
                    // If the element count is 0, returns an empty XML item.
                    RuntimeStaticMethod createXmlMethod = getRuntimeMethod(context, B_TYPE_CREATOR_CLASS,
                            CREATE_XML_ITEM, Collections.emptyList());
                    createXmlMethod.setArgValues(Collections.singletonList(xmlStrValue));
                    result = strToXmlMethod.invokeSafely();
                } else if (xmlSequence.getChildrenCount() == 1) {
                    // If the sequence contain only one element, returns it as a single XMLItem.
                    result = xmlSequence.getChildByIndex(0);
                }
            }
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}
