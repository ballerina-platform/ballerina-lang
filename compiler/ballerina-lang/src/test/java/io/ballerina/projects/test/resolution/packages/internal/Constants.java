/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test.resolution.packages.internal;

/**
 * Contains various constants used through out the package resolution test framework.
 *
 * @since 2.0.0
 */
public class Constants {

    public static final String APP_FILE_NAME = "app.dot";
    public static final String REPO_DIR_NAME = "repositories";
    public static final String CENTRAL_REPO_FILE_NAME = "central.dot";
    public static final String DIST_REPO_FILE_NAME = "dist.dot";
    public static final String LOCAL_REPO_FILE_NAME = "local.dot";
    public static final String DEPS_TOML_FILE_NAME = "dependencies.dot";
    public static final String EXP_GRAPH_STICKY_FILE_NAME = "expected-graph-sticky.dot";
    public static final String EXP_GRAPH_NO_STICKY_FILE_NAME = "expected-graph-nosticky.dot";

    private Constants() {
    }
}
