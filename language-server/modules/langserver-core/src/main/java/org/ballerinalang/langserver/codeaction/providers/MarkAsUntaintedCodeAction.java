/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Code Action for marking as untainted.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MarkAsUntaintedCodeAction extends AbstractCodeActionProvider {

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.TAINTED_PARAM_PASSED))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.fileUri();
        Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 0) {
            String param = matcher.group(1);
            String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
            // Extract specific content range
            Range range = diagnostic.getRange();
            String content = positionDetails.matchedNode().toSourceCode();
            // Add `untaint` keyword
            matcher = CommandConstants.NO_CONCAT_PATTERN.matcher(content);
            String editText = matcher.find() ? "<@untainted>  " + content : "<@untainted> (" + content + ")";
            // Create text-edit
            List<TextEdit> edits = new ArrayList<>();
            edits.add(new TextEdit(range, editText));
            return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return new ArrayList<>();
    }
}
