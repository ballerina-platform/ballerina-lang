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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.NodeKind;

/**
 * @since 0.94
 */
public class BLangImportPackage extends BLangNode implements ImportPackageNode {

    public PackageID pkgId;
    public BLangIdentifier alias;

    @Override
    public PackageID getPackageID() {
        return pkgId;
    }

    @Override
    public void setPackageID(PackageID pkgId) {
        this.pkgId = pkgId;
    }

    @Override
    public BLangIdentifier getAlias() {
        return alias;
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
        return pkgId.equals(that.pkgId) && alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        int result = pkgId.hashCode();
        result = 31 * result + alias.hashCode();
        return result;
    }
}
