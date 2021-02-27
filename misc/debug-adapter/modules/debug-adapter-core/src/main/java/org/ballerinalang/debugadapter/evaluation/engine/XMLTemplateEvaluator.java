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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.Collections;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.FROM_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.XML_FROM_STRING_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * XML template expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class XMLTemplateEvaluator extends Evaluator {

    private final TemplateExpressionNode syntaxNode;

    public XMLTemplateEvaluator(SuspendedContext context, TemplateExpressionNode templateExpressionNode) {
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
            Value xmlStrValue = VMUtils.make(context, xmlStrBuilder.toString()).getJdiValue();

            RuntimeStaticMethod fromStringMethod = getRuntimeMethod(context, FROM_STRING_CLASS, XML_FROM_STRING_METHOD,
                    Collections.singletonList(B_STRING_CLASS));
            fromStringMethod.setArgValues(Collections.singletonList(xmlStrValue));
            Value result = fromStringMethod.invoke();
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
