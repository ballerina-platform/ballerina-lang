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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package io.ballerina.runtime.test;

import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnostic;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for Runtime Diagnosis.
 */
public class RuntimeDiagnosisTests {

    @Test
    public void testDiagnosisLog() {
        RuntimeDiagnosticLog runtimeDiagnosticLog = new RuntimeDiagnosticLog();
        runtimeDiagnosticLog.error(ErrorCodes.TYPE_CAST_ERROR, "testOrg/main:0.1.0(main.bal:25)", "string", "int");
        runtimeDiagnosticLog.warn(ErrorCodes.ARRAY_INDEX_OUT_OF_RANGE, "testOrg/main:0.1.0(main.bal:27)", 5, 4);
        Assert.assertEquals(runtimeDiagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(runtimeDiagnosticLog.getWarningCount(), 1);
        List<RuntimeDiagnostic> diagnosticList = runtimeDiagnosticLog.getDiagnosticList();
        Assert.assertEquals(diagnosticList.get(0).message(), "incompatible types: 'string' cannot be cast to 'int'");
        Assert.assertEquals(diagnosticList.get(1).message(), "array index out of range: index: 5, size: 4");
    }
}
