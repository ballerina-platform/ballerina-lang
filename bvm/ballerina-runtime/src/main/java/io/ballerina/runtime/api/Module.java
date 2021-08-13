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
package io.ballerina.runtime.api;

import java.util.Objects;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.EMPTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.ORG_NAME_SEPARATOR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.VERSION_SEPARATOR;

/**
 * {@code Module} represents the module of defined type in Ballerina.
 *
 * @since 0.995.0
 */
public class Module {

    private String org;
    private String name;
    private String majorVersion;
    private int hashCode;

    public Module(String org, String name, String majorVersion) {
        this.org = org;
        this.name = name;
        this.majorVersion = majorVersion;
        hashCode = Objects.hash(org, name, majorVersion);
    }

    public Module(String org, String name) {
        this.org = org;
        this.name = name;
        this.majorVersion = "";
        hashCode = Objects.hash(org, name);
    }

    public String getOrg() {
        return org;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public String getVersion() {
        return majorVersion;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Module bPackage = (Module) o;
        return Objects.equals(org, bPackage.org) &&
                Objects.equals(name, bPackage.name) &&
                Objects.equals(majorVersion, bPackage.majorVersion);
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

        if (majorVersion == null || majorVersion.equals(EMPTY)) {
            return orgName + this.name;
        }

        return orgName + this.name + VERSION_SEPARATOR + this.majorVersion;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
