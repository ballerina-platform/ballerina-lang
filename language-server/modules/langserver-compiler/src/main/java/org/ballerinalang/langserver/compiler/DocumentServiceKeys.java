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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

/**
 * Text Document Service context keys for the completion operation context.
 *
 * @since 0.95.5
 */
public class DocumentServiceKeys {
    public static final LSContext.Key<String> FILE_URI_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<TextDocumentPositionParams> POSITION_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> RELATIVE_FILE_PATH_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<CompilerContext> COMPILER_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<Either<SymbolInformation, DocumentSymbol>>> SYMBOL_LIST_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> CURRENT_PKG_NAME_KEY
            = new LSContext.Key<>();
    @Deprecated
    public static final LSContext.Key<PackageID> CURRENT_PACKAGE_ID_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> SOURCE_ROOT_KEY
            = new LSContext.Key<>();
    @Deprecated
    public static final LSContext.Key<BLangPackage> CURRENT_BLANG_PACKAGE_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<ModuleSymbol> CURRENT_MODULE_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<BLangPackage>> BLANG_PACKAGES_CONTEXT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<String> SYMBOL_QUERY
            = new LSContext.Key<>();
    public static final LSContext.Key<LSDocumentIdentifier> LS_DOCUMENT_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<List<BLangImportPackage>> CURRENT_DOC_IMPORTS_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> IS_CACHE_SUPPORTED
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> IS_CACHE_OUTDATED_SUPPORTED
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> COMPILE_FULL_PROJECT
            = new LSContext.Key<>();
    public static final LSContext.Key<WorkspaceDocumentManager> DOC_MANAGER_KEY
            = new LSContext.Key<>();
    public static final LSContext.Key<Boolean> ENABLE_STDLIB_DEFINITION_KEY
            = new LSContext.Key<>();
}
