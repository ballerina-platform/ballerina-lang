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

import javax.annotation.Nullable;

/**
 * {@code Module} represents module json from central.
 */
public class Module {
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName(SERIALIZED_NAME_NAME) private String name;

    public static final String SERIALIZED_NAME_SUMMARY = "summary";
    @SerializedName(SERIALIZED_NAME_SUMMARY) private String summary;

    public static final String SERIALIZED_NAME_README = "readme";
    @SerializedName(SERIALIZED_NAME_README) private String readme;

    public static final String SERIALIZED_NAME_API_DOC_U_R_L = "apiDocURL";
    @SerializedName(SERIALIZED_NAME_API_DOC_U_R_L) private String apiDocURL;

    public static final String SERIALIZED_NAME_EXECUTABLE = "executable";
    @SerializedName(SERIALIZED_NAME_EXECUTABLE) private Boolean executable = false;

    public static final String SERIALIZED_NAME_PACKAGE_URL = "packageUrl";
    @SerializedName(SERIALIZED_NAME_PACKAGE_URL) private String packageUrl;

    public Module name(String name) {

        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Module summary(String summary) {

        this.summary = summary;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Module readme(String readme) {

        this.readme = readme;
        return this;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public Module apiDocURL(String apiDocURL) {

        this.apiDocURL = apiDocURL;
        return this;
    }

    public String getApiDocURL() {
        return apiDocURL;
    }

    public void setApiDocURL(String apiDocURL) {
        this.apiDocURL = apiDocURL;
    }

    public Module executable(Boolean executable) {

        this.executable = executable;
        return this;
    }

    @Nullable
    public Boolean getExecutable() {
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    public Module packageUrl(String packageUrl) {

        this.packageUrl = packageUrl;
        return this;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public void setPackageUrl(String packageUrl) {
        this.packageUrl = packageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Module moduleJsonSchema = (Module) o;
        return Objects.equals(this.name, moduleJsonSchema.name) && Objects
                .equals(this.summary, moduleJsonSchema.summary) && Objects.equals(this.readme, moduleJsonSchema.readme)
                && Objects.equals(this.apiDocURL, moduleJsonSchema.apiDocURL) && Objects
                .equals(this.executable, moduleJsonSchema.executable) && Objects
                .equals(this.packageUrl, moduleJsonSchema.packageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, summary, readme, apiDocURL, executable, packageUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModuleJsonSchema {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
        sb.append("    readme: ").append(toIndentedString(readme)).append("\n");
        sb.append("    apiDocURL: ").append(toIndentedString(apiDocURL)).append("\n");
        sb.append("    executable: ").append(toIndentedString(executable)).append("\n");
        sb.append("    packageUrl: ").append(toIndentedString(packageUrl)).append("\n");
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
