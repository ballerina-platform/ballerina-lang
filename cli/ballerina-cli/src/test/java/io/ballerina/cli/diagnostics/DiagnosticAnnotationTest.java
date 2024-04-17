/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.cli.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for DiagnosticAnnotation class.
 *
 * @since 2201.9.0
 */
public class DiagnosticAnnotationTest {

    @Test
    public void testColorAnnotations() {
        String errorColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.ERROR);
        String warningColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.WARNING);
        String hintColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.HINT);
        String message = "Hello World!";

        String errorMessage = DiagnosticAnnotation.getColoredString(message, errorColor, true);
        String expected =
                DiagnosticAnnotation.JANSI_ANNOTATOR + "red" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(errorMessage, expected);

        String warningMessage = DiagnosticAnnotation.getColoredString(message, warningColor, true);
        expected = DiagnosticAnnotation.JANSI_ANNOTATOR + "yellow" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(warningMessage, expected);

        String hintMessage = DiagnosticAnnotation.getColoredString(message, hintColor, true);
        expected = DiagnosticAnnotation.JANSI_ANNOTATOR + "blue" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(hintMessage, expected);
    }

    @Test
    public void testTruncation() {
        String message = "This is a long message that needs to be truncated";
        int importantPartStart = 15;
        int importantPartLength = 7;
        Map<Integer, String> truncatedLines = new HashMap<>();
        truncatedLines.put(49, "This is a long message that needs to be truncated");
        truncatedLines.put(30, "This is a long message that...");
        truncatedLines.put(25, "This is a long message...");
        truncatedLines.put(20, "...ong message th...");
        truncatedLines.put(13, "...message...");
        truncatedLines.put(10, "...essa...");
        truncatedLines.put(5, "......");
        truncatedLines.put(0, "......");

        int[] lengths = {49, 30, 25, 20, 13, 10, 5, 0};
        for (int length : lengths) {
            DiagnosticAnnotation.TruncateResult truncatedMessage =
                    DiagnosticAnnotation.truncate(message, length, importantPartStart, importantPartLength);
            String expected = truncatedLines.get(length);
            Assert.assertEquals(truncatedMessage.line(), expected);
        }
    }

}
