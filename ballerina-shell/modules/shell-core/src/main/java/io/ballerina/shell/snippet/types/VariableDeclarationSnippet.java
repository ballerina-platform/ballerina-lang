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

import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
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
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.shell.snippet.SnippetSubKind;
import io.ballerina.shell.utils.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * These will be variable declarations.
 * Currently only module level variable declarations are accepted.
 *
 * @since 2.0.0
 */
public class VariableDeclarationSnippet extends AbstractSnippet<ModuleVariableDeclarationNode>
        implements ExecutableSnippet, DeclarationSnippet {
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
    public Set<String> names() {
        Set<String> foundVariableIdentifiers = new HashSet<>();
        rootNode.accept(new VariableNameFinder(foundVariableIdentifiers));
        return foundVariableIdentifiers;
    }

    /**
     * Imports required for the type.
     * This will return prefixes used in the type for each variable name.
     * This will only return proper values if
     */
    public Map<String, Set<String>> importsForType() {
        if (rootNode.typedBindingPattern().bindingPattern() instanceof CaptureBindingPatternNode) {
            String variableName = ((CaptureBindingPatternNode) rootNode.typedBindingPattern()
                    .bindingPattern()).variableName().text();
            Set<String> importPrefixes = new HashSet<>();
            rootNode.accept(new ImportPrefixFinder(importPrefixes));
            return Map.of(variableName, importPrefixes);
        }
        return Map.of();
    }

    /**
     * Variable types for each variable name.
     * If any of the types cannot be determined this will return {@code Optional.empty()}.
     * Currently, only `A a = INIT` type are processed.
     * Name will be quoted.
     */
    public Optional<Map<String, TypeInfo>> types() {
        if (rootNode.typedBindingPattern().bindingPattern() instanceof CaptureBindingPatternNode) {
            TypeDescriptorNode typeDescriptorNode = rootNode.typedBindingPattern().typeDescriptor();
            String variableName = ((CaptureBindingPatternNode) rootNode.typedBindingPattern()
                    .bindingPattern()).variableName().text();

            // VAR cannot be determined.
            if (typeDescriptorNode.kind().equals(SyntaxKind.VAR_TYPE_DESC)) {
                return Optional.empty();
            }
            // ARR[*] cannot be determined.
            if (typeDescriptorNode instanceof ArrayTypeDescriptorNode) {
                ArrayTypeDescriptorNode arrTypeNode = (ArrayTypeDescriptorNode) typeDescriptorNode;
                if (arrTypeNode.memberTypeDesc().kind().equals(SyntaxKind.ASTERISK_LITERAL)) {
                    return Optional.empty();
                }
            }

            // Find the import prefixes required
            Set<String> importPrefixes = new HashSet<>();
            rootNode.accept(new ImportPrefixFinder(importPrefixes));

            String quotedVariableName = StringUtils.quoted(variableName);
            String variableType = typeDescriptorNode.toSourceCode();
            TypeInfo typeInfo = new TypeInfo(variableType, importPrefixes);
            return Optional.of(Map.of(quotedVariableName, typeInfo));
        }
        return Optional.empty();
    }

    /**
     * A helper class to find the var dclns declared in a snippet.
     *
     * @since 2.0.0
     */
    private static class VariableNameFinder extends NodeVisitor {
        private final Set<String> foundVariableIdentifiers;

        public VariableNameFinder(Set<String> foundVariableIdentifiers) {
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
            String unescapedIdentifier = IdentifierUtils.unescapeUnicodeCodepoints(token.text());
            foundVariableIdentifiers.add(unescapedIdentifier);
        }
    }

    /**
     * A helper class to find the import prefixes in a snippet.
     *
     * @since 2.0.0
     */
    private static class ImportPrefixFinder extends NodeVisitor {
        private final Set<String> foundImportPrefixes;

        public ImportPrefixFinder(Set<String> foundImportPrefixes) {
            this.foundImportPrefixes = foundImportPrefixes;
        }

        @Override
        public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            addPrefix(qualifiedNameReferenceNode.identifier());
        }

        private void addPrefix(Token token) {
            String unescapedIdentifier = IdentifierUtils.unescapeUnicodeCodepoints(token.text());
            foundImportPrefixes.add(unescapedIdentifier);
        }
    }

    /**
     * A variable type with import information.
     *
     * @since 2.0.0
     */
    public static class TypeInfo {
        private final String type;
        private final Set<String> imports;

        public TypeInfo(String type, Set<String> imports) {
            this.type = type;
            this.imports = imports;
        }

        public Set<String> getImports() {
            return imports;
        }

        public String getType() {
            return type;
        }
    }
}
