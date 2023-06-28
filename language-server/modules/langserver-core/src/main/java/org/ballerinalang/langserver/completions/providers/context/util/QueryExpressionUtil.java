/*
 *  Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility to provide completions related to query expressions.
 */
public class QueryExpressionUtil {

    public static List<LSCompletionItem> getCommonKeywordCompletions(BallerinaCompletionContext context) {
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_WHERE.get()),
                new SnippetCompletionItem(context, Snippet.KW_LET.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_LET.get()),
                new SnippetCompletionItem(context, Snippet.KW_OUTER.get()),
                new SnippetCompletionItem(context, Snippet.KW_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.KW_ORDERBY.get()),
                new SnippetCompletionItem(context, Snippet.KW_LIMIT.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_DO.get()),
                new SnippetCompletionItem(context, Snippet.KW_SELECT.get()),
                new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                new SnippetCompletionItem(context, Snippet.KW_GROUPBY.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_GROUPBY.get()),
                new SnippetCompletionItem(context, Snippet.KW_COLLECT.get())
        );
    }

    public static List<FunctionSymbol> getLangLibMethods(BallerinaCompletionContext context) {

        Types types = context.currentSemanticModel().get().types();
        
        List<FunctionSymbol> langLibMethods = types.INT.langLibMethods();
        langLibMethods.addAll(types.DECIMAL.langLibMethods());
        langLibMethods.addAll(types.FLOAT.langLibMethods());
        langLibMethods.addAll(types.STRING.langLibMethods());
        langLibMethods.addAll(types.BOOLEAN.langLibMethods());
        langLibMethods.addAll(types.ANY.langLibMethods());
        langLibMethods.addAll(types.ANYDATA.langLibMethods());
        langLibMethods.addAll(types.BYTE.langLibMethods());
        langLibMethods.addAll(types.ERROR.langLibMethods());
        langLibMethods.addAll(types.FUNCTION.langLibMethods());
        langLibMethods.addAll(types.FUTURE.langLibMethods());
        langLibMethods.addAll(types.HANDLE.langLibMethods());
        langLibMethods.addAll(types.JSON.langLibMethods());
        langLibMethods.addAll(types.NEVER.langLibMethods());
        langLibMethods.addAll(types.NIL.langLibMethods());
        langLibMethods.addAll(types.READONLY.langLibMethods());
        langLibMethods.addAll(types.REGEX.langLibMethods());
        langLibMethods.addAll(types.STREAM.langLibMethods());
        langLibMethods.addAll(types.TYPEDESC.langLibMethods());
        langLibMethods.addAll(types.XML.langLibMethods());

        return langLibMethods.stream().distinct().collect(Collectors.toList());
    }
}
