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
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.generateNamedArgs;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;

/**
 * Evaluator implementation for function invocation expressions.
 *
 * @since 2.0.0
 */
public class FunctionInvocationExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final String functionName;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;

    public FunctionInvocationExpressionEvaluator(SuspendedContext context, FunctionCallExpressionNode node,
                                                 List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
        this.functionName = syntaxNode.functionName().toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // Trying to get the function definition information using the semantic API.
            Optional<FunctionSymbol> functionDef = findFunctionWithinModule();
            if (functionDef.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                        functionName));
            }

            String className = constructClassNameFrom(functionDef.get());
            GeneratedStaticMethod jvmMethod = EvaluationUtils.getGeneratedMethod(context, className, functionName);
            FunctionTypeSymbol functionTypeDesc = functionDef.get().typeDescriptor();
            Map<String, Value> argValueMap = generateNamedArgs(context, functionName, functionTypeDesc, argEvaluators);
            jvmMethod.setNamedArgValues(argValueMap);
            Value result = jvmMethod.invoke();
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private Optional<FunctionSymbol> findFunctionWithinModule() {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        List<Symbol> functionMatches = semanticContext.moduleLevelSymbols()
                .stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION
                        && modifyName(symbol.name()).equals(functionName))
                .collect(Collectors.toList());
        if (functionMatches.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable((FunctionSymbol) functionMatches.get(0));
    }

    private String constructClassNameFrom(FunctionSymbol functionDef) {
        String className = functionDef.location().lineRange().filePath().replaceAll(BAL_FILE_EXT + "$", "");
        ModuleID moduleMeta = functionDef.moduleID();

        return new StringJoiner(".")
                .add(encodeModuleName(moduleMeta.orgName()))
                .add(encodeModuleName(moduleMeta.moduleName()))
                .add(moduleMeta.version().replaceAll("\\.", "_"))
                .add(className)
                .toString();
    }

    /**
     * This util is used as a workaround till the ballerina identifier encoding/decoding mechanisms get fixed.
     * Todo - remove
     */
    public static String modifyName(String identifier) {
        return IdentifierUtils.decodeIdentifier(IdentifierModifier.encodeIdentifier(identifier,
                IdentifierModifier.IdentifierType.OTHER));
    }
}
