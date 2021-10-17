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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
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

    private static final String HEX_INDICATOR_PREFIX_PATTERN = "0[xX]";
    private static final String FLOAT_TYPE_SUFFIX_PATTERN = "[fF]$";
    private static final String DECIMAL_TYPE_SUFFIX_PATTERN = "[dD]$";

    public BasicLiteralEvaluator(EvaluationContext context, BasicLiteralNode node) {
        this(context, node, node.literalToken().text());
    }

    public BasicLiteralEvaluator(EvaluationContext context, NilLiteralNode node) {
        this(context, node, node.toSourceCode().trim());
    }

    public BasicLiteralEvaluator(EvaluationContext context, Token node) {
        this(context, node, node.text());
    }

    private BasicLiteralEvaluator(EvaluationContext context, Node node, String literalString) {
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
                    throw createUnsupportedLiteralError(literalString);
            }
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    /**
     * Parses a user given literal expression string into a ballerina numeric value and return as an instance of
     * {@code  BExpressionValue}.
     */
    private BExpressionValue parseAndGetNumericValue(String literalString) throws EvaluationException {
        // A numeric-literal represents a value belonging to one of the basic types int, float or decimal. The basic
        // type to which the value belongs is determined as follows:
        //
        // 1. if the numeric-literal includes a FloatTypeSuffix, then the basic type is float;
        // 2. if the numeric-literal includes a DecimalTypeSuffix, then the basic type is decimal;
        // 3. if the numeric-literal is a HexFloatingPointLiteral, then the basic type is float;
        // 4. otherwise, the basic type depends on the applicable expected numeric type (where the possible basic types
        //    are int, float and decimal):
        //    - if the applicable contextually expected type is a subtype of decimal, then the basic type is decimal;
        //    - if the applicable contextually expected type is a subtype of float, then the basic type is float;
        //    - otherwise, if the numeric literal is an int-literal, then the basic type is int;
        //    - otherwise, the basic type is float.
        switch (((BasicLiteralNode) syntaxNode).literalToken().kind()) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
                return VMUtils.make(context, Long.parseLong(literalString));
            case HEX_INTEGER_LITERAL_TOKEN:
                // Removes hex indicator.
                String withoutHexIndicator = literalString.replaceFirst(HEX_INDICATOR_PREFIX_PATTERN, "");
                return VMUtils.make(context, Long.parseLong(withoutHexIndicator, 16));
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
                if (isDecimalType(literalString)) {
                    // Removes decimal type suffix.
                    String stringWithoutSuffix = literalString.replaceAll(DECIMAL_TYPE_SUFFIX_PATTERN, "");
                    return new BExpressionValue(context, createDecimalValueFrom(stringWithoutSuffix));
                } else {
                    // Removes float type suffix.
                    String stringWithoutSuffix = literalString.replaceAll(FLOAT_TYPE_SUFFIX_PATTERN, "");
                    return VMUtils.make(context, Double.parseDouble(stringWithoutSuffix));
                }
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                // Todo - add support for hex floats
                throw createUnsupportedLiteralError(literalString);
            default:
                throw createUnsupportedLiteralError(literalString);
        }
    }

    /**
     * Parses the user input string into a decimal value using ValueCreator API in ballerina runtime.
     *
     * @param decimalValueString user input string
     * @return parsed decimal value
     */
    private Value createDecimalValueFrom(String decimalValueString) throws EvaluationException {
        RuntimeStaticMethod method = getRuntimeMethod(context, B_VALUE_CREATOR_CLASS,
                CREATE_DECIMAL_VALUE_METHOD, Collections.singletonList(EvaluationUtils.JAVA_STRING_CLASS));
        method.setArgValues(Collections.singletonList(EvaluationUtils.getAsJString(context, decimalValueString)));
        return method.invokeSafely();
    }

    /**
     * Checks if the given literal string is a ballerina decimal literal.
     */
    private static boolean isDecimalType(String literalString) {
        Pattern pattern = Pattern.compile(DECIMAL_TYPE_SUFFIX_PATTERN);
        Matcher matcher = pattern.matcher(literalString);
        return matcher.find();
    }

    private static EvaluationException createUnsupportedLiteralError(String literalString) {
        return createEvaluationException("Unsupported basic literal detected: " + literalString);
    }
}
