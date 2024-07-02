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
package org.ballerinalang.langserver.hover;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.util.MarkupUtils;
import org.eclipse.lsp4j.Hover;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hover Provider for Ballerina.
 *
 * @since 2201.1.2
 */
public class HoverObjectResolver {

    private final HoverContext context;

    public HoverObjectResolver(HoverContext context) {
        this.context = context;
    }

    /**
     * Provides the corresponding hover object for a given symbol.
     * @param symbol Symbol.
     * @return {@link Hover} hover object.
     */
    public Hover getHoverObjectForSymbol(Symbol symbol) {
        switch (symbol.kind()) {
            case FUNCTION:
                return getHoverObjectForSymbol((FunctionSymbol) symbol);
            case METHOD:
                return getHoverObjectForSymbol((MethodSymbol) symbol);
            case RESOURCE_METHOD:
                return getHoverObjectForSymbol((ResourceMethodSymbol) symbol);
            case TYPE_DEFINITION:
                return getHoverObjectForSymbol((TypeDefinitionSymbol) symbol);
            case CLASS:
                return getHoverObjectForSymbol((ClassSymbol) symbol);
            case VARIABLE:
                return getHoverObjectForSymbol((VariableSymbol) symbol);
            case PARAMETER:
                return getHoverObjectForSymbol((ParameterSymbol) symbol);
            case TYPE:
                if (symbol instanceof TypeReferenceTypeSymbol refTypeSymbol) {
                    return getHoverObjectForSymbol(refTypeSymbol.definition());
                }
                return HoverUtil.getHoverObject();
            default:
                return HoverUtil.getDescriptionOnlyHoverObject(symbol);
        }
    }

    private Hover getHoverObjectForSymbol(VariableSymbol variableSymbol) {
        Optional<Documentation> documentation = variableSymbol.documentation();
        List<String> hoverContent = new ArrayList<>();
        if (documentation.isPresent() && documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }
        TypeSymbol varTypeSymbol = variableSymbol.typeDescriptor();
        if (varTypeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR) {
            String type = varTypeSymbol.signature();
            String varName = variableSymbol.getName().isPresent() ? " " + variableSymbol.getName().get() : "";
            String modifiedVariable = MarkupUtils.quotedString(type)
                    + CommonUtil.escapeEscapeCharsInIdentifier(varName);
            hoverContent.add(modifiedVariable);
        }
        return HoverUtil.getHoverObject(hoverContent.stream()
                .collect(Collectors.joining(MarkupUtils.getHorizontalSeparator())));
    }

    private Hover getHoverObjectForSymbol(ParameterSymbol symbol) {
        String hoverContent = "";
        TypeSymbol typeSymbol = symbol.typeDescriptor();
        if (typeSymbol.typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return HoverUtil.getHoverObject(hoverContent);
        }
        String type = typeSymbol.signature();
        String parameterName = symbol.getName().isPresent() ? " " + symbol.getName().get() : "";
        hoverContent = MarkupUtils.quotedString(type) + CommonUtil.escapeEscapeCharsInIdentifier(parameterName);
        return HoverUtil.getHoverObject(hoverContent);
    }

    private Hover getHoverObjectForSymbol(ClassSymbol symbol) {
        Optional<Documentation> documentation = symbol.documentation();
        if (documentation.isEmpty()) {
            return HoverUtil.getHoverObject();
        }

        return getHoverObjectForSymbol(symbol, documentation.get());
    }

    private Hover getHoverObjectForSymbol(TypeDefinitionSymbol symbol) {
        TypeSymbol rawType = CommonUtil.getRawType(symbol.typeDescriptor());
        Optional<Documentation> documentation = symbol.documentation();

        if (documentation.isEmpty()) {
            return HoverUtil.getHoverObject();
        }
        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return getHoverObjectForSymbol((RecordTypeSymbol) rawType, documentation.get());
        }
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            return getHoverObjectForSymbol((ObjectTypeSymbol) rawType, documentation.get());
        }
        return HoverUtil.getDescriptionOnlyHoverObject(documentation.get());
    }

    private Hover getHoverObjectForSymbol(FunctionSymbol functionSymbol) {
        Optional<Documentation> documentation = functionSymbol.documentation();
        if (documentation.isEmpty()) {
            return HoverUtil.getHoverObject();
        }
        List<String> hoverContent = new ArrayList<>();
        documentation.get().description().ifPresent(hoverContent::add);
        List<PathParameterSymbol> parameterSymbols = new ArrayList<>();
        boolean isResourceMethod = functionSymbol.kind() == SymbolKind.RESOURCE_METHOD;

        if (isResourceMethod) {
            ResourcePath resourcePath = ((ResourceMethodSymbol) functionSymbol).resourcePath();
            switch (resourcePath.kind()) {
                case PATH_SEGMENT_LIST -> {
                    PathSegmentList pathSegmentList = (PathSegmentList) resourcePath;
                    List<PathParameterSymbol> pathParameterSymbols = pathSegmentList.pathParameters();
                    parameterSymbols.addAll(pathParameterSymbols);
                    pathSegmentList.pathRestParameter().ifPresent(parameterSymbols::add);
                }
                case PATH_REST_PARAM -> parameterSymbols.add(((PathRestParam) resourcePath).parameter());
                default -> {
                    // ignore
                }
            }
        }
        Map<String, String> paramsMap = documentation.get().parameterMap();
        List<String> params = new ArrayList<>();

        if (!paramsMap.isEmpty() || !parameterSymbols.isEmpty()) {
            params.add(MarkupUtils.header(3, ContextConstants.PARAM_TITLE) + CommonUtil.MD_LINE_SEPARATOR);
            params.addAll(parameterSymbols.stream().map(param -> {
                if (param.getName().isEmpty()) {
                    return MarkupUtils.quotedString(NameUtil
                            .getModifiedTypeName(context, param.typeDescriptor()));
                }
                String paramName = param.getName().get();
                String desc = paramsMap.getOrDefault(paramName, "");
                return MarkupUtils.quotedString(NameUtil.getModifiedTypeName(context, param.typeDescriptor())) + " "
                        + MarkupUtils.italicString(MarkupUtils.boldString(paramName)) + " : " + desc;
            }).toList());
            params.addAll(functionSymbol.typeDescriptor().params().get().stream().map(param -> {
                if (param.getName().isEmpty()) {
                    return MarkupUtils.quotedString(NameUtil
                            .getModifiedTypeName(context, param.typeDescriptor()));
                }
                String paramName = param.getName().get();
                String desc = paramsMap.getOrDefault(paramName, "");
                String defaultValueEdit = "";
                if (param.paramKind() == ParameterKind.DEFAULTABLE) {
                    // Get file path for the symbol
                    Optional<Path> filePathForSymbol = this.context.workspace().project(this.context.filePath())
                            .flatMap(project -> PathUtil.getFilePathForSymbol(functionSymbol, project, this.context));
                    // Lookup the parameter node from syntax tree using the parameter symbol
                    Optional<NonTerminalNode> paramNode = this.context.workspace().syntaxTree(filePathForSymbol.get())
                            .flatMap(syntaxTree -> CommonUtil.findNode(param, syntaxTree));
                    if (paramNode.isPresent() && paramNode.get().kind() == SyntaxKind.DEFAULTABLE_PARAM
                            && !((DefaultableParameterNode) paramNode.get()).expression().isMissing()) {
                        // If there's a default value, use that instead of the default value of the type
                        DefaultableParameterNode node = (DefaultableParameterNode) paramNode.get();
                        defaultValueEdit = MarkupUtils
                                .quotedString(String.format("(default: %s)", node.expression().toSourceCode()));
                    }
                    // Else we are not going to provide a default value since it can be incorrect.
                    // The default value can be an expression and will be evaluated at the runtime. Therefore, 
                    // we cannot provide a default value for the parameter.
                }
                return MarkupUtils.quotedString(NameUtil.getModifiedTypeName(context, param.typeDescriptor())) + " "
                        + MarkupUtils.italicString(MarkupUtils.boldString(paramName)) + " : " + desc + defaultValueEdit;
            }).toList());

            Optional<ParameterSymbol> restParam = functionSymbol.typeDescriptor().restParam();
            if (restParam.isPresent()) {
                TypeSymbol typeSymbol = restParam.get().typeDescriptor();
                String modifiedTypeName = typeSymbol.typeKind() == TypeDescKind.ARRAY ? NameUtil
                        .getModifiedTypeName(context, ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor())
                        : NameUtil.getModifiedTypeName(context, typeSymbol);

                StringBuilder restParamBuilder = new StringBuilder(MarkupUtils.quotedString(modifiedTypeName + "..."));
                if (restParam.get().getName().isPresent()) {
                    String paramName = paramsMap.get(restParam.get().getName().get());
                    if (paramName == null) {
                        paramName = "";
                    }
                    restParamBuilder.append(" ")
                            .append(MarkupUtils.italicString(MarkupUtils.boldString(restParam.get().getName().get())))
                            .append(" : ").append(paramName);
                }
                params.add(restParamBuilder.toString());
            }

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }
        if (documentation.get().returnDescription().isPresent()) {
            TypeSymbol returnTypeDesc = functionSymbol.typeDescriptor().returnTypeDescriptor().orElseThrow();
            String returnTypeName = MarkupUtils.quotedString(NameUtil.getModifiedTypeName(context, returnTypeDesc));
            String returnDoc = MarkupUtils.header(3, ContextConstants.RETURN_TITLE) + CommonUtil.MD_LINE_SEPARATOR +
                    returnTypeName + " : " + documentation.get().returnDescription().get();
            hoverContent.add(returnDoc);
        }

        return HoverUtil.getHoverObject(hoverContent.stream()
                .collect(Collectors.joining(MarkupUtils.getHorizontalSeparator())));
    }

    private Hover getHoverObjectForSymbol(RecordTypeSymbol recordType, Documentation documentation) {
        List<String> hoverContent = new ArrayList<>();
        if (documentation.description().isPresent()) {
            hoverContent.add(documentation.description().get());
        }

        Map<String, String> paramsMap = documentation.parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(MarkupUtils.header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(recordType.fieldDescriptors().entrySet().stream()
                    .map(fieldEntry -> {
                        String desc = paramsMap.get(fieldEntry.getKey());
                        String typeName = NameUtil
                                .getModifiedTypeName(context, fieldEntry.getValue().typeDescriptor());
                        return MarkupUtils.quotedString(typeName) + " "
                                + MarkupUtils.italicString(MarkupUtils.boldString(fieldEntry.getKey())) + " : " + desc;
                    }).toList());
            Optional<TypeSymbol> restTypeDesc = recordType.restTypeDescriptor();
            restTypeDesc.ifPresent(typeSymbol ->
                    params.add(MarkupUtils.quotedString(NameUtil.getModifiedTypeName(context, typeSymbol) + "...")));
            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }

        return HoverUtil.getHoverObject(hoverContent.stream()
                .collect(Collectors.joining(MarkupUtils.getHorizontalSeparator())));
    }

    private Hover getHoverObjectForSymbol(ObjectTypeSymbol classSymbol, Documentation documentation) {
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
                params.add(MarkupUtils.header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

                params.addAll(classSymbol.fieldDescriptors().entrySet().stream()
                        .filter(fieldEntry -> HoverUtil.withValidAccessModifiers(
                                fieldEntry.getValue(), currentPackage.get(), currentModule.get().moduleId(), context))
                        .map(fieldEntry -> {
                            String desc = paramsMap.get(fieldEntry.getKey());
                            String modifiedTypeName =
                                    NameUtil.getModifiedTypeName(context, fieldEntry.getValue().typeDescriptor());
                            return MarkupUtils.quotedString(modifiedTypeName) + " " +
                                    MarkupUtils.italicString(MarkupUtils.boldString(fieldEntry.getKey()))
                                    + " : " + desc;
                        }).toList());
                if (params.size() > 1) {
                    hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
                }
            }

            List<String> methods = new ArrayList<>();
            classSymbol.methods().entrySet().stream()
                    .filter(methodSymbol -> HoverUtil.withValidAccessModifiers(methodSymbol.getValue(),
                            currentPackage.get(), currentModule.get().moduleId(), context))
                    .forEach(methodEntry -> {
                        MethodSymbol methodSymbol = methodEntry.getValue();
                        StringBuilder methodInfo = new StringBuilder();
                        Optional<Documentation> methodDoc = methodSymbol.documentation();
                        String signature = CommonUtil.getModifiedSignature(context, methodSymbol.signature());
                        methodInfo.append(MarkupUtils.quotedString(signature));
                        if (methodDoc.isPresent() && methodDoc.get().description().isPresent()) {
                            methodInfo.append(CommonUtil.MD_LINE_SEPARATOR).append(methodDoc.get().description().get());
                        }
                        methods.add(MarkupUtils.bulletItem(methodInfo.toString()));
                    });

            if (!methods.isEmpty()) {
                methods.add(0, MarkupUtils.header(3, ContextConstants.METHOD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);
                hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, methods));
            }
        }

        return HoverUtil.getHoverObject(hoverContent.stream()
                .collect(Collectors.joining(MarkupUtils.getHorizontalSeparator())));
    }

    /**
     * Get hover for expression nodes. Note that we are supplying hover for a selected set of expressions.
     *
     * @param exprNode Expression node
     * @return Hover
     */
    public Hover getHoverObjectForExpression(Node exprNode) {
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

                if (typeSymbol instanceof ClassSymbol classSymbol) {
                    if (classSymbol.initMethod().isEmpty()) {
                        break;
                    }

                    MethodSymbol initMethodSymbol = classSymbol.initMethod().get();
                    return getHoverObjectForSymbol(initMethodSymbol);
                }
        }
        return HoverUtil.getHoverObject();
    }

}
