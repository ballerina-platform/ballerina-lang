/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.blob;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

/**
 * Test Native functions in ballerina.lang.blobs.
 */
@SuppressWarnings("javadoc")
public class BlobTest {

    private CompileResult result;

    private static final String content = "This is a sample string";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/blob/blob-test.bal");
    }


    @Test(description = "Get string representation of blob")
    public void testToString() throws UnsupportedEncodingException {
        BValue[] args = {new BBlob(content.getBytes("UTF-8")), new BString("UTF-8")};
        BValue[] returns = BRunUtil.invoke(result, "toString", args);

        final String expected = content;
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

}
