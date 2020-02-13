/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for validate the module names.
 */
public class UtilsTest {

    private PullCommand pullCommand = new PullCommand();

    @DataProvider(name = "valid-data-provider")
    public Object[][] positiveDataProviderMethod() {
        return new Object[][] {
                { "orgName/module" },
                { "org_name/module" },
                { "org_name/module.1" },
                { "orgName/module_1" },
                { "orgName1/module" },
                { "orgName/module:1" },
                { "orgName/module:*" },
                { "orgName/module:1.1" },
                { "orgName/module:1.*" },
                { "orgName/module:1.0.1" },
                { "orgName/module:0.11.0" },
                { "orgName/module:1.*.1" },
                { "orgName/module:1.*.*" },
        };
    }

    @DataProvider(name = "invalid-data-provider")
    public Object[][] negativeProviderMethod() {
        return new Object[][] {
                { "orgName" },
                { "module" } ,
                { "_orgName/module" },
                { "orgName/_module" },
                { "1orgName/module" },
                { "orgName/1module" },
                { "orgName/module:" },
                { "org-name/module" },
                { "orgName/module-1" },
                { "orgName/" },
                { "orgName/module:1." },
                { "orgName/module:1.1." }
        };
    }

    @Test(description = "Test the valid module names", dataProvider = "valid-data-provider")
    public void positiveChecks(String data) {
        Assert.assertTrue(pullCommand.validateModuleName(data));
    }

    @Test(description = "Test the invalid module names", dataProvider = "invalid-data-provider")
    public void negativeChecks(String data) {
        Assert.assertFalse(pullCommand.validateModuleName(data));
    }
}
