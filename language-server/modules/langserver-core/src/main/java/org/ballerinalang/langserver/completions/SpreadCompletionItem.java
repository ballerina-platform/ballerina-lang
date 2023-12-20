/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Optional;

/**
 * Represents a Spread Completion Item.
 * Eg: ...varName
 *
 * @since 2201.8.0
 */
public class SpreadCompletionItem extends AbstractLSCompletionItem {
    private final Symbol expression;

    public SpreadCompletionItem(BallerinaCompletionContext context, CompletionItem completionItem, Symbol expression) {
        super(context, completionItem, CompletionItemType.SPREAD);
        this.expression = expression;
    }

    public Optional<Symbol> getExpression() {
        return Optional.ofNullable(expression);
    }
}
