/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.configuration;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for http:resourceConfig path field.
 *
 * @since 0.995.0
 */
public class ResourceConfigPathTest {

    private static final String INVALID_RESOURCE_PARAMETERS =
            "invalid resource parameter(s): cannot specify > 2 parameters without specifying path config and/or body " +
                    "config in the resource annotation";

    @Test
    public void testResourceConfigPathAnnotationsNegativeCases() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/services/configuration/resource-config-path-field.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 12);
        assertResponse(diag[0], "Illegal closing brace detected in resource path config", 12);
        assertResponse(diag[1], "Illegal closing brace detected in resource path config", 19);
        assertResponse(diag[2], "Incomplete path param expression", 26);
        assertResponse(diag[3], "Incomplete path param expression", 34);
        assertResponse(diag[4], "Invalid param expression in resource path config", 42);
        assertResponse(diag[5], "Illegal empty expression in resource path config", 50);
        assertResponse(diag[6], "Invalid param expression in resource path config", 50);
        assertResponse(diag[7], "Illegal expression in resource path config", 58);
        assertResponse(diag[8], "Illegal closing brace detected in resource path config", 66);
        assertResponse(diag[9], "Illegal closing brace detected in resource path config", 74);
        assertResponse(diag[10], "Illegal open brace character in resource path config", 82);
        assertResponse(diag[11], "Illegal expression in resource path config", 90);
    }

    @Test
    public void testPathParamAndSignatureParamMatch() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/services/configuration/resource-arg--pathparam-match.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 8);
        assertResponse(diag[0], INVALID_RESOURCE_PARAMETERS, 10);
        assertResponse(diag[1], INVALID_RESOURCE_PARAMETERS, 18);
        assertResponse(diag[2], INVALID_RESOURCE_PARAMETERS, 43);
        assertResponse(diag[3], "Empty data binding param value", 46);
        assertResponse(diag[4], "Invalid data binding param in the signature : expected 'naMe', but found 'name'", 55);
        assertResponse(diag[5], INVALID_RESOURCE_PARAMETERS, 61);
        assertResponse(diag[6], "Invalid data binding param in the signature : expected 'naMe', but found 'name'", 63);
        assertResponse(diag[7], INVALID_RESOURCE_PARAMETERS, 69);
    }

    private void assertResponse(Diagnostic diag, String msg, int line) {
        Assert.assertEquals(diag.message(), msg);
        Assert.assertEquals(diag.location().lineRange().startLine().line(), line);
    }
}
