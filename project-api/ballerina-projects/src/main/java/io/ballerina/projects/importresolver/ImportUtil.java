/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.importresolver;

import io.ballerina.projects.Document;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Vector;

/**
 * The {@code SyntaxTree} util functions for import resolver.
 *
 * @since 2.0.0
 */
public class ImportUtil extends TreeModifier {

    private List<Import> getImportsFromSyntaxTree(SyntaxTree syntaxTree) {
        List<Import> imports = new ArrayList<>();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ImportDeclarationNode> importNodes = modifyNodeList(modulePartNode.imports());

        for (ImportDeclarationNode importDeclarationNode : importNodes) {
            String orgName = null;
            Optional<ImportOrgNameNode> importOrgNameNode = importDeclarationNode.orgName();
            if (importOrgNameNode.isPresent()) {
                orgName = importOrgNameNode.get().orgName().text();
            }
            String moduleName = importDeclarationNode.moduleName().toString();
            String version = importDeclarationNode.version().toString();
            imports.add(new Import(orgName, moduleName, version));
        }
        return imports;
    }

    // TODO: Made it public to avoid a spotbug failure. Please fix this
    public List<Import> getImportsFromModule(Iterable<Document> documents) {
        List<Import> imports = new ArrayList<>();
        for (Document document : documents) {
            imports.addAll(getImportsFromSyntaxTree(document.syntaxTree()));
        }
        return imports;
    }

    public static List<Import> sortDependencies(List<Import> dependencies) {
        int[] dependencyOrder = sort(dependencies);

        List<Import> sortedDeps = new ArrayList<>();
        for (int idx : dependencyOrder) {
            sortedDeps.add(dependencies.get(idx));
        }
        return sortedDeps;
    }

    private static int[] sort(List<Import> imports) {
        int numberOfNodes = imports.size();
        int[] indegrees = new int[numberOfNodes];
        int[] sortedElts = new int[numberOfNodes];

        List<Integer>[] dependencyMatrix = new ArrayList[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            dependencyMatrix[i] = new ArrayList<>();
        }

        int i = 0;
        for (Import anImport : imports) {
            if (anImport.getDependencies() != null && !anImport.getDependencies().isEmpty()) {
                for (Import dependency : anImport.getDependencies()) {
                    int idx = imports.indexOf(dependency);
                    if (idx == -1) {
                        String message = String.format("Module [%s] depends on module [%s], but it couldn't be found.",
                                anImport, dependency);
                        throw new RuntimeException(message);
                    }
                    dependencyMatrix[i].add(idx);
                }
            }
            i++;
        }

        // fill in degrees
        for (int j = 0; j < numberOfNodes; j++) {
            List<Integer> dependencies = dependencyMatrix[j];
            for (int node : dependencies) {
                indegrees[node]++;
            }
        }

        // Create a queue and enqueue all vertices with indegree 0
        Queue<Integer> q = new LinkedList<>();
        for (i = 0; i < numberOfNodes; i++) {
            if (indegrees[i] == 0) {
                q.add(i);
            }
        }

        // Initialize count of visited vertices
        int cnt = 0;

        // Create a vector to store result (A topological ordering of the vertices)
        Vector<Integer> topOrder = new Vector<>();
        while (!q.isEmpty()) {
            // Extract front of queue (or perform dequeue) and add it to topological order
            int u = q.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes of dequeued node u and decrease their in-degree by 1
            for (int node : dependencyMatrix[u]) {
                // If in-degree becomes zero, add it to queue
                if (--indegrees[node] == 0) {
                    q.add(node);
                }
            }
            cnt++;
        }

        // Check if there was a cycle
        if (cnt != numberOfNodes) {
            String message = "Cyclic module dependency detected";
            throw new RuntimeException(message);
        }

        i = numberOfNodes - 1;
        for (int elt : topOrder) {
            sortedElts[i] = elt;
            i--;
        }

        return sortedElts;
    }
}
