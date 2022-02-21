/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.object;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests for `service objects` and `service classes`.
 *
 * @since 2.0.0
 */
public class ServiceObjectBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        int i = 0;
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/service_test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/object/test_service_objects.bal");
    }

    @Test
    public void testServiceClassBala() {
        BRunUtil.invoke(result, "testServiceObjectValue");
    }
}
