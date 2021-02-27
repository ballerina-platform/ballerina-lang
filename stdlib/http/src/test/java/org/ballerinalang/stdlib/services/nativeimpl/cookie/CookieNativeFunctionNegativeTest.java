/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.nativeimpl.cookie;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina/HTTP inbound response negative native functions.
 */
public class CookieNativeFunctionNegativeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        String basePath = "test-src/services/nativeimpl/cookie/";
        result = BCompileUtil.compile(basePath + "cookie-native-function-negative.bal");
    }

    @Test(description = "Test to add a cookie with unmatched domain to the cookie store")
    public void testAddCookieWithUnmatchedDomain() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieWithUnmatchedDomain");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to add a cookie with unmatched path to the cookie store")
    public void testAddCookieWithUnmatchedPath() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieWithUnmatchedPath");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to add a similar cookie as in the cookie store coming from a non-http request url, but " +
            "existing old cookie is http only")
    public void testAddSimilarCookie() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddSimilarCookie");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the Return Values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
        Assert.assertEquals(bvalue.get("value").stringValue(), "239d4dmnmsddd34");
    }

    @Test(description = "Test to add a http only cookie coming from a non-http url")
    public void testAddHttpOnlyCookie() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHttpOnlyCookie");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a secure only cookie to unsecured request url")
    public void testGetSecureCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetSecureCookieFromCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a http only cookie to non-http request url")
    public void testGetHttpOnlyCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetHttpOnlyCookieFromCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a host only cookie to a sub-domain from the cookie store ")
    public void testGetCookieToUnmatchedDomain1() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookieToUnmatchedDomain1");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a cookie to unmatched request domain")
    public void testGetCookieToUnmatchedDomain2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookieToUnmatchedDomain2");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a cookie to unmatched request path")
    public void testGetCookieToUnmatchedPath1() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookieToUnmatchedPath1");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get a cookie with unspecified path to unmatched request path")
    public void testGetCookieToUnmatchedPath2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookieToUnmatchedPath2");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to remove a specific cookie which is not in the cookie store when persistent cookie " +
            "handler is not configured")
    public void testRemoveCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveCookieFromCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the Return Values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add cookies more than the number in maxTotalCookieCount in cookie configuration")
    public void testCheckMaxTotalCookieCount() {
        BValue[] returnVals = BRunUtil.invoke(result, "testCheckMaxTotalCookieCount");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 2);
        BMap<String, BValue> bvalue1 = (BMap) returnVals[0];
        BMap<String, BValue> bvalue2 = (BMap) returnVals[1];
        Assert.assertEquals(bvalue1.get("name").stringValue(), "SID001");
        Assert.assertEquals(bvalue2.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add cookies more than the number in maxCookiesPerDomain in cookie configuration")
    public void testCheckMaxCookiesPerDomain() {
        BValue[] returnVals = BRunUtil.invoke(result, "testCheckMaxCookiesPerDomain");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 2);
        BMap<String, BValue> bvalue1 = (BMap) returnVals[0];
        BMap<String, BValue> bvalue2 = (BMap) returnVals[1];
        Assert.assertEquals(bvalue1.get("name").stringValue(), "SID001");
        Assert.assertEquals(bvalue2.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to give invalid file extension when creating a CsvPersistentCookieHandler object")
    public void testAddPersistentCookieWithoutPersistentStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddPersistentCookieWithoutPersistentStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to remove a specific cookie which is not in the cookie store, when there is a " +
            "persistent cookie store",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*KeyNotFound \\{\"message\":\"cannot find key.*")
    public void testRemovePersistentCookieFromCookieStore_1() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemovePersistentCookieFromCookieStore_1");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the Return Values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to remove a specific cookie which is not in the cookie store, when there is no " +
            "persistent cookie store")
    public void testRemovePersistentCookieFromCookieStore_2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemovePersistentCookieFromCookieStore_2");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to remove all cookies when there is no persistent cookie store")
    public void testRemoveAllCookiesFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllCookiesFromCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }
}
