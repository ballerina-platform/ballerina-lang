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
    static final String RUN_COMMAND = "runOld";
    static final String BUILD_COMMAND = "buildOld";
    static final String COMPILE_COMMAND = "compileOld";
    static final String TEST_COMMAND = "testOld";
    static final String INIT_COMMAND = "initOld";
    static final String NEW_COMMAND = "newOld";
    static final String ADD_COMMAND = "addOld";
    static final String LIST_COMMAND = "listOld";
    static final String PULL_COMMAND = "pullOld";
    static final String PUSH_COMMAND = "pushOld";
    static final String SEARCH_COMMAND = "searchOld";
    static final String CLEAN_COMMAND = "cleanOld";
    static final String UNINSTALL_COMMAND = "uninstallOld";

    //module name format : <org-name>/<module-name> | <org-name>/<module-name>:<version>
    //version format : 1, 1.*, 1.*.*
    static final String MODULE_NAME_REGEX = "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[*\\d]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[*\\d]+\\.[*\\d]+|" +
            "[^0-9_][_\\w]+/[^0-9_][_\\.\\w]+:[*\\d]+\\.[*\\d]+\\.[*\\d]+";
}
