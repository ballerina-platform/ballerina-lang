/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import java.util.Objects;

import static org.ballerinalang.jvm.util.BLangConstants.ANON_ORG;
import static org.ballerinalang.jvm.util.BLangConstants.DOT;
import static org.ballerinalang.jvm.util.BLangConstants.EMPTY;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.jvm.util.BLangConstants.VERSION_SEPARATOR;

/**
 * {@code BPackage} represents the package of defined type in Ballerina.
 *
 * @since 0.995.0
 */
public class BPackage {

    public String org;
    public String name;
    public String version;
    private int hashCode;

    public BPackage(String org, String name, String version) {
        this.org = org;
        this.name = name;
        this.version = version;
        hashCode = Objects.hash(org, name, version);
    }

    public BPackage(String org, String name) {
        this.org = org;
        this.name = name;
        this.version = "";
        hashCode = Objects.hash(org, name);
    }

    public String getOrg() {
        return org;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
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
        BPackage bPackage = (BPackage) o;
        return Objects.equals(org, bPackage.org) &&
                Objects.equals(name, bPackage.name) &&
                Objects.equals(version, bPackage.version);
    }

    @Override
    public String toString() {
        if (DOT.equals(this.name)) {
            return this.name;
        }

        String orgName = "";
        if (this.org != null && !this.org.equals(ANON_ORG)) {
            orgName = this.org + ORG_NAME_SEPARATOR;
        }

        if (version == null || version.equals(EMPTY)) {
            return orgName + this.name;
        }

        return orgName + this.name + VERSION_SEPARATOR + this.version;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
