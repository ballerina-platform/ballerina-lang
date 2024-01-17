/*
 *  Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.common.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests the utility methods in FunctionGenerator.
 */
public class FunctionGeneratorTest {

    @Test(dataProvider = "dataProvider")
    public void testProcessModuleIDsInText(String text, String expected) {
        String result = FunctionGenerator.processModuleIDsInText(text);
        Assert.assertEquals(result, expected);
    }

    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][]{
                {"ballerina/module1:0.1.0:Response", "module1:Response"},
                {"ballerina/module.part:2.5.1:PartRecord", "part:PartRecord"},
                {"ballerinax/module.part:2.5.1:PartRecord", "part:PartRecord"},
                {"example/test.example:1.2.3:Test", "example:Test"},
                {"int|string|example/test.example:1.2.3:Test|json", "int|string|example:Test|json"},
                {
                        "int|ballerina/module1:0.1.0:Response|ballerina/module1.mod1:0.1.0:Response|json",
                        "int|module1:Response|mod1:Response|json"
                },
        };
    }
}
