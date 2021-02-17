/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.snippet.types;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.shell.snippet.SnippetSubKind;
import io.ballerina.shell.utils.QuotedIdentifier;
import io.ballerina.shell.utils.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * These will be variable declarations.
 * Currently only module level variable declarations are accepted.
 *
 * @since 2.0.0
 */
public class VariableDeclarationSnippet extends AbstractSnippet<ModuleVariableDeclarationNode>
        implements TopLevelDeclarationSnippet {
    public VariableDeclarationSnippet(ModuleVariableDeclarationNode rootNode) {
        super(SnippetSubKind.VARIABLE_DECLARATION, rootNode);
    }

    /**
     * This will ONLY return the metadata and qualifiers of this node.
     * Whitespaces will be added between all qualifiers and metadata.
     *
     * @return Qualifiers/Metadata as string.
     */
    public String qualifiersAndMetadata() {
        String metadata = rootNode.metadata().map(Node::toSourceCode).orElse("");
        String qualifiers = rootNode.qualifiers().stream()
                .map(Node::toSourceCode).collect(Collectors.joining(" "));
        return metadata + qualifiers;
    }

    /**
     * Variable names that are defined in this snippet.
     */
    public Set<QuotedIdentifier> names() {
        Set<QuotedIdentifier> foundVariableIdentifiers = new HashSet<>();
        rootNode.accept(new VariableNameFinder(foundVariableIdentifiers));
        return foundVariableIdentifiers;
    }

    /**
     * Variable types for each variable name.
     * If any of the types cannot be determined this will return {@code Optional.empty()}.
     * Currently, only `A a = INIT` type are processed.
     * Name will be quoted.
     * <p>
     * TODO: This method is only required because some inferred signatures are not syntactically correct.
     * This can be removed after the signatures are fixed. (#28695, #28693, #28434)
     */
    public Map<QuotedIdentifier, TypeInfo> types() {
        // VAR and ARR[*] cannot be determined.
        TypeDescriptorNode typeDescriptorNode = rootNode.typedBindingPattern().typeDescriptor();
        TypeDeterminableFinder determinableFinder = new TypeDeterminableFinder();
        typeDescriptorNode.accept(determinableFinder);
        if (!determinableFinder.isDeterminable()) {
            return Map.of();
        }

        // Currently only supports CaptureBindingPatternNode
        if (rootNode.typedBindingPattern().bindingPattern() instanceof CaptureBindingPatternNode) {
            // Find the import prefixes required
            Set<QuotedIdentifier> importPrefixes = new HashSet<>();
            typeDescriptorNode.accept(new ImportPrefixFinder(importPrefixes));

            // Get the quoted variable name and type
            CaptureBindingPatternNode bindingPattern = (CaptureBindingPatternNode) rootNode.typedBindingPattern()
                    .bindingPattern();
            String variableName = StringUtils.unescapeUnicodeCodepoints(bindingPattern.variableName().text());
            QuotedIdentifier quotedVariableName = new QuotedIdentifier(variableName);
            String variableType = typeDescriptorNode.toSourceCode().trim();

            // Return the map
            TypeInfo typeInfo = new TypeInfo(variableType, importPrefixes);
            return Map.of(quotedVariableName, typeInfo);
        }
        return Map.of();
    }

    /**
     * A helper class to find import prefixes in a type.
     *
     * @since 2.0.0
     */
    private static class ImportPrefixFinder extends NodeVisitor {
        private final Set<QuotedIdentifier> importPrefixes;

        public ImportPrefixFinder(Set<QuotedIdentifier> importPrefixes) {
            this.importPrefixes = importPrefixes;
        }

        @Override
        public void visit(QualifiedNameReferenceNode node) {
            importPrefixes.add(new QuotedIdentifier(node.modulePrefix().text()));
        }
    }

    /**
     * A helper class to find the var dclns declared in a snippet.
     *
     * @since 2.0.0
     */
    private static class VariableNameFinder extends NodeVisitor {
        private final Set<QuotedIdentifier> foundVariableIdentifiers;

        public VariableNameFinder(Set<QuotedIdentifier> foundVariableIdentifiers) {
            this.foundVariableIdentifiers = foundVariableIdentifiers;
        }

        @Override
        public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
            addIdentifier(captureBindingPatternNode.variableName());
        }

        @Override
        public void visit(RestBindingPatternNode restBindingPatternNode) {
            addIdentifier(restBindingPatternNode.variableName().name());
        }

        @Override
        public void visit(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
            addIdentifier(fieldBindingPatternVarnameNode.variableName().name());
        }

        private void addIdentifier(Token token) {
            String unescapedIdentifier = StringUtils.unescapeUnicodeCodepoints(token.text());
            foundVariableIdentifiers.add(new QuotedIdentifier(unescapedIdentifier));
        }
    }

    /**
     * A helper class to determine if type can be determined
     * from just the syntax tree.
     * If the syntax tree contains VAR or ARR[*], it cannot be determined.
     *
     * @since 2.0.0
     */
    private static class TypeDeterminableFinder extends NodeVisitor {
        private boolean determinable;

        public TypeDeterminableFinder() {
            this.determinable = true;
        }

        @Override
        public void visit(BasicLiteralNode basicLiteralNode) {
            if (basicLiteralNode.kind().equals(SyntaxKind.ASTERISK_LITERAL)) {
                determinable = false;
            }
        }

        @Override
        public void visit(Token token) {
            if (token.kind().equals(SyntaxKind.VAR_KEYWORD)) {
                determinable = false;
            }
        }

        public boolean isDeterminable() {
            return determinable;
        }
    }

    /**
     * A variable type with import information.
     *
     * @since 2.0.0
     */
    public static class TypeInfo {
        private final String type;
        private final Set<QuotedIdentifier> imports;

        public TypeInfo(String type, Set<QuotedIdentifier> imports) {
            this.type = type;
            this.imports = imports;
        }

        public Set<QuotedIdentifier> getImports() {
            return imports;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("{type='%s', imports=%s}", type, imports);
        }
    }
}
