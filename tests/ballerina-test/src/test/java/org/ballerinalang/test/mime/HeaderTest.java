package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HeaderTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/header-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test whether the correct http header value is returned when the header exist as requested")
    public void testGetHeaderAsIs() {
        BString headerName = new BString("Content-Type");
        BString headerValue = new BString("application/json");
        BString headerNameToBeUsedForRetrieval = new BString("Content-Type");
        BValue[] args = {headerName, headerValue, headerNameToBeUsedForRetrieval};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/json");
    }

    @Test(description = "Test whether the case is ignored when dealing with http headers")
    public void testCaseInsensitivityOfHeaders() {
        BString headerName = new BString("content-type");
        BString headerValue = new BString("application/json");
        BString headerNameToBeUsedForRetrieval = new BString("ConTeNT-TYpE");
        BValue[] args = {headerName, headerValue, headerNameToBeUsedForRetrieval};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/json");
    }

    @Test(description = "Test adding multiple headers to entity")
    public void testAddingMultipleHeaders() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddingMultipleHeaders", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"header1\":[\"value1\"], \"header2\":[\"value2\"]," +
                " \"header3\":[\"value3\"]}");
    }

    @Test(description = "Test adding multiple values to same header")
    public void testAddingMultipleValuesToSameHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddingMultipleValuesToSameHeader", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "{\"heAder1\":[\"value1\", \"value2\", \"value3\"], " +
                "\"hEader2\":[\"value3\", \"value4\"]}");
        Assert.assertEquals(returns[1].stringValue(), "[\"value1\", \"value2\", \"value3\"]");
        Assert.assertEquals(returns[2].stringValue(), "value3");
    }

    @Test(description = "Test set header function")
    public void testSetHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeader", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "{\"HeADEr2\":[\"totally different value\"]}");
        Assert.assertNull(returns[1]);
        Assert.assertEquals(returns[2].stringValue(), "totally different value");
    }

    @Test(description = "Test set header after add header")
    public void testSetHeaderAfterAddheader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterAddHeader", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "{\"heAder1\":[\"value1\", \"value2\", \"value3\"], " +
                "\"HeADEr2\":[\"totally different value\"]}");
        Assert.assertEquals(returns[1].stringValue(), "[\"value1\", \"value2\", \"value3\"]");
        Assert.assertEquals(returns[2].stringValue(), "totally different value");
    }

    @Test(description = "Test add header after set header")
    public void testAddHeaderAfterSetheader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeaderAfterSetHeader", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "{\"heAder1\":[\"value1\", \"value2\", \"value3\"], " +
                "\"HeADEr2\":[\"totally different value\", \"value4\"]}");
        Assert.assertEquals(returns[1].stringValue(), "[\"totally different value\", \"value4\"]");
        Assert.assertEquals(returns[2].stringValue(), "totally different value");
    }

    @Test(description = "Test remove header function")
    public void testRemoveHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveHeader", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "{\"HeADEr2\":[\"totally different value\"]}");
        Assert.assertNull(returns[1]);
        Assert.assertEquals(returns[2].stringValue(), "totally different value");
    }

    @Test(description = "Test getting a value out of a non existence header")
    public void testNonExistenceHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testNonExistenceHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0].stringValue());
    }
}
