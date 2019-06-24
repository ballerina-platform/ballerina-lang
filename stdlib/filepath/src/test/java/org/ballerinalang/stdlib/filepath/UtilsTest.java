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

package org.ballerinalang.stdlib.filepath;

import org.ballerinalang.jvm.values.ErrorValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.InvalidPathException;

import static org.ballerinalang.stdlib.filepath.Constants.ERROR_REASON_PREFIX;
import static org.ballerinalang.stdlib.filepath.Utils.UNKNOWN_MESSAGE;
import static org.ballerinalang.stdlib.filepath.Utils.UNKNOWN_REASON;

/**
 * Test utility functions in path module.
 *
 * @since 0.995.0
 */
public class UtilsTest {

    @Test
    public void testGetPathError() {
        InvalidPathException exp = new InvalidPathException("/User/ballerina/path\\test", "Invalid path format");
        String reason = "INVALID_PATH";

        // Get Path error with reason and throwable.
        ErrorValue error1 = Utils.getPathError(reason, exp);
        Assert.assertEquals(error1.getReason(), ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error1.getDetails().toString(),
                            "{\"message\":\"Invalid path format: /User/ballerina/path\\test\"}");

        // Get Path error without reason.
        ErrorValue error2 = Utils.getPathError(null, exp);
        Assert.assertEquals(error2.getReason(), ERROR_REASON_PREFIX + UNKNOWN_REASON);
        Assert.assertEquals(error2.getDetails().toString(),
                            "{\"message\":\"Invalid path format: /User/ballerina/path\\test\"}");

        // Get Path error without throwable.
        ErrorValue error3 = Utils.getPathError(reason, (Throwable) null);
        Assert.assertEquals(error3.getReason(), ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error3.getDetails().toString(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");

        // Get Path error without both reason and throwable.
        ErrorValue error4 = Utils.getPathError(null, (Throwable) null);
        Assert.assertEquals(error4.getReason(), ERROR_REASON_PREFIX + UNKNOWN_REASON);
        Assert.assertEquals(error4.getDetails().toString(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");

        // Get Path error without throwable message.
        Exception exp2 = new Exception();
        ErrorValue error5 = Utils.getPathError(reason, exp2);
        Assert.assertEquals(error5.getReason(), ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error5.getDetails().toString(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");
    }
}
