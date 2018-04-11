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

import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a specific package and its version.
 *
 * @since 0.94
 */
public class PackageID {

    public static final PackageID DEFAULT = new PackageID(Names.ANON_ORG, Names.DEFAULT_PACKAGE, Names.DEFAULT_VERSION);
    public final Name orgName;
    public Name name;
    public Name version = Names.DEFAULT_VERSION;

    public boolean isUnnamed = false;
    public Name sourceFileName = null;

    public List<Name> nameComps;

    public PackageID(Name orgName, List<Name> nameComps, Name version) {
        this.orgName = orgName;
        this.nameComps = nameComps;
        this.name = new Name(
                nameComps.stream()
                        .map(Name::getValue)
                        .collect(Collectors.joining(".")));
        this.version = version;
    }

    public PackageID(Name orgName, Name name, Name version) {
        this.orgName = orgName;
        this.name = name;
        this.version = version;
        if (name == Names.DEFAULT_PACKAGE) {
            this.nameComps = Lists.of(Names.DEFAULT_PACKAGE);
        } else {
            this.nameComps = Arrays.stream(name.value.split("\\."))
                    .map(Name::new).collect(Collectors.toList());
        }
    }

    /**
     * Creates a {@code PackageID} for an unnamed package.
     *
     * @param sourceFileName name of the .bal file
     */
    public PackageID(String sourceFileName) {
        this.orgName = Names.ANON_ORG;
//        this.name = new Name(Names.DOT + sourceFileName);
        this.name = Names.DEFAULT_PACKAGE;
        this.nameComps = new ArrayList<Name>(1) {{
            add(name);
        }};
        this.isUnnamed = true;
        this.sourceFileName = new Name(sourceFileName);
    }

    public Name getName() {
        return name;
    }

    public Name getNameComp(int index) {
        return nameComps.get(index);
    }

    public List<Name> getNameComps() {
        return nameComps;
    }

    public Name getPackageVersion() {
        return version;
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
        int result = nameComps.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String orgName = "";
        if (this.orgName != null && this.orgName != Names.ANON_ORG) {
            orgName = this.orgName + "/";
        }

        if (version == Names.DEFAULT_VERSION || version.equals(Names.EMPTY)) {
            return orgName + this.name.value;
        }

        return orgName + this.name + ":" + this.version;
    }

    public Name getOrgName() {
        return orgName;
    }

    public String bvmAlias() {
        // TODO: remove null check, it should never be null
        if (this.orgName != null && this.orgName == Names.ANON_ORG) {
            return this.name.toString();
        } else {
            return this.orgName + "." + this.getName();
        }
    }
}
