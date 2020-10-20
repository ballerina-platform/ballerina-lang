/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.latest;

import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Expression Context tests.
 * 
 * @since 2.0.0
 */
public class TypeDescContextTest extends CompletionTestNew {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }
    
    @Override
    public String getTestResourceDir() {
        return "typedesc_context";
    }

    @Override
    public List<String> skipList() {
        return Arrays.asList(
                "object_typedesc1.json",
                "object_typedesc2.json",
                "object_typedesc3.json",
                "object_typedesc4.json",
                "object_typedesc5.json",
                "object_typedesc6.json",
                "object_typedesc7.json",
                "object_typedesc8.json",
                "object_typedesc9.json",
                "object_typedesc10.json",
                "object_typedesc11.json",
                "object_typedesc12.json",
                "function_typedesc16.json",
                "function_typedesc17.json",
                "function_typedesc18.json",
                "function_typedesc19.json",
                "table_typedesc4.json",
                "table_typedesc7.json"
        );
    }
}
