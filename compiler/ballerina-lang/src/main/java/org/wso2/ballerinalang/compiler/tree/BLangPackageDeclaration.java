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

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageDeclarationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class BLangPackageDeclaration extends BLangNode implements PackageDeclarationNode {

    public List<BLangIdentifier> pkgNameComps;
    public BLangIdentifier version;
    public BLangIdentifier orgName;

    @Override
    public List<? extends IdentifierNode> getPackageName() {
        return pkgNameComps;
    }

    @Override
    public void setPackageName(List<? extends IdentifierNode> nameParts) {
        this.pkgNameComps = new ArrayList<>();
        this.pkgNameComps.add((BLangIdentifier) nameParts);
    }

    @Override
    public IdentifierNode getPackageVersion() {
        return version;
    }

    @Override
    public void setPackageVersion(IdentifierNode version) {
        this.version = (BLangIdentifier) version;
    }

    @Override
    public String toString() {
        String versionStr = (this.version.value != null) ? this.version.value : "";
        if (!versionStr.isEmpty()) {
            versionStr = " version " + versionStr;
        }

        return "package " + getPackageNameStr() + versionStr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.PACKAGE_DECLARATION;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getPackageNameStr() {
        return String.join(".", pkgNameComps.stream().map(id -> id.value).collect(Collectors.toList()));
    }
}
