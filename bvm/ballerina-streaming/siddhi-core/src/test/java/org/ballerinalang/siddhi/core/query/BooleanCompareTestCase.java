/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.testng.annotations.Test;

/**
 * Test expression comparisons with Boolean values.
 */
public class BooleanCompareTestCase {

    private static final String X_GREATER_THAN_Y_CONDITION = "x > y";
    private static final String X_LESS_THAN_Y_CONDITION = "x < y";
    private static final String X_GREATER_THAN_EQUAL_Y_CONDITION = "x >= y";
    private static final String X_LESS_THAN_EQUAL_Y_CONDITION = "x <= y";
    private static final String X_EQUALS_Y_CONDITION = "x == y";
    private static final String X_NOT_EQUALS_Y_CONDITION = "x != y";

    private static final String X_BOOL_Y_INT_DEF = "x bool, y int";
    private static final String X_INT_Y_BOOL_DEF = "x int, y bool";
    private static final String X_LONG_Y_BOOL_DEF = "x long, y bool";
    private static final String X_FLOAT_Y_BOOL_DEF = "x float, y bool";
    private static final String X_DOUBLE_Y_BOOL_DEF = "x double, y bool";

    // Greater than comparisons
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test1() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test2() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test3() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test4() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test5() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    // Less than comparisons
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test6() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test7() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test8() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test9() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test10() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    // Greater than or equal comparison
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test11() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_EQUAL_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test12() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_EQUAL_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test13() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_EQUAL_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test14() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_EQUAL_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test15() {
        String executionPlan = generateExecutionPlan(X_GREATER_THAN_EQUAL_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    // Less than or equal comparison
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test16() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_EQUAL_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test17() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_EQUAL_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test18() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_EQUAL_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test19() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_EQUAL_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test20() {
        String executionPlan = generateExecutionPlan(X_LESS_THAN_EQUAL_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    // Equals comparison
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test21() {
        String executionPlan = generateExecutionPlan(X_EQUALS_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test22() {
        String executionPlan = generateExecutionPlan(X_EQUALS_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test23() {
        String executionPlan = generateExecutionPlan(X_EQUALS_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test24() {
        String executionPlan = generateExecutionPlan(X_EQUALS_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test25() {
        String executionPlan = generateExecutionPlan(X_EQUALS_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    // Not equals comparison
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test26() {
        String executionPlan = generateExecutionPlan(X_NOT_EQUALS_Y_CONDITION, X_BOOL_Y_INT_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test27() {
        String executionPlan = generateExecutionPlan(X_NOT_EQUALS_Y_CONDITION, X_INT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test28() {
        String executionPlan = generateExecutionPlan(X_NOT_EQUALS_Y_CONDITION, X_LONG_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test29() {
        String executionPlan = generateExecutionPlan(X_NOT_EQUALS_Y_CONDITION, X_FLOAT_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void test30() {
        String executionPlan = generateExecutionPlan(X_NOT_EQUALS_Y_CONDITION, X_DOUBLE_Y_BOOL_DEF);
        new SiddhiManager().createSiddhiAppRuntime(executionPlan);
    }

    private String generateExecutionPlan(String filter, String fields) {
        return new StringBuilder()
                .append("@App:name('filterTest1') ")
                .append("define stream cseEventStream (").append(fields).append(");")
                .append("@info(name = 'query1') ")
                .append("from cseEventStream[").append(filter).append("] ")
                .append("select symbol, price ")
                .append("insert into outputStream;")
                .toString();
    }
}
