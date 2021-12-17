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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return HoverUtil.getDefaultHoverObject();
        }
        
        Position cursorPosition = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        // Check for the cancellation before the time consuming operation
        context.checkCancelled();
        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(), linePosition);
        // Check for the cancellation after the time consuming operation
        context.checkCancelled();
        if (symbolAtCursor.isEmpty()) {
            Range nodeRange = new Range(context.getCursorPosition(), context.getCursorPosition());
            NonTerminalNode nodeAtCursor = CommonUtil.findNode(nodeRange, srcFile.get().syntaxTree());
            if (nodeAtCursor != null) {
                MatchedExpressionNodeResolver expressionResolver = new MatchedExpressionNodeResolver(nodeAtCursor);
                Optional<ExpressionNode> expr = expressionResolver.findExpression(nodeAtCursor);
                if (expr.isPresent()) {
                    return getHoverForExpression(context, expr.get());
                }
            }
            
            return HoverUtil.getDefaultHoverObject();
        }

        return getHoverForSymbol(symbolAtCursor.get(), context);
    }

    private static Hover getHoverForSymbol(Symbol symbol, HoverContext context) {
        switch (symbol.kind()) {
            case FUNCTION:
                return getFunctionHoverMarkupContent((FunctionSymbol) symbol, context);
            case METHOD:
                return getFunctionHoverMarkupContent((MethodSymbol) symbol, context);
            case RESOURCE_METHOD:
                return getFunctionHoverMarkupContent((ResourceMethodSymbol) symbol, context);
            case TYPE_DEFINITION:
                return getTypeDefHoverMarkupContent((TypeDefinitionSymbol) symbol, context);
            case CLASS:
                return getClassHoverMarkupContent((ClassSymbol) symbol, context);
            case OBJECT_FIELD:
            case RECORD_FIELD:
            case CONSTANT:
            case ANNOTATION:
            case ENUM:
            case ENUM_MEMBER:
            case CLASS_FIELD:
                return getDescriptionOnlyHoverObject(symbol);
            case VARIABLE:
                return getVariableHoverMarkupContent((VariableSymbol) symbol);
            case TYPE:
                if (symbol instanceof TypeReferenceTypeSymbol) {
                    return getHoverForSymbol(((TypeReferenceTypeSymbol) symbol).definition(), context);
                } else {
                    return HoverUtil.getDefaultHoverObject();
                }
            default:
                return HoverUtil.getDefaultHoverObject();
        }
    }

    /**
     * Get hover for expression nodes. Note that we are supplying hover for a selected set of expressions.
     *
     * @param context  Context
     * @param exprNode Expression node
     * @return Hover
     */
    private static Hover getHoverForExpression(HoverContext context, Node exprNode) {
        switch (exprNode.kind()) {
            case IMPLICIT_NEW_EXPRESSION:
            case EXPLICIT_NEW_EXPRESSION:
                Optional<TypeSymbol> optionalTypeSymbol = context.currentSemanticModel()
                        .flatMap(semanticModel -> semanticModel.typeOf(exprNode))
                        .map(CommonUtil::getRawType);
                if (optionalTypeSymbol.isEmpty()) {
                    break;
                }

                TypeSymbol typeSymbol = optionalTypeSymbol.get();
                // If the type we got is a union, init() function should have an error? return type.
                // Need to filter that here.
                if (typeSymbol.typeKind() == TypeDescKind.UNION) {
                    UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                    Optional<TypeSymbol> classTypeSymbol = unionTypeSymbol.memberTypeDescriptors().stream()
                            .map(CommonUtil::getRawType)
                            .filter(member -> member.typeKind() != TypeDescKind.ERROR)
                            .findFirst();
                    if (classTypeSymbol.isEmpty()) {
                        break;
                    }
                    typeSymbol = classTypeSymbol.get();
                }

                if (typeSymbol instanceof ClassSymbol) {
                    ClassSymbol classSymbol = (ClassSymbol) typeSymbol;
                    if (classSymbol.initMethod().isEmpty()) {
                        break;
                    }

                    MethodSymbol initMethodSymbol = classSymbol.initMethod().get();
                    return getFunctionHoverMarkupContent(initMethodSymbol, context);
                }
        }
        return getDefaultHoverObject();
    }

    private static Hover getObjectHoverMarkupContent(Documentation documentation, ObjectTypeSymbol classSymbol,
                                                     HoverContext context) {
        List<String> hoverContent = new ArrayList<>();
        if (documentation.description().isPresent()) {
            hoverContent.add(documentation.description().get());
        }

        Optional<Package> currentPackage = context.workspace()
                .project(context.filePath()).map(Project::currentPackage);
        Optional<Module> currentModule = context.currentModule();

        if (currentModule.isPresent() && currentPackage.isPresent()) {
            Map<String, String> paramsMap = documentation.parameterMap();
            if (!paramsMap.isEmpty()) {
                List<String> params = new ArrayList<>();
                params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

                params.addAll(classSymbol.fieldDescriptors().entrySet().stream()
                        .filter(fieldEntry -> withValidAccessModifiers(
                                fieldEntry.getValue(), currentPackage.get(), currentModule.get().moduleId(), context))
                        .map(fieldEntry -> {
                            String desc = paramsMap.get(fieldEntry.getKey());
                            String modifiedTypeName =
                                    CommonUtil.getModifiedTypeName(context, fieldEntry.getValue().typeDescriptor());
                            return quotedString(modifiedTypeName) + " " + italicString(boldString(fieldEntry.getKey()))
                                    + " : " + desc;
                        }).collect(Collectors.toList()));
                if (params.size() > 1) {
                    hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
                }
            }

            List<String> methods = new ArrayList<>();
            classSymbol.methods().entrySet().stream()
                    .filter(methodSymbol -> withValidAccessModifiers(methodSymbol.getValue(),
                            currentPackage.get(), currentModule.get().moduleId(), context))
                    .forEach(methodEntry -> {
                        MethodSymbol methodSymbol = methodEntry.getValue();
                        StringBuilder methodInfo = new StringBuilder();
                        Optional<Documentation> methodDoc = methodSymbol.documentation();
                        String signature = CommonUtil.getModifiedSignature(context, methodSymbol.signature());
                        methodInfo.append(quotedString(signature));
                        if (methodDoc.isPresent() && methodDoc.get().description().isPresent()) {
                            methodInfo.append(CommonUtil.MD_LINE_SEPARATOR).append(methodDoc.get().description().get());
                        }
                        methods.add(bulletItem(methodInfo.toString()));
                    });

            if (!methods.isEmpty()) {
                methods.add(0, header(3, ContextConstants.METHOD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);
                hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, methods));
            }
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static Hover getTypeDefHoverMarkupContent(TypeDefinitionSymbol symbol, HoverContext context) {
        TypeSymbol rawType = CommonUtil.getRawType(symbol.typeDescriptor());
        Optional<Documentation> documentation = symbol.documentation();

        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return getRecordTypeHoverContent(documentation.get(), (RecordTypeSymbol) rawType, context);
        }
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            return getObjectHoverMarkupContent(documentation.get(), (ObjectTypeSymbol) rawType, context);
        }

        return getDescriptionOnlyHoverObject(documentation.get());
    }

    private static Hover getClassHoverMarkupContent(ClassSymbol symbol, HoverContext context) {
        Optional<Documentation> documentation = symbol.documentation();

        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        return getObjectHoverMarkupContent(documentation.get(), symbol, context);
    }

    private static Hover getRecordTypeHoverContent(Documentation documentation,
                                                   RecordTypeSymbol recordType,
                                                   HoverContext ctx) {
        List<String> hoverContent = new ArrayList<>();
        if (documentation.description().isPresent()) {
            hoverContent.add(documentation.description().get());
        }

        Map<String, String> paramsMap = documentation.parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(recordType.fieldDescriptors().entrySet().stream()
                    .map(fieldEntry -> {
                        String desc = paramsMap.get(fieldEntry.getKey());
                        String typeName = CommonUtil.getModifiedTypeName(ctx, fieldEntry.getValue().typeDescriptor());
                        return quotedString(typeName) + " " + italicString(boldString(fieldEntry.getKey()))
                                + " : " + desc;
                    }).collect(Collectors.toList()));
            Optional<TypeSymbol> restTypeDesc = recordType.restTypeDescriptor();
            restTypeDesc.ifPresent(typeSymbol ->
                    params.add(quotedString(CommonUtil.getModifiedTypeName(ctx, typeSymbol) + "...")));
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

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    private static Hover getDescriptionOnlyHoverObject(Documentation documentation) {
        String description = "";
        if (documentation.description().isPresent()) {
            description = documentation.description().get();
        }
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(description);
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    public static Hover getDescriptionOnlyHoverObject(Symbol symbol) {
        if (!(symbol instanceof Documentable) || ((Documentable) symbol).documentation().isEmpty()) {
            return getDefaultHoverObject();
        }

        return getDescriptionOnlyHoverObject(((Documentable) symbol).documentation().get());
    }

    private static Hover getFunctionHoverMarkupContent(FunctionSymbol symbol, HoverContext ctx) {
        Optional<Documentation> documentation = symbol.documentation();
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

            params.addAll(symbol.typeDescriptor().params().get().stream()
                    .map(param -> {
                        if (param.getName().isEmpty()) {
                            return quotedString(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor()));
                        }
                        String paramName = param.getName().get();
                        String desc = paramsMap.get(paramName);
                        return quotedString(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor())) + " "
                                + italicString(boldString(paramName)) + " : " + desc;
                    }).collect(Collectors.toList()));

            Optional<ParameterSymbol> restParam = symbol.typeDescriptor().restParam();
            if (restParam.isPresent()) {
                String modifiedTypeName = CommonUtil.getModifiedTypeName(ctx, restParam.get().typeDescriptor());
                StringBuilder restParamBuilder = new StringBuilder(quotedString(modifiedTypeName + "..."));
                if (restParam.get().getName().isPresent()) {
                    restParamBuilder.append(" ").append(italicString(boldString(restParam.get().getName().get())))
                            .append(" : ").append(paramsMap.get(restParam.get().getName().get()));
                }
                params.add(restParamBuilder.toString());
            }

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }
        if (documentation.get().returnDescription().isPresent()) {
            TypeSymbol returnTypeDesc = symbol.typeDescriptor().returnTypeDescriptor().orElseThrow();
            String returnTypeName = quotedString(CommonUtil.getModifiedTypeName(ctx, returnTypeDesc));
            String returnDoc = header(3, ContextConstants.RETURN_TITLE) + CommonUtil.MD_LINE_SEPARATOR +
                    returnTypeName + " : " + documentation.get().returnDescription().get();
            hoverContent.add(returnDoc);
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static Hover getVariableHoverMarkupContent(VariableSymbol symbol) {
        Optional<Documentation> documentation = symbol.documentation();
        List<String> hoverContent = new ArrayList<>();
        if (documentation.isPresent() && documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }

        TypeSymbol varTypeSymbol = symbol.typeDescriptor();
        String type = varTypeSymbol.signature();
        String varName = symbol.getName().isPresent() ? " " + symbol.getName().get() : "";
        String modifiedVariable = quotedString(type) + CommonUtil.escapeEscapeCharsInIdentifier(varName);
        hoverContent.add(modifiedVariable);

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

    /**
     * Check if a given symbol has valid access modifiers to be visible with in the give context.
     *
     * @param symbol         Symbol.
     * @param currentPackage Current Package.
     * @param currentModule  Current Module.
     * @return {@link Boolean} Whether the symbol is visible in the current context.
     */
    private static Boolean withValidAccessModifiers(Symbol symbol, Package currentPackage,
                                                    ModuleId currentModule, HoverContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<ModuleSymbol> typeSymbolModule = symbol.getModule();

        if (project.isEmpty() || typeSymbolModule.isEmpty()) {
            return false;
        }

        boolean isResource = false;
        boolean isPrivate = false;
        boolean isPublic = false;
        boolean isRemote = false;

        if (symbol instanceof Qualifiable) {
            Qualifiable qSymbol = (Qualifiable) symbol;
            isPrivate = qSymbol.qualifiers().contains(Qualifier.PRIVATE);
            isPublic = qSymbol.qualifiers().contains(Qualifier.PUBLIC);
            isResource = qSymbol.qualifiers().contains(Qualifier.RESOURCE);
            isRemote = qSymbol.qualifiers().contains(Qualifier.REMOTE);
        }

        if (isResource || isRemote || isPublic) {
            return true;
        }

        ModuleID objModuleId = typeSymbolModule.get().id();
        return (!isPrivate && objModuleId.moduleName().equals(currentModule.moduleName())
                && objModuleId.orgName().equals(currentPackage.packageOrg().value()));
    }
}
