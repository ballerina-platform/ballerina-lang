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

import io.ballerina.build.Document;
import io.ballerina.build.Module;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TreeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<Import> getImportsFromModule(Module module) {
        List<Import> imports = new ArrayList<>();
        Iterable<Document> documents = module.documents();

        for (Document document : documents) {
            imports.addAll(getImportsFromSyntaxTree(document.syntaxTree()));
        }
        return imports;
    }
}
