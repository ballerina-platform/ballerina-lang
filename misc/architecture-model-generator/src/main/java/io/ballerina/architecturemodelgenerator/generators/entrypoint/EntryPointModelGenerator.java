/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.entrypoint;

import io.ballerina.architecturemodelgenerator.generators.ModelGenerator;
import io.ballerina.architecturemodelgenerator.generators.entrypoint.nodevisitors.FunctionNodeVisitor;
import io.ballerina.architecturemodelgenerator.model.EntryPoint;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;

import java.nio.file.Path;

/**
 * Build entry point model based on a given Ballerina package.
 *
 * @since 2201.4.0
 */
public class EntryPointModelGenerator extends ModelGenerator {

    public EntryPointModelGenerator(PackageCompilation packageCompilation, Module module) {
        super(packageCompilation, module);
    }

    public EntryPoint generate() {
        EntryPoint entryPoint = null;
        for (DocumentId documentId :getModule().documentIds()) {
            SyntaxTree syntaxTree = getModule().document(documentId).syntaxTree();
            Path filePath = getModuleRootPath().resolve(syntaxTree.filePath());
            FunctionNodeVisitor functionNodeVisitor = new FunctionNodeVisitor(
                    getPackageCompilation(), getSemanticModel(), getModule().packageInstance(), filePath);
            syntaxTree.rootNode().accept(functionNodeVisitor);
            EntryPoint entryPointVisited = functionNodeVisitor.getEntryPoint();
            if (entryPointVisited != null) {
                entryPoint = entryPointVisited;
            }
        }
        return entryPoint;
    }
}
