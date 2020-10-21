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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code Action related Utils.
 *
 * @since 1.0.1
 */
public class CodeActionUtil {

    private CodeActionUtil() {
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param identifier Document Identifier
     * @param cursorLine Cursor line
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType topLevelNodeInLine(LSContext context, TextDocumentIdentifier identifier,
                                                        int cursorLine, WorkspaceDocumentManager docManager)
            throws CompilationFailedException {
        Optional<Path> filePath = CommonUtil.getPathFromURI(identifier.getUri());
        if (filePath.isEmpty()) {
            return null;
        }

        BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
        List<Diagnostic> diagnostics = bLangPackage.getDiagnostics();
        context.put(CodeActionKeys.DIAGNOSTICS_KEY, CodeActionUtil.toDiagnostics(diagnostics));

        ModulePartNode modulePartNode;
        try {
            modulePartNode = docManager.getTree(filePath.get()).rootNode();
        } catch (WorkspaceDocumentException e) {
            return null;
        }

        List<ModuleMemberDeclarationNode> members = modulePartNode.members().stream().collect(Collectors.toList());
        for (ModuleMemberDeclarationNode member : members) {
            boolean isSameLine = member.lineRange().startLine().line() == cursorLine;
            boolean isWithinLines = cursorLine > member.lineRange().startLine().line() &&
                    cursorLine < member.lineRange().endLine().line();
            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                if (isSameLine) {
                    // Cursor on the service
                    return CodeActionNodeType.SERVICE;
                } else if (isWithinLines) {
                    // Cursor within the service
                    ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) member;
                    for (Node resourceNode : ((ServiceBodyNode) serviceDeclrNode.serviceBody()).resources()) {
                        boolean isSameResLine = resourceNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && resourceNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                            // Cursor on the resource function
                            return CodeActionNodeType.RESOURCE;
                        }
                    }
                }
            } else if (isSameLine && member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                return CodeActionNodeType.FUNCTION;
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                if (isSameLine) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                        return CodeActionNodeType.RECORD;
                    } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return CodeActionNodeType.OBJECT;
                    }
                } else if (isWithinLines && typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                    for (Node memberNode : objectTypeDescNode.members()) {
                        boolean isSameResLine = memberNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && memberNode.kind() == SyntaxKind.METHOD_DECLARATION) {
                            // Cursor on the object function
                            return CodeActionNodeType.OBJECT_FUNCTION;
                        }
                    }
                }
            } else if (member.kind() == SyntaxKind.CLASS_DEFINITION) {
                if (isSameLine) {
                    // Cursor on the class
                    return CodeActionNodeType.CLASS;
                } else if (isWithinLines) {
                    // Cursor within the class
                    ClassDefinitionNode classDefNode = (ClassDefinitionNode) member;
                    for (Node memberNode : classDefNode.members()) {
                        boolean isSameResLine = memberNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && memberNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                            // Cursor on the class function
                            return CodeActionNodeType.CLASS_FUNCTION;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Translates ballerina diagnostics into lsp4j diagnostics.
     *
     * @param ballerinaDiags a list of {@link org.ballerinalang.util.diagnostic.Diagnostic}
     * @return a list of {@link Diagnostic}
     */
    public static List<org.eclipse.lsp4j.Diagnostic> toDiagnostics(List<Diagnostic> ballerinaDiags) {
        List<org.eclipse.lsp4j.Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaDiags.forEach(diagnostic -> {
            org.eclipse.lsp4j.Diagnostic lsDiagnostic = new org.eclipse.lsp4j.Diagnostic();
            lsDiagnostic.setSeverity(DiagnosticSeverity.Error);
            lsDiagnostic.setMessage(diagnostic.message());
            Range range = new Range();

            Location location = diagnostic.location();
            LineRange lineRange = location.lineRange();
            int startLine = lineRange.startLine().line(); // LSP diagnostics range is 0 based
            int startChar = lineRange.startLine().offset();
            int endLine = lineRange.endLine().line();
            int endChar = lineRange.endLine().offset();

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            range.setStart(new Position(startLine, startChar));
            range.setEnd(new Position(endLine, endChar));
            lsDiagnostic.setRange(range);

            lsDiagnostics.add(lsDiagnostic);
        });

        return lsDiagnostics;
    }
}
