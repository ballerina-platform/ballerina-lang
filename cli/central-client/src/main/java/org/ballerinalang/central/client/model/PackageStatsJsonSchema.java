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
 * {@code PackageStatsJsonSchema} represents package stats json.
 */
public class PackageStatsJsonSchema {
    public static final String SERIALIZED_NAME_PULL_COUNT = "pullCount";
    @SerializedName(SERIALIZED_NAME_PULL_COUNT) private Integer pullCount;

    public PackageStatsJsonSchema pullCount(Integer pullCount) {

        this.pullCount = pullCount;
        return this;
    }

    @javax.annotation.Nullable
    public Integer getPullCount() {
        return pullCount;
    }

    public void setPullCount(Integer pullCount) {
        this.pullCount = pullCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageStatsJsonSchema packageStatsJsonSchema = (PackageStatsJsonSchema) o;
        return Objects.equals(this.pullCount, packageStatsJsonSchema.pullCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pullCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PackageStatsJsonSchema {\n");
        sb.append("    pullCount: ").append(toIndentedString(pullCount)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
