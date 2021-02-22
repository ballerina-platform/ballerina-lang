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

import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Code Action for marking as untainted.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MarkAsUntaintedCodeAction extends AbstractCodeActionProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().contains(CommandConstants.TAINTED_PARAM_PASSED))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.message();
        String uri = context.fileUri();
        Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 0) {
            String param = matcher.group(1);
            String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
            // Extract specific content range
            Range range = CommonUtil.toRange(diagnostic.location().lineRange());
            Document document = context.workspace().document(context.filePath()).orElseThrow();
            String content = getContent(document, CommonUtil.toRange(diagnostic.location().lineRange()));
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

    private String getContent(Document document, Range range) {
        Position start = range.getStart();
        Position end = range.getEnd();
        TextDocument textDocument = document.textDocument();
        if (start.getLine() == end.getLine()) {
            String line = textDocument.line(start.getLine()).text();
            return line.substring(start.getCharacter(), end.getCharacter());
        } else {
            StringBuilder str = new StringBuilder();
            // Append start line
            str.append(textDocument.line(start.getLine()).text().substring(start.getCharacter()));
            // Append middle lines
            int diff = end.getLine() - start.getLine();
            if (diff > 1) {
                for (int i = 0; i < diff; i++) {
                    str.append(textDocument.line(start.getLine()).text());
                }
            }
            // Append end line
            str.append(textDocument.line(start.getLine()).text(), 0, end.getCharacter());
            return str.toString();
        }
    }
}
