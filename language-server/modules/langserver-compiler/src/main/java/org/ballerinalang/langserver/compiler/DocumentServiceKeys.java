/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

/**
 * Text Document Service context keys for the completion operation context.
 * @since 0.95.5
 */
public class DocumentServiceKeys {
    public static final LSContext.Key<String> FILE_URI_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<TextDocumentPositionParams> POSITION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> FILE_NAME_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<CompilerContext> COMPILER_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<ParserRuleContext> PARSER_RULE_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<TokenStream> TOKEN_STREAM_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Vocabulary> VOCABULARY_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Integer> TOKEN_INDEX_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<SymbolTable> SYMBOL_TABLE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<SymbolInformation>> SYMBOL_LIST_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> CURRENT_PACKAGE_NAME_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<LSContext> OPERATION_META_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<BLangPackage> CURRENT_BLANG_PACKAGE_CONTEXT_KEY
            = new LSContext.Key<>();
}
