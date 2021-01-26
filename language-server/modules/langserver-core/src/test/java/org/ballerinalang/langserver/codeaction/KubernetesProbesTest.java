/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import org.testng.annotations.DataProvider;

/**
 * Test cases for adding ballerina service as a liveness or readiness probe to Kuberentes.toml.
 *
 * @since 2.0.0
 */
public class KubernetesProbesTest extends AbstractCodeActionTest {

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                { "addprobe1.json", "probegen.bal" }
        };
    }

    @Override
    public String getResourceDir() {
        return "add-to-k8s-probe";
    }
}
