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
package io.ballerinalang.compiler.parser.test.diagnostics;

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.diagnostics.DiagnosticWarningCode;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test diagnostic codes.
 *
 * @since Swan Lake
 */
public class DiagnosticCodeTest {

    private static final String DUPLICATE_DIAGNOSTIC_IDS_FOUND = "Duplicate diagnostic IDs found";
    private static final String DUPLICATE_DIAGNOSTIC_MSGKEYS_FOUND = "Duplicate diagnostic messageKeys found";

    @Test
    public void testDiagnosticErrorCodesUniqueness() {
        DiagnosticErrorCode[] codes = DiagnosticErrorCode.values();
        validateDiagnosticUniqueness(codes);
    }

    @Test
    public void testDiagnosticWarningCodesUniqueness() {
        DiagnosticWarningCode[] codes = DiagnosticWarningCode.values();
        validateDiagnosticUniqueness(codes);
    }

    // helpers
    private void validateDiagnosticUniqueness(DiagnosticCode[] codes) {
        ArrayList<String> duplicateDiagnosticIds = new ArrayList<>();
        ArrayList<String> duplicateDiagnosticMsgKey = new ArrayList<>();
        for (int i = 0; i < codes.length; i++) {
            for (int j = i + 1; j < codes.length; j++) {
                if (codes[i].diagnosticId().equals(codes[j].diagnosticId())) {
                    duplicateDiagnosticIds.add(codes[i].diagnosticId());
                }

                if (codes[i].messageKey().equals(codes[j].messageKey())) {
                    duplicateDiagnosticMsgKey.add(codes[i].messageKey());
                }
            }
        }

        if (duplicateDiagnosticIds.size() > 0) {
            Assert.fail(DUPLICATE_DIAGNOSTIC_IDS_FOUND + ": " + arrayToString(duplicateDiagnosticIds));
        }

        if (duplicateDiagnosticMsgKey.size() > 0) {
            Assert.fail(DUPLICATE_DIAGNOSTIC_MSGKEYS_FOUND + ": " + arrayToString(duplicateDiagnosticMsgKey));
        }
    }

    private String arrayToString(List listOfStrings) {
        return String.join(", ", listOfStrings);
    }
}
