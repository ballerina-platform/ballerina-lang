/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for gRPC deadline support.
 *
 * @since 1.1.4
 */
@Test(groups = "grpc-test")
public class GrpcDeadlineTestCase {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients",
                "23_grpc_deadline_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test data extraction successful when deadline not exceeded")
    public void testRegisterPatient() {
        BValue[] responses = BRunUtil.invoke(result, "testRegisterPatient", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
    }

    @Test(description = "Test data extraction failed when deadline exceeded")
    public void testGetPatientInfo() {
        BValue[] responses = BRunUtil.invoke(result, "testGetPatientInfo", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BError);
    }
}
