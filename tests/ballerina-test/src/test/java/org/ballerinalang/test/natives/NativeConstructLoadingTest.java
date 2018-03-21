/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.natives;

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.natives.NativeUnitLoader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for native construct loading.
 */
public class NativeConstructLoadingTest {

    private NativeUnitLoader nativeLoader;

    @BeforeClass
    public void setup() {
        this.nativeLoader = NativeUnitLoader.getInstance();
    }

    @Test
    public void testLoadingExistingFunction() {
        NativeCallableUnit function = this.nativeLoader.loadNativeFunction("ballerina.math", "pow");
        Assert.assertNotNull(function);
    }

    @Test
    public void testLoadingNonExistingFunction() {
        NativeCallableUnit function = this.nativeLoader.loadNativeFunction("ballerina.lang.system", "foo");
        Assert.assertNull(function);
    }

    @Test
    public void testLoadingFunctionInNonExistingPackage() {
        NativeCallableUnit function = this.nativeLoader.loadNativeFunction("ballerina.lang.foo", "println");
        Assert.assertNull(function);
    }
}
