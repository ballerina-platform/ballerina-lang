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
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.LanguageClient;
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

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.generateName;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

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
                        String commandTitle = CommandConstants.IMPORT_MODULE_TITLE + " "
                                + pkgEntry.getFullPackageNameAlias();
                        CommandArgument pkgArgument = new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME,
                                                                          pkgEntry.getFullPackageNameAlias());
                        CommandArgument docUriArgument = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI,
                                                                             params.getTextDocument().getUri());
                        commands.add(new Command(commandTitle, CommandConstants.CMD_IMPORT_MODULE,
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
            Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                    diagnosticMessage.toLowerCase(Locale.ROOT)
            );
            if (matcher.find() && matcher.groupCount() > 0) {
                List<Object> args = new ArrayList<>();
                String pkgName = matcher.group(1);
                args.add(new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME, pkgName));
                args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, params.getTextDocument().getUri()));
                String commandTitle = CommandConstants.PULL_MOD_TITLE;
                commands.add(new Command(commandTitle, CommandConstants.CMD_PULL_MODULE, args));
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
    public static String getObjectConstructorSnippet(List<BLangVariable> fields, int baseOffset) {
        List<String> fieldNames = fields.stream()
                .filter(bField -> ((bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC))
                .map(bField -> bField.getName().getValue())
                .collect(Collectors.toList());
        String offsetStr = String.join("", Collections.nCopies(baseOffset, " "));

        return offsetStr + "new(" + String.join(", ", fieldNames) + ") {" + CommonUtil.LINE_SEPARATOR
                + CommonUtil.LINE_SEPARATOR + offsetStr + "}" + CommonUtil.LINE_SEPARATOR;
    }

    /**
     * Sends a message to the language server client.
     *
     * @param client      Language Server client
     * @param messageType message type
     * @param message     message
     */
    public static void notifyClient(LanguageClient client, MessageType messageType, String message) {
        client.showMessage(new MessageParams(messageType, message));
    }

    /**
     * Clears diagnostics of the client by sending an text edit event.
     * @param client            Language Server client
     * @param lsCompiler        Language Server Compiler instance
     * @param diagnosticsHelper diagnostics helper
     * @param documentUri       Current text document URI
     */
    public static void clearDiagnostics(LanguageClient client, LSCompiler lsCompiler,
                                         DiagnosticsHelper diagnosticsHelper, String documentUri) {
        Path filePath = Paths.get(URI.create(documentUri));
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
        diagnosticsHelper.compileAndSendDiagnostics(client, lsCompiler, filePath, compilationPath);
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
    public static DocAttachmentInfo getFunctionDocumentationByPosition(BLangPackage bLangPackage, int line) {
        List<BLangFunction> filteredFunctions = new ArrayList<>();
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            
            if (topLevelNode instanceof BLangFunction) {
                filteredFunctions.add((BLangFunction) topLevelNode);
            } else if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                filteredFunctions
                        .addAll(((BLangObjectTypeNode) (((BLangTypeDefinition) topLevelNode).typeNode)).getFunctions());
            }
        }

        for (BLangFunction filteredFunction : filteredFunctions) {
            DiagnosticPos functionPos = CommonUtil.toZeroBasedPosition(filteredFunction.getPosition());
            int functionStart = functionPos.getStartLine();
            if (functionStart == line) {
                return getFunctionNodeDocumentation(filteredFunction);
            }
        }

        return null;
    }

    private static DocAttachmentInfo getFunctionNodeDocumentation(BLangFunction bLangFunction) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos functionPos =  CommonUtil.toZeroBasedPosition(bLangFunction.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangFunction.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangFunction.getPosition(), annotations);
        int offset = functionPos.getStartColumn();
        
        List<BLangVariable> params = new ArrayList<>(bLangFunction.getParameters());
        if (bLangFunction.getRestParameters() != null) {
            params.add((BLangVariable) bLangFunction.getRestParameters());
        }
        params.addAll(bLangFunction.getDefaultableParameters()
                .stream()
                .map(bLangVariableDef -> bLangVariableDef.var)
                .collect(Collectors.toList()));
        params.sort(new FunctionArgsComparator());

        params.forEach(param -> attributes.add(getDocAttributeFromBLangVariable((BLangVariable) param, offset)));
        if (bLangFunction.symbol.retType.getKind() != TypeKind.NIL) {
            attributes.add(getReturnFieldDescription(offset));
        }

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, functionPos.getStartColumn()), docStart);
    }

    static DocAttachmentInfo getRecordOrObjectDocumentation(BLangTypeDefinition typeDef) {
        List<String> attributes = new ArrayList<>();
        List<BLangVariable> fields = new ArrayList<>();
        DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(typeDef.getPosition());
        List<BLangAnnotationAttachment> annotations = typeDef.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(typeDef.getPosition(), annotations);

        if (typeDef.typeNode instanceof BLangObjectTypeNode) {
            fields.addAll(((BLangObjectTypeNode) typeDef.typeNode).fields);
        } else if (typeDef.typeNode instanceof BLangRecordTypeNode) {
            fields.addAll(((BLangRecordTypeNode) typeDef.typeNode).fields);
        }
        int offset = structPos.getStartColumn();
        fields.forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, structPos.getStartColumn()), docStart);
    }

    /**
     * Get the Documentation attachment for the endpoint.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the enum in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the enum
     */
    public static DocAttachmentInfo getEndpointDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangEndpoint) {
                BLangEndpoint endpointNode = (BLangEndpoint) topLevelNode;
                DiagnosticPos enumPos = CommonUtil.toZeroBasedPosition(endpointNode.getPosition());
                int endpointStart = enumPos.getStartLine();
                if (endpointStart == line) {
                    return getEndpointNodeDocumentation(endpointNode);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getEndpointNodeDocumentation(BLangEndpoint bLangEndpoint) {
        DiagnosticPos endpointPos = CommonUtil.toZeroBasedPosition(bLangEndpoint.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangEndpoint.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangEndpoint.getPosition(), annotations);

        return new DocAttachmentInfo(getDocumentationAttachment(null, endpointPos.getStartColumn()), docStart);
    }

    static DocAttachmentInfo getResourceNodeDocumentation(BLangResource bLangResource) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos resourcePos = CommonUtil.toZeroBasedPosition(bLangResource.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangResource.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangResource.getPosition(), annotations);
        bLangResource.getParameters().forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, resourcePos.getStartColumn())));
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, resourcePos.getStartColumn()), docStart);
    }

    /**
     * Get the Documentation attachment for the service.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the service in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the service
     */
    public static DocAttachmentInfo getServiceDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangService && topLevelNode.getPosition().getStartLine() - 1 == line) {
                BLangService serviceNode = (BLangService) topLevelNode;
                return getServiceNodeDocumentation(serviceNode);
            }
        }

        return null;
    }

    static DocAttachmentInfo getServiceNodeDocumentation(BLangService bLangService) {
        DiagnosticPos servicePos = CommonUtil.toZeroBasedPosition(bLangService.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangService.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangService.getPosition(), annotations);
        return new DocAttachmentInfo(getDocumentationAttachment(null, servicePos.getStartColumn()), docStart);
    }
    
    /**
     * Get the Documentation attachment for the type nodes.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the type node in the source
     * @return {@link DocAttachmentInfo}   Documentation attachment for the type node
     */
    public static DocAttachmentInfo getTypeNodeDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (!(topLevelNode instanceof BLangTypeDefinition)) {
                continue;
            }
            BLangTypeDefinition typeDef = (BLangTypeDefinition) topLevelNode;
            DiagnosticPos typeNodePos = CommonUtil.toZeroBasedPosition(typeDef.getPosition());
            if ((typeDef.symbol.kind == SymbolKind.OBJECT || typeDef.symbol.kind == SymbolKind.RECORD)
                    && typeNodePos.getStartLine() == line) {
                return getTypeNodeDocumentation((BLangTypeDefinition) topLevelNode);
            }
        }
        
        return null;
    }

    /**
     * Get Documentation edit for node at a given position.
     *
     * @param topLevelNodeType  top level node type
     * @param bLangPkg          BLang package
     * @param line              position to be compared with
     * @return Document attachment info
     */
    public static CommandUtil.DocAttachmentInfo getDocumentationEditForNodeByPosition(String topLevelNodeType,
                                                                                      BLangPackage bLangPkg,
                                                                                      int line) {
        CommandUtil.DocAttachmentInfo docAttachmentInfo = null;
        switch (topLevelNodeType) {
            case UtilSymbolKeys.FUNCTION_KEYWORD_KEY:
                docAttachmentInfo = CommandUtil.getFunctionDocumentationByPosition(bLangPkg, line);
                break;
            case UtilSymbolKeys.ENDPOINT_KEYWORD_KEY:
                docAttachmentInfo = CommandUtil.getEndpointDocumentationByPosition(bLangPkg, line);
                break;
            case UtilSymbolKeys.SERVICE_KEYWORD_KEY:
                docAttachmentInfo = CommandUtil.getServiceDocumentationByPosition(bLangPkg, line);
                break;
            case UtilSymbolKeys.RECORD_KEYWORD_KEY:
            case UtilSymbolKeys.OBJECT_KEYWORD_KEY:
                docAttachmentInfo = CommandUtil.getTypeNodeDocumentationByPosition(bLangPkg, line);
                break;
            default:
                break;
        }

        return docAttachmentInfo;
    }

    /**
     * Get the documentation edit attachment info for a given particular node.
     *
     * @param node Node given
     * @return Doc Attachment Info
     */
    public static CommandUtil.DocAttachmentInfo getDocumentationEditForNode(Node node) {
        CommandUtil.DocAttachmentInfo docAttachmentInfo = null;
        switch (node.getKind()) {
            case FUNCTION:
                if (((BLangFunction) node).markdownDocumentationAttachment == null) {
                    docAttachmentInfo = CommandUtil.getFunctionNodeDocumentation((BLangFunction) node);
                }
                break;
            case TYPE_DEFINITION:
                if (((BLangTypeDefinition) node).markdownDocumentationAttachment == null
                        && (((BLangTypeDefinition) node).typeNode instanceof BLangRecordTypeNode
                        || ((BLangTypeDefinition) node).typeNode instanceof BLangObjectTypeNode)) {
                    docAttachmentInfo = CommandUtil.getRecordOrObjectDocumentation((BLangTypeDefinition) node);
                }
                break;
            case ENDPOINT:
                docAttachmentInfo = CommandUtil.getEndpointNodeDocumentation((BLangEndpoint) node);
                break;
            case RESOURCE:
                if (((BLangResource) node).markdownDocumentationAttachment == null) {
                    BLangResource bLangResource = (BLangResource) node;
                    docAttachmentInfo = CommandUtil.getResourceNodeDocumentation(bLangResource);
                }
                break;
            case SERVICE:
                if (((BLangService) node).markdownDocumentationAttachment == null) {
                    BLangService bLangService = (BLangService) node;
                    docAttachmentInfo = CommandUtil.getServiceNodeDocumentation(bLangService);
                }
                break;
            default:
                break;
        }

        return docAttachmentInfo;
    }

    /**
     * Apply a given single text edit.
     *
     * @param editText      Edit text to be inserted
     * @param range         Line Range to be processed
     * @param identifier    Document identifier
     * @param client        Language Client
     * @return {@link ApplyWorkspaceEditParams}     Workspace edit params
     */
    public static ApplyWorkspaceEditParams applySingleTextEdit(String editText, Range range,
                                                                VersionedTextDocumentIdentifier identifier,
                                                                LanguageClient client) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
        TextEdit textEdit = new TextEdit(range, editText);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(identifier,
                Collections.singletonList(textEdit));
        workspaceEdit.setDocumentChanges(Collections.singletonList(textDocumentEdit));
        applyWorkspaceEditParams.setEdit(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    /**
     * Apply a workspace edit for the current instance.
     *
     * @param textDocumentEdits     List of document edits for current session
     * @param client                Language Client
     * @return {@link Object}       workspace edit parameters
     */
    public static Object applyWorkspaceEdit(List<TextDocumentEdit> textDocumentEdits, LanguageClient client) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        workspaceEdit.setDocumentChanges(textDocumentEdits);
        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }
    
    static DocAttachmentInfo getTypeNodeDocumentation(BLangTypeDefinition typeNode) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos typeNodePos = CommonUtil.toZeroBasedPosition(typeNode.getPosition());
        int offset = typeNodePos.getStartColumn();
        List<BLangAnnotationAttachment> annotations = typeNode.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(typeNode.getPosition(), annotations);
        List<VariableNode> publicFields = new ArrayList<>();
        if (typeNode.symbol.kind == SymbolKind.OBJECT) {
            publicFields.addAll(((BLangObjectTypeNode) typeNode.typeNode).getFields()
                    .stream()
                    .filter(field -> field.getFlags().contains(Flag.PUBLIC))
                    .collect(Collectors.toList()));
            
        } else if (typeNode.symbol.kind == SymbolKind.RECORD) {
            publicFields.addAll(((BLangRecordTypeNode) typeNode.typeNode).getFields());
        }
        
        publicFields.forEach(variableNode ->
                attributes.add(getDocAttributeFromBLangVariable((BLangVariable) variableNode, offset)));
        
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, offset), docStart);
    }

    private static String getDocAttributeFromBLangVariable(BLangVariable bLangVariable, int offset) {
        return getDocumentationAttribute(bLangVariable.getName().getValue(), offset);
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_MODULE);
    }

    private static boolean isUndefinedFunction(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_FUNCTION);
    }

    private static boolean isVariableAssignmentRequired(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED);
    }

    private static boolean isUnresolvedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNRESOLVED_MODULE);
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
            return String.format("# Description%n%s", offsetStr);
        }

        String joinedList = String.join(" \r\n", attributes);
        return String.format("# Description%n%s#%n%s%n%s", offsetStr, joinedList, offsetStr);
    }
    
    private static Position getDocumentationStartPosition(DiagnosticPos nodePos,
                                                          List<BLangAnnotationAttachment> annotations) {
        DiagnosticPos startPos;
        if (annotations.isEmpty()) {
            startPos = CommonUtil.toZeroBasedPosition(nodePos);
        } else {
            startPos = CommonUtil.toZeroBasedPosition(annotations.get(0).getPosition());
        }

        return new Position(startPos.getStartLine(), startPos.getStartColumn());
    }

    /**
     * Holds the meta information required for the documentation attachment.
     */
    public static class DocAttachmentInfo {

        private String docAttachment;

        private Position docStartPos;

        DocAttachmentInfo(String docAttachment, Position docStartPos) {
            this.docAttachment = docAttachment;
            this.docStartPos = docStartPos;
        }

        public String getDocAttachment() {
            return docAttachment;
        }

        public Position getDocStartPos() {
            return docStartPos;
        }
    }
    
    private static class FunctionArgsComparator implements Serializable, Comparator<BLangVariable> {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(BLangVariable arg1, BLangVariable arg2) {
            return arg1.getPosition().getStartColumn() - arg2.getPosition().getStartColumn();
        }
    }
}
