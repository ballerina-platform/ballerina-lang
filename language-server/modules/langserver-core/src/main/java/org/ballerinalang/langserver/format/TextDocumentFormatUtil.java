/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.format;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilities for text document format.
 */
public class TextDocumentFormatUtil {

    /**
     * Get the AST for the current text document's content.
     *
     * @param params          Document Formatting parameters
     * @param documentManager Workspace document manager instance
     * @param context         Document formatting context
     * @return {@link JsonObject}   AST as a Json Object
     */
    public static JsonObject getAST(DocumentFormattingParams params, WorkspaceDocumentManager documentManager,
                                    LSServiceOperationContext context) {
        String documentUri = params.getTextDocument().getUri();
        String[] uriParts = documentUri.split(Pattern.quote(File.separator));
        String fileName = uriParts[uriParts.length - 1];
        final BLangPackage bLangPackage = TextDocumentServiceUtil.getBLangPackage(context, documentManager,
                true, LSCustomErrorStrategy.class, false).get(0);
        context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, bLangPackage.symbol.getName().getValue());
        final List<Diagnostic> diagnostics = new ArrayList<>();
        JsonArray errors = new JsonArray();
        JsonObject result = new JsonObject();
        result.add("errors", errors);

        Gson gson = new Gson();
        JsonElement diagnosticsJson = gson.toJsonTree(diagnostics);
        result.add("diagnostics", diagnosticsJson);

        BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().stream().
                filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().orElseGet(null);
        JsonElement modelElement = CommonUtil.generateJSON(compilationUnit, new HashMap<>());
        result.add("model", modelElement);

        return result;
    }
}
