/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.toml.ballerinatoml.completion;

import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.toml.TomlCompletionExtension;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Completion extension implementation for Ballerina.toml file.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class BallerinaTomlCompletionExtension implements TomlCompletionExtension {

    @Override
    public boolean validate(CompletionParams inputParams) {
        String uri = inputParams.getTextDocument().getUri();
        Optional<Path> pathFromURI = CommonUtil.getPathFromURI(uri);
        Path fileNamePath = pathFromURI.isEmpty() ? null : pathFromURI.get().getFileName();
        if (fileNamePath == null) {
            return false;
        }
        String fileName = fileNamePath.toString();
        return fileName.equals(ProjectConstants.BALLERINA_TOML);
    }

    @Override
    public List<CompletionItem> execute(CompletionParams completionParams,
                                        CompletionContext completionContext,
                                        LanguageServerContext languageServerContext) throws Throwable {
        BallerinaTomlCompletionContext ballerinaTomlCompletionContext =
                new BallerinaTomlCompletionContext(completionContext, languageServerContext);
        return BallerinaTomlCompletionUtil.getCompletionItems(ballerinaTomlCompletionContext, languageServerContext);
    }
}
