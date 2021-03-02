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
package io.ballerina.projects.test;

import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeSuite;

/**
 * Parent test class for all project api test cases. This will provide basic functionality for tests.
 *
 * @since 2.0.0
 */
public class BaseTest {

    @BeforeSuite
    public void init() {
        // Here package_a depends on package_b
        // and package_b depends on package_c
        // Therefore package_c is transitive dependency of package_a

        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_c");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_b");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_e");

        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_alpha");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_beta");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_GA");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_l_with_unstable_dep");
    }
}
