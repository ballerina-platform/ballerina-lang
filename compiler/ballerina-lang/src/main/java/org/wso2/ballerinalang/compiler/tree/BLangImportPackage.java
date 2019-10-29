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
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class BLangImportPackage extends BLangNode implements ImportPackageNode {

    public List<BLangIdentifier> pkgNameComps;
    public BLangIdentifier version;
    public BLangIdentifier alias;
    public BPackageSymbol symbol;
    public BLangIdentifier orgName;
    public BLangIdentifier compUnit;

    @Override
    public List<BLangIdentifier> getPackageName() {
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
    public BLangIdentifier getAlias() {
        return alias;
    }

    @Override
    public void setAlias(IdentifierNode alias) {
        this.alias = (BLangIdentifier) alias;
    }

    @Override
    public IdentifierNode getOrgName() {
        return orgName;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.IMPORT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BLangImportPackage that = (BLangImportPackage) o;
        if (pkgNameComps != null ? !pkgNameComps.equals(that.pkgNameComps) :
                that.pkgNameComps != null) {
            return false;
        }

        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        int result = pkgNameComps != null ? pkgNameComps.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "import " + getQualifiedPackageName();
    }
    
    public String getQualifiedPackageName() {
        String orgName = this.orgName.toString();
        String pkgName = String.join(".", pkgNameComps.stream()
                .map(id -> id.value)
                .collect(Collectors.toList()));

        String versionStr = (this.version.value != null) ? this.version.value : "";
        if (!versionStr.isEmpty()) {
            versionStr = " version " + versionStr;
        }

        String aliasStr = (this.alias.value != null && !this.alias.value.equals(pkgName)) ?
                " as " + this.alias.value : "";

        return (orgName.isEmpty() ? "" : orgName + '/') + pkgName + versionStr + aliasStr;
    }
}
