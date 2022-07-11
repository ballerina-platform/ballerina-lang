/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.snippet.types;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.SnippetKind;
import io.ballerina.shell.snippet.SnippetSubKind;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link io.ballerina.shell.snippet.Snippet}.
 *
 * @param <T> Type of the child root node
 * @since 2.0.0
 */
public class AbstractSnippet<T extends Node> implements Snippet {
    protected final SnippetSubKind subKind;
    protected final T rootNode;

    protected AbstractSnippet(SnippetSubKind subKind, T rootNode) {
        this.subKind = subKind;
        this.rootNode = rootNode;
    }

    /**
     * Finds all the used imports in this snippet.
     *
     * @return Set of all the used import module prefixes.
     */
    @Override
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
    @Override
    public SnippetKind getKind() {
        return subKind.getKind();
    }

    @Override
    public String toString() {
        return rootNode.toSourceCode();
    }

    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractSnippet<?> that = (AbstractSnippet<?>) o;
        return subKind == that.subKind &&
                rootNode.equals(that.rootNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subKind, rootNode);
    }

    /**
     * A helper class to find the imports used in a snippet.
     *
     * @since 2.0.0
     */
    private static class ImportNameFinder extends NodeVisitor {
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
