/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {

    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private static final String COMMA = ",";
    private static final List<String> TERMINAL_CHARACTERS = Arrays.asList(OPEN_BRACKET, COMMA, ".");

    /**
     * Get the name of the callable item. This ideally should be a ballerina function.
     *
     * @param position          Position of the signature help triggered
     * @param fileContent       File content to search callable item name
     * @return {@link String}   Callable Item name
     */
    public static String getCallableItemName(Position position, String fileContent) {
        int lineNumber = position.getLine();
        int character = position.getCharacter();
        // Here add offset of 2 since the indexing is zero based
        String[] lineTokens = fileContent.split("\\r?\\n", lineNumber + 2);
        String line = lineTokens[lineNumber];

        int backTrackPosition = character - 1;
        Stack<String> closeBracketStack = new Stack<>();


        while (true) {
            if (backTrackPosition < 0) {
                return "";
            }
            String currentToken = Character.toString(line.charAt(backTrackPosition));
            if (CLOSE_BRACKET.equals(currentToken)) {
                closeBracketStack.push(CLOSE_BRACKET);
            } else if (OPEN_BRACKET.equals(currentToken)) {
                if (!closeBracketStack.isEmpty()) {
                    closeBracketStack.pop();
                } else {
                    return getFunctionName(line, backTrackPosition - 1);
                }
            }
            backTrackPosition--;
        }
    }

    /**
     * Get the functionSignatureHelp instance.
     *
     * @param functionName              name of the function
     * @param pkg                       BLang Package
     * @param compilerContext           compiler context instance
     * @return {@link SignatureHelp}    Signature help for the completion
     */
    public static SignatureHelp getFunctionSignatureHelp(String functionName, BLangPackage pkg,
                                                         CompilerContext compilerContext) {
        SymbolResolver symbolResolver = SymbolResolver.getInstance(compilerContext);
        SymbolEnv pkgEnv = SymbolEnter.getInstance(compilerContext).packageEnvs.get(pkg.symbol);
        List<List<SignatureParamInfo>> paramLists = new ArrayList<>();

        Map<Name, Scope.ScopeEntry> visibleSymbols = symbolResolver.getAllVisibleInScopeSymbols(pkgEnv);
        // Scan through the visible packages
        List<Scope.ScopeEntry> bInvokableSymbols = visibleSymbols.entrySet()
                .stream()
                .filter(mapEntry -> mapEntry.getValue().symbol instanceof BInvokableSymbol
                        && mapEntry.getValue().symbol.getName().getValue().equals(functionName))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        bInvokableSymbols.forEach(bInvokableSymbol -> {
            List<SignatureParamInfo> paramList = new ArrayList<>();
            ((BInvokableSymbol) bInvokableSymbol.symbol).getParameters().forEach(bVarSymbol -> {
                SignatureParamInfo signatureParamInfo = new SignatureParamInfo();
                signatureParamInfo.setParamType(bVarSymbol.getType().toString());
                signatureParamInfo.setParamValue(bVarSymbol.getName().getValue());
                paramList.add(signatureParamInfo);
            });
            paramLists.add(paramList);
        });

        SignatureHelp signatureHelp = new SignatureHelp();
        signatureHelp.setSignatures(getSignatureInformations(paramLists, functionName));
        signatureHelp.setActiveParameter(0);
        signatureHelp.setActiveSignature(0);

        return signatureHelp;
    }

    private static List<SignatureInformation> getSignatureInformations(List<List<SignatureParamInfo>> paramInfoList,
                                                                      String funcName) {

        List<SignatureInformation> signatureInformationList = new ArrayList<>();
        paramInfoList.forEach(paramsList -> {
            String signature = funcName + "(" +
                    paramsList.stream()
                            .map(SignatureParamInfo::toString)
                            .collect(Collectors.joining(", ")) +
                    ")";
            SignatureInformation signatureInformation = new SignatureInformation();
            signatureInformation.setLabel(signature);
            signatureInformationList.add(signatureInformation);
        });
        return signatureInformationList;
    }

    private static String getFunctionName(String line, int startPosition) {
        int counter = startPosition;
        String callableItemName = "";
        while (true) {
            if (counter < 0) {
                return callableItemName;
            }
            char c = line.charAt(counter);
            if (!(Character.isLetterOrDigit(c)
                    || "_".equals(Character.toString(c))) || TERMINAL_CHARACTERS.contains(Character.toString(c))) {
                callableItemName = line.substring(counter + 1, startPosition + 1);
                break;
            }
            counter--;
        }
        return callableItemName;
    }

    private static class SignatureParamInfo {
        private String paramValue;

        private String paramType;

        void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        void setParamType(String paramType) {
            this.paramType = paramType;
        }

        @Override
        public String toString() {
            return this.paramType + " " + this.paramValue;
        }
    }
}
