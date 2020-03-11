/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codelenses.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.langserver.codelenses.CodeLensUtil;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNodeByPosition;

/**
 * Code lenses provider for adding all documentation for top level items.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider")
public class DocsCodeLensesProvider extends AbstractCodeLensesProvider {
    public DocsCodeLensesProvider() {
        super("docs.CodeLenses");
        LSClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            isEnabled = newConfig.getCodeLens().getDocs().isEnabled();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        BLangCompilationUnit cUnit = context.get(CodeLensesProviderKeys.COMPILATION_UNIT_KEY);
        String fileUri = context.get(CodeLensesProviderKeys.FILE_URI_KEY);
        BLangPackage bLangPackage = context.get(CodeLensesProviderKeys.BLANG_PACKAGE_KEY);
        for (TopLevelNode topLevelNode : cUnit.getTopLevelNodes()) {
            addDocLenses(lenses, fileUri, bLangPackage, topLevelNode, context);
        }
        return lenses;
    }

    private void addDocLenses(List<CodeLens> lenses, String fileUri, BLangPackage bLangPackage,
                              TopLevelNode topLevelNode, LSContext context) {
        TopLevelNodeDetail nodeDetail = resolveTopLevelTypeDetails(topLevelNode);
        if (nodeDetail == null) {
            // Skip, unknown construct
            return;
        }
        String nodeType = nodeDetail.type;
        String nodeName = nodeDetail.name;
        Position nodePosition = nodeDetail.position;
        Position nodeTopMostPos = nodeDetail.topMostPosition;
        boolean hasDocumentation = nodeDetail.hasDocumentation;
        DocAttachmentInfo info = getDocumentationEditForNodeByPosition(
                nodeType, bLangPackage, topLevelNode.getPosition().getStartLine() - 1, context);
        if (info == null) {
            // Skip, can not document
            return;
        }
        if (topLevelNode instanceof AnnotatableNode &&
                (((AnnotatableNode) topLevelNode).getFlags() == null ||
                        !((AnnotatableNode) topLevelNode).getFlags().contains(Flag.PUBLIC))) {
            // Skip, does not have public modifier
            return;
        }
        Command command;
        if (hasDocumentation) {
            CommandArgument nameArg = new CommandArgument(CommandConstants.ARG_KEY_FUNCTION_NAME, nodeName);
            List<Object> args = new ArrayList<>(Collections.singletonList(nameArg));
            command = new Command("Preview Docs", "ballerina.showDocs", args);
            lenses.add(new CodeLens(new Range(nodeTopMostPos, nodeTopMostPos), command, null));
        } else {
            CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType);
            CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, fileUri);
            CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE,
                                                            String.valueOf(nodePosition.getLine()));
            List<Object> args = new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart));
            command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE,
                                  AddDocumentationExecutor.COMMAND, args);
            lenses.add(new CodeLens(new Range(nodeTopMostPos, nodeTopMostPos), command, null));
        }
    }

    private TopLevelNodeDetail resolveTopLevelTypeDetails(TopLevelNode topLevelNode) {
        if (topLevelNode.getWS() == null) {
            // Skip $anon$ constructs
            return null;
        } else if (topLevelNode instanceof BLangTypeDefinition) {
            BLangTypeDefinition definition = (BLangTypeDefinition) topLevelNode;
            return TopLevelNodeDetail.fromTypeDefinition(definition);
        } else if (topLevelNode instanceof BLangFunction) {
            BLangFunction func = (BLangFunction) topLevelNode;
            return TopLevelNodeDetail.fromFunction(func);
        } else if (topLevelNode instanceof BLangService) {
            BLangService service = (BLangService) topLevelNode;
            return TopLevelNodeDetail.fromService(service);
        }
        return null;
    }

    private static class TopLevelNodeDetail {
        String type;
        String name;
        Position position;
        Position topMostPosition;
        boolean hasDocumentation;

        TopLevelNodeDetail(String type, String name, Position position,
                           Position topmostPos, boolean hasDocumentation) {
            this.type = type;
            this.name = name;
            this.position = position;
            this.topMostPosition = topmostPos;
            this.hasDocumentation = hasDocumentation;
        }

        static TopLevelNodeDetail fromFunction(BLangFunction func) {
            if (BLangConstants.MAIN_FUNCTION_NAME.equals(func.name.value)) {
                // Skip main function
                return null;
            }
            int sLine = func.pos.sLine - 1;
            sLine = CodeLensUtil.getTopMostLocOfAnnotations(func.annAttachments, sLine);
            sLine = CodeLensUtil.getTopMostLocOfDocs(func.markdownDocumentationAttachment, sLine);
            boolean hasDocs = (func.markdownDocumentationAttachment != null);
            Position pos = new Position(func.pos.sLine - 1, 0);
            Position topmostPos = new Position(sLine, 0);
            return new TopLevelNodeDetail("function", func.name.value, pos, topmostPos, hasDocs);
        }

        static TopLevelNodeDetail fromService(BLangService service) {
            int sLine = service.pos.sLine - 1;
            sLine = CodeLensUtil.getTopMostLocOfAnnotations(service.annAttachments, sLine);
            sLine = CodeLensUtil.getTopMostLocOfDocs(service.markdownDocumentationAttachment, sLine);
            boolean hasDocs = (service.markdownDocumentationAttachment != null);
            Position pos = new Position(service.pos.sLine - 1, 0);
            Position topmostPos = new Position(sLine, 0);
            return new TopLevelNodeDetail("service", service.name.value, pos, topmostPos, hasDocs);
        }

        static TopLevelNodeDetail fromTypeDefinition(BLangTypeDefinition definition) {
            boolean hasDocs = (definition.markdownDocumentationAttachment != null);
            int sLine = definition.pos.sLine - 1;
            sLine = CodeLensUtil.getTopMostLocOfAnnotations(definition.annAttachments, sLine);
            sLine = CodeLensUtil.getTopMostLocOfDocs(definition.markdownDocumentationAttachment, sLine);
            Position pos = new Position(definition.pos.sLine - 1, 0);
            Position topmostPos = new Position(sLine, 0);
            if (definition.typeNode instanceof BLangObjectTypeNode) {
                if (!((BLangObjectTypeNode) definition.typeNode).flagSet.contains(Flag.SERVICE)) {
                    return new TopLevelNodeDetail("object", definition.name.value, pos, topmostPos, hasDocs);
                }
            } else if (definition.typeNode instanceof BLangRecordTypeNode) {
                return new TopLevelNodeDetail("record", definition.name.value, pos, topmostPos, hasDocs);
            }
            return null;
        }
    }
}
