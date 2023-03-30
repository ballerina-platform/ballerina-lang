/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.util.MarkupUtils;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility for building a SignatureInformation.
 *
 * @since 2201.1.1
 */
public class SignatureInfoModelBuilder {
    private final Symbol symbol;
    private final SignatureContext context;
    private final List<ParameterInfoModel> parameterModels;

    private List<ParameterInfoModel> includedRecordParams;
    private Either<String, MarkupContent> signatureDescription;
    private final Map<String, String> paramsMap = new HashMap<>();

    public SignatureInfoModelBuilder(Symbol symbol, SignatureContext context) {
        this.symbol = symbol;
        this.context = context;
        this.parameterModels = new ArrayList<>();
        this.includedRecordParams = new ArrayList<>();
        fillParamInfoModels();
    }

    /**
     * Build the possible list of signature information.
     *
     * @return {@link List} of SignatureInformation
     */
    public List<SignatureInformation> build() {
        List<SignatureInformation> result = new ArrayList<>();

        StringBuilder defaultLabelBuilder = new StringBuilder(getFunctionName());
        defaultLabelBuilder.append("(");

        List<ParameterInformation> defaultSignatureParamInfo = new ArrayList<>();
        List<ParameterInformation> expandedSignatureParamInfo = new ArrayList<>();
        StringBuilder expandedModelPrefix = new StringBuilder();

        for (int i = 0; i < parameterModels.size(); i++) {
            ParameterInfoModel paramModel = parameterModels.get(i);
            int labelOffset = defaultLabelBuilder.toString().length();
            if (!includedRecordParams.isEmpty() && includedRecordParams.get(0).equals(paramModel)) {
                expandedModelPrefix.append(defaultLabelBuilder.toString());
            }
            String parameterType = paramModel.getParameter().getType();
            Optional<String> parameterName = paramModel.getParameter().getName();
            defaultLabelBuilder.append(parameterType);
            ParameterInformation paramInfo = new ParameterInformation();
            MarkupContent parameterDocumentation =
                    getParameterDocumentation(parameterType, parameterName.orElse(""), paramModel.getDescription());
            paramInfo.setDocumentation(parameterDocumentation);
            int paramStart = labelOffset;
            int paramEnd = labelOffset + parameterType.length();
            if (parameterName.isPresent()) {
                paramStart = paramEnd + 1;
                paramEnd += (parameterName.get() + " ").length();
                defaultLabelBuilder.append(" ").append(parameterName.get());
            }
            if (i < parameterModels.size() - 1) {
                defaultLabelBuilder.append(", ");
            }
            paramInfo.setLabel(Tuple.two(paramStart, paramEnd));

            defaultSignatureParamInfo.add(paramInfo);
            if (!includedRecordParams.isEmpty() && !paramModel.isIncludedRecordParam()) {
                expandedSignatureParamInfo.add(paramInfo);
            }
        }

        if (!includedRecordParams.isEmpty()) {
            int firstIncludedRecordParamIndex = this.parameterModels.indexOf(includedRecordParams.get(0));

            List<String> expandedParamList = new ArrayList<>();
            int labelOffset = expandedModelPrefix.toString().length();
            for (int i = 0; i < includedRecordParams.size(); i++) {
                ParameterInfoModel includedParamInfo = includedRecordParams.get(i);
                TypeSymbol paramTypeSymbol = includedParamInfo.getParameter().getParameterSymbol().typeDescriptor();
                Pair<String, List<ParameterInformation>> includedRecordParamInfo =
                        getIncludedRecordParamInfo(paramTypeSymbol, labelOffset);
                // If the params of included record param is empty, skip it. These params can be empty if the
                // record only has never typed fields.
                if (includedRecordParamInfo.getValue().isEmpty()) {
                    continue;
                }
                
                expandedParamList.add(includedRecordParamInfo.getKey());
                expandedSignatureParamInfo.addAll(includedRecordParamInfo.getValue());
                
                labelOffset += includedRecordParamInfo.getKey().length();

                // If there's more parameters, we add a comma between parameter segments.
                if (firstIncludedRecordParamIndex + i < parameterModels.size() - 1) {
                    // Comma + a space
                    labelOffset += 2;
                }
            }

            // If there are expanded params coming from included record params, we add them as a separate signature.
            if (!expandedParamList.isEmpty()) {
                StringBuilder expandedLabelBuilder = new StringBuilder(expandedModelPrefix.toString());
                
                expandedLabelBuilder
                        .append(String.join(", ", expandedParamList))
                        .append(")");
                result.add(getSignatureInfo(expandedLabelBuilder.toString(), expandedSignatureParamInfo));
            }
        }

        defaultLabelBuilder.append(")");
        result.add(getSignatureInfo(defaultLabelBuilder.toString(), defaultSignatureParamInfo));

        return result;
    }

    private SignatureInformation getSignatureInfo(String label, List<ParameterInformation> parameters) {
        SignatureInformation expandedSignatureInfo = new SignatureInformation();
        expandedSignatureInfo.setDocumentation(this.signatureDescription);
        expandedSignatureInfo.setLabel(label);
        expandedSignatureInfo.setParameters(parameters);
        return expandedSignatureInfo;
    }

    private void fillParamInfoModels() {
        fillDescriptionModels();
        Optional<FunctionTypeSymbol> functionTypeSymbol = getFunctionType(symbol);
        List<ParameterSymbol> parameterSymbols = functionTypeSymbol
                .map(typeSymbol -> typeSymbol.params().orElse(new ArrayList<>()))
                .orElse(Collections.emptyList());
        if (parameterSymbols.isEmpty()) {
            return;
        }

        List<Parameter> parameters = parameterSymbols
                .subList(skipFirstParam() ? 1 : 0, parameterSymbols.size())
                .stream()
                .map(param -> new Parameter(param, false, false, context))
                .collect(Collectors.toList());

        Optional<ParameterSymbol> restParam = functionTypeSymbol.flatMap(FunctionTypeSymbol::restParam);
        restParam.ifPresent(parameter -> parameters.add(new Parameter(parameter, false, true, context)));

        // Create a list of param info models
        for (Parameter param : parameters) {
            String desc = param.getName().isPresent() && paramsMap.containsKey(param.getName().get()) ?
                    paramsMap.get(param.getName().get()) : "";
            parameterModels.add(new ParameterInfoModel(param, desc));
        }

        includedRecordParams = this.parameterModels.stream()
                .filter(ParameterInfoModel::isIncludedRecordParam)
                .collect(Collectors.toList());
    }

    /**
     * For the langlib methods, we skip the first parameter, when it is called in a method call.
     *
     * @return {@link Boolean}
     */
    private boolean skipFirstParam() {
        return (symbol.kind() == SymbolKind.FUNCTION || symbol.kind() == SymbolKind.METHOD)
                && (symbol.getModule().isPresent() && CommonUtil.isLangLib(symbol.getModule().get().id()));
    }

    private void fillDescriptionModels() {
        Optional<Documentation> documentation =
                symbol instanceof Documentable ? ((Documentable) symbol).documentation() : Optional.empty();
        // Check for documentations of the function and parameters
        if (documentation.isEmpty()) {
            return;
        }
        if (documentation.get().description().isPresent()) {
            this.setSignatureDescription(documentation.get().description().get().trim(), context);
        }
        documentation.get().parameterMap().forEach(paramsMap::put);
    }

    private void setSignatureDescription(String signatureDescription, SignatureContext signatureContext) {
        SignatureInformationCapabilities capabilities = signatureContext.capabilities().getSignatureInformation();
        List<String> documentationFormat = capabilities != null ? capabilities.getDocumentationFormat()
                : new ArrayList<>();
        if (documentationFormat != null
                && !documentationFormat.isEmpty()
                && documentationFormat.get(0).equals(CommonUtil.MARKDOWN_MARKUP_KIND)) {
            MarkupContent signatureMarkupContent = new MarkupContent();
            signatureMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
            signatureMarkupContent.setValue(
                    MarkupUtils.boldString("Description") + CommonUtil.MD_LINE_SEPARATOR + signatureDescription);
            this.signatureDescription = Either.forRight(signatureMarkupContent);
        } else {
            this.signatureDescription =
                    Either.forLeft("Description" + CommonUtil.LINE_SEPARATOR + signatureDescription);
        }
    }

    private Pair<String, List<ParameterInformation>> getIncludedRecordParamInfo(TypeSymbol typeSymbol,
                                                                                int startOffset) {
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);

        if (rawType.typeKind() != TypeDescKind.RECORD) {
            return Pair.of(null, null);
        }

        StringBuilder paramLabelBuilder = new StringBuilder();
        Map<String, String> parameterDocumentation = getParameterDocumentationMap(typeSymbol);
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        List<RecordFieldSymbol> fieldSymbols =
                new ArrayList<>(((RecordTypeSymbol) rawType).fieldDescriptors().values());

        for (int i = 0; i < fieldSymbols.size(); i++) {
            RecordFieldSymbol recordFieldSymbol = fieldSymbols.get(i);
            TypeSymbol fieldType = recordFieldSymbol.typeDescriptor();
            // Never typed fields are used to prevent those fields from being specified as a parameter.
            // log:printInfo() is one such example. Such fields should not be included in the signature help.
            if (fieldType.typeKind() == TypeDescKind.NEVER) {
                continue;
            }
            int labelOffset = startOffset + paramLabelBuilder.length();
            ParameterInformation paramInfo = new ParameterInformation();
            String typeSignature = fieldType.signature();
            Optional<String> fieldName = recordFieldSymbol.getName();
            recordFieldSymbol.typeDescriptor().signature();
            String paramDocs = parameterDocumentation
                    .getOrDefault(fieldName.orElse(""), recordFieldSymbol.signature());
            getParameterDocumentation(typeSignature, fieldName.orElse(""), paramDocs);
            paramLabelBuilder.append(recordFieldSymbol.typeDescriptor().signature());
            int paramStart = labelOffset;
            int paramEnd = labelOffset + typeSignature.length();
            if (fieldName.isPresent()) {
                paramStart = paramEnd + 1;
                paramEnd += (1 + fieldName.get().length());
                paramLabelBuilder.append(" ").append(fieldName.get());
            }

            if (i < fieldSymbols.size() - 1) {
                paramLabelBuilder.append(", ");
            }

            paramInfo.setLabel(Tuple.two(paramStart, paramEnd));

            parameterInformationList.add(paramInfo);
        }

        return Pair.of(paramLabelBuilder.toString(), parameterInformationList);
    }

    private String getFunctionName() {
        // Node at cursor and the function name should never be empty at this stage
        String functionName = this.symbol.getName().get();
        Optional<NonTerminalNode> nodeAtCursor = context.getNodeAtCursor();

        SyntaxKind syntaxKind = nodeAtCursor.get().kind();
        if (functionName.equals(Names.USER_DEFINED_INIT_SUFFIX.getValue())
                && (syntaxKind == SyntaxKind.IMPLICIT_NEW_EXPRESSION
                || syntaxKind == SyntaxKind.EXPLICIT_NEW_EXPRESSION)) {
            return SyntaxKind.NEW_KEYWORD.stringValue();
        } else {
            return functionName;
        }
    }

    private Map<String, String> getParameterDocumentationMap(Symbol symbol) {
        if (symbol.kind() != SymbolKind.TYPE || ((TypeSymbol) symbol).typeKind() != TypeDescKind.TYPE_REFERENCE
                || ((Documentable) ((TypeReferenceTypeSymbol) symbol).definition()).documentation().isEmpty()) {
            return Collections.emptyMap();
        }

        Documentation documentation =
                ((Documentable) ((TypeReferenceTypeSymbol) symbol).definition()).documentation().get();
        return Collections.unmodifiableMap(documentation.parameterMap());
    }

    private MarkupContent getParameterDocumentation(String type, String paramName, String description) {
        MarkupContent paramDocumentation = new MarkupContent();
        paramDocumentation.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder markupContent = new StringBuilder();

        markupContent.append(MarkupUtils.boldString("Parameter"))
                .append(CommonUtil.MD_LINE_SEPARATOR)
                .append(MarkupUtils.boldString(MarkupUtils.quotedString(type) + paramName));
        if (!description.isBlank()) {
            markupContent.append(": ").append(description);
        }
        paramDocumentation.setValue(markupContent.toString());

        return paramDocumentation;
    }

    private Optional<FunctionTypeSymbol> getFunctionType(Symbol symbol) {
        TypeSymbol type;

        switch (symbol.kind()) {
            case FUNCTION:
            case METHOD:
                return Optional.of(((FunctionSymbol) symbol).typeDescriptor());
            case VARIABLE:
                type = ((VariableSymbol) symbol).typeDescriptor();
                break;
            case PARAMETER:
                type = ((ParameterSymbol) symbol).typeDescriptor();
                break;
            default:
                return Optional.empty();
        }

        if (type.typeKind() == TypeDescKind.FUNCTION) {
            return Optional.of((FunctionTypeSymbol) type);
        }

        return Optional.empty();
    }
}
