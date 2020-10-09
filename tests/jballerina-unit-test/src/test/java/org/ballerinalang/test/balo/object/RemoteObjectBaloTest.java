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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.balo.object;

//import org.ballerinalang.core.model.values.BString;
//import org.ballerinalang.core.model.values.BValue;
//import org.ballerinalang.test.balo.BaloCreator;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.BRunUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;

/**
 * Balo test for Endpoints and remote functions.
 *
 * @since 0.985.0
 */
public class RemoteObjectBaloTest {

//    private CompileResult compileResult;
//
//    @BeforeClass
//    public void setup() {
//        BaloCreator.cleanCacheDirectories();
//        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
//        compileResult = BCompileUtil.compile("test-src/balo/test_balo/object/test_client_objects.bal");
//    }
//
//    @Test
//    public void testRemoteObject() {
//        BValue[] result = BRunUtil.invoke(compileResult, "testCheck");
//        Assert.assertEquals(result.length, 1);
//        Assert.assertEquals(result[0].stringValue(), "i1 {}");
//
//        result = BRunUtil.invoke(compileResult, "testNewEP", new BValue[]{new BString("done")});
//        Assert.assertEquals(result.length, 1);
//        Assert.assertEquals(result[0].stringValue(), "donedone");
//    }
//
//    @AfterClass
//    public void tearDown() {
//        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
//    }
}
