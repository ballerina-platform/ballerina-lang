/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.xmlutils;

import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of xmlutils.
 *
 * @since 1.0
 */
public class XmlUtilsTest {

    private CompileResult result;
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/xmlutils_test.bal");
    }

    @Test
    public void testFromJSONFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testFromJSON");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.XML_TAG);
        Assert.assertEquals(returns[0].stringValue(), "<name>John</name><age>30</age>");
    }

    @Test
    public void testFromTableFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testFromTable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
            "<results>" +
                "<result>" +
                    "<id>1</id><age>30</age><salary>300.5</salary><name>Mary</name><married>true</married>" +
                "</result>" +
                "<result>" +
                    "<id>2</id><age>20</age><salary>300.5</salary><name>John</name><married>true</married>" +
                "</result>" +
                "</results>");
    }
}
