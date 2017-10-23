package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test the functionality of composite connectors
 */
public class CompositeConnectorTest {

    private CompileResult result;

    @Test(description = "Test composite connector for load balancing")
    public void testCompositeConnector() {
        result = BCompileUtil.compile("test-src/connectors/composite-connector-test.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "URI1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "URI2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with internal filters for all sub connectors", enabled = false)
    public void testCompositeConnectorInternalFilterAll() {
        result = BCompileUtil.compile("test-src/connectors/composite-connector-internal-filter-test.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "XXXX";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "YYYY";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with external filter", enabled = false)
    public void testCompositeConnectorExternalFilterAll() {
        result = BCompileUtil.compile("test-src/connectors/composite-connector-external-filter-test.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "URI1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "URI2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with external filter with base connector", enabled = false)
    public void testCompositeConnectorExternalFilterBaseAll() {
        result = BCompileUtil.compile("test-src/connectors/comp-conn-external-filter-base-test.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "LB1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "LB2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with internal filter and array based syntax", enabled = false)
    public void testCompositeConnectorInternalArray() {
        result = BCompileUtil.compile("test-src/connectors/composite-connector-internal-array.bal");
        BValue[] returns = BRunUtil.invoke(result, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "XXXX";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "2222222";
        Assert.assertEquals(value2.stringValue(), expected2);
    }
}
