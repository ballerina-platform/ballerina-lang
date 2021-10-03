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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.VariableUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_ACCESS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.VARIABLE_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.engine.EvaluationTypeResolver.isPublicSymbol;

/**
 * Ballerina qualified name reference evaluator implementation.
 *
 * @since 2.0.0
 */
public class QualifiedNameReferenceEvaluator extends Evaluator {

    private final String nameReference;
    private final QualifiedNameReferenceNode syntaxNode;

    public QualifiedNameReferenceEvaluator(EvaluationContext context, QualifiedNameReferenceNode node) {
        super(context);
        this.syntaxNode = node;
        this.nameReference = node.identifier().text().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            String modulePrefix = syntaxNode.modulePrefix().text();
            if (!resolvedImports.containsKey(modulePrefix)) {
                throw createEvaluationException(IMPORT_RESOLVING_ERROR, modulePrefix);
            }

            // Validates whether the given module has a public constant, using semantic API.
            ModuleSymbol moduleSymbol = resolvedImports.get(modulePrefix).getModuleSymbol();
            List<ConstantSymbol> constSymbol = moduleSymbol.constants().stream()
                    .filter(constantSymbol -> constantSymbol.getName().isPresent()
                            && constantSymbol.getName().get().equals(nameReference))
                    .collect(Collectors.toList());

            if (constSymbol.isEmpty()) {
                throw createEvaluationException(String.format("undefined/non-public constant '%s' in module '%s'",
                        nameReference, modulePrefix));
            } else if (!isPublicSymbol(constSymbol.get(0))) {
                throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_ACCESS, nameReference);
            }

            Optional<BExpressionValue> moduleVariable = VariableUtils.searchModuleVariables(context, moduleSymbol,
                    nameReference);
            if (moduleVariable.isEmpty()) {
                throw createEvaluationException(VARIABLE_NOT_FOUND, nameReference);
            }
            return moduleVariable.get();
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}
