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

package org.ballerinalang.debugadapter.evaluation.validator;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.parser.DebugParser;
import org.ballerinalang.debugadapter.evaluation.parser.DebugParserException;
import org.ballerinalang.debugadapter.evaluation.parser.ExpressionParser;
import org.ballerinalang.debugadapter.evaluation.validator.impl.ExpressionValidator;
import org.ballerinalang.debugadapter.evaluation.validator.impl.InvalidInputValidator;
import org.ballerinalang.debugadapter.evaluation.validator.impl.StatementValidator;
import org.ballerinalang.debugadapter.evaluation.validator.impl.TopLevelDeclarationValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be be used to validate debug user expression inputs and get the parsed syntax node of the expression,
 * if present.
 * <p>
 * Please note that this is a trial-based implementation and hence the user input might go though several parsing
 * cycles.
 *
 * @since 2.0.0
 */
public class SerialExpressionValidator extends Validator {

    private ExpressionValidator expressionValidator;
    private List<Validator> otherValidators;

    public SerialExpressionValidator() {
        super(new DebugParser());
        this.expressionValidator = null;
        this.otherValidators = null;
    }

    /**
     * Validates a given user expression input and retrieves the parsed syntax node of the expression,
     * if presents.
     *
     * @param source string source
     * @return syntax tree node instance of the user expression
     * @throws EvaluationException if any validation/parsing error is detected.
     */
    public ExpressionNode validateAndParse(String source) throws Exception {
        this.validate(source);
        // Retrieves the expression syntax tree, which was cached during the final validation phase.
        SyntaxTree cachedTree = expressionValidator.getDebugParser().getSyntaxTreeFor(source);
        return expressionValidator.getExpressionNodeFrom(cachedTree);
    }

    @Override
    public void validate(String source) throws Exception {
        loadValidators();
        try {
            expressionValidator.validate(source);
        } catch (DebugParserException | ValidatorException e) {
            // If any validation errors are detected during the expression validation, falls back on other validators
            // to determine the exact error.
            fallBackOnOtherValidators(source);
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    e.getMessage()));
        } catch (Exception e) {
            // If any generic errors are detected during the expression validation, falls back on other validators to
            // determine the exact error.
            fallBackOnOtherValidators(source);
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "expression validation failed due to: " + e.getMessage()));
        }
    }

    /**
     * Runs the user input through out all other validation phases to determine the exact error. Returns immediately
     * if any validation/parsing error is detected.
     *
     * @param source string source
     * @throws EvaluationException if any validation/parsing error is detected.
     */
    private void fallBackOnOtherValidators(String source) throws EvaluationException {
        for (Validator validator : otherValidators) {
            try {
                validator.validate(source);
            } catch (DebugParserException | ValidatorException e) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        e.getMessage()));
            } catch (Exception e) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "expression validation failed due to: " + e.getMessage()));
            }
        }
    }

    /**
     * Loads all validator types, which are required to process the user input.
     * Note: validation order is important and should be preserved, to reduce the number of parser trials.
     */
    private void loadValidators() {
        expressionValidator = new ExpressionValidator(new ExpressionParser());
        otherValidators = new ArrayList<>();
        otherValidators.add(new InvalidInputValidator(debugParser));
        otherValidators.add(new TopLevelDeclarationValidator(debugParser));
        otherValidators.add(new StatementValidator());
    }
}
