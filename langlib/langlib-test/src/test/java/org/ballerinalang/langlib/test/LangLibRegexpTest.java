/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import io.ballerina.runtime.api.utils.StringUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for the lang.regexp library.
 *
 * @since 1.0
 */
public class LangLibRegexpTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/regexp_test.bal");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testFind() {
        BRunUtil.invoke(compileResult, "testFind");
    }

    @Test
    public void testFindGroups() {
        BRunUtil.invoke(compileResult, "testFindGroups");
    }

    @Test
    public void testFindAll() {
        BRunUtil.invoke(compileResult, "testFindAll");
    }

    @Test
    public void testMatchAt() {
        BRunUtil.invoke(compileResult, "testMatchAt");
    }

    @Test
    public void testMatchGroupsAt() {
        BRunUtil.invoke(compileResult, "testMatchGroupsAt");
    }

    @Test
    public void testIsFullMatch() {
        BRunUtil.invoke(compileResult, "testIsFullMatch");
    }

    @Test
    public void testFullMatchGroups() {
        BRunUtil.invoke(compileResult, "testFullMatchGroups");
    }


    public static void print(Object value) {
        System.out.println("############################");
        System.out.println(StringUtils.getStringValue(value, null));
    }
}
