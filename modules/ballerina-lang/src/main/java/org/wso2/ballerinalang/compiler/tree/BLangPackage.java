/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Identifier;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.ConstantNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.GlobalVariableNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.NamespaceDeclarationNode;
import org.ballerinalang.model.tree.PackageLevelNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.StructNode;

import java.util.List;

/**
 * @since 0.94
 */
public class BLangPackage extends BLangNode implements PackageNode {
    @Override
    public List<? extends Identifier> getNameComponents() {
        return null;
    }

    @Override
    public Identifier getVersion() {
        return null;
    }

    @Override
    public List<? extends PackageLevelNode> getTopLevelConstructs() {
        return null;
    }

    @Override
    public List<? extends ImportPackageNode> getImports() {
        return null;
    }

    @Override
    public List<? extends NamespaceDeclarationNode> getNamespaceDeclarations() {
        return null;
    }

    @Override
    public List<? extends ConstantNode> getConstantDefinitions() {
        return null;
    }

    @Override
    public List<? extends GlobalVariableNode> getGlobalVariableDefinitions() {
        return null;
    }

    @Override
    public List<BLangService> getServices() {
        return null;
    }

    @Override
    public List<? extends ConnectorNode> getConnectors() {
        return null;
    }

    @Override
    public List<? extends FunctionNode> getFunctions() {
        return null;
    }

    @Override
    public List<? extends StructNode> getStructs() {
        return null;
    }

    @Override
    public List<? extends AnnotationNode> getAnnotations() {
        return null;
    }
}
