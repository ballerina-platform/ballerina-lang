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
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test functionality of xmlutils.
 *
 * @since 1.0
 */
public class XmlUtilsTest {
    @Test
    public void testToJsonFunction() {
        CompileResult result = BCompileUtil.compile("test-src/xml-to-json-test.bal");
        BValue[] returns = BRunUtil.invoke(result, "testToJson");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMapType) returns[0].getType()).getConstrainedType().getTag(), TypeTags.JSON_TAG);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
    }
}
