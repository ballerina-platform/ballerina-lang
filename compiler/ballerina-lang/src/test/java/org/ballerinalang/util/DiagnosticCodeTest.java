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
package org.ballerinalang.util;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
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
        validateDiagnosticUniqueness(DiagnosticErrorCode.class);
    }

    @Test
    public void testDiagnosticWarningCodesUniqueness() {
        validateDiagnosticUniqueness(DiagnosticWarningCode.class);
    }

    // helpers
    private <E extends DiagnosticCode> void validateDiagnosticUniqueness(Class<E> diagnosticCode) {
        E[] diagCodes = diagnosticCode.getEnumConstants();
        ArrayList<String> duplicateDiagnosticIds = new ArrayList<>();
        ArrayList<String> duplicateDiagnosticMsgKey = new ArrayList<>();
        for (int i = 0; i < diagCodes.length; i++) {
            for (int j = i + 1; j < diagCodes.length; j++) {
                if (diagCodes[i].diagnosticId().equals(diagCodes[j].diagnosticId())) {
                    duplicateDiagnosticIds.add(diagCodes[i].diagnosticId());
                }

                if (diagCodes[i].messageKey().equals(diagCodes[j].messageKey())) {
                    duplicateDiagnosticMsgKey.add(diagCodes[i].messageKey());
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
