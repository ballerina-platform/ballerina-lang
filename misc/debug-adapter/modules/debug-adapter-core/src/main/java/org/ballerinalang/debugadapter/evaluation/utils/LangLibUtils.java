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

package org.ballerinalang.debugadapter.evaluation.utils;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.FunctionNodeFinder;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.LANG_LIB_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.LANG_LIB_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.variable.BVariableType.ARRAY;
import static org.ballerinalang.debugadapter.variable.BVariableType.INT;
import static org.ballerinalang.debugadapter.variable.BVariableType.MAP;

/**
 * Ballerina lang libraries related utils.
 *
 * @since 2.0.0
 */
public class LangLibUtils {

    public static final String LANG_LIB_ORG = "ballerina";
    public static final String LANG_LIB_PACKAGE_PREFIX = "lang.";
    public static final String LANG_LIB_VALUE = "value";

    private LangLibUtils() {
    }

    public static Optional<Package> getLangLibPackage(SuspendedContext context, String langLibName) {
        Project project = context.getProject();
        PackageCache pkgCache = project.projectEnvironmentContext().environment().getService(PackageCache.class);
        List<Package> packages = pkgCache.getPackages(PackageOrg.from(LANG_LIB_ORG),
                PackageName.from(LANG_LIB_PACKAGE_PREFIX + langLibName));

        if (packages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(packages.get(0));
    }

    public static GeneratedStaticMethod loadLangLibMethod(SuspendedContext context, BExpressionValue result,
                                                          String langLibCls, String method) throws EvaluationException {
        try {
            return getGeneratedMethod(context, langLibCls, method);
        } catch (EvaluationException e) {
            throw createEvaluationException(LANG_LIB_METHOD_NOT_FOUND, method, result.getType().getString());
        }
    }

    public static String getQualifiedLangLibClassName(Package langLibPackage, String langLibName)
            throws EvaluationException {
        try {
            return new StringJoiner(".")
                    .add(LANG_LIB_ORG)
                    .add(encodeModuleName(LANG_LIB_PACKAGE_PREFIX + langLibName))
                    .add(String.valueOf(langLibPackage.packageVersion().value().major()))
                    .add(langLibName)
                    .toString();
        } catch (Exception ignored) {
            throw createEvaluationException(LANG_LIB_NOT_FOUND, langLibName);
        }
    }

    public static Optional<ModuleSymbol> getLangLibDefinition(SuspendedContext context, String langLibName) {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        LinePosition position = LinePosition.from(context.getLineNumber(), 0);

        return semanticContext.visibleSymbols(context.getDocument(), position)
                .stream()
                .filter(symbol -> {
                    if (symbol.kind() != MODULE) {
                        return false;
                    }

                    ModuleID moduleID = ((ModuleSymbol) symbol).id();
                    return moduleID.orgName().equals(LANG_LIB_ORG)
                            && moduleID.moduleName().startsWith(LANG_LIB_PACKAGE_PREFIX)
                            && moduleID.moduleName().endsWith(langLibName);
                })
                .findFirst()
                .map(symbol -> (ModuleSymbol) symbol);
    }

    public static Optional<FunctionDefinitionNode> getLangLibFunctionDefinition(Package langLib, String functionName) {
        FunctionNodeFinder functionFinder = new FunctionNodeFinder(functionName);
        return functionFinder.searchInModule(langLib.getDefaultModule());
    }

    public static String getAssociatedLangLibName(BVariableType bVarType) {
        switch (bVarType) {
            case INT:
            case BYTE:
                return INT.getString();
            case ARRAY:
            case TUPLE:
                return ARRAY.getString();
            case RECORD:
            case MAP:
                return MAP.getString();
            case FLOAT:
            case DECIMAL:
            case STRING:
            case BOOLEAN:
            case STREAM:
            case OBJECT:
            case ERROR:
            case FUTURE:
            case TYPE_DESC:
            case XML:
            case TABLE:
                return bVarType.getString();
            default:
                return LANG_LIB_VALUE;
        }
    }
}
