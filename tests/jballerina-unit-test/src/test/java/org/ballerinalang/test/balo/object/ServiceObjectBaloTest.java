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

package org.ballerinalang.test.balo.object;

import org.ballerinalang.nativeimpl.jvm.servicetests.ServiceValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `service objects` and `service classes`.
 *
 * @since 2.0.0
 */
public class ServiceObjectBaloTest {

    private CompileResult result;

    @Test
    public void testReadonlyRecordFields() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/service_test_project", "testorg", "serv_classes");
        result = BCompileUtil.compile("test-src/balo/test_balo/object/test_service_objects.bal");
    }

    @Test
    public void testReadonlyType() {
        BRunUtil.invoke(result, "testServiceObjectValue");
    }

    @AfterClass
    public void tearDown() {
        ServiceValue.reset();
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "serv_classes");
    }
}
