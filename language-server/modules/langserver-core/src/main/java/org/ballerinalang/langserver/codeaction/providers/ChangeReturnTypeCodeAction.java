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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.runtime.util.BLangConstants;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Code Action for incompatible return types.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ChangeReturnTypeCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        if (!(diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        NonTerminalNode matchedNode = positionDetails.matchedNode();
        if (filePath.isEmpty() || matchedNode.kind() != SyntaxKind.FUNCTION_DEFINITION) {
            return Collections.emptyList();
        }
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 1) {
            String foundType = matcher.group(2);
            FunctionDefinitionNode funcDef = (FunctionDefinitionNode) matchedNode;
            if (!BLangConstants.MAIN_FUNCTION_NAME.equals(funcDef.functionName().text())) {
                // Process full-qualified BType name  eg. ballerina/http:Client and if required; add
                // auto-import
                matcher = CommandConstants.FQ_TYPE_PATTERN.matcher(foundType);
                List<TextEdit> edits = new ArrayList<>();
                String editText = extractTypeName(matcher, syntaxTree, context, foundType, edits);

                // Process function node
                Position start;
                Position end;
                if (funcDef.functionSignature().returnTypeDesc().isEmpty()) {
                    // eg. function test() {...}
                    LinePosition closeParanPos =
                            funcDef.functionSignature().closeParenToken().lineRange().startLine();
                    start = new Position(closeParanPos.line(), closeParanPos.offset() - 1);
                    end = start;
                    editText = " returns (" + editText + ")";
                } else {
                    // eg. function test() returns () {...}
                    ReturnTypeDescriptorNode returnTypeDesc = funcDef.functionSignature().returnTypeDesc().get();
                    LinePosition retStart = returnTypeDesc.lineRange().startLine();
                    LinePosition retEnd = returnTypeDesc.lineRange().endLine();
                    start = new Position(retStart.line(),
                                         retStart.offset());
                    end = new Position(retEnd.line(), retEnd.offset());
                }
                edits.add(new TextEdit(new Range(start, end), editText));

                // Add code action
                String commandTitle = CommandConstants.CHANGE_RETURN_TYPE_TITLE + foundType + "'";
                return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
            }
        }
        return Collections.emptyList();
    }

    private static String extractTypeName(Matcher matcher, SyntaxTree syntaxTree,
                                          LSContext context, String foundType,
                                          List<TextEdit> edits) {
        if (matcher.find() && matcher.groupCount() > 2) {
            String orgName = matcher.group(1);
            String moduleName = matcher.group(2);
            String typeName = matcher.group(3);
            String pkgId = orgName + "/" + moduleName;
            PackageID currentPkgId = context.get(
                    DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
            if (pkgId.equals(currentPkgId.toString())) {
                // TODO: Check the validity of this check since currentPkgId.toString() returns version as well.
                foundType = typeName;
            } else {
                boolean pkgAlreadyImported = ((ModulePartNode) syntaxTree.rootNode()).imports().stream()
                        .anyMatch(importPkg -> {
                            ImportModel importModel = ImportModel.from(importPkg);
                            return importModel.orgName.equals(orgName)
                                    && importModel.moduleName.equals(moduleName);
                        });
                if (!pkgAlreadyImported) {
                    edits.addAll(CommonUtil.getAutoImportTextEdits(orgName, moduleName, context));
                }
                foundType = moduleName + CommonKeys.PKG_DELIMITER_KEYWORD + typeName;
            }
        }
        return foundType;
    }
}
