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
package org.ballerinalang.langserver.command;

import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for the command related operations.
 */
public class CommandUtil {

    /**
     * Get the command for auto documentation Generation.
     * @param nodeType          Type of the node on which the documentation generated
     * @param docUri            Document Uri
     * @param line              Line of the command being executed
     * @return {@link Command}  Document Generation command
     */
    public static Command getDocGenerationCommand(String nodeType, String docUri, int line) {
        CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType);
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE,
                String.valueOf(line));

        return new Command(CommandConstants.ADD_DOCUMENTATION_TITLE, CommandConstants.CMD_ADD_DOCUMENTATION,
                new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart)));
    }

    /**
     * Get the command for generate all documentation.
     * @param docUri            Document Uri
     * @return {@link Command}  All Document Generation command
     */
    public static Command getAllDocGenerationCommand(String docUri) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        return new Command(CommandConstants.ADD_ALL_DOC_TITLE, CommandConstants.CMD_ADD_ALL_DOC,
                new ArrayList<>(Collections.singletonList(docUriArg)));
    }

    /**
     * Get the command instances for a given diagnostic.
     *
     * @param diagnostic     Diagnostic to get the command against
     * @param params         Code Action parameters
     * @param documentManager Document manager
     * @return {@link List}    List of commands related to the given diagnostic
     */
    public static List<Command> getCommandsByDiagnostic(Diagnostic diagnostic, CodeActionParams params,
                                                        WorkspaceDocumentManager documentManager) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Command> commands = new ArrayList<>();
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                              diagnosticMessage.lastIndexOf("'"));
            LSDocument sourceDocument = new LSDocument(params.getTextDocument().getUri());
            Path openedPath = CommonUtil.getPath(sourceDocument);
            String sourceRoot = LSCompiler.getSourceRoot(openedPath);
            sourceDocument.setSourceRoot(sourceRoot);
            List<BallerinaPackage> packagesList = new ArrayList<>();
            Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                    .forEach(packagesList::addAll);
            packagesList.stream()
                    .filter(pkgEntry -> {
                        String fullPkgName = pkgEntry.getFullPackageNameAlias();
                        return fullPkgName.endsWith("." + packageAlias) || fullPkgName.endsWith("/" + packageAlias);
                    })
                    .forEach(pkgEntry -> {
                        String commandTitle = CommandConstants.IMPORT_PKG_TITLE + " "
                                + pkgEntry.getFullPackageNameAlias();
                        CommandArgument pkgArgument = new CommandArgument(CommandConstants.ARG_KEY_PKG_NAME,
                                                                          pkgEntry.getFullPackageNameAlias());
                        CommandArgument docUriArgument = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI,
                                                                             params.getTextDocument().getUri());
                        commands.add(new Command(commandTitle, CommandConstants.CMD_IMPORT_PACKAGE,
                                                 new ArrayList<>(Arrays.asList(pkgArgument, docUriArgument))));
                    });
        } else if (isUndefinedFunction(diagnosticMessage)) {
            BLangInvocation functionNode = getFunctionNode(params, documentManager);
            String functionName = functionNode.name.getValue();
            List<Object> args = new ArrayList<>();
            BLangNode parent = functionNode.parent;
            if (parent != null) {
                String returnSignature = FunctionGenerator.getFuncReturnSignature(parent);
                if (returnSignature != null) {
                    args.add(new CommandArgument(CommandConstants.ARG_KEY_RETURN_TYPE, returnSignature));
                }
                String returnStatement = FunctionGenerator.getFuncReturnDefaultStatement(parent, "    return {%1};");
                if (returnStatement != null) {
                    args.add(new CommandArgument(CommandConstants.ARG_KEY_RETURN_DEFAULT_VAL, returnStatement));
                }
                List<String> arguments = FunctionGenerator.getFuncArguments(functionNode);
                if (arguments != null) {
                    args.add(new CommandArgument(CommandConstants.ARG_KEY_FUNC_ARGS, String.join(", ", arguments)));
                }
            }
            args.add(new CommandArgument(CommandConstants.ARG_KEY_FUNC_NAME, functionName));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, params.getTextDocument().getUri()));
            String commandTitle = CommandConstants.CREATE_FUNCTION_TITLE + " " + functionName + "(...)";
            commands.add(new Command(commandTitle, CommandConstants.CMD_CREATE_FUNCTION, args));
        }
        return commands;
    }

    /**
     * Get the Documentation attachment for the function.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the function in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the function
     */
    static DocAttachmentInfo getFunctionDocumentationByPosition(BLangPackage bLangPackage, int line) {
        List<FunctionNode> filteredFunctions = new ArrayList<>();
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            
            if (topLevelNode instanceof BLangFunction) {
                filteredFunctions.add((BLangFunction) topLevelNode);
            } else if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                filteredFunctions
                        .addAll(((BLangObjectTypeNode) (((BLangTypeDefinition) topLevelNode).typeNode)).getFunctions());
            }

            for (FunctionNode filteredFunction : filteredFunctions) {
                DiagnosticPos functionPos =
                        CommonUtil.toZeroBasedPosition((DiagnosticPos) filteredFunction.getPosition());
                int functionStart = functionPos.getStartLine();
                if (functionStart == line) {
                    return getFunctionNodeDocumentation(filteredFunction, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getFunctionNodeDocumentation(FunctionNode bLangFunction, int replaceFrom) {
        DiagnosticPos functionPos =  CommonUtil.toZeroBasedPosition((DiagnosticPos) bLangFunction.getPosition());
        int offset = functionPos.getStartColumn();
        List<String> attributes = new ArrayList<>();
        if (bLangFunction.getReceiver() != null && bLangFunction.getReceiver() instanceof BLangVariable) {
            BLangVariable receiverNode = (BLangVariable) bLangFunction.getReceiver();
            attributes.add(getDocumentationAttribute(receiverNode.docTag.getValue(), receiverNode.getName().getValue(),
                    offset));
        }
        bLangFunction.getParameters().forEach(bLangVariable ->
                        attributes.add(getDocAttributeFromBLangVariable((BLangVariable) bLangVariable, offset)));
        // TODO: Fix with the latest changes properly
//        bLangFunction.getReturnParameters()
//                .forEach(bLangVariable -> {
//                    if (!bLangVariable.getName().getValue().isEmpty()) {
//                        attributes.add(getDocAttributeFromBLangVariable((BLangVariable) bLangVariable, offset));
//                    }
//                });

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, functionPos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the struct definition.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the struct in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the struct
     */
    static DocAttachmentInfo getRecordOrObjectDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangTypeDefinition) {
                BLangTypeDefinition typeDefNode = (BLangTypeDefinition) topLevelNode;
                DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(typeDefNode.getPosition());
                int structStart = structPos.getStartLine();
                if (structStart == line) {
                    return getRecordOrObjectDocumentation(typeDefNode, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getRecordOrObjectDocumentation(BLangTypeDefinition typeDef, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        List<BLangVariable> fields = new ArrayList<>();
        DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(typeDef.getPosition());
        if (typeDef.typeNode instanceof BLangObjectTypeNode) {
            fields.addAll(((BLangObjectTypeNode) typeDef.typeNode).fields);
        } else if (typeDef.typeNode instanceof BLangRecordTypeNode) {
            fields.addAll(((BLangRecordTypeNode) typeDef.typeNode).fields);
        }
        int offset = structPos.getStartColumn();
        fields.forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, structPos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the endpoint.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the enum in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the enum
     */
    static DocAttachmentInfo getEndpointDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangEndpoint) {
                BLangEndpoint endpointNode = (BLangEndpoint) topLevelNode;
                DiagnosticPos enumPos = CommonUtil.toZeroBasedPosition(endpointNode.getPosition());
                int endpointStart = enumPos.getStartLine();
                if (endpointStart == line) {
                    return getEndpointNodeDocumentation(endpointNode, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getEndpointNodeDocumentation(BLangEndpoint bLangEndpoint, int replaceFrom) {
        DiagnosticPos endpointPos = CommonUtil.toZeroBasedPosition(bLangEndpoint.getPosition());

        return new DocAttachmentInfo(getDocumentationAttachment(null, endpointPos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the resource.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the resource in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the resource
     */
    static DocAttachmentInfo getResourceDocumentationByPosition(BLangPackage bLangPackage, int line) {
        // TODO: Currently resource position is invalid and we use the annotation attachment positions.
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangService) {
                BLangService serviceNode = (BLangService) topLevelNode;

                for (BLangResource bLangResource : serviceNode.getResources()) {
                    List<BLangAnnotationAttachment> annotationAttachments = bLangResource.getAnnotationAttachments();
                    DiagnosticPos resourcePos = CommonUtil.toZeroBasedPosition(bLangResource.getPosition());
                    if (!annotationAttachments.isEmpty()) {
                        DiagnosticPos lastAttachmentPos = CommonUtil.toZeroBasedPosition(
                                annotationAttachments.get(annotationAttachments.size() - 1).getPosition());
                        if (lastAttachmentPos.getEndLine() < line && line < resourcePos.getEndLine()) {
                            return getResourceNodeDocumentation(bLangResource, lastAttachmentPos.getEndLine() + 1);
                        }
                    } else if (resourcePos.getStartLine() == line) {
                        return getResourceNodeDocumentation(bLangResource, line);
                    }
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getResourceNodeDocumentation(BLangResource bLangResource, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos resourcePos = CommonUtil.toZeroBasedPosition(bLangResource.getPosition());
        bLangResource.getParameters()
                .forEach(bLangVariable -> {
                    if (!(bLangVariable.symbol instanceof BEndpointVarSymbol)) {
                        attributes.add(getDocAttributeFromBLangVariable(bLangVariable, resourcePos.getStartColumn()));
                    }
                });
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, resourcePos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the service.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the service in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the service
     */
    static DocAttachmentInfo getServiceDocumentationByPosition(BLangPackage bLangPackage, int line) {
        // TODO: Currently resource position is invalid and we use the annotation attachment positions.
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangService) {
                BLangService serviceNode = (BLangService) topLevelNode;
                DiagnosticPos servicePos = CommonUtil.toZeroBasedPosition(serviceNode.getPosition());
                List<BLangAnnotationAttachment> annotationAttachments = serviceNode.getAnnotationAttachments();
                if (!annotationAttachments.isEmpty()) {
                    DiagnosticPos lastAttachmentPos = CommonUtil.toZeroBasedPosition(
                            annotationAttachments.get(annotationAttachments.size() - 1).getPosition());

                    if (lastAttachmentPos.getEndLine() < line && line < servicePos.getEndLine()) {
                        return getServiceNodeDocumentation(serviceNode, lastAttachmentPos.getEndLine() + 1);
                    }
                } else if (servicePos.getStartLine() == line) {
                    return getServiceNodeDocumentation(serviceNode, line);
                }
            }
        }

        return null;
    }
    
    /**
     * Get the Documentation attachment for the type nodes.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the type node in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the type node
     */
    static DocAttachmentInfo getTypeNodeDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            DiagnosticPos typeNodePos;
            typeNodePos = (DiagnosticPos) topLevelNode.getPosition();
            if ((topLevelNode instanceof BLangTypeDefinition &&
                    (((BLangTypeDefinition) topLevelNode).symbol.getKind() == SymbolKind.OBJECT
                            || ((BLangTypeDefinition) topLevelNode).symbol.getKind() == SymbolKind.RECORD))
                    && typeNodePos.getStartLine() - 1 == line) {
                return getTypeNodeDocumentation(topLevelNode, line);
            }
        }
        
        return null;
    }
    
    static DocAttachmentInfo getTypeNodeDocumentation(TopLevelNode typeNode, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos typeNodePos = CommonUtil.toZeroBasedPosition((DiagnosticPos) typeNode.getPosition());
        int offset = typeNodePos.getStartColumn();
        List<VariableNode> publicFields = new ArrayList<>();
        if (typeNode instanceof BLangTypeDefinition &&
                ((BLangTypeDefinition) typeNode).symbol.getKind() == SymbolKind.OBJECT) {
            publicFields.addAll(((BLangObjectTypeNode) ((BLangTypeDefinition) typeNode).typeNode).getFields().stream()
                    .filter(field -> field.getFlags().contains(Flag.PUBLIC)).collect(Collectors.toList()));
            
        } else if (typeNode instanceof BLangTypeDefinition &&
                ((BLangTypeDefinition) typeNode).symbol.getKind() == SymbolKind.RECORD) {
            publicFields.addAll(((BLangObjectTypeNode) ((BLangTypeDefinition) typeNode).typeNode).getFields());
        }
        
        publicFields.forEach(variableNode -> {
            attributes.add(getDocAttributeFromBLangVariable((BLangVariable) variableNode, offset));
        });
        
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, offset), replaceFrom);
    }

    static DocAttachmentInfo getServiceNodeDocumentation(BLangService bLangService, int replaceFrom) {
        DiagnosticPos servicePos = CommonUtil.toZeroBasedPosition(bLangService.getPosition());
        return new DocAttachmentInfo(getDocumentationAttachment(null, servicePos.getStartColumn()), replaceFrom);
    }

    private static String getDocAttributeFromBLangVariable(BLangVariable bLangVariable, int offset) {
        return getDocumentationAttribute(bLangVariable.docTag.getValue(), bLangVariable.getName().getValue(), offset);
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_PACKAGE);
    }

    private static boolean isUndefinedFunction(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_FUNCTION);
    }

    private static BLangInvocation getFunctionNode(CodeActionParams params,
                                                   WorkspaceDocumentManager documentManager) {
        LSServiceOperationContext renameContext = new LSServiceOperationContext();
        List<Location> contents = new ArrayList<>();
        Position position = params.getRange().getStart();
        position.setCharacter(position.getCharacter() + 1);
        renameContext.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());
        renameContext.put(DocumentServiceKeys.POSITION_KEY,
                          new TextDocumentPositionParams(params.getTextDocument(), position));
        List<BLangPackage> bLangPackages = LSCompiler.getBLangPackage(renameContext, documentManager, false,
                                                                      LSCustomErrorStrategy.class, true);
        // Get the current package.
        BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages,
                                                                                  params.getTextDocument().getUri());

        renameContext.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                          currentBLangPackage.symbol.getName().getValue());
        renameContext.put(NodeContextKeys.REFERENCE_NODES_KEY, contents);

        // Run the position calculator for the current package.
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(renameContext);
        currentBLangPackage.accept(positionTreeVisitor);
        return (BLangInvocation) renameContext.get(NodeContextKeys.NODE_KEY);
    }


    /**
     * Inner class for the command argument holding argument key and argument value.
     */
    private static class CommandArgument {
        private String argumentK;

        private String argumentV;

        CommandArgument(String argumentK, String argumentV) {
            this.argumentK = argumentK;
            this.argumentV = argumentV;
        }

        public String getArgumentK() {
            return argumentK;
        }

        public String getArgumentV() {
            return argumentV;
        }
    }

    private static String getDocumentationAttribute(String docTag, String field, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s%s{{%s}}", offsetStr, docTag, field);
    }

    private static String getDocumentationAttachment(List<String> attributes, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        if (attributes == null || attributes.isEmpty()) {
            return String.format("%sdocumentation {%n%s\t%n%s}", offsetStr, offsetStr, offsetStr);
        }

        String joinedList = String.join(" \r\n\t", attributes);
        return String.format("%sdocumentation {%n%s\t%n\t%s%n%s}", offsetStr, offsetStr, joinedList, offsetStr);
    }

    /**
     * Holds the meta information required for the documentation attachment.
     */
    static class DocAttachmentInfo {

        DocAttachmentInfo(String docAttachment, int replaceStartFrom) {
            this.docAttachment = docAttachment;
            this.replaceStartFrom = replaceStartFrom;
        }

        private String docAttachment;

        private int replaceStartFrom;

        String getDocAttachment() {
            return docAttachment;
        }

        int getReplaceStartFrom() {
            return replaceStartFrom;
        }
    }
}
