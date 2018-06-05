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
package org.wso2.ballerinalang.compiler;

/**
 * Defines constants related to the Ballerina.lock file.
 *
 * @since 0.973.1
 */
public class LockFileConstants {

    public static final String VERSION = "version";
    public static final String ORG_NAME = "org-name";
    static final String BALLERINA_LOCK_FILE_NAME = "Ballerina.lock";
    static final String BALLERINA = "ballerina";
    static final String DEPENDENCIES = "dependencies";
    static final String PROJECT_LOCK = "project-lock";
    static final String LOCKFILE_VERSION = "lockfile-version";
    static final String BALLERINA_VERSION = "ballerina-version";
    static final String BALLERINA_VERSION_PROPERTY = "ballerina.version";
    static final String LAUNCHER_PROPERTIES_FILE = "/META-INF/launcher.properties";

    private LockFileConstants() {
    }
}
