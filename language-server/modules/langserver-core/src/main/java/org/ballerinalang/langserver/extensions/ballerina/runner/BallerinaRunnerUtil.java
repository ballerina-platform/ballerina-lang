/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.runner;

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.eclipse.lsp4j.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Ballerina Runner Util.
 *
 * @since 2201.11.0
 */
public class BallerinaRunnerUtil {

    public static Map<String, List<Diagnostic>> getErrorDiagnosticMap(WorkspaceManager workspaceManager,
                                                                      Project project, Path projectRoot) {
        Collection<io.ballerina.tools.diagnostics.Diagnostic> diagnostics = project.currentPackage()
                .getCompilation().diagnosticResult().errors();
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
        for (io.ballerina.tools.diagnostics.Diagnostic diag : diagnostics) {
            LineRange lineRange = diag.location().lineRange();
            Diagnostic result = DiagnosticsHelper.getLSDiagnosticsFromCompilationDiagnostics(lineRange, diag);
            String resolvedUri = projectRoot.resolve(lineRange.fileName()).toUri().toString();
            String fileURI = PathUtil.getModifiedUri(workspaceManager, resolvedUri);
            List<Diagnostic> clientDiagnostics = diagnosticsMap.computeIfAbsent(fileURI, s -> new ArrayList<>());
            clientDiagnostics.add(result);
        }
        return diagnosticsMap;
    }

    public static BallerinaRunnerService.TypeBindingPair extractParamDetails(ParameterNode param) {
        switch (param.kind()) {
            case DEFAULTABLE_PARAM -> {
                DefaultableParameterNode defaultableParam = (DefaultableParameterNode) param;
                return new BallerinaRunnerService.TypeBindingPair(
                        defaultableParam.typeName().toString().strip(),
                        Objects.requireNonNull(defaultableParam.paramName().orElse(null)).text(),
                        defaultableParam.expression().toString());
            }
            case REST_PARAM -> {
                RestParameterNode restParam = (RestParameterNode) param;
                return new BallerinaRunnerService.TypeBindingPair(
                        restParam.typeName().toString().strip(),
                        Objects.requireNonNull(restParam.paramName().orElse(null)).text(),
                        null);
            }
            case INCLUDED_RECORD_PARAM -> {
                IncludedRecordParameterNode includedRecordParam = (IncludedRecordParameterNode) param;
                return new BallerinaRunnerService.TypeBindingPair(
                        includedRecordParam.typeName().toString().strip(),
                        Objects.requireNonNull(includedRecordParam.paramName().orElse(null)).text(),
                        null);
            }
            default -> {
                RequiredParameterNode requiredParam = (RequiredParameterNode) param;
                return new BallerinaRunnerService.TypeBindingPair(
                        requiredParam.typeName().toString().strip(),
                        Objects.requireNonNull(requiredParam.paramName().orElse(null)).text(),
                        null);
            }
        }
    }
}
