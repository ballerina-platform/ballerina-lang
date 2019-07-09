/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.xslt;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

import static org.ballerinalang.stdlib.common.CommonTestUtils.getAbsoluteFilePath;

/**
 * Class to test XSL transformation.
 */
public class XSLTTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/read-from-file.bal");
    }

    @Test(description = "Test hub start up and URL identification")
    public void performXslt() throws URISyntaxException {
        String xmlFilePath = "datafiles/cd_catalog.xml";
        String xslFilePath = "datafiles/cd_catalog.xsl";

        BValue[] args = {new BString(getAbsoluteFilePath(xmlFilePath)), new BString(getAbsoluteFilePath(xslFilePath))};
        BValue[] returns = BRunUtil.invoke(compileResult, "readFromFile", args);
        Assert.assertNotNull(returns[0].stringValue());
//        Assert.assertEquals(returns[0].stringValue(), readFileContent(resourceToRead), "XML content mismatch.");

    }

//    @Test(description = "Test hub start up and URL identification")
    public void performSimpleTransform() throws URISyntaxException {
        BValue[] returns = BRunUtil.invoke(compileResult, "simpleTransform");
//        Assert.assertNotNull(returns[0].stringValue());
//        Assert.assertEquals(returns[0].stringValue(), readFileContent(resourceToRead), "XML content mismatch.");

    }
}
