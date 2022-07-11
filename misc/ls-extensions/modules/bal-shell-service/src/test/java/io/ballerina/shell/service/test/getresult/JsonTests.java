/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service.test.getresult;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * getResult End point tests for json.
 *
 * @since 2201.1.1
 */
public class JsonTests extends AbstractGetResultTest {
    @Test(description = "Test for json")
    public void testJson() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("json.objects.json");
    }

    @Test(description = "Test for accessing json fields")
    public void testJsonAccess() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("json.access.json");
    }
}
