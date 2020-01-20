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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLSequence;
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
        compileResult = BCompileUtil.compileOffline("test-src/read-from-file.bal");
    }

    @Test()
    public void readFromFile() throws URISyntaxException {
        String xmlFilePath = "datafiles/cd_catalog.xml";
        String xslFilePath = "datafiles/cd_catalog.xsl";

        BValue[] args = {new BString(getAbsoluteFilePath(xmlFilePath)), new BString(getAbsoluteFilePath(xslFilePath))};
        BValue[] returns = BRunUtil.invoke(compileResult, "readFromFile", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BXMLSequence);
        Assert.assertEquals(((BXMLSequence) returns[0]).getElementName().stringValue(), "html");
        Assert.assertEquals(((BXMLSequence) returns[0]).elements().children().children().size(), 5);
        Assert.assertEquals(((BXMLSequence) returns[0]).children().children().children().getItem(0).toString(),
                            "My CD Collection");
    }

    @Test()
    public void readFromSource() throws URISyntaxException {
        String xmlFilePath = "datafiles/source.xml";
        String xslFilePath = "datafiles/source.xsl";

        BValue[] args = {new BString(getAbsoluteFilePath(xmlFilePath)), new BString(getAbsoluteFilePath(xslFilePath))};
        BValue[] returns = BRunUtil.invoke(compileResult, "readFromFile", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BXMLSequence);
        Assert.assertEquals(((BXMLSequence) returns[0]).getElementName().stringValue(), "html");
        Assert.assertEquals(((BXMLSequence) returns[0]).children().children().children().getItem(0).toString(),
                            "Source Transform");
    }

    @Test()
    public void readMultiRootedXml() throws URISyntaxException {
        String xmlFilePath = "datafiles/cd_catalog.xml";
        String xslFilePath = "datafiles/cd_catalog_multi.xsl";

        BValue[] args = {new BString(getAbsoluteFilePath(xmlFilePath)), new BString(getAbsoluteFilePath(xslFilePath))};
        BValue[] returns = BRunUtil.invoke(compileResult, "readMultiRootedXml", args);
        Assert.assertTrue(returns[0] instanceof BXMLSequence);
        Assert.assertEquals(returns[0].stringValue(), "<html>\n<body>\n<h2>My CD Collection</h2>\n" +
                "<table border=\"1\">\n<tr bgcolor=\"#9acd32\">\n<th style=\"text-align:left\">Title</th><th " +
                "style=\"text-align:left\">Artist</th>\n</tr>\n<tr>\n<td>Empire Burlesque</td><td>Bob Dylan</td>\n" +
                "</tr>\n</table>\n</body>\n</html>");
    }

    @Test()
    public void directInvoke() throws URISyntaxException {
        BValue[] returns = BRunUtil.invoke(compileResult, "transform");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(((BError) returns[0]).getReason(), "{ballerina/xslt}XSLTError");
        Assert.assertTrue(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue()
                                  .contains("Unexpected character 'H' (code 72) in prolog; expected '<'"));
    }
}
