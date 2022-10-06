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

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.modifyName;

/**
 * Class resolver implementation for resolving visible Ballerina class definitions(object types) for a given source
 * location, using the semantic APIs.
 *
 * @since 2.0.0
 */
public class ClassDefinitionResolver {

    private final SuspendedContext context;

    public ClassDefinitionResolver(SuspendedContext context) {
        this.context = context;
    }

    public Optional<ClassSymbol> findBalClassDefWithinModule(String className) {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        return semanticContext.moduleSymbols()
                .stream()
                .filter(symbol -> symbol.kind() == SymbolKind.CLASS
                        && modifyName(symbol.getName().orElse("")).equals(className))
                .findFirst()
                .map(symbol -> (ClassSymbol) symbol);
    }

    public Optional<ClassSymbol> findBalClassDefWithinModule(ModuleSymbol moduleSymbol, String className) {
        return moduleSymbol.classes().stream()
                .filter(symbol -> modifyName(symbol.getName().orElse("")).equals(className))
                .findFirst();
    }

    public Optional<ClassSymbol> findBalClassDefWithinDependencies(String orgName, String packageName,
                                                                   String className) {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        Optional<ModuleSymbol> moduleSymbol = semanticContext
                .visibleSymbols(context.getDocument(), LinePosition.from(context.getLineNumber(), 0))
                .stream()
                .filter(symbol -> symbol.kind() == SymbolKind.MODULE)
                .map(symbol -> (ModuleSymbol) symbol)
                .filter(symbol -> symbol.id().orgName().equals(orgName)
                        && symbol.id().modulePrefix().equals(packageName))
                .findAny();

        if (moduleSymbol.isEmpty()) {
            return Optional.empty();
        }

        return moduleSymbol.get().classes().stream()
                .filter(symbol -> symbol.getName().get().equals(className))
                .findAny();
    }
}
