/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.isolation;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;

/**
 * Test cases related to warnings regarding service and/or remote/resource methods not being called concurrently
 * because they are not marked/inferred as isolated.
 *
 * @since 2.0.0
 */
public class IsolationWarningTest {
    private static final String NON_ISOLATED_METHOD_WARNING = "concurrent calls will not be made to this method since" +
            " the method is not an 'isolated' method";
    private static final String NON_ISOLATED_SERVICE_WARNING = "concurrent calls will not be made to this method " +
            "since the service is not an 'isolated' service";
    private static final String NON_ISOLATED_SERVICE_AND_METHOD_WARNING = "concurrent calls will not be made to this " +
            "method since the service and the method are not 'isolated'";

    @Test
    public void testIsolationWarnings() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_warnings_for_service_methods.bal");
        int i = 0;
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 26, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 30, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 43, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 47, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 62, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 66, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 79, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 83, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 101, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 105, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 118, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 122, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 137, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 141, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 154, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 158, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 176, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_WARNING, 180, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 193, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 197, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 211, 5);
        validateWarning(result, i++, NON_ISOLATED_METHOD_WARNING, 215, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 228, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 232, 5);
        assertEquals(result.getWarnCount(), i);
    }
}
