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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
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
     * @param position Cursor position
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType topLevelNodeInLine(LSContext context, TextDocumentIdentifier identifier,
                                                        Position position, WorkspaceDocumentManager docManager)
            throws CompilationFailedException {
        Optional<Path> filePath = CommonUtil.getPathFromURI(identifier.getUri());
        if (filePath.isEmpty()) {
            return null;
        }

        BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
        List<Diagnostic> diagnostics = bLangPackage.getDiagnostics();
        context.put(CodeActionKeys.DIAGNOSTICS_KEY, CodeActionUtil.toDiagnostics(diagnostics));

        ModulePartNode modulePartNode;
        int cursorPositionOffset;
        try {
            SyntaxTree syntaxTree = docManager.getTree(filePath.get());
            modulePartNode = syntaxTree.rootNode();
            cursorPositionOffset = syntaxTree.textDocument().textPositionFrom(LinePosition.from(position.getLine(),
                    position.getCharacter()));
        } catch (WorkspaceDocumentException e) {
            return null;
        }

        List<ModuleMemberDeclarationNode> members = modulePartNode.members().stream().collect(Collectors.toList());
        for (ModuleMemberDeclarationNode member : members) {
            boolean isWithinStartSegment = isWithinStartCodeSegment(member, cursorPositionOffset);
            boolean isWithinBody = isWithinBody(member, cursorPositionOffset);
            if (!isWithinStartSegment && !isWithinBody) {
                continue;
            }

            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                if (isWithinStartSegment) {
                    // Cursor on the service
                    return CodeActionNodeType.SERVICE;
                } else {
                    // Cursor within the service
                    ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) member;
                    for (Node resourceNode : ((ServiceBodyNode) serviceDeclrNode.serviceBody()).resources()) {
                        if (resourceNode.kind() == SyntaxKind.FUNCTION_DEFINITION
                                && isWithinStartCodeSegment(resourceNode, cursorPositionOffset)) {
                            // Cursor on the resource function
                            return CodeActionNodeType.RESOURCE;
                        }
                    }
                }
            } else if (isWithinStartSegment && member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                return CodeActionNodeType.FUNCTION;
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                if (isWithinStartSegment) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                        return CodeActionNodeType.RECORD;
                    } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return CodeActionNodeType.OBJECT;
                    }
                } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                    for (Node memberNode : objectTypeDescNode.members()) {
                        if (memberNode.kind() == SyntaxKind.METHOD_DECLARATION
                                && isWithinStartCodeSegment(memberNode, cursorPositionOffset)) {
                            // Cursor on the object function
                            return CodeActionNodeType.OBJECT_FUNCTION;
                        }
                    }
                }
            } else if (member.kind() == SyntaxKind.CLASS_DEFINITION) {
                if (isWithinStartSegment) {
                    // Cursor on the class
                    return CodeActionNodeType.CLASS;
                } else {
                    // Cursor within the class
                    ClassDefinitionNode classDefNode = (ClassDefinitionNode) member;
                    for (Node memberNode : classDefNode.members()) {
                        if (memberNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION
                                && isWithinStartCodeSegment(memberNode, cursorPositionOffset)) {
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
     * @param ballerinaDiags a list of {@link Diagnostic}
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

    /**
     * Returns if given position's offset is within the code body of give node.
     *
     * @param node Node in which the code body is considered
     * @param positionOffset Offset of the position
     * @return {@link Boolean} If within the body or not
     */
    private static boolean isWithinBody(Node node, int positionOffset) {
        if (!(node instanceof NonTerminalNode)) {
            return false;
        }

        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION:
                TextRange functionBodyTextRange = ((FunctionDefinitionNode) node).functionBody().textRange();
                return isWithinRange(positionOffset, functionBodyTextRange.startOffset(),
                        functionBodyTextRange.endOffset());
            case SERVICE_DECLARATION:
                TextRange serviceBodyTextRange = ((ServiceDeclarationNode) node).serviceBody().textRange();
                return isWithinRange(positionOffset, serviceBodyTextRange.startOffset(),
                        serviceBodyTextRange.endOffset());
            case CLASS_DEFINITION:
                ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) node;
                return isWithinRange(positionOffset, classDefinitionNode.openBrace().textRange().startOffset(),
                        classDefinitionNode.closeBrace().textRange().endOffset());
            case TYPE_DEFINITION:
                TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) node;
                return isWithinRange(positionOffset,
                        typeDefinitionNode.typeDescriptor().textRange().startOffset(),
                        typeDefinitionNode.semicolonToken().textRange().startOffset());
            default:
                return false;
        }
    }

    /**
     * Returns if given position's offset is within the starting code segment of give node.
     *
     * @param node Node in which the code start segment is considered
     * @param positionOffset Offset of the position
     * @return {@link Boolean} If within the start segment or not
     */
    private static boolean isWithinStartCodeSegment(Node node, int positionOffset) {
        if (!(node instanceof NonTerminalNode)) {
            return false;
        }

        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION:
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                Optional<MetadataNode> functionMetadata = functionDefinitionNode.metadata();
                int functionStartOffset = functionMetadata.map(metadataNode
                        -> metadataNode.textRange().endOffset()).orElseGet(()
                        -> functionDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, functionStartOffset,
                        functionDefinitionNode.functionBody().textRange().startOffset());
            case SERVICE_DECLARATION:
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                Optional<MetadataNode> serviceMetadata = serviceDeclarationNode.metadata();
                int serviceStartOffset = serviceMetadata.map(metadataNode
                        -> metadataNode.textRange().endOffset()).orElseGet(()
                        -> serviceDeclarationNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, serviceStartOffset,
                        serviceDeclarationNode.serviceBody().textRange().startOffset());
            case METHOD_DECLARATION:
                MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) node;
                Optional<MetadataNode> methodMetadata = methodDeclarationNode.metadata();
                int methodStartOffset = methodMetadata.map(metadataNode
                        -> metadataNode.textRange().endOffset()).orElseGet(()
                        -> methodDeclarationNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, methodStartOffset,
                        methodDeclarationNode.semicolon().textRange().endOffset());
            case CLASS_DEFINITION:
                ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) node;
                Optional<MetadataNode> classMetadata = classDefinitionNode.metadata();
                int classStartOffset = classMetadata.map(metadataNode
                        -> metadataNode.textRange().endOffset()).orElseGet(()
                        -> classDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, classStartOffset,
                        classDefinitionNode.openBrace().textRange().endOffset());
            case TYPE_DEFINITION:
                TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) node;
                Optional<MetadataNode> typeMetadata = typeDefinitionNode.metadata();
                int typeStartOffset = typeMetadata.map(metadataNode
                        -> metadataNode.textRange().endOffset()).orElseGet(()
                        -> typeDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, typeStartOffset,
                        typeDefinitionNode.typeDescriptor().textRange().startOffset());
            default:
                return false;
        }
    }

    /**
     * Returns if given position's offset is within the given range.
     *
     * @param positionOffset Offset of the position
     * @param startOffSet Offset of start
     * @param endOffset Offset of end
     * @return {@link Boolean} If within the range or not
     */
    private static boolean isWithinRange(int positionOffset, int startOffSet, int endOffset) {
        return positionOffset > startOffSet && positionOffset < endOffset;
    }
}
