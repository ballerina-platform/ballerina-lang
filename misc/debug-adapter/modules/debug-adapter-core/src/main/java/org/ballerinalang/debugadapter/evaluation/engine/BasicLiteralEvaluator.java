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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.Collections;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_DECIMAL_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Evaluator implementation for Basic literals.
 *
 * @since 2.0.0
 */
public class BasicLiteralEvaluator extends Evaluator {

    private final Node syntaxNode;
    private final String literalString;

    private static final String HEX_INDICATOR_PREFIX_REGEX = "0[Xx]";
    private static final String FLOATING_POINT_TYPE_SUFFIX_REGEX = "[dDfF]$";

    public BasicLiteralEvaluator(SuspendedContext context, BasicLiteralNode node) {
        this(context, node, node.literalToken().text());
    }

    public BasicLiteralEvaluator(SuspendedContext context, NilLiteralNode node) {
        this(context, node, node.toSourceCode().trim());
    }

    public BasicLiteralEvaluator(SuspendedContext context, Token node) {
        this(context, node, node.text());
    }

    private BasicLiteralEvaluator(SuspendedContext context, Node node, String literalString) {
        super(context);
        this.syntaxNode = node;
        this.literalString = literalString;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            SyntaxKind basicLiteralKind = syntaxNode.kind();
            switch (basicLiteralKind) {
                case NIL_LITERAL:
                    return new BExpressionValue(context, null);
                case NUMERIC_LITERAL:
                    return parseAndGetNumericValue(literalString.trim());
                case BOOLEAN_LITERAL:
                    return VMUtils.make(context, Boolean.parseBoolean(literalString.trim()));
                case STRING_LITERAL:
                case TEMPLATE_STRING:
                    return VMUtils.make(context, literalString);
                default:
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "Unsupported basic literal detected: " + literalString));
            }
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    /**
     * Parses a user given literal expression string into a ballerina numeric value and return as an instance of
     * {@code  BExpressionValue}.
     */
    private BExpressionValue parseAndGetNumericValue(String literalString) throws EvaluationException {
        SyntaxKind literalTokenKind = ((BasicLiteralNode) syntaxNode).literalToken().kind();
        if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
            return VMUtils.make(context, Long.parseLong(literalString));
        } else if (literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
            // Removes hex indicator.
            String withoutHexIndicator = literalString.replaceFirst(HEX_INDICATOR_PREFIX_REGEX, "");
            String withoutTypeSuffix = withoutHexIndicator.replaceAll(FLOATING_POINT_TYPE_SUFFIX_REGEX, "");
            return VMUtils.make(context, Long.parseLong(withoutTypeSuffix, 16));
        } else if (literalTokenKind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN) {
            return new BExpressionValue(context, createDecimalValue(literalString));
        } else if (literalTokenKind == SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN) {
            // Todo - add support for hex floats
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "Unsupported basic literal detected: " + literalString));
        } else {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "Unsupported basic literal detected: " + literalString));
        }
    }

    /**
     * Parses the user input string into a decimal value using ValueCreator API in ballerina runtime.
     *
     * @param decimalValueString user input string
     * @return parsed decimal value
     */
    private Value createDecimalValue(String decimalValueString) throws EvaluationException {
        RuntimeStaticMethod method = getRuntimeMethod(context, B_VALUE_CREATOR_CLASS,
                CREATE_DECIMAL_VALUE_METHOD, Collections.singletonList(EvaluationUtils.JAVA_STRING_CLASS));
        method.setArgValues(Collections.singletonList(EvaluationUtils.getAsJString(context, decimalValueString)));
        return method.invoke();
    }
}
