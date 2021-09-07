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
package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Represents a named argument completion ite.
 *
 * @since 2.0.0
 */
public class NamedArgCompletionItem extends AbstractLSCompletionItem {

    private final Either<ParameterSymbol, RecordFieldSymbol> parameterSymbol;

    public NamedArgCompletionItem(BallerinaCompletionContext context, CompletionItem completionItem,
                                  Either<ParameterSymbol, RecordFieldSymbol> symbol) {
        super(context, completionItem, CompletionItemType.NAMED_ARG);
        this.parameterSymbol = symbol;
    }

    public Either<ParameterSymbol, RecordFieldSymbol> getParameterSymbol() {
        return parameterSymbol;
    }
}
