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
package org.ballerinalang.model.elements;

import org.ballerinalang.model.tree.IdentifierNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;

import static org.wso2.ballerinalang.compiler.util.Names.DEFAULT_VERSION;

/**
 * This represents a specific package and its version.
 *
 * @since 0.94
 */
public class PackageID {

    public static final PackageID EMPTY = new PackageID(Names.EMPTY, Names.EMPTY);

    private List<IdentifierNode> nameComps;

    private IdentifierNode versionNode;

    public Name name;
    public Name version;

    public PackageID(List<IdentifierNode> nameComps, IdentifierNode version) {
        this.nameComps = nameComps;
        this.versionNode = version;
        this.populateNameCompsAsString();
        this.version = version != null ? new Name(version.getValue()) : DEFAULT_VERSION;
    }

    public PackageID(Name name, Name version) {
        this.name = name;
        this.version = version;
    }

    public Name getPackageName() {
        return name;
    }

    public Name getPackageVersion() {
        return version;
    }

    public List<IdentifierNode> getNameComps() {
        return nameComps;
    }

    private void populateNameCompsAsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.nameComps.size(); i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(this.nameComps.get(i).getValue());
        }
        this.name = new Name(builder.toString());
    }

    public IdentifierNode getVersion() {
        return versionNode;
    }

    public void setNameComps(List<IdentifierNode> nameComps) {
        this.nameComps = nameComps;
        this.populateNameCompsAsString();
    }

    public void setVersion(IdentifierNode version) {
        this.versionNode = version;
    }

    public int getNameCompCount() {
        return this.nameComps.size();
    }

    public IdentifierNode getNameComponent(int index) {
        return this.nameComps.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageID packageID = (PackageID) o;
        return name.equals(packageID.name) && version.equals(packageID.version);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.getVersion().getValue() + "]";
    }
}
