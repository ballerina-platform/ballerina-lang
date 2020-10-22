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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ResolvedPackagesJsonSchema
 */
public class ResolvedPackagesJsonSchema {
    public static final String SERIALIZED_NAME_PACKAGES = "packages";
    @SerializedName(SERIALIZED_NAME_PACKAGES) private List<ResolvedPackagesJsonSchemaPackages> packages = null;

    public ResolvedPackagesJsonSchema packages(List<ResolvedPackagesJsonSchemaPackages> packages) {

        this.packages = packages;
        return this;
    }

    public ResolvedPackagesJsonSchema addPackagesItem(ResolvedPackagesJsonSchemaPackages packagesItem) {
        if (this.packages == null) {
            this.packages = new ArrayList<>();
        }
        this.packages.add(packagesItem);
        return this;
    }

    /**
     * Get packages
     *
     * @return packages
     **/
    @javax.annotation.Nullable
    public List<ResolvedPackagesJsonSchemaPackages> getPackages() {
        return packages;
    }

    public void setPackages(List<ResolvedPackagesJsonSchemaPackages> packages) {
        this.packages = packages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResolvedPackagesJsonSchema resolvedPackagesJsonSchema = (ResolvedPackagesJsonSchema) o;
        return Objects.equals(this.packages, resolvedPackagesJsonSchema.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolvedPackagesJsonSchema {\n");
        sb.append("    packages: ").append(toIndentedString(packages)).append("\n");
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
