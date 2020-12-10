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
package org.ballerinalang.langserver.hover;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover content.
     *
     * @param context Hover operation context
     * @return {@link Hover} Hover content
     */
    public static Hover getHover(HoverContext context) {
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(context.filePath());
        if (semanticModel.isEmpty()) {
            return HoverUtil.getDefaultHoverObject();
        }

        Position cursorPosition = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        Optional<Symbol> symbolAtCursor = semanticModel.get()
                .symbol(context.filePath().getFileName().getFileName().toString(), linePosition);
        if (symbolAtCursor.isEmpty()) {
            return HoverUtil.getDefaultHoverObject();
        }

        switch (symbolAtCursor.get().kind()) {
            // Handle Listener and Enum
            case FUNCTION:
                return getFunctionHoverMarkupContent((FunctionSymbol) symbolAtCursor.get());
            case TYPE_DEFINITION:
                return getTypeDefHoverMarkupContent(((TypeDefinitionSymbol) symbolAtCursor.get()).typeDescriptor());
            case TYPE:
                return getTypeDefHoverMarkupContent(CommonUtil.getRawType((TypeSymbol) symbolAtCursor.get()));
            case CLASS:
                return getClassHoverMarkupContent((ClassSymbol) symbolAtCursor.get());
            case CONSTANT:
            case ANNOTATION:
            default:
                return HoverUtil.getDefaultHoverObject();
        }
    }

    private static Hover getClassHoverMarkupContent(ClassSymbol classSymbol) {
        Optional<Documentation> documentation = classSymbol.docAttachment();
        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        List<String> hoverContent = new ArrayList<>();
        if (documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }

        Map<String, String> paramsMap = documentation.get().parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(classSymbol.fieldDescriptors().stream()
                    .map(field -> {
                        String paramName = field.name();
                        String desc = paramsMap.get(paramName);
                        return quotedString(field.typeDescriptor().signature()) + " "
                                + italicString(boldString(paramName)) + " : " + desc;
                    }).collect(Collectors.toList()));

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }

        List<String> methods = new ArrayList<>();
        classSymbol.methods().forEach(method -> {
            StringBuilder methodInfo = new StringBuilder();
            Optional<Documentation> methodDoc = method.docAttachment();
            methodInfo.append(quotedString(method.typeDescriptor().signature()));
            if (methodDoc.isPresent() && methodDoc.get().description().isPresent()) {
                methodInfo.append(CommonUtil.MD_LINE_SEPARATOR).append(methodDoc.get().description().get());
            }
            methods.add(bulletItem(methodInfo.toString()));
        });

        if (!methods.isEmpty()) {
            methods.add(0, header(3, ContextConstants.METHOD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);
            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, methods));
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static Hover getTypeDefHoverMarkupContent(TypeSymbol symbol) {
        TypeSymbol rawType = CommonUtil.getRawType(symbol);

        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return getRecordTypeHoverContent((RecordTypeSymbol) rawType);
        }
        return getDefaultHoverObject();
    }

    private static Hover getRecordTypeHoverContent(RecordTypeSymbol recordType) {
        Optional<Documentation> documentation = recordType.docAttachment();
        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        List<String> hoverContent = new ArrayList<>();
        if (documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }

        Map<String, String> paramsMap = documentation.get().parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(recordType.fieldDescriptors().stream()
                    .map(field -> {
                        String paramName = field.name();
                        String desc = paramsMap.get(paramName);
                        return quotedString(field.typeDescriptor().signature()) + " "
                                + italicString(boldString(paramName)) + " : " + desc;
                    }).collect(Collectors.toList()));
            Optional<TypeSymbol> restTypeDesc = recordType.restTypeDescriptor();
            restTypeDesc.ifPresent(typeSymbol -> params.add(quotedString(typeSymbol.signature() + "...")));

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the default hover object.
     *
     * @return {@link Hover} hover default hover object.
     */
    public static Hover getDefaultHoverObject() {
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue("");
        hover.setContents(hoverMarkupContent);

        return hover;
    }

//    private static boolean skipFirstParam(LSContext context, BInvokableSymbol invokableSymbol) {
    // Fixme in the next phase
//        NonTerminalNode evalNode = context.get(CompletionKeys.TOKEN_AT_CURSOR_KEY).parent();
//        return CommonUtil.isLangLibSymbol(invokableSymbol) && evalNode.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
//        return false;
//    }

    private static Hover getFunctionHoverMarkupContent(FunctionSymbol symbol) {
        Optional<Documentation> documentation = symbol.docAttachment();
        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        List<String> hoverContent = new ArrayList<>();
        if (documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }
        Map<String, String> paramsMap = documentation.get().parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.PARAM_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(symbol.typeDescriptor().parameters().stream()
                    .map(param -> {
                        if (param.name().isEmpty()) {
                            return quotedString(param.typeDescriptor().signature());
                        }
                        String paramName = param.name().get();
                        String desc = paramsMap.get(paramName);
                        return quotedString(param.typeDescriptor().signature()) + " "
                                + italicString(boldString(paramName)) + " : " + desc;
                    }).collect(Collectors.toList()));

            Optional<ParameterSymbol> restParam = symbol.typeDescriptor().restParam();
            if (restParam.isPresent()) {
                StringBuilder restParamBuilder =
                        new StringBuilder(quotedString(restParam.get().typeDescriptor().signature() + "..."));
                if (restParam.get().name().isPresent()) {
                    restParamBuilder.append(" ").append(italicString(boldString(restParam.get().name().get())))
                            .append(" : ").append(paramsMap.get(restParam.get().name().get()));
                }
                params.add(restParamBuilder.toString());
            }

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }
        if (documentation.get().returnDescription().isPresent()) {
            String returnDoc = header(3, ContextConstants.RETURN_TITLE) + CommonUtil.MD_LINE_SEPARATOR +
                    symbol.typeDescriptor().returnTypeDescriptor().get().signature()
                    + documentation.get().returnDescription().get();
            hoverContent.add(returnDoc);
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static String getHorizontalSeparator() {
        return CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "---"
                + CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR;
    }

    private static String quotedString(String value) {
        return "`" + value.trim() + "`";
    }

    private static String boldString(String value) {
        return "**" + value.trim() + "**";
    }

    private static String italicString(String value) {
        return "*" + value.trim() + "*";
    }

    private static String bulletItem(String value) {
        return "+ " + value.trim() + CommonUtil.MD_LINE_SEPARATOR;
    }

    private static String header(int level, String header) {
        return String.join("", Collections.nCopies(level, "#")) + " " + header;
    }
}
