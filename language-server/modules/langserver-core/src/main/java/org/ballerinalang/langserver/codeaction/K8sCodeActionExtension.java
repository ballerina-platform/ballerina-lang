/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.codeaction;

import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Code action extension implementation for Kubernetes.toml.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class K8sCodeActionExtension implements CodeActionExtension {

    @Override
    public boolean validate(CodeActionParams inputParams) {
        String uri = inputParams.getTextDocument().getUri();
        Path fileNamePath = Paths.get(URI.create(uri)).getFileName();
        if (fileNamePath == null) {
            return false;
        }
        String fileName = fileNamePath.toString();
        return fileName.equals(ProjectConstants.KUBERNETES_TOML);
    }

    @Override
    public List<? extends CodeAction> execute(CodeActionParams inputParams, CodeActionContext context,
                                              LanguageServerContext serverContext) {
        return K8sCodeActionRouter.getAvailableCodeActions(context);
    }
}
