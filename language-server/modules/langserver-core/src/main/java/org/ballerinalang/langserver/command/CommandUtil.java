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

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
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
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.generateName;

/**
 * Utilities for the command related operations.
 */
public class CommandUtil {

    private CommandUtil() {
    }

    /**
     * Get the command for auto documentation Generation.
     * @param nodeType          Type of the node on which the documentation generated
     * @param docUri            Document Uri
     * @param line              Line of the command being executed
     * @return {@link Command}  Document Generation command
     */
    private static Command getDocGenerationCommand(String nodeType, String docUri, int line) {
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
    private static Command getAllDocGenerationCommand(String docUri) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        return new Command(CommandConstants.ADD_ALL_DOC_TITLE, CommandConstants.CMD_ADD_ALL_DOC,
                new ArrayList<>(Collections.singletonList(docUriArg)));
    }

    private static Command getConstructorGenerationCommand(String docUri, int line) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument startLineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
        return new Command(CommandConstants.CREATE_CONSTRUCTOR_TITLE, CommandConstants.CMD_CREATE_CONSTRUCTOR,
                new ArrayList<>(Arrays.asList(docUriArg, startLineArg)));
    }

    /**
     * Get the commands for the given node type.
     *
     * @param nodeType          Node Type
     * @param docUri            Document URI
     * @param line              Node line
     * @return {@link List}     List of commands for the line
     */
    public static List<Command> getCommandForNodeType(String nodeType, String docUri, int line) {
        List<Command> commands = new ArrayList<>();
        
        if (UtilSymbolKeys.OBJECT_KEYWORD_KEY.equals(nodeType)) {
            commands.add(getConstructorGenerationCommand(docUri, line));
        }
        commands.add(getDocGenerationCommand(nodeType, docUri, line));
        commands.add(getAllDocGenerationCommand(docUri));
        
        return commands;
    }

    /**
     * Get the command instances for a given diagnostic.
     *
     * @param diagnostic        Diagnostic to get the command against
     * @param params            Code Action parameters
     * @param documentManager   Document manager
     * @param lsCompiler        LSCompiler Instance
     * @return {@link List}     List of commands related to the given diagnostic
     */
    public static List<Command> getCommandsByDiagnostic(Diagnostic diagnostic, CodeActionParams params,
                                                        WorkspaceDocumentManager documentManager,
                                                        LSCompiler lsCompiler) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Command> commands = new ArrayList<>();
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                              diagnosticMessage.lastIndexOf("'"));
            LSDocument sourceDocument = new LSDocument(params.getTextDocument().getUri());
            String sourceRoot = LSCompilerUtil.getSourceRoot(sourceDocument.getPath());
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
            BLangInvocation functionNode = getFunctionNode(params, documentManager, lsCompiler);
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
        } else if (isVariableAssignmentRequired(diagnosticMessage)) {
            BLangInvocation functionNode = getFunctionNode(params, documentManager, lsCompiler);
            List<Object> args = new ArrayList<>();
            String varName = generateName(1, getAllEntries(functionNode));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_VAR_NAME, varName));
            String returnSignature = FunctionGenerator.getFuncReturnSignature(functionNode.type);
            args.add(new CommandArgument(CommandConstants.ARG_KEY_RETURN_TYPE, returnSignature));
            String funcLocation = functionNode.pos.sLine + "," + functionNode.pos.sCol;
            args.add(new CommandArgument(CommandConstants.ARG_KEY_FUNC_LOCATION, funcLocation));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, params.getTextDocument().getUri()));
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            commands.add(new Command(commandTitle, CommandConstants.CMD_CREATE_VARIABLE, args));
        } else if (isUnresolvedPackage(diagnosticMessage)) {
            Matcher matcher = CommandConstants.UNRESOLVED_PACKAGE_PATTERN.matcher(
                    diagnosticMessage.toLowerCase(Locale.ROOT)
            );
            if (matcher.find() && matcher.groupCount() > 0) {
                List<Object> args = new ArrayList<>();
                String pkgName = matcher.group(1);
                args.add(new CommandArgument(CommandConstants.ARG_KEY_PKG_NAME, pkgName));
                args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, params.getTextDocument().getUri()));
                String commandTitle = CommandConstants.PULL_PKG_TITLE;
                commands.add(new Command(commandTitle, CommandConstants.CMD_PULL_PACKAGE, args));
            }
        }
        return commands;
    }

    /**
     * Get the object constructor snippet generated from public object fields.
     *
     * @param fields            List of Fields
     * @param baseOffset        Offset of snippet                         
     * @return {@link String}   Constructor snippet as String
     */
    static String getObjectConstructorSnippet(List<BLangVariable> fields, int baseOffset) {
        List<String> fieldNames = fields.stream()
                .filter(bField -> ((bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC))
                .map(bField -> bField.getName().getValue())
                .collect(Collectors.toList());
        String offsetStr = String.join("", Collections.nCopies(baseOffset, " "));

        return offsetStr + "new(" + String.join(", ", fieldNames) + ") {" + CommonUtil.LINE_SEPARATOR
                + CommonUtil.LINE_SEPARATOR + offsetStr + "}" + CommonUtil.LINE_SEPARATOR;
    }

    private static Set<String> getAllEntries(BLangInvocation functionNode) {
        Set<String> strings = new HashSet<>();
        BLangPackage packageNode = getPackageNode(functionNode);
        if (packageNode != null) {
            packageNode.getGlobalVariables().forEach(globalVar -> strings.add(globalVar.name.value));
            packageNode.getGlobalEndpoints().forEach(endpoint -> strings.add(endpoint.getName().getValue()));
            packageNode.getServices().forEach(service -> strings.add(service.name.value));
            packageNode.getFunctions().forEach(func -> strings.add(func.name.value));
        }
        Map<Name, Scope.ScopeEntry> entries = functionNode.symbol.scope.entries;
        entries.forEach((name, scopeEntry) -> strings.add(name.value));
        return strings;
    }

    private static BLangPackage getPackageNode(BLangNode bLangNode) {
        BLangNode parent = bLangNode.parent;
        if (parent != null) {
            return (parent instanceof BLangPackage) ? (BLangPackage) parent : getPackageNode(parent);
        }
        return null;
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
        }

        for (FunctionNode filteredFunction : filteredFunctions) {
            DiagnosticPos functionPos =
                    CommonUtil.toZeroBasedPosition((DiagnosticPos) filteredFunction.getPosition());
            int functionStart = functionPos.getStartLine();
            if (functionStart == line) {
                return getFunctionNodeDocumentation(filteredFunction, line);
            }
        }

        return null;
    }

    static DocAttachmentInfo getFunctionNodeDocumentation(FunctionNode bLangFunction, int replaceFrom) {
        DiagnosticPos functionPos =  CommonUtil.toZeroBasedPosition((DiagnosticPos) bLangFunction.getPosition());
        int offset = functionPos.getStartColumn();
        List<String> attributes = new ArrayList<>();
        bLangFunction.getParameters().forEach(bLangVariable ->
                        attributes.add(getDocAttributeFromBLangVariable((BLangVariable) bLangVariable, offset)));
        if (((BLangFunction) bLangFunction).symbol.retType.getKind() != TypeKind.NIL) {
            attributes.add(getReturnFieldDescription(offset));
        }

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, functionPos.getStartColumn()), replaceFrom);
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

    static DocAttachmentInfo getResourceNodeDocumentation(BLangResource bLangResource, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos resourcePos = CommonUtil.toZeroBasedPosition(bLangResource.getPosition());
        bLangResource.getParameters().forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, resourcePos.getStartColumn())));
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
                            CommonUtil.getLastItem(annotationAttachments).getPosition());
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
                    (((BLangTypeDefinition) topLevelNode).symbol.kind == SymbolKind.OBJECT
                            || ((BLangTypeDefinition) topLevelNode).symbol.kind == SymbolKind.RECORD))
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
                ((BLangTypeDefinition) typeNode).symbol.kind == SymbolKind.OBJECT) {
            publicFields.addAll(((BLangObjectTypeNode) ((BLangTypeDefinition) typeNode).typeNode).getFields().stream()
                    .filter(field -> field.getFlags().contains(Flag.PUBLIC)).collect(Collectors.toList()));
            
        } else if (typeNode instanceof BLangTypeDefinition &&
                ((BLangTypeDefinition) typeNode).symbol.kind == SymbolKind.RECORD) {
            publicFields.addAll(((BLangRecordTypeNode) ((BLangTypeDefinition) typeNode).typeNode).getFields());
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
        return getDocumentationAttribute(bLangVariable.getName().getValue(), offset);
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_PACKAGE);
    }

    private static boolean isUndefinedFunction(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_FUNCTION);
    }

    private static boolean isVariableAssignmentRequired(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED);
    }

    private static boolean isUnresolvedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNRESOLVED_PACKAGE);
    }

    private static BLangInvocation getFunctionNode(CodeActionParams params,
                                                   WorkspaceDocumentManager documentManager,
                                                   LSCompiler lsCompiler) {
        LSServiceOperationContext context = new LSServiceOperationContext();
        Position position = new Position();
        position.setLine(params.getRange().getStart().getLine());
        position.setCharacter(params.getRange().getStart().getCharacter() + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, params.getTextDocument().getUri());
        context.put(DocumentServiceKeys.POSITION_KEY,
                          new TextDocumentPositionParams(params.getTextDocument(), position));
        List<BLangPackage> bLangPackages = lsCompiler.getBLangPackage(context, documentManager, false,
                                                                      LSCustomErrorStrategy.class, true).getLeft();
        // Get the current package.
        BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages,
                                                                                  params.getTextDocument().getUri());

        context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY,
                          currentBLangPackage.symbol.getName().getValue());

        // Run the position calculator for the current package.
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(context);
        currentBLangPackage.accept(positionTreeVisitor);
        return (BLangInvocation) context.get(NodeContextKeys.NODE_KEY);
    }


    /**
     * Inner class for the command argument holding argument key and argument value.
     */
    public static class CommandArgument {
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

    private static String getDocumentationAttribute(String field, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + %s - %s Parameter Description", offsetStr, field, field);
    }

    private static String getReturnFieldDescription(int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + return - Return Value Description", offsetStr);
    }

    private static String getDocumentationAttachment(List<String> attributes, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        if (attributes == null || attributes.isEmpty()) {
            return String.format("%s# Description", offsetStr);
        }

        String joinedList = String.join(" \r\n", attributes);
        return String.format("%s# Description%n%s#%n%s", offsetStr, offsetStr, joinedList);
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
