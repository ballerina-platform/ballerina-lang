/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.connector;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class ConnectorNodeVisitor extends LSNodeVisitor {

    private String name;
    private Map<PackageID, BLangImportPackage> packageMap = new HashMap<>();
    private boolean found = false;
    private List<BLangTypeDefinition> connectors;
    private Map<String, BLangTypeDefinition> records;

    public ConnectorNodeVisitor(String name) {
        this.name = name;
        connectors = new ArrayList<>();
        records = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<BLangTypeDefinition> getConnectors() {
        return connectors;
    }

    public Map<String, BLangTypeDefinition> getRecords() {
        return records;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        pkgNode.getImports().forEach(importPackage -> {
            if (importPackage.symbol != null) {
                this.packageMap.put(importPackage.symbol.pkgID, importPackage);
            }
        });
        topLevelNodes.stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(topLevelNode -> ((BLangNode) topLevelNode).accept(this));
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        if (!found) {
            compUnit.getTopLevelNodes().forEach(n -> ((BLangNode) n).accept(this));
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.getTypeNode() instanceof BLangObjectTypeNode) {
            if (((BLangObjectTypeNode) typeDefinition.getTypeNode()).flagSet.contains(Flag.CLIENT)) {
                this.connectors.add(typeDefinition);
                typeDefinition.getTypeNode().accept(this);
            }

        } else if (typeDefinition.getTypeNode() instanceof BLangRecordTypeNode) {
            this.records.put(((BLangRecordTypeNode) typeDefinition.getTypeNode()).symbol.type.toString(),
                    typeDefinition);
        } else if (typeDefinition.getTypeNode() instanceof BLangUnionTypeNode) {
            this.records.put(((BLangUnionTypeNode) typeDefinition.getTypeNode()).type.toString(),
                    typeDefinition);
        }
    }

}
