/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.util;

/**
 * Repository related util methods.
 *
 * @since 2.0.0
 */
public class RepoUtils {

    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/2.0/registry";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/2.0/registry";
    private static final String DEV_URL = "https://api.dev-central.ballerina.io/2.0/registry";

    public static final String BALLERINA_STAGE_CENTRAL = "BALLERINA_STAGE_CENTRAL";
    public static final String BALLERINA_DEV_CENTRAL = "BALLERINA_DEV_CENTRAL";
    public static final boolean SET_BALLERINA_STAGE_CENTRAL = Boolean.parseBoolean(
            System.getenv(BALLERINA_STAGE_CENTRAL));
    public static final boolean SET_BALLERINA_DEV_CENTRAL = Boolean.parseBoolean(
            System.getenv(BALLERINA_DEV_CENTRAL));

    private RepoUtils() {
    }

    /**
     * Get the remote repo URL.
     *
     * @return URL of the remote repository
     */
    public static String getRemoteRepoURL() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return STAGING_URL;
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return DEV_URL;
        }
        return PRODUCTION_URL;
    }
}
