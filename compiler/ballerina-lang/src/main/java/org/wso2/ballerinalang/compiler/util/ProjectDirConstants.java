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
package org.wso2.ballerinalang.compiler.util;

/**
 * Defines constants related to the project directory.
 *
 * @since 0.964.0
 */
public final class ProjectDirConstants {


        private ProjectDirConstants() {
    }


    public static final String BLANG_SOURCE_EXT = ".bal";
    public static final String BLANG_COMPILED_PROG_EXT = ".balx";
    public static final String BLANG_COMPILED_PKG_BINARY_EXT = ".bala";
    public static final String BLANG_COMPILED_PKG_BIR_EXT = ".bir";
    public static final String BLANG_COMPILED_PKG_EXT = ".bala";
    public static final String BLANG_COMPILED_JAR_EXT = ".jar";

    public static final String MANIFEST_FILE_NAME = "Ballerina.toml";
    public static final String LOCK_FILE_NAME = "Ballerina.lock";
    public static final String MODULE_MD_FILE_NAME = "Module.md";
    public static final String DOT_BALLERINA_DIR_NAME = ".ballerina";
    public static final String DOT_BALLERINA_REPO_DIR_NAME = "repo";

    public static final String SOURCE_DIR_NAME = "src";
    public static final String BIN_DIR_NAME = "bin";
    public static final String TARGET_DIR_NAME = "target";
    public static final String TARGET_BALA_DIRECTORY = "bala";
    public static final String TARGET_API_DOC_DIRECTORY = "apidocs";

    public static final String RESOURCE_DIR_NAME = "resources";
    public static final String TEST_DIR_NAME = "tests";
    public static final String CACHES_DIR_NAME = "caches";
    public static final String DIST_BIR_CACHE_DIR_NAME = "bir-cache";
    public static final String BALLERINA_CENTRAL_DIR_NAME = "central.ballerina.io";
    public static final String USER_REPO_OBJ_DIRNAME = "obj";
    public static final String USER_REPO_BIR_DIRNAME = "bir";

    public static final String HOME_REPO_ENV_KEY = "BALLERINA_HOME_DIR";
    public static final String HOME_REPO_DEFAULT_DIRNAME = ".ballerina";
    public static final String SETTINGS_FILE_NAME = "Settings.toml";

    public static final String BALLERINA_HOME = "ballerina.home";
    public static final String BALLERINA_HOME_LIB = "lib";
    public static final String BALLERINA_HOME_BRE = "bre";
    public static final String USER_DIR = "user.dir";

    public static final String BALLERINA_VERSION = "ballerina.version";
    public static final String BALLERINA_PACK_VERSION = "ballerina.packVersion";
    public static final String BALLERINA_SHORT_VERSION = "ballerina.version";
    public static final String BALLERINA_SPEC_VERSION = "spec.version";
    public static final String PROPERTIES_FILE = "/META-INF/tool.properties";

    public static final String BALLERINA_SOURCE_ROOT = "ballerina.source.root";

    public static final String NIGHTLY_BUILD = "nightly.build";

    public static final String DS_STORE_FILE = ".DS_Store";

    public static final String BALLERINA_CENTRAL_ACCESS_TOKEN = "BALLERINA_CENTRAL_ACCESS_TOKEN";

    public static final String MAIN_CLASS_MANIFEST_ENTRY = "Main-Class";

    public static final String JAVA_MAIN = "main";

    public static final String FILE_NAME_DELIMITER = "-";

    // Bala specific constants
    public static final String BALA_METADATA_DIR_NAME = "metadata";
    public static final String BALA_METADATA_FILE = "BALA.toml";
    public static final String BALA_MODULE_METADATA_FILE = "MODULE.toml";
    public static final String BALA_DOC_DIR_NAME = "docs";
    public static final String BALA_PLATFORM_LIB_DIR_NAME = "platform-libs";

    public static final String BALA_CACHE_DIR_NAME = "bala_cache";
    public static final String BIR_CACHE_DIR_NAME = "bir_cache";
    public static final String JAR_CACHE_DIR_NAME = "jar_cache";
    public static final String JSON_CACHE_DIR_NAME = "json_cache";

    public static final String BLANG_PKG_DEFAULT_VERSION = "0.0.0";

    public static final String MODULES_ROOT = "modules";

}
