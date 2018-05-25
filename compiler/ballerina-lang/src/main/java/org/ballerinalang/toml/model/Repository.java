/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

/**
 * Defines the repository object fields.
 *
 * @since 0.971.1
 */
public class Repository {
    private String name;
    private String url;
    private String accessToken;

    /**
     * Get the url of the repository.
     *
     * @return repository url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set url of the repository.
     *
     * @param url repository url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the access token pertaining to the repository.
     *
     * @return access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the access token pertaining to the repository.
     *
     * @param accessToken access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Get repository name.
     *
     * @return name of the repository
     */
    public String getName() {
        return name;
    }

    /**
     * Set the repository name.
     *
     * @param name repository name
     */
    public void setName(String name) {
        this.name = name;
    }
}
