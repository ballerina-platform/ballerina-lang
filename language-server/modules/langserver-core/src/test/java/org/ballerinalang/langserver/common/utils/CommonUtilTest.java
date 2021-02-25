/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link CommonUtil}s.
 */
public class CommonUtilTest {

    /**
     * Tests the correctness of {@link CommonUtil#isValidIdentifier(String)} method.
     */
    @Test(dataProvider = "data-provider")
    public void testIsIdentifierValid(String identifier, boolean valid) {
        Assert.assertEquals(CommonUtil.isValidIdentifier(identifier), valid);
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"6name", false},
                {"varName", true},
                {"_varName", true},
                {"_varName\uD83D\uDE05", true},
                {"n@me", false},
                {"a1", true},
                {"'6name", true},
                {"name", true},
                {"_6", true},
                {"544", false},
                {"name]", false},
                {"_{name}", false},
                {"වාර්", true},
                {"\uE000", false},
                {"\uE001", false},
                {"\uF8FF", false},
                // In java we cannot specify unicode code points with more than 4 characters. Therefore, using
                // surrogate pairs
                {"\uDBBF\uDFFD", false},
                {"\uDBBF\uDFFD", false},
                {"\uDBC0\uDC01", false},
                {"\uDBFF\uDFFD", false},
                {"汉字", true},
                {"பெயர்", true},
        };
    }
}
