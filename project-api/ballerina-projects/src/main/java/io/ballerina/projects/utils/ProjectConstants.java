/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.utils;

/**
 * Defines constants related to the project directory.
 *
 * @since 2.0.0
 */
public class ProjectConstants {

    public static final String BLANG_SOURCE_EXT = ".bal";

    public static final String BALLERINA_TOML = "Ballerina.toml";
    public static final String GITIGNORE_FILE_NAME = ".gitignore";
    public static final String MODULE_MD_FILE_NAME = "Module.md";
    public static final String PACKAGE_MD_FILE_NAME = "Package.md";

    public static final String SOURCE_DIR_NAME = "src";
    public static final String BIN_DIR_NAME = "bin";
    public static final String TARGET_DIR_NAME = "target";

    public static final String CACHES_DIR_NAME = "caches";

    public static final String HOME_REPO_ENV_KEY = "BALLERINA_HOME_DIR";
    public static final String HOME_REPO_DEFAULT_DIRNAME = ".ballerina";

    public static final String USER_DIR = "user.dir";
    public static final String USER_NAME = "user.name";

    // Balo specific constants
    public static final String MODULES_ROOT = "modules";
    public static final String LIB_DIR = "lib";

    public static final String BALO_DIR_NAME = "balo";
    public static final String BALO_CACHE_DIR_NAME = "balo_cache";
    public static final String BIR_CACHE_DIR_NAME = "bir_cache";
    public static final String JAR_CACHE_DIR_NAME = "jar_cache";
    public static final String JSON_CACHE_DIR_NAME = "json_cache";
    public static final String TEST_DIR_NAME = "tests";

    public static final String BLANG_COMPILED_PKG_BINARY_EXT = ".balo";
    public static final String BLANG_COMPILED_PKG_BIR_EXT = ".bir";
    public static final String BLANG_COMPILED_JAR_EXT = ".jar";
    public static final String RESOURCE_DIR_NAME = "resources";

    public static final String BALLERINA_VERSION = "ballerina.version";
    public static final String PROPERTIES_FILE = "/META-INF/tool.properties";

    public static final String TARGET_BALO_DIR_NAME = "balo";
    public static final String BALLERINA_INSTALL_DIR_PROP = "ballerina.home";
    public static final String DIST_CACHE_DIRECTORY = "distribution-cache";

    public static final String ANON_ORG = "$anon";
    public static final String DOT = ".";
    public static final String DEFAULT_VERSION = "0.0.0";
}
