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
package org.ballerinalang.langserver.signature;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {

    private static final String OPEN_BRACKET = "(";

    private static final String CLOSE_BRACKET = ")";

    private static final String COMMA = ",";

    private static final List<String> TERMINAL_CHARACTERS = Arrays.asList(OPEN_BRACKET, COMMA, ".");

    private SignatureHelpUtil() {
    }

    /**
     * Capture the callable item information such as name, package of the item, delimiter (. or :), and etc.
     *
     * @param position          Position of the signature help triggered
     * @param fileContent       File content to search callable item name
     * @param serviceContext    Text Document service context instance for the signature help operation
     */
    public static void captureCallableItemInfo(Position position, String fileContent,
                                               LSServiceOperationContext serviceContext) {
        int lineNumber = position.getLine();
        int character = position.getCharacter();
        int paramCounter = 0;
        // Here add offset of 2 since the indexing is zero based
        String[] lineTokens = fileContent.split("\\r?\\n", lineNumber + 2);
        String line = lineTokens[lineNumber];

        int backTrackPosition = character - 1;
        Deque<String> closeBracketStack = new ArrayDeque<>();

        while (true) {
            if (backTrackPosition < 0) {
                break;
            }
            String currentToken = Character.toString(line.charAt(backTrackPosition));
            if (COMMA.equals(currentToken)) {
                paramCounter++;
            } else if (CLOSE_BRACKET.equals(currentToken)) {
                closeBracketStack.push(CLOSE_BRACKET);
            } else if (OPEN_BRACKET.equals(currentToken)) {
                if (!closeBracketStack.isEmpty()) {
                    closeBracketStack.pop();
                    paramCounter = 0;
                } else {
                    setItemInfo(line, backTrackPosition - 1, serviceContext);
                }
            }
            backTrackPosition--;
        }

        serviceContext.put(SignatureKeys.PARAMETER_COUNT, paramCounter);
    }

    /**
     * Get the functionSignatureHelp instance.
     *
     * @param ctx                       Signature help context
     * @return {@link SignatureHelp}    Signature help for the completion
     */
    public static SignatureHelp getFunctionSignatureHelp(LSServiceOperationContext ctx) {
        String delimiter = ctx.get(SignatureKeys.ITEM_DELIMITER);
        String idAgainst = ctx.get(SignatureKeys.IDENTIFIER_AGAINST);
        String funcName = ctx.get(SignatureKeys.CALLABLE_ITEM_NAME);
        List<SymbolInfo> visibleSymbols = ctx.get(SignatureKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> functions;

        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        
        if (!idAgainst.isEmpty() && (delimiter.equals(UtilSymbolKeys.DOT_SYMBOL_KEY)
                || delimiter.equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD))) {
            functions = FilterUtils.getInvocationAndFieldSymbolsOnVar(ctx, idAgainst, delimiter, visibleSymbols);
        } else if (!idAgainst.isEmpty() && (delimiter.equals(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY))) {
            functions = getEndpointActionsByName(idAgainst, visibleSymbols);
        } else {
            functions = visibleSymbols;
        }

        functions.removeIf(symbolInfo -> !CommonUtil.isValidInvokableSymbol(symbolInfo.getScopeEntry().symbol));
        List<SignatureInformation> signatureInformationList = functions
                .stream()
                .map(symbolInfo
                        -> getSignatureInformation((BInvokableSymbol) symbolInfo.getScopeEntry().symbol, funcName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        SignatureHelp signatureHelp = new SignatureHelp();
        signatureHelp.setSignatures(signatureInformationList);
        signatureHelp.setActiveParameter(ctx.get(SignatureKeys.PARAMETER_COUNT));
        signatureHelp.setActiveSignature(0);

        return signatureHelp;
    }

    /**
     * Get the signature information for the given Ballerina function.
     *
     * @param bInvokableSymbol BLang Invokable symbol
     * @return {@link SignatureInformation}     Signature information for the function
     */
    private static SignatureInformation getSignatureInformation(BInvokableSymbol bInvokableSymbol, String funcName) {
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        List<String> nameComps = Arrays.asList(bInvokableSymbol.getName().getValue().split("\\."));
        
        if (!funcName.equals(CommonUtil.getLastItem(nameComps))) {
            return null;
        }

        SignatureInfoModel signatureInfoModel = getSignatureInfoModel(bInvokableSymbol);
        // Join the function parameters to generate the function's signature
        String paramsJoined = signatureInfoModel.getParameterInfoModels().stream().map(parameterInfoModel -> {
            // For each of the parameters, create a parameter info instance
            ParameterInformation parameterInformation =
                    new ParameterInformation(parameterInfoModel.paramValue, parameterInfoModel.description);
            parameterInformationList.add(parameterInformation);

            return parameterInfoModel.toString();
        }).collect(Collectors.joining(", "));
        signatureInformation.setLabel(CommonUtil.getLastItem(nameComps) + "(" + paramsJoined + ")");
        signatureInformation.setParameters(parameterInformationList);
        signatureInformation.setDocumentation(signatureInfoModel.signatureDescription);

        return signatureInformation;
    }

    /**
     * Get the required signature information filled model.
     *
     * @param bInvokableSymbol                  Invokable symbol
     * @return {@link SignatureInfoModel}       SignatureInfoModel containing signature information
     */
    private static SignatureInfoModel getSignatureInfoModel(BInvokableSymbol bInvokableSymbol) {
        Map<String, String> paramDescMap = new HashMap<>();
        SignatureInfoModel signatureInfoModel = new SignatureInfoModel();
        List<ParameterInfoModel> paramModels = new ArrayList<>();
        MarkdownDocAttachment docAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        
        signatureInfoModel.setSignatureDescription(docAttachment.description.trim());
        docAttachment.parameters.forEach(attribute ->
                paramDescMap.put(attribute.getName(), attribute.getDescription()));

        bInvokableSymbol.getParameters().forEach(bVarSymbol -> {
            ParameterInfoModel parameterInfoModel = new ParameterInfoModel();
            parameterInfoModel.setParamType(bVarSymbol.getType().toString());
            parameterInfoModel.setParamValue(bVarSymbol.getName().getValue());
            parameterInfoModel.setDescription(paramDescMap.get(bVarSymbol.getName().getValue()));
            paramModels.add(parameterInfoModel);
        });

        signatureInfoModel.setParameterInfoModels(paramModels);

        return signatureInfoModel;
    }

    private static void setItemInfo(String line, int startPosition, LSServiceOperationContext signatureContext) {
        int counter = startPosition;
        String callableItemName = "";
        StringBuilder delimiter = new StringBuilder();
        while (counter > 0) {
            char c = line.charAt(counter);
            if (!(Character.isLetterOrDigit(c)
                    || "_".equals(Character.toString(c)))
                    || TERMINAL_CHARACTERS.contains(Character.toString(c))) {
                callableItemName = line.substring(counter + 1, startPosition + 1);
                if (">".equals(String.valueOf(c)) && "-".equals(String.valueOf(line.charAt(counter - 1)))) {
                    delimiter.append(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY);
                    counter--;
                } else {
                    delimiter.append(String.valueOf(c));
                }
                captureIdentifierAgainst(line, counter, signatureContext, delimiter.toString());
                break;
            }
            counter--;
        }
        signatureContext.put(SignatureKeys.CALLABLE_ITEM_NAME, callableItemName);
        signatureContext.put(SignatureKeys.ITEM_DELIMITER, delimiter.toString());
    }

    /**
     * Capture the identifier against (s.contains() here s is the identifier against).
     * @param line              Current line being evaluated
     * @param startPosition     Evaluation start position
     * @param signatureContext  Signature help context
     */
    private static void captureIdentifierAgainst(String line, int startPosition,
                                                 LSServiceOperationContext signatureContext, String delimiter) {
        int counter = startPosition;
        String identifier = "";
        if (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(delimiter)
                || UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(delimiter)
                || UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY.equals(delimiter)) {
            counter--;
            while (counter > 0) {
                char c = line.charAt(counter);
                if (TERMINAL_CHARACTERS.contains(Character.toString(c)) || Character.toString(c).equals(" ")
                        || Character.toString(c).equals("\n")) {
                    identifier = line.substring(counter + 1, startPosition);
                    break;
                }
                counter--;
            }
        }
        signatureContext.put(SignatureKeys.IDENTIFIER_AGAINST, identifier.trim());
    }
    
    private static List<SymbolInfo> getEndpointActionsByName(String epName, List<SymbolInfo> symbolInfoList) {
        List<SymbolInfo> filteredList = new ArrayList<>();
        SymbolInfo filteredSymbol = symbolInfoList.stream()
                .filter(symbolInfo -> {
                    BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                    return bSymbol.getName().getValue().equals(epName) && bSymbol instanceof BEndpointVarSymbol;
                })
                .findFirst().orElse(null);
        if (filteredSymbol != null) {
            filteredList.addAll(FilterUtils
                    .getEndpointActions((BEndpointVarSymbol) filteredSymbol.getScopeEntry().symbol));
        }
        
        return filteredList;
    } 

    /**
     * Parameter information model to hold the parameter information meta data.
     */
    private static class ParameterInfoModel {

        private String paramValue;

        private String paramType;

        private String description;

        void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        void setParamType(String paramType) {
            this.paramType = paramType;
        }

        void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return this.paramType + " " + this.paramValue;
        }
    }

    /**
     * Signature information model to collect the info required for the signature.
     */
    private static class SignatureInfoModel {

        private List<ParameterInfoModel> parameterInfoModels;

        private String signatureDescription;

        List<ParameterInfoModel> getParameterInfoModels() {
            return parameterInfoModels;
        }

        void setParameterInfoModels(List<ParameterInfoModel> parameterInfoModels) {
            this.parameterInfoModels = parameterInfoModels;
        }

        void setSignatureDescription(String signatureDescription) {
            this.signatureDescription = signatureDescription;
        }
    }
}
