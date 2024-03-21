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

import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateHint;
import static org.testng.Assert.assertEquals;

/**
 * Test cases related to warnings regarding service and/or remote/resource methods not being called concurrently
 * because they are not marked/inferred as isolated.
 *
 * @since 2.0.0
 */
public class IsolationWarningTest {
    private static final String NON_ISOLATED_METHOD_HINT = "concurrent calls will not be made to this method since" +
            " the method is not an 'isolated' method";
    private static final String NON_ISOLATED_SERVICE_HINT = "concurrent calls will not be made to this method " +
            "since the service is not an 'isolated' service";
    private static final String NON_ISOLATED_SERVICE_AND_METHOD_HINT = "concurrent calls will not be made to this " +
            "method since the service and the method are not 'isolated'";

    @Test
    public void testIsolationWarnings() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_warnings_for_service_methods.bal");
        int i = 0;
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 26, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 30, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 39, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 43, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 51, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 55, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 65, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 69, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 78, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 82, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 90, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 94, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 103, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 107, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 117, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 121, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 133, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 137, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 146, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 150, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 158, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 162, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 172, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 176, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 185, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 189, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 197, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 201, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 210, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 214, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 224, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 228, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 240, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 244, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 253, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 257, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 265, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 269, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 279, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 283, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 291, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 295, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 303, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 307, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 316, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 320, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 330, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 334, 5);
        assertEquals(result.getHintCount(), i);

        LineRange lineRange = result.getDiagnostics()[0].location().lineRange();
        assertEquals(lineRange.filePath(), "isolation_warnings_for_service_methods.bal");
        assertEquals(lineRange.startLine().line(), 25);
        assertEquals(lineRange.startLine().offset(), 4);
        assertEquals(lineRange.endLine().line(), 25);
        assertEquals(lineRange.endLine().offset(), 4);
    }
}
