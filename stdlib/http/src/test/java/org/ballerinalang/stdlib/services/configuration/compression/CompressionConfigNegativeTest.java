/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.stdlib.services.configuration.compression;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Negative Unit tests for 'Compression' service annotation.
 *
 * @since 0.982.0
 */
public class CompressionConfigNegativeTest {

    @Test(description = "Test the compilation error for invalid Compression state")
    public void testInvalidCompressionState() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/configuration/compression/invalid-compression-state.bal").getPath())
                                                                   .getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                            "incompatible types: expected '(AUTO|ALWAYS|NEVER)', found 'string'");
    }

    @Test(description = "Test the compilation error for multiple compression configs")
    public void testRedundantCompressionConfig() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/configuration/compression/redundant-compression-config.bal").getPath())
                                                                   .getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                "Invalid multiple configurations for compression");
    }

    @Test(description = "Test the compilation error for bogus content types")
    public void testBogusContentTypes() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/configuration/compression/bogus-content-type.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                            "Invalid Content-Type value for compression: 'hello=/#bal'");
    }

}
