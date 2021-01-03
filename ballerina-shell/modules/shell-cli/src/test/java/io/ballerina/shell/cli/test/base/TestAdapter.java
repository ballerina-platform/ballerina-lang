/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.cli.test.base;

import io.ballerina.shell.cli.TerminalAdapter;
import org.testng.Assert;

import java.util.List;

/**
 * Adapter to test cases.
 */
public class TestAdapter extends TerminalAdapter {
    private final List<TestCase> testCases;
    private int currentTest;

    public TestAdapter(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    @Override
    protected String color(String text, int color) {
        return text;
    }

    @Override
    public String readLine(String prefix, String postfix) {
        return currentTestCase().getInput();
    }

    @Override
    public void println(String text) {
    }

    @Override
    public void result(String text) {
        Assert.assertEquals(text, currentTestCase().getOutput(),
                currentTestCase().getDescription());
        currentTest++;
    }

    private TestCase currentTestCase() {
        return testCases.get(currentTest);
    }
}
