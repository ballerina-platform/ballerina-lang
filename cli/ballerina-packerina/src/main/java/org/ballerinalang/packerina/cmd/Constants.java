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
package org.ballerinalang.packerina.cmd;

/**
 * Class containing packerina command related constants.
 *
 * @since 0.982.0
 */
public class Constants {
    static final String RUN_COMMAND = "run";
    static final String BUILD_COMMAND = "build";
    static final String COMPILE_COMMAND = "compile";
    static final String DOC_COMMAND = "doc";
    static final String TEST_COMMAND = "test";
    static final String INIT_COMMAND = "init";
    static final String NEW_COMMAND = "new";
    static final String ADD_COMMAND = "add";
    static final String LIST_COMMAND = "list";
    static final String PULL_COMMAND = "pull";
    static final String PUSH_COMMAND = "push";
    static final String SEARCH_COMMAND = "search";
    static final String CLEAN_COMMAND = "clean";
    static final String UNINSTALL_COMMAND = "uninstall";

    //Module name format: <org-name>/<module-name> | <org-name>/<module-name>:<version>
    //Module version format: 1, 1.*, 1.*.*
    static final String MODULE_NAME_REGEX = "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[\\*\\d]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[\\*\\d]+\\.[\\*\\d]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[\\*\\d]+\\.[\\*\\d]+\\.[\\*\\d]+";
}
