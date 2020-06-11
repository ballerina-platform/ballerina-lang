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

import static org.wso2.ballerinalang.compiler.util.Names.ANNOTATIONS_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.ARRAY_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.BOOLEAN_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.DECIMAL_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.DEFAULT_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.ERROR_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.FLOAT_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.FUTURE_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.INTERNAL_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.INT_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.MAP_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.OBJECT_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.QUERY_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.STREAM_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.STRING_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.TABLE_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.TYPEDESC_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.VALUE_VERSION;
import static org.wso2.ballerinalang.compiler.util.Names.XML_VERSION;

/**
 * This represents a specific package and its version.
 *
 * @since 0.94
 */
public class PackageID {

    public static final PackageID DEFAULT = new PackageID(Names.ANON_ORG, Names.DEFAULT_PACKAGE, DEFAULT_VERSION);

    // Lang.* Modules IDs

    // lang.__internal module is visible only to the compiler and peer lang.* modules.
    public static final PackageID INTERNAL = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.INTERNAL), INTERNAL_VERSION);

    // Visible Lang modules.
    public static final PackageID ANNOTATIONS = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.ANNOTATIONS), ANNOTATIONS_VERSION);
    public static final PackageID ARRAY = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.ARRAY), ARRAY_VERSION);
    public static final PackageID DECIMAL = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.DECIMAL), DECIMAL_VERSION);
    public static final PackageID ERROR = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.ERROR), ERROR_VERSION);
    public static final PackageID FLOAT = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.FLOAT), FLOAT_VERSION);
    public static final PackageID FUTURE = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.FUTURE), FUTURE_VERSION);
    public static final PackageID INT = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.INT), INT_VERSION);
    public static final PackageID MAP = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.MAP), MAP_VERSION);
    public static final PackageID OBJECT = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.OBJECT), OBJECT_VERSION);
    public static final PackageID STREAM = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.STREAM), STREAM_VERSION);
    public static final PackageID STRING = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.STRING), STRING_VERSION);
    public static final PackageID TABLE = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.TABLE), TABLE_VERSION);
    public static final PackageID TYPEDESC = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.TYPEDESC), TYPEDESC_VERSION);
    public static final PackageID VALUE = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.VALUE), VALUE_VERSION);
    public static final PackageID XML = new PackageID(Names.BALLERINA_ORG,
                                                      Lists.of(Names.LANG, Names.XML), XML_VERSION);
    public static final PackageID BOOLEAN = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.BOOLEAN), BOOLEAN_VERSION);
    public static final PackageID QUERY = new PackageID(Names.BALLERINA_ORG,
            Lists.of(Names.LANG, Names.QUERY), QUERY_VERSION);

    public final Name orgName;
    public Name name;
    public Name version = DEFAULT_VERSION;

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
     * @param orgName        organization name
     * @param sourceFileName name of the .bal file
     * @param version        version
     */
    public PackageID(Name orgName, String sourceFileName, Name version) {
        this.orgName = orgName;
        this.name = Names.DEFAULT_PACKAGE;
        this.version = version;
        this.nameComps = Lists.of(Names.DEFAULT_PACKAGE);
        this.isUnnamed = true;
        this.sourceFileName = new Name(sourceFileName);
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

        PackageID other = (PackageID) o;
        boolean samePkg = false;

        if (this.isUnnamed == other.isUnnamed) {
            samePkg = (!this.isUnnamed) || (this.sourceFileName.equals(other.sourceFileName));
        }

        return samePkg && orgName.equals(other.orgName) && name.equals(other.name) && version.equals(other.version);
    }

    @Override
    public int hashCode() {
        int result = orgName != null ? orgName.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (Names.DOT.equals(this.name)) {
            return this.name.value;
        }

        String orgName = "";
        if (this.orgName != null && !this.orgName.equals(Names.ANON_ORG)) {
            orgName = this.orgName + Names.ORG_NAME_SEPARATOR.value;
        }

        if (version.equals(Names.EMPTY)) {
            return orgName + this.name.value;
        }

        return orgName + this.name + Names.VERSION_SEPARATOR.value + this.version;
    }

    public Name getOrgName() {
        return orgName;
    }

    public static boolean isLangLibPackageID(PackageID packageID) {

        if (!packageID.getOrgName().equals(Names.BALLERINA_ORG)) {
            return false;
        }
        return packageID.nameComps.size() > 1 && packageID.nameComps.get(0).equals(Names.LANG);
    }
}
