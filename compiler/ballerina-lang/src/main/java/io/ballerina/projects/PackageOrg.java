/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.util.ProjectConstants;

import java.util.Objects;

/**
 * Represents the name of a {@code Package}.
 *
 * @since 2.0.0
 */
public class PackageOrg {
    private final String packageOrgStr;
    private static final String BALLERINA_ORG_NAME = "ballerina";
    private static final String BALLERINA_I_ORG_NAME = "ballerinai";
    private static final String BALLERINA_X_ORG_NAME = "ballerinax";
    public static final PackageOrg BALLERINA_ORG = new PackageOrg(BALLERINA_ORG_NAME);
    public static final PackageOrg BALLERINA_I_ORG = new PackageOrg(BALLERINA_I_ORG_NAME);
    public static final PackageOrg BALLERINA_X_ORG = new PackageOrg(BALLERINA_X_ORG_NAME);

    private PackageOrg(String packageOrgStr) {
        this.packageOrgStr = packageOrgStr;
    }

    public static PackageOrg from(String packageNameStr) {
        if (BALLERINA_ORG_NAME.equals(packageNameStr)) {
            return BALLERINA_ORG;
        }

        // TODO Check whether the packageOrg is a valid Ballerina identifier
        return new PackageOrg(packageNameStr);
    }

    public String value() {
        return packageOrgStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageOrg that = (PackageOrg) o;
        return packageOrgStr.equals(that.packageOrgStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageOrgStr);
    }

    @Override
    public String toString() {
        return packageOrgStr;
    }

    public boolean anonymous() {
        return ProjectConstants.ANON_ORG.equals(packageOrgStr);
    }

    public boolean isBallerinaOrg() {
        return this == BALLERINA_ORG;
    }

    public boolean isBallerinaxOrg() {
        return this == BALLERINA_X_ORG;
    }
}
