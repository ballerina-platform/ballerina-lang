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

import org.ballerinalang.langserver.LSPackageCache;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
     * @param diagnostic        Diagnostic to get the command against
     * @param params            Code Action parameters
     * @param lsPackageCache    Lang Server Package cache
     * @return  {@link List}    List of commands related to the given diagnostic
     */
    public static List<Command> getCommandsByDiagnostic(Diagnostic diagnostic, CodeActionParams params, 
                                                        LSPackageCache lsPackageCache) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Command> commands = new ArrayList<>();
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                    diagnosticMessage.lastIndexOf("'"));
            LSDocument sourceDocument = new LSDocument(params.getTextDocument().getUri());
            Path openedPath = CommonUtil.getPath(sourceDocument);
            String sourceRoot = TextDocumentServiceUtil.getSourceRoot(openedPath);
            sourceDocument.setSourceRoot(sourceRoot);

            lsPackageCache.getPackageMap().entrySet().stream()
                    .filter(pkgEntry -> {
                        String fullPkgName = pkgEntry.getValue().packageID.orgName.getValue()
                                + "/" + pkgEntry.getValue().packageID.getName().getValue();
                        return fullPkgName.endsWith("." + packageAlias) || fullPkgName.endsWith("/" + packageAlias);
                    })
                    .forEach(pkgEntry -> {
                        PackageID packageID = pkgEntry.getValue().packageID;
                        String commandTitle = CommandConstants.IMPORT_PKG_TITLE + " " + packageID.getName().toString();
                        String fullPkgName = packageID.getOrgName() + "/" + packageID.getName().getValue();
                        CommandArgument pkgArgument =
                                new CommandArgument(CommandConstants.ARG_KEY_PKG_NAME, fullPkgName);
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
    static DocAttachmentInfo getFunctionDocumentationByPosition(BLangPackage bLangPackage, int line) {
        List<FunctionNode> filteredFunctions = new ArrayList<>();
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            
            if (topLevelNode instanceof BLangFunction) {
                filteredFunctions.add((BLangFunction) topLevelNode);
            } else if (topLevelNode instanceof BLangObject) {
                filteredFunctions.addAll(((BLangObject) topLevelNode).getFunctions());
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
     * @return {@link String}   Documentation attachment for the struct
     */
    static DocAttachmentInfo getStructDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangStruct) {
                BLangStruct structNode = (BLangStruct) topLevelNode;
                DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(structNode.getPosition());
                int structStart = structPos.getStartLine();
                if (structStart == line) {
                    return getStructNodeDocumentation(structNode, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getStructNodeDocumentation(BLangStruct bLangStruct, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(bLangStruct.getPosition());
        int offset = structPos.getStartColumn();
        bLangStruct.getFields().forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, structPos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the enum.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the enum in the source
     * @return {@link String}   Documentation attachment for the enum
     */
    static DocAttachmentInfo getEnumDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangEnum) {
                BLangEnum enumNode = (BLangEnum) topLevelNode;
                DiagnosticPos enumPos = CommonUtil.toZeroBasedPosition(enumNode.getPosition());
                int enumStart = enumPos.getStartLine();
                if (enumStart == line) {
                    return getEnumNodeDocumentation(enumNode, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getEnumNodeDocumentation(BLangEnum bLangEnum, int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos enumPos = CommonUtil.toZeroBasedPosition(bLangEnum.getPosition());
        int offset = enumPos.getStartColumn();

        bLangEnum.getEnumerators().forEach(enumerator -> attributes
                .add(getDocumentationAttribute(DocTag.FIELD.getValue(), enumerator.getName().getValue(), offset)));

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, enumPos.getStartColumn()), replaceFrom);
    }

    /**
     * Get the Documentation attachment for the transformer.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the transformer in the source
     * @return {@link String}   Documentation attachment for the transformer
     */
    static DocAttachmentInfo getTransformerDocumentationByPosition(BLangPackage bLangPackage, int line) {
        for (TopLevelNode topLevelNode : bLangPackage.topLevelNodes) {
            if (topLevelNode instanceof BLangTransformer) {
                BLangTransformer transformerNode = (BLangTransformer) topLevelNode;
                DiagnosticPos transformerPos = CommonUtil.toZeroBasedPosition(transformerNode.getPosition());
                int transformerStart = transformerPos.getStartLine();
                if (transformerStart == line) {
                    return getTransformerNodeDocumentation(transformerNode, line);
                }
            }
        }

        return null;
    }

    static DocAttachmentInfo getTransformerNodeDocumentation(BLangTransformer bLangTransformer,
                                                                     int replaceFrom) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos transformerPos = CommonUtil.toZeroBasedPosition(bLangTransformer.getPosition());
        int offset = transformerPos.getStartColumn();

        attributes.add(getDocAttributeFromBLangVariable(bLangTransformer.source, offset));
        bLangTransformer.retParams.forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));
        bLangTransformer.requiredParams.forEach(bLangVariable ->
                attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, transformerPos.getStartColumn()),
                replaceFrom);
    }

    /**
     * Get the Documentation attachment for the resource.
     * @param bLangPackage      BLangPackage built
     * @param line              Start line of the resource in the source
     * @return {@link String}   Documentation attachment for the resource
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
     * @return {@link String}   Documentation attachment for the service
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
