/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.codeaction.spi;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;

import java.util.List;

/**
 * Represents the interface for the Ballerina Range Based Code Action Provider.
 *
 * @since 2201.2.0
 */
public interface RangeBasedCodeActionProvider extends LSCodeActionProvider {

    /**
     * Returns the list of code actions based on cursor position or selected range.
     *
     * @param context    code action context
     * @param posDetails range based position details
     * @return list of Code Actions
     */
    List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails);

    /**
     * Returns the list of syntax kinds that the code action belongs to.
     *
     * @return list of syntax kinds
     */
    List<SyntaxKind> getSyntaxKinds();

    /**
     * Checks whether the syntax is valid.
     *
     * @param context         code action context
     * @param positionDetails {@link RangeBasedPositionDetails}
     * @return True if syntactically correct, false otherwise.
     */
    default boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return true;
    }
}
