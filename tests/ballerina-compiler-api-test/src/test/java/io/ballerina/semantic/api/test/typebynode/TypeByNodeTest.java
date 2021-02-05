/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.typebynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.Test;

/**
 * Tests for getting the type of an expression by giving the relevant syntax tree node as an arg.
 *
 * @since 2.0.0
 */
public abstract class TypeByNodeTest {

    private int assertCount = 0;

    @Test
    public void testLookup() {
        Project project = BCompileUtil.loadProject(getTestSourcePath());
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();
        SemanticModel model = currentPackage.getCompilation().getSemanticModel(defaultModuleId);
        syntaxTree.rootNode().accept(getNodeVisitor(model));
        verifyAssertCount();
    }

    abstract String getTestSourcePath();

    abstract NodeVisitor getNodeVisitor(SemanticModel model);

    abstract void verifyAssertCount();

    void incrementAssertCount() {
        this.assertCount++;
    }

    int getAssertCount() {
        return this.assertCount;
    }
}
