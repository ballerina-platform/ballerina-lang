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

package io.ballerina.shell.snippet;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Snippets are individual statements.
 * <p>
 * Every snippet must have a kind (which dictates where the snippet should go) and a
 * sub kind (depicting the statement type) Each snippet must refer to a single statement.
 * That means if the same input line contained several statements, it would be parsed
 * into several snippets. (This separation is done in preprocessor.)
 * <p>
 * In processing the snippets, if a snippet contained an error and failed to run,
 * the execution of the snippet would be stopped. If the snippet was contained in a
 * line with more snippets, (if the input contained multiple snippets)
 * all the snippets would be ditched.
 * This also means that an error snippet is taken as if it were never given.
 * <p>
 * Also, names given to the REPL may never be overridden.
 * (If {@code x} variable is defined, you cannot redefine variable {@code x} even
 * with the same type. Same goes for functions, classes etc..)
 * However, any valid redeclaration in a different scope may be possible.
 *
 * @since 2.0.0
 */
public abstract class Snippet {
    protected final SnippetSubKind subKind;
    protected Node rootNode;

    protected Snippet(SnippetSubKind subKind, Node rootNode) {
        this.subKind = subKind;
        this.rootNode = rootNode;
    }

    /**
     * Finds all the used imports in this snippet.
     *
     * @return Set of all the used import module prefixes.
     */
    public Set<String> usedImports() {
        Set<String> imports = new HashSet<>();
        rootNode.accept(new ImportNameFinder(imports));
        return imports;
    }

    /**
     * Kind is the category of the snippet.
     * This determines the position where the snippet will go.
     *
     * @return Snippet kind value.
     */
    public SnippetKind getKind() {
        return subKind.getKind();
    }

    /**
     * Modifies the tree of the snippet.
     *
     * @param treeModifier Modifier to use.
     */
    public void modify(TreeModifier treeModifier) {
        this.rootNode = this.rootNode.apply(treeModifier);
    }

    public boolean isImport() {
        return this.getKind() == SnippetKind.IMPORT_DECLARATION;
    }

    public boolean isModuleMemberDeclaration() {
        return this.getKind() == SnippetKind.MODULE_MEMBER_DECLARATION;
    }

    public boolean isStatement() {
        return this.getKind() == SnippetKind.STATEMENT;
    }

    public boolean isExpression() {
        return this.getKind() == SnippetKind.EXPRESSION;
    }

    public boolean isVariableDeclaration() {
        return this.getKind() == SnippetKind.VARIABLE_DECLARATION;
    }

    @Override
    public String toString() {
        return rootNode.toSourceCode();
    }

    /**
     * A helper class to find the imports used in a snippet.
     *
     * @since 2.0.0
     */
    protected static class ImportNameFinder extends NodeVisitor {
        private final Set<String> imports;

        public ImportNameFinder(Set<String> imports) {
            this.imports = imports;
        }

        @Override
        public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            super.visit(qualifiedNameReferenceNode);
            imports.add(qualifiedNameReferenceNode.modulePrefix().text());
        }
    }
}
