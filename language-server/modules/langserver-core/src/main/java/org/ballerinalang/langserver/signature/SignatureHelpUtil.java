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

import org.ballerinalang.langserver.common.constants.DocumentServiceKeys;
import org.ballerinalang.langserver.common.context.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
     * Capture the callable item information such as name, package of the item, delimiter (. or :), and etc.
     *
     * @param position          Position of the signature help triggered
     * @param fileContent       File content to search callable item name
     * @param serviceContext    Text Document service context instance for the signature help operation
     */
    public static void captureCallableItemInfo(Position position, String fileContent,
                                               TextDocumentServiceContext serviceContext) {
        int lineNumber = position.getLine();
        int character = position.getCharacter();
        int paramCounter = 0;
        // Here add offset of 2 since the indexing is zero based
        String[] lineTokens = fileContent.split("\\r?\\n", lineNumber + 2);
        String line = lineTokens[lineNumber];

        int backTrackPosition = character - 1;
        Stack<String> closeBracketStack = new Stack<>();

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
     * @param context                   Signature help context
     * @return {@link SignatureHelp}    Signature help for the completion
     */
    public static SignatureHelp getFunctionSignatureHelp(TextDocumentServiceContext context) {
        // Get the functions List
        List<SymbolInfo> functions = context.get(SignatureKeys.FILTERED_FUNCTIONS);
        List<SignatureInformation> signatureInformationList = functions
                .stream()
                .map(symbolInfo -> getSignatureInformation((BInvokableSymbol) symbolInfo.getScopeEntry().symbol,
                        context))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        SignatureHelp signatureHelp = new SignatureHelp();
        signatureHelp.setSignatures(signatureInformationList);
        signatureHelp.setActiveParameter(context.get(SignatureKeys.PARAMETER_COUNT));
        signatureHelp.setActiveSignature(0);

        return signatureHelp;
    }

    /**
     * Get the signature information for the given Ballerina function.
     *
     * @param bInvokableSymbol BLang Invokable symbol
     * @param signatureContext Signature operation context
     * @return {@link SignatureInformation}     Signature information for the function
     */
    private static SignatureInformation getSignatureInformation(BInvokableSymbol bInvokableSymbol,
                                                                TextDocumentServiceContext signatureContext) {
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        List<ParameterInfoModel> paramInfoModels = getParamInfoList(bInvokableSymbol, signatureContext);
        String functionName = bInvokableSymbol.getName().getValue();

        // Join the function parameters to generate the function's signature
        String paramsJoined = paramInfoModels.stream().map(parameterInfoModel -> {
            // For each of the parameters, create a parameter info instance
            ParameterInformation parameterInformation =
                    new ParameterInformation(parameterInfoModel.paramValue, parameterInfoModel.description);
            parameterInformationList.add(parameterInformation);

            return parameterInfoModel.toString();
        }).collect(Collectors.joining(", "));
        signatureInformation.setLabel(functionName + "(" + paramsJoined + ")");
        signatureInformation.setParameters(parameterInformationList);

        return signatureInformation;
    }

    /**
     * Get the list of Parameter information data models for the given ballerina function.
     *
     * @param bInvokableSymbol  Invokable symbol
     * @return {@link List}     List of parameter info data models
     */
    private static List<ParameterInfoModel> getParamInfoList(BInvokableSymbol bInvokableSymbol,
                                                             TextDocumentServiceContext signatureContext) {
        List<ParameterInfoModel> paramList = new ArrayList<>();
        Name packageName = bInvokableSymbol.pkgID.getName();
        String functionName = signatureContext.get(SignatureKeys.CALLABLE_ITEM_NAME);
        CompilerContext compilerContext = signatureContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        BLangPackage bLangPackage = signatureContext.get(DocumentServiceKeys.B_LANG_PACKAGE_CONTEXT_KEY).
                getPackageByName(compilerContext, packageName);

        BLangFunction blangFunction = bLangPackage.getFunctions().stream()
                .filter(bLangFunction -> bLangFunction.getName().getValue().equals(functionName))
                .findFirst()
                .orElse(null);

        bInvokableSymbol.getParameters().forEach(bVarSymbol -> {
            ParameterInfoModel parameterInfoModel = new ParameterInfoModel();
            parameterInfoModel.setParamType(bVarSymbol.getType().toString());
            parameterInfoModel.setParamValue(bVarSymbol.getName().getValue());
            parameterInfoModel.setDescription(getParameterDescription(blangFunction.getAnnotationAttachments(),
                    bVarSymbol.getName().getValue()));
            paramList.add(parameterInfoModel);
        });

        return paramList;
    }

    /**
     * Get the parameter description for the given parameter name from the annotation attachments.
     * Need to filter out the Param annotations only.
     *
     * @param attachments List of Annotation attachments
     * @param paramName   Parameter name
     * @return {@link String}   Parameter description
     */
    private static String getParameterDescription(List<BLangAnnotationAttachment> attachments, String paramName) {
        return attachments
                .stream()
                .map(annotationAttachment -> {
                    String desc = null;
                    // Need to filter the Prams only
                    if (annotationAttachment.getAnnotationName().getValue().equals("Param")) {
                        List<BLangAnnotAttachmentAttribute> attributes = annotationAttachment.getAttributes();
                        desc = filterParameterAttribute(attributes, paramName);
                    }
                    return desc;
                })
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Filter the value attribute from the particular annotation attachment's attributes.
     *
     * @param attributes Attributes of the annotation attachment related to the parameter name
     * @param paramName  Parameter name
     * @return {@link String}   Description of the parameter attribute
     */
    private static String filterParameterAttribute(List<BLangAnnotAttachmentAttribute> attributes, String paramName) {
        return attributes.stream()
                .map(attribute -> {
                    String value = attribute.value.getValue().toString();
                    // Need to filter the value attribute
                    if (attribute.getName().getValue().equals("value") && value.startsWith(paramName)) {
                        return value;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static void setItemInfo(String line, int startPosition, TextDocumentServiceContext signatureContext) {
        int counter = startPosition;
        String callableItemName = "";
        String delimiter = "";
        while (true) {
            if (counter < 0) {
                break;
            }
            char c = line.charAt(counter);
            if (!(Character.isLetterOrDigit(c) || "_".equals(Character.toString(c)))
                    || TERMINAL_CHARACTERS.contains(Character.toString(c))) {
                callableItemName = line.substring(counter + 1, startPosition + 1);
                delimiter = String.valueOf(line.charAt(counter));
                captureIdentifierAgainst(line, counter, signatureContext);
                break;
            }
            counter--;
        }
        signatureContext.put(SignatureKeys.CALLABLE_ITEM_NAME, callableItemName);
        signatureContext.put(SignatureKeys.ITEM_DELIMITER, delimiter);
    }

    /**
     * Capture the identifier against (s.contains() here s is the identifier against).
     * @param line              Current line being evaluated
     * @param startPosition     Evaluation start position
     * @param signatureContext  Signature help context
     */
    private static void captureIdentifierAgainst(String line, int startPosition,
                                                 TextDocumentServiceContext signatureContext) {
        int counter = startPosition;
        String identifier = "";
        if (".".equals(Character.toString(line.charAt(counter)))
                || ":".equals(Character.toString(line.charAt(counter)))) {
            counter--;
            while (true) {
                if (counter < 0) {
                    break;
                }
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
}
