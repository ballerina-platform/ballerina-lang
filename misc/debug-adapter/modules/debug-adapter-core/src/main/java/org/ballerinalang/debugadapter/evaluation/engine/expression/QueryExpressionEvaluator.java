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
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.QueryExpressionProcessor;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.engine.QueryExpressionProcessor.QUERY_FUNCTION_NAME;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CLASSLOAD_AND_INVOKE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getAsJString;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Query expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class QueryExpressionEvaluator extends Evaluator {

    private final QueryExpressionNode syntaxNode;

    public QueryExpressionEvaluator(SuspendedContext context, QueryExpressionNode queryExpressionNode) {
        super(context);
        this.syntaxNode = queryExpressionNode;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        QueryExpressionProcessor queryProcessor = new QueryExpressionProcessor(context, syntaxNode);
        try {
            Map.Entry<Path, String> execPathAndMainClassName = queryProcessor.generateExecutables();
            Path executablePath = execPathAndMainClassName.getKey();
            String mainClassName = execPathAndMainClassName.getValue();

            List<String> argTypes = new ArrayList<>();
            argTypes.add(JAVA_STRING_CLASS);
            argTypes.add(JAVA_STRING_CLASS);
            argTypes.add(JAVA_STRING_CLASS);
            argTypes.add(JAVA_OBJECT_ARRAY_CLASS);
            RuntimeStaticMethod loadQueryClasses = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    CLASSLOAD_AND_INVOKE_METHOD, argTypes);

            List<Value> argList = new ArrayList<>();
            argList.add(getAsJString(context, executablePath.toAbsolutePath().toString()));
            argList.add(getAsJString(context, mainClassName));
            argList.add(getAsJString(context, QUERY_FUNCTION_NAME));

            // adds all the captured variable values as rest arguments.
            argList.addAll(queryProcessor.getExternalVariableValues());
            loadQueryClasses.setArgValues(argList);
            loadQueryClasses.invokeSafely();
            Value queryResult = loadQueryClasses.invokeSafely();

            return new BExpressionValue(context, queryResult);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        } finally {
            // Need to dispose the query processor, which will clean up temporary resources generated during the
            // evaluation.
            queryProcessor.dispose();
        }
    }
}
