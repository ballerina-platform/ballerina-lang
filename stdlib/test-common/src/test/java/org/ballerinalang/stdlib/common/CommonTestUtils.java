/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.common;

import org.apache.commons.logging.Log;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Common test utilities used in ballerina stdlib tests.
 *
 * @since 0.990.3
 */

public class CommonTestUtils {
    /**
     * Will identify the absolute path from the relative.
     *
     * @param relativePath the relative file path location.
     * @return the absolute path.
     */
    public static String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

    public static void assertJBytesWithBBytes(byte[] jBytes, byte[] bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes[i], jBytes[i], "Invalid byte value returned.");
        }
    }

    public static void printDiagnostics(CompileResult timerCompileResult, Log log) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.message() + " : " + e.location()));
    }
}
