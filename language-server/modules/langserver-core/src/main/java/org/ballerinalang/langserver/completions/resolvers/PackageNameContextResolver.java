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

package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Completion Item Resolver for the Package name context.
 */
public class PackageNameContextResolver extends AbstractItemResolver {
    
    private static final ArrayList<String> TERMINAL_TOKENS = new ArrayList<>(Arrays.asList("\n", "\r", ";", "{",
            "<EOF>", "const", "function", "struct", "service", "connector", "enum", "transform", "import",
            "streamlet"));
    
    private static final String IMPORT_KEYWORD = "import";
    
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int currentTokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        StringBuilder importPkgName = new StringBuilder();
        Token evalToken = tokenStream.get(currentTokenIndex);
        
        while (true) {
            if (evalToken.getText().equals(IMPORT_KEYWORD)) {
                break;
            }
            currentTokenIndex--;
            if (currentTokenIndex < 0) {
                break;
            }
            evalToken = tokenStream.get(currentTokenIndex);
        }
        
        if (currentTokenIndex >= 0) {
            currentTokenIndex++;
            String tokenText;
            while (true) {
                if (currentTokenIndex > tokenStream.size()) {
                    break;
                }
                evalToken = tokenStream.get(currentTokenIndex);
                tokenText = evalToken.getText();
                if (evalToken.getChannel() == Token.DEFAULT_CHANNEL) {
                    if (TERMINAL_TOKENS.contains(tokenText)) {
                        break;
                    }
                    importPkgName.append(tokenText);
                }
                currentTokenIndex++;
            }
        }

        BLangPackageContext bLangPkgContext = completionContext.get(DocumentServiceKeys.B_LANG_PACKAGE_CONTEXT_KEY);
        ArrayList<PackageID> sdkPackages = bLangPkgContext
                .getSDKPackages(completionContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));

        ArrayList<String> currentLabelList = new ArrayList<>();
        sdkPackages.forEach(packageID -> {
            String packageName = packageID.getName().getValue();
            String comparingPkgName = importPkgName.toString().substring(0, importPkgName.toString().lastIndexOf("."))
                    + ".";
            
            if (packageName.startsWith(comparingPkgName)) {
                String trimmedPkgName = packageName.replaceFirst(comparingPkgName, "");
                String label = trimmedPkgName;
                String insertText = label;
                
                if (trimmedPkgName.contains(".")) {
                    label = trimmedPkgName.substring(0, trimmedPkgName.indexOf("."));
                    insertText = label + ".";
                }
                
                if (!currentLabelList.contains(label)) {
                    currentLabelList.add(label);
                    CompletionItem item = new CompletionItem();
                    item.setLabel(label);
                    item.setInsertText(insertText);
                    item.setKind(CompletionItemKind.File);
                    completionItems.add(item);
                }
            }
        });
        
        return completionItems;
    }
}
