/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Holds the function info.
 *
 * @since 2.0.0
 */
public class TestFunction implements CTestSteps {

    private String functionName;
    private boolean assertParamFlag = false;
    private boolean paramFlag = false;
    private boolean panicFlag = true;
    private List<List<Map<String, String>>> assertDataProvider = new ArrayList<>();
    private List<List<Map<String, String>>> dataProvider = new ArrayList<>();

    public void addDataProvider(List<Map<String, String>> parameters) {
        this.dataProvider.add(parameters);
    }

    public void addAssertDataProvider(List<Map<String, String>> parameters) {
        this.assertDataProvider.add(parameters);
    }

    public List<List<Map<String, String>>> getDataProvider() {
        return this.dataProvider;
    }

    public List<List<Map<String, String>>> getAssertDataProvider() {
        return this.assertDataProvider;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public void setAssertParamFlag() {
        this.assertParamFlag = true;
    }

    public void setParamFlag() {
        this.paramFlag = true;
    }

    public void unSetPanicFlag() {
        this.panicFlag = false;
    }

    public boolean isAssertParamFlag() {
        return this.assertParamFlag;
    }

    public boolean isParamFlag() {
        return this.paramFlag;
    }

    public boolean getPanicFlag() {
        return this.panicFlag;
    }
}
