/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import com.google.common.primitives.Ints;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Highlight provider for Semantic Highlighting.
 */

public class SemanticHighlightProvider {

public void getHighlights(ExtendedLanguageClient client, LSContext context, WorkspaceDocumentManager docManager)
        throws CompilationFailedException {

    LSModuleCompiler.getBLangPackages(context, docManager, null, true, true, true);

    List<SemanticHighlightProvider.HighlightInfo> highlights = new ArrayList<SemanticHighlightProvider.HighlightInfo>();
    context.put(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY, highlights);

    SemanticHighlightingVisitor semanticHighlightingVisitor = new SemanticHighlightingVisitor(context);
    BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
    //TODO Trace log update
    if (bLangPackage != null) {
        bLangPackage.accept(semanticHighlightingVisitor);
    }

    Map<Integer, int[]> lineInfo = new HashMap<>();
    context.get(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY)
            .forEach(element-> {
                SemanticHighlightingToken semanticHighlightingToken = new SemanticHighlightingToken
                        (element.identifier.pos.sCol - 1, element.identifier.pos.eCol - element.identifier.pos.sCol,
                                element.scopeEnum.getScopeId());
                int line = element.identifier.pos.sLine - 1;
                int[] token = {semanticHighlightingToken.getCharacter(),
                        semanticHighlightingToken.getLength(), semanticHighlightingToken.getScope()};
                if (lineInfo.get(line) != null) {
                    int[] cur = lineInfo.get(line);
                    lineInfo.put(line, Ints.concat(cur, token));
                } else {
                    lineInfo.put(line, token);
                }
                String tokenArr = Arrays.toString(lineInfo.get(line));
                String encodedToken = Base64.getEncoder().encodeToString(tokenArr.getBytes(Charset.forName("UTF-8")));
                SemanticHighlightingInformation semanticHighlightingInformation
                        = new SemanticHighlightingInformation(line, encodedToken);
                client.publishTextHighlighting(semanticHighlightingInformation);
            });
}

/**
 * Highlight information for each token.
 */
public static class HighlightInfo {

    ScopeEnum scopeEnum;
    BLangIdentifier identifier;

    public HighlightInfo(ScopeEnum scopeEnum, BLangIdentifier identifier) {
        this.scopeEnum = scopeEnum;
        this.identifier = identifier;
    }

    public ScopeEnum getScopeEnum() {
        return scopeEnum;
    }

    public BLangIdentifier getIdentifier() {
        return identifier;
    }
}
}
