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

package org.ballerinalang.central.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * ResolvedPackagesJsonSchemaPackages
 */
public class ResolvedPackagesJsonSchemaPackages {
    public static final String SERIALIZED_NAME_ORGANIZATION = "organization";
    @SerializedName(SERIALIZED_NAME_ORGANIZATION) private String organization;

    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName(SERIALIZED_NAME_NAME) private String name;

    public static final String SERIALIZED_NAME_VERSION = "version";
    @SerializedName(SERIALIZED_NAME_VERSION) private String version;

    public ResolvedPackagesJsonSchemaPackages organization(String organization) {

        this.organization = organization;
        return this;
    }

    /**
     * Get organization
     *
     * @return organization
     **/
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public ResolvedPackagesJsonSchemaPackages name(String name) {

        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResolvedPackagesJsonSchemaPackages version(String version) {

        this.version = version;
        return this;
    }

    /**
     * Get version
     *
     * @return version
     **/
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResolvedPackagesJsonSchemaPackages resolvedPackagesJsonSchemaPackages = (ResolvedPackagesJsonSchemaPackages) o;
        return Objects.equals(this.organization, resolvedPackagesJsonSchemaPackages.organization) && Objects
                .equals(this.name, resolvedPackagesJsonSchemaPackages.name) && Objects
                .equals(this.version, resolvedPackagesJsonSchemaPackages.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, name, version);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolvedPackagesJsonSchemaPackages {\n");
        sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
