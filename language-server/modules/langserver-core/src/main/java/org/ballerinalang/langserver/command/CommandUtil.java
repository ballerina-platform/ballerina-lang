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

import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.repository.PackageRepository;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
     * Get the command instances for a given diagnostic.
     * @param diagnostic        Diagnostic to get the command against
     * @param params            Code Action parameters
     * @param documentManager   Document Manager instance
     * @param pkgContext        BLang Package Context
     * @return  {@link List}    List of commands related to the given diagnostic
     */
    public static List<Command> getCommandsByDiagnostic(Diagnostic diagnostic, CodeActionParams params,
                                                        WorkspaceDocumentManager documentManager,
                                                        BLangPackageContext pkgContext) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Command> commands = new ArrayList<>();
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                    diagnosticMessage.lastIndexOf("'"));

            Path openedPath = CommonUtil.getPath(params.getTextDocument().getUri());
            String pkgName = TextDocumentServiceUtil.getPackageFromContent(documentManager.getFileContent(openedPath));
            String sourceRoot = TextDocumentServiceUtil.getSourceRoot(openedPath, pkgName);
            PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
            CompilerContext context = TextDocumentServiceUtil.prepareCompilerContext(packageRepository, sourceRoot,
                    false);
            ArrayList<PackageID> sdkPackages = pkgContext.getSDKPackages(context);
            sdkPackages.stream()
                    .filter(packageID -> packageID.getName().toString().endsWith("." + packageAlias))
                    .forEach(packageID -> {
                        String commandTitle = CommandConstants.IMPORT_PKG_TITLE + " " + packageID.getName().toString();
                        CommandArgument pkgArgument =
                                new CommandArgument(CommandConstants.ARG_KEY_PKG_NAME, packageID.getName().toString());
                        CommandArgument docUriArgument = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI,
                                params.getTextDocument().getUri());
                        commands.add(new Command(commandTitle, CommandConstants.CMD_IMPORT_PACKAGE,
                                new ArrayList<>(Arrays.asList(pkgArgument, docUriArgument))));
                    });
        }

        return commands;
    }

    /**
     * Get the Documentation attachment for the function.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the function in the source
     * @return {@link String}   Documentation attachment for the function
     */
    static String getFunctionDocumentation(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangFunction) {
                BLangFunction functionNode = (BLangFunction) topLevelNode;
                int functionStart = CommonUtil.toZeroBasedPosition(functionNode.getPosition()).getStartLine();
                if (functionStart == line) {
                    List<String> attributes = new ArrayList<>();
                    if (functionNode.getReceiver() != null && functionNode.getReceiver() instanceof BLangVariable) {
                        BLangVariable receiverNode = (BLangVariable) functionNode.getReceiver();
                        attributes.add(getDocumentationAttribute(
                                receiverNode.docTag.getValue(), receiverNode.getName().getValue()));
                    }
                    functionNode.getParameters()
                            .forEach(bLangVariable -> attributes.add(getDocAttributeFromBLangVariable(bLangVariable)));
                    functionNode.getReturnParameters()
                            .forEach(bLangVariable -> attributes.add(getDocAttributeFromBLangVariable(bLangVariable)));
                    return getDocumentationAttachment(attributes);
                }
            }
        }

        return null;
    }

    /**
     * Get the Documentation attachment for the struct definition.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the struct in the source
     * @return {@link String}   Documentation attachment for the struct
     */
    static String getStructDocumentation(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangStruct) {
                BLangStruct structNode = (BLangStruct) topLevelNode;
                int structStart = CommonUtil.toZeroBasedPosition(structNode.getPosition()).getStartLine();
                if (structStart == line) {
                    List<String> attributes = new ArrayList<>();
                    structNode.getFields().forEach(bLangVariable ->
                            attributes.add(getDocAttributeFromBLangVariable(bLangVariable)));
                    return getDocumentationAttachment(attributes);
                }
            }
        }

        return null;
    }

    /**
     * Get the Documentation attachment for the enum.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the enum in the source
     * @return {@link String}   Documentation attachment for the enum
     */
    static String getEnumDocumentation(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangEnum) {
                BLangEnum enumNode = (BLangEnum) topLevelNode;
                int enumStart = CommonUtil.toZeroBasedPosition(enumNode.getPosition()).getStartLine();
                if (enumStart == line) {
                    List<String> attributes = new ArrayList<>();
                    enumNode.getEnumerators().forEach(bLangEnumerator -> attributes
                            .add(getDocumentationAttribute(DocTag.FIELD.getValue(),
                                    bLangEnumerator.getName().getValue())));
                    return getDocumentationAttachment(attributes);
                }
            }
        }

        return null;
    }

    /**
     * Get the Documentation attachment for the transformer.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the transformer in the source
     * @return {@link String}   Documentation attachment for the transformer
     */
    static String getTransformerDocumentation(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangTransformer) {
                BLangTransformer transformerNode = (BLangTransformer) topLevelNode;
                int transformerStart = CommonUtil.toZeroBasedPosition(transformerNode.getPosition()).getStartLine();
                if (transformerStart == line) {
                    List<String> attributes = new ArrayList<>();
                    attributes.add(getDocAttributeFromBLangVariable(transformerNode.source));
                    transformerNode.retParams.forEach(bLangVariable ->
                            attributes.add(getDocAttributeFromBLangVariable(bLangVariable)));
                    transformerNode.requiredParams.forEach(bLangVariable ->
                            attributes.add(getDocAttributeFromBLangVariable(bLangVariable)));

                    return getDocumentationAttachment(attributes);
                }
            }
        }

        return null;
    }

    private static String getDocAttributeFromBLangVariable(BLangVariable bLangVariable) {
        return getDocumentationAttribute(bLangVariable.docTag.getValue(), bLangVariable.getName().getValue());
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_PACKAGE);
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

    private static String getDocumentationAttribute(String docTag, String field) {
        return String.format("%s{{%s}}", docTag, field);
    }

    private static String getDocumentationAttachment(List<String> attributes) {
        String joinedList = String.join(" \r\n\t", attributes);
        return String.format("documentation {%n\t%n\t%s%n}", joinedList);
    }
}
