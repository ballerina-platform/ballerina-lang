/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 */

package io.ballerina.runtime.test.api;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BResourceMethodType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for runtime APIs related to resource method types.
 *
 * @since 2.0.0
 */
public class ResourceMethodTypeApiTests {

    private static final Module module = new Module("myOrg", "test_module", "1.0.0");

    @Test
    public void resourceMethodTypeApiTest() {
        Parameter[] parameters = {
                new Parameter("id", false, null, PredefinedTypes.TYPE_STRING),
                new Parameter("count", true, "getDefaultCount", PredefinedTypes.TYPE_INT)
        };
        BFunctionType functionType = new BFunctionType(module, parameters, null,
                PredefinedTypes.TYPE_STRING, 0, "getUser");
        BObjectType parentObjectType = new BObjectType("UserService", module, 0);
        String[] resourcePath = {"users", "[string id]"};
        BResourceMethodType resourceMethodType = new BResourceMethodType("getUser", module, parentObjectType,
                functionType, new Type[]{PredefinedTypes.TYPE_STRING}, 0, "get", resourcePath);

        Assert.assertEquals(resourceMethodType.getAccessor(), "get");
        Assert.assertEquals(resourceMethodType.getResourcePath(), resourcePath);
        Assert.assertEquals(resourceMethodType.getParamNames(), new String[]{"id", "count"});
        Assert.assertEquals(resourceMethodType.getParamDefaultability(), new Boolean[]{false, true});
        Assert.assertEquals(resourceMethodType.toString(),
                "resource function get users/[string id](string id,int count) returns (string)");
    }

    @Test
    public void duplicateResourceMethodTypeTest() {
        BFunctionType functionType = new BFunctionType(module, new Parameter[0], null, PredefinedTypes.TYPE_NULL,
                0, "resource");
        BObjectType parentObjectType = new BObjectType("Service", module, 0);
        BResourceMethodType resourceMethodType = new BResourceMethodType("resource", module, parentObjectType,
                functionType, new Type[0], 0, "post", new String[]{"items"});

        MethodType duplicate = resourceMethodType.duplicate();

        Assert.assertTrue(duplicate instanceof BResourceMethodType);
        Assert.assertEquals(duplicate.getName(), resourceMethodType.getName());
        Assert.assertEquals(duplicate.getParameters(), resourceMethodType.getParameters());
        Assert.assertEquals(duplicate.getReturnType(), resourceMethodType.getReturnType());
        Assert.assertEquals(((BResourceMethodType) duplicate).getAccessor(), "post");
        Assert.assertEquals(((BResourceMethodType) duplicate).getResourcePath(), new String[]{"items"});
    }
}
