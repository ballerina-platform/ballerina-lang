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
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 47, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 51, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 55, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 59, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 63, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 67, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 71, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 79, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 83, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 93, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 97, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 101, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 105, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 109, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 113, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 117, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 121, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 125, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 134, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 138, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 146, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 150, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 154, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 158, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 162, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 166, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 170, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 174, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 178, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 187, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 191, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 201, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 205, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 209, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 213, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 217, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 221, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 225, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 229, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 233, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 245, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 249, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 258, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 262, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 266, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 270, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 274, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 278, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 282, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 286, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 290, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 298, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 302, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 312, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 316, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 320, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 324, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 328, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 332, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 336, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 340, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 344, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 353, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 357, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 365, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 369, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 373, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 377, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 381, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 385, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 389, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 393, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 397, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 406, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 410, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 420, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 424, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 428, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 432, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 436, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 440, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 444, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 448, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 452, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 464, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_HINT, 468, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 477, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 481, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 485, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 489, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 493, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 497, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 501, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 505, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 509, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 517, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 521, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 531, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 535, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 539, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 543, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 547, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 551, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 555, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 559, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 563, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 571, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 575, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 583, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 587, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 591, 5);        
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 595, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 599, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 603, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 607, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 611, 5);
        validateHint(result, i++, NON_ISOLATED_METHOD_HINT, 615, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 624, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 628, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 638, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 642, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 646, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 650, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 654, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 658, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 662, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 666, 5);
        validateHint(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_HINT, 670, 5);
        assertEquals(result.getHintCount(), i);

        LineRange lineRange = result.getDiagnostics()[0].location().lineRange();
        assertEquals(lineRange.filePath(), "isolation_warnings_for_service_methods.bal");
        assertEquals(lineRange.startLine().line(), 25);
        assertEquals(lineRange.startLine().offset(), 4);
        assertEquals(lineRange.endLine().line(), 25);
        assertEquals(lineRange.endLine().offset(), 4);
    }
}
