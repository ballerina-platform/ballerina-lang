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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for ballerina/http cookie success native functions.
 */
public class CookieNativeFunctionSuccessTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "services", "nativeimpl",
                                    "cookie");
        result = BCompileUtil.compile(sourceRoot.resolve("cookie-native-function.bal").toString());
    }

    @Test(description = "Test to add cookie with same domain and path values as in the request url , into cookie store")
    public void testAddCookieToCookieStore1() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieToCookieStore1");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add cookie coming from a sub domain of the cookie's domain value, into cookie store")
    public void testAddCookieToCookieStore2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieToCookieStore2");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add a host only cookie into cookie store")
    public void testAddCookieToCookieStore3() {

        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieToCookieStore3");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add cookie with unspecified path value, into cookie store")
    public void testAddCookieToCookieStore4() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieToCookieStore4");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add cookie coming from a sub directory of the cookie's path value, into cookie store")
    public void testAddCookieToCookieStore5() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookieToCookieStore5");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add a third party cookie into cookie store")
    public void testAddThirdPartyCookieToCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddThirdPartyCookieToCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add an array of cookies into cookie store")
    public void testAddCookiesToCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookiesToCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 2);
        BMap<String, BValue> bvalue1 = (BMap) returnVals[0];
        BMap<String, BValue> bvalue2 = (BMap) returnVals[1];
        Assert.assertEquals(bvalue1.get("name").stringValue(), "SID001");
        Assert.assertEquals(bvalue2.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add a similar cookie as in the store")
    public void testAddSimilarCookieToCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddSimilarCookieToCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
        Assert.assertEquals(bvalue.get("value").stringValue(), "6789mnmsddd34");
    }

    @Test(description = "Test to add cookies concurrently into cookie store")
    public void testAddCookiesConcurrentlyToCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddCookiesConcurrentlyToCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        // As all concurrently added cookies are same, only one cookie is in the cookie store.
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get the relevant cookie with same domain and path values as in the request url from " +
            "cookie store")
    public void testGetCookiesFromCookieStore1() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore1");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get the relevant cookie to a sub domain of the cookie's domain value from cookie " +
            "store")
    public void testGetCookiesFromCookieStore2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore2");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get a host only cookie to the relevant domain from cookie store")
    public void testGetCookiesFromCookieStore3() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore3");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get the relevant cookie to a sub directory of the cookie's path value from cookie " +
            "store")
    public void testGetCookiesFromCookieStore4() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore4");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get a cookie with unspecified path value to the relevant path from cookie store")
    public void testGetCookiesFromCookieStore5() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore5");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get cookies when both matched and unmatched cookies are available in the cookie " +
            "store.")
    public void testGetCookiesFromCookieStore6() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesFromCookieStore6");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get a secure cookie to a secure url from cookie store")
    public void testGetSecureCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetSecureCookieFromCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to remove a specific session cookie from the cookie store")
    public void testRemoveCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveCookieFromCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to remove all cookies from the cookie store")
    public void testRemoveAllCookiesInCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllCookiesInCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to add persistent cookie into cookie store")
    public void testAddPersistentCookieToCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddPersistentCookieToCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to add persistent cookie with a value below 69 for the year in expires attribute")
    public void testAddPersistentCookieToCookieStore_2() {
        BValue[] returnVals = BRunUtil.invoke(result, "testAddPersistentCookieToCookieStore_2");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get the relevant persistent cookie from the cookie store")
    public void testGetPersistentCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetPersistentCookieFromCookieStore");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to remove a specific persistent cookie from the cookie store")
    public void testRemovePersistentCookieFromCookieStore() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemovePersistentCookieFromCookieStore");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }

    @Test(description = "Test to get all cookies from the cookie store, which match the given cookie name")
    public void testGetCookiesByName() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesByName");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 1);
        BMap<String, BValue> bvalue = (BMap) returnVals[0];
        Assert.assertEquals(bvalue.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to get all cookies from the cookie store, which match the given cookie domain")
    public void testGetCookiesByDomain() {
        BValue[] returnVals = BRunUtil.invoke(result, "testGetCookiesByDomain");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the return values");
        Assert.assertTrue(returnVals.length == 2);
        BMap<String, BValue> bvalue1 = (BMap) returnVals[0];
        BMap<String, BValue> bvalue2 = (BMap) returnVals[1];
        Assert.assertEquals(bvalue1.get("name").stringValue(), "SID001");
        Assert.assertEquals(bvalue2.get("name").stringValue(), "SID002");
    }

    @Test(description = "Test to remove all cookies from the cookie store, which match the given cookie domain")
    public void testRemoveCookiesByDomain() {
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveCookiesByDomain");
        Assert.assertTrue(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                          "Cookie objects are in the Return Values");
    }
}
