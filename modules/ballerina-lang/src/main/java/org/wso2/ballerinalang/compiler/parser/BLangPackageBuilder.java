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
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;

import java.util.Stack;

/**
 * This class builds the package AST of a Ballerina source file.
 */
public class BLangPackageBuilder {

    //private PackageNode pkgNode;
    
    private Stack<TypeNode> typeNodeStack = new Stack<>();
    
    //private Stack<BlockNode> blockNodeStack = new Stack<>();
    
    //private InvokableNode currentInvokableNode;
        
    public BLangPackageBuilder(PackageNode pkgNode) {
        //this.pkgNode = pkgNode;
    }
    
    public void addValueType(String valueType) {
        ValueTypeNode valueTypeNode = TreeBuilder.createValueTypeNode();
        valueTypeNode.setTypeKind(TreeUtils.stringToTypeKind(valueType));
        this.typeNodeStack.push(valueTypeNode);
    }
    
    public void startFunctionDef() {
        //this.currentInvokableNode = TreeBuilder.createFunctionNode();
    }
    
}
