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
package org.ballerinalang.langserver.codelenses.provider;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProvider;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.command.CommandUtil.CommandArgument;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.lang.reflect.Field;
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
@JavaSPIService("org.ballerinalang.langserver.codelenses.LSCodeLensesProvider")
public class DocsCodeLensesProvider implements LSCodeLensesProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "docs.CodeLenses";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
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
            addDocLenses(lenses, fileUri, bLangPackage, topLevelNode);
        }
        return lenses;
    }

    private void addDocLenses(List<CodeLens> lenses, String fileUri, BLangPackage bLangPackage,
                              TopLevelNode topLevelNode) {
        Pair<String, String> node = resolveTopLevelType(topLevelNode);
        String nodeType = node.getLeft();
        String nodeName = node.getRight();
        DocAttachmentInfo info = getDocumentationEditForNodeByPosition(
                nodeType, bLangPackage, topLevelNode.getPosition().getStartLine() - 1);
        boolean isDocumentable = (info != null);
        Class<? extends TopLevelNode> aTopLeveNodeClass = topLevelNode.getClass();
        if (topLevelNode instanceof AnnotatableNode &&
                (((AnnotatableNode) topLevelNode).getFlags() == null ||
                        !((AnnotatableNode) topLevelNode).getFlags().contains(Flag.PUBLIC))) {
            // If NOT Public; skip
            return;
        }
        int line = topLevelNode.getPosition().getStartLine() - 1;
        int col = topLevelNode.getPosition().getStartColumn();
        boolean hasDocumentation = false;
        for (Field field : aTopLeveNodeClass.getFields()) {
            try {
                if (field.getName().equals("markdownDocumentationAttachment")
                        && field.get(topLevelNode) != null
                        && field.get(topLevelNode) instanceof BLangMarkdownDocumentation) {
                    BLangMarkdownDocumentation docs = (BLangMarkdownDocumentation) field.get(topLevelNode);
                    line = docs.getPosition().getStartLine() - 1;
                    col = docs.getPosition().getStartColumn();
                    hasDocumentation = true;
                }
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        if (isDocumentable) {
            Command command;
            if (hasDocumentation) {
                CommandArgument nameArg = new CommandArgument(CommandConstants.ARG_KEY_FUNCTION_NAME, nodeName);
                List<Object> args = new ArrayList<>(Collections.singletonList(nameArg));
                command = new Command("Preview Docs", "ballerina.showDocs", args);
            } else {
                CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType);
                CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, fileUri);
                CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE,
                                                                String.valueOf(line));
                List<Object> args = new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart));
                command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE,
                                      CommandConstants.CMD_ADD_DOCUMENTATION, args);
            }
            Position pos = new Position(line, col);
            CodeLens lens = new CodeLens(new Range(pos, pos), command, null);
            lenses.add(lens);
        }
    }

    private Pair<String, String> resolveTopLevelType(TopLevelNode topLevelNode) {
        if (topLevelNode instanceof BLangTypeDefinition) {
            BLangTypeDefinition definition = (BLangTypeDefinition) topLevelNode;
            if (definition.typeNode instanceof BLangObjectTypeNode) {
                if (!((BLangObjectTypeNode) definition.typeNode).flagSet.contains(Flag.SERVICE)) {
                    return new ImmutablePair<>("object", definition.name.value);
                }
            } else if (definition.typeNode instanceof BLangRecordTypeNode) {
                return new ImmutablePair<>("record", definition.name.value);
            }
        } else if (topLevelNode instanceof BLangFunction) {
            BLangFunction func = (BLangFunction) topLevelNode;
            return new ImmutablePair<>("function", func.name.value);
        } else if (topLevelNode instanceof BLangService) {
            BLangService service = (BLangService) topLevelNode;
            return new ImmutablePair<>("service", service.name.value);
        }
        return new ImmutablePair<>("", "");
    }
}
