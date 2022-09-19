/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Bala tests for client resource access action.
 *
 * @since 2201.2.0
 */
public class ClientResourceAccessBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/client_test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/object/resource_access_action.bal");
    }

    @Test(dataProvider = "testClientResourceAccessActionBalaData")
    public void testClientResourceAccessActionBala(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider
    public Object[][] testClientResourceAccessActionBalaData() {
        return new Object[][]{
                {"testBasicClientResourceAccess"},
                {"testClientResourceAccessContainingComputedResourceAccessSegments"},
                {"testConstantAndUserDefinedTypesInResourcePathWithoutVarName"},
                {"testWithResourceAccessRestSegment"},
                {"testResourceAccessWithArguments"},
                {"testStaticTypeOfClientResourceAccessAction"},
                {"testResourceAccessOfAnObjectConstructedViaObjectCons"},
                {"testResourceAccessContainingSpecialChars"},
                {"testClosuresFromPathParams"}
        };
    }

    @Test
    public void testDeprecatedConstructUsageAtRuntimeWithWarning() {
        int index = 0;
        // TODO: improve the warning message for accessing a deprecated resource #36977
        BAssertUtil.validateWarning(result, index++,
                "usage of construct 'testorg/client_classes:0.1.0:MyClient8.get' is deprecated", 360, 16);
        BAssertUtil.validateWarning(result, index++,
                "usage of construct 'testorg/client_classes:0.1.0:MyClient8.get' is deprecated", 363, 20);
        BAssertUtil.validateWarning(result, index++,
                "usage of construct 'testorg/client_classes:0.1.0:MyClient8.post' is deprecated", 366, 16);
        Assert.assertEquals(result.getWarnCount(), index);

        BRunUtil.invoke(result, "testAccessingDeprecatedResource");
    }
}
