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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.identifier.Utils;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.SymbolBasedArgProcessor;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_ACCESS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_FUNCTION;
import static org.ballerinalang.debugadapter.evaluation.engine.EvaluationTypeResolver.isPublicSymbol;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.constructQualifiedClassName;

/**
 * Evaluator implementation for function invocation expressions.
 *
 * @since 2.0.0
 */
public class FunctionInvocationExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final String functionName;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;

    public FunctionInvocationExpressionEvaluator(EvaluationContext context, FunctionCallExpressionNode node,
                                                 List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
        this.functionName = syntaxNode.functionName().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE ?
                ((QualifiedNameReferenceNode) syntaxNode.functionName()).identifier().text().trim() :
                syntaxNode.functionName().toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            FunctionSymbol functionDef = resolveFunctionDefinitionSymbol();
            String className = constructQualifiedClassName(functionDef);
            GeneratedStaticMethod jvmMethod = EvaluationUtils.getGeneratedMethod(context, className, functionName);

            SymbolBasedArgProcessor argProcessor = new SymbolBasedArgProcessor(context, functionName, jvmMethod
                    .getJDIMethodRef(), functionDef);
            List<Value> orderedArgsList = argProcessor.process(argEvaluators);

            jvmMethod.setArgValues(orderedArgsList);
            Value result = jvmMethod.invokeSafely();
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    private FunctionSymbol resolveFunctionDefinitionSymbol() throws EvaluationException {

        List<FunctionSymbol> functionMatches;
        Optional<String> modulePrefix;
        // If the function name is a qualified name reference, need to resolve the imported module symbol first.
        if (syntaxNode.functionName().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            modulePrefix = Optional.of(((QualifiedNameReferenceNode) syntaxNode.functionName()).modulePrefix().text());
            if (!resolvedImports.containsKey(modulePrefix.get())) {
                throw createEvaluationException(IMPORT_RESOLVING_ERROR, modulePrefix.get());
            }
            functionMatches = resolvedImports.get(modulePrefix.get()).getResolvedSymbol().functions().stream()
                    .filter(symbol -> symbol.getName().isPresent() &&
                            modifyName(symbol.getName().get()).equals(functionName))
                    .collect(Collectors.toList());

            if (functionMatches.size() == 1 && !isPublicSymbol(functionMatches.get(0))) {
                throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_ACCESS, functionName);
            }
        } else {
            modulePrefix = Optional.empty();
            SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
            functionMatches = semanticContext.moduleSymbols()
                    .stream()
                    .filter(symbol -> symbol.kind() == FUNCTION && symbol.getName().isPresent()
                            && modifyName(symbol.getName().get()).equals(functionName))
                    .map(symbol -> (FunctionSymbol) symbol)
                    .collect(Collectors.toList());
        }

        if (functionMatches.isEmpty()) {
            if (modulePrefix.isPresent()) {
                throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_FUNCTION, functionName, modulePrefix.get());
            } else {
                throw createEvaluationException(FUNCTION_NOT_FOUND, syntaxNode.functionName().toSourceCode().trim());
            }
        } else if (functionMatches.size() > 1) {
            throw createEvaluationException("Multiple function definitions found with name: '" + functionName + "'");
        }

        return functionMatches.get(0);
    }

    /**
     * This util is used as a workaround till the ballerina identifier encoding/decoding mechanisms get fixed.
     * Todo - remove
     */
    public static String modifyName(String identifier) {
        return Utils.decodeIdentifier(IdentifierModifier.encodeIdentifier(identifier,
                IdentifierModifier.IdentifierType.OTHER));
    }
}
