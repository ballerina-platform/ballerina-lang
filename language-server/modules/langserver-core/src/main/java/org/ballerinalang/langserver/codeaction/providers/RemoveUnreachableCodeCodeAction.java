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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;

/**
 * Code action to remove unreachable code.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class RemoveUnreachableCodeCodeAction extends AbstractCodeActionProvider {

    private static final String CODE_ACTION_NAME = "REMOVE_UNREACHABLE_CODE";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, CodeActionContext context) {
        if (!DiagnosticErrorCode.UNREACHABLE_CODE.diagnosticId().equals(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        LineRange lineRange = diagnostic.location().lineRange();
        TextEdit edit = new TextEdit(CommonUtil.toRange(lineRange), "");
        return List.of(createQuickFixCodeAction(CommandConstants.REMOVE_UNREACHABLE_CODE_TITLE, List.of(edit), context.fileUri()));
    }

    @Override
    public String getName() {
        return CODE_ACTION_NAME;
    }
}
