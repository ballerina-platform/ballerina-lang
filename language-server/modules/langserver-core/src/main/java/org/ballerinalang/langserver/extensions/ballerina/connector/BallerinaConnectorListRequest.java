/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

/**
 * Request to get connectorList.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorListRequest {

    private String file;
    private String query;
    private String packageName;
    private String connector;
    private String description;
    private String template;
    private String keyword;
    private String ballerinaVersion;
    private boolean platform;
    private boolean userPackages;

    private int limit;
    private int offset;
    private String sort;

    public BallerinaConnectorListRequest() {
    }

    public BallerinaConnectorListRequest(String file, String packageName) {
        this.file = file;
        this.packageName = packageName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public boolean isPlatform() {
        return platform;
    }

    public void setPlatform(boolean platform) {
        this.platform = platform;
    }

    public boolean isUserPackages() {
        return userPackages;
    }

    public void setUserPackages(boolean userPackages) {
        this.userPackages = userPackages;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorListRequest{" +
                "file='" + file + '\'' +
                ", query='" + query + '\'' +
                ", packageName='" + packageName + '\'' +
                ", connector='" + connector + '\'' +
                ", description='" + description + '\'' +
                ", template='" + template + '\'' +
                ", keyword='" + keyword + '\'' +
                ", ballerinaVersion='" + ballerinaVersion + '\'' +
                ", platform=" + platform +
                ", userPackages=" + userPackages +
                ", limit=" + limit +
                ", offset=" + offset +
                ", sort='" + sort + '\'' +
                '}';
    }
}
