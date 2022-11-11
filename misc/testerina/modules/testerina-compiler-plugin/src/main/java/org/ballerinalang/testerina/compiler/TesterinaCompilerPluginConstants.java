/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.compiler;

/**
 * Testerina Compiler Plugin Constant Class holds constants used in the code generation/ modification.
 *
 * @since 2201.3.0
 */
public class TesterinaCompilerPluginConstants {

    public static final String TEST_REGISTER_FUNCTION = "registerTest";
    public static final String SET_OPTIONS_FUNCTION = "setTestOptions";
    public static final String START_SUITE_FUNCTION = "startSuite";
    public static final String TEST_ORG_NAME = "ballerina";
    public static final String TEST_MODULE_NAME = "test";
    public static final String TEST_EXEC_FUNCTION = "__execute__";
    public static final String TEST_EXEC_FILENAME = "test_execute";
    public static final String TEST_REGISTRAR_EXEC_FUNCTION = "executeTestRegistrar";
    public static final int REGISTERS_PER_FUNCTION = 150;

    public static final String TARGET_PATH_PARAMETER = "targetPath";
    public static final String PACKAGE_NAME_PARAMETER = "packageName";
    public static final String MODULE_NAME_PARAMETER = "moduleName";
    public static final String REPORT_PATH_PARAMETER = "report";
    public static final String COVERAGE_PATH_PARAMETER = "coverage";
    public static final String GROUPS_PARAMETER = "groups";
    public static final String DISABLE_GROUPS_PARAMETER = "disableGroups";
    public static final String TESTS_PARAMETER = "tests";
    public static final String RERUN_FAILED_PARAMETER = "rerunFailed";
    public static final String LIST_GROUPS_PARAMETER = "listGroups";
}
