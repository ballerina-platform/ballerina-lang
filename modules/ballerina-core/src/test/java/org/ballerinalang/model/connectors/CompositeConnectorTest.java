package org.ballerinalang.model.connectors;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test the functionality of composite connectors
 */
public class CompositeConnectorTest {
    private ProgramFile programFile;

    @BeforeClass()
    public void setup() {
        //programFile = BTestUtils.getProgramFile("samples/connectors/composite-connector-test.bal");
    }

    @Test(description = "Test composite connector for load balancing")
    public void testCompositeConnector() {
        programFile = BTestUtils.getProgramFile("lang/connectors/composite-connector-test.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "URI1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "URI2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with internal filters for all sub connectors")
    public void testCompositeConnectorInternalFilterAll() {
        programFile = BTestUtils.getProgramFile("lang/connectors/composite-connector-internal-filter-test.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "XXXX";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "YYYY";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with external filter")
    public void testCompositeConnectorExternalFilterAll() {
        programFile = BTestUtils.getProgramFile("lang/connectors/composite-connector-external-filter-test.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "URI1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "URI2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    //@Test(description = "Test composite connector with external filter with base connector")
    public void testCompositeConnectorExternalFilterBaseAll() {
        programFile = BTestUtils.getProgramFile("lang/connectors/comp-conn-external-filter-base-test.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "LB1";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "LB2";
        Assert.assertEquals(value2.stringValue(), expected2);
    }

    @Test(description = "Test composite connector with internal filter and array based syntax")
    public void testCompositeConnectorInternalArray() {
        programFile = BTestUtils.getProgramFile("lang/connectors/composite-connector-internal-array.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 2);
        BString value1 = (BString) returns[0];
        BString value2 = (BString) returns[1];
        String expected = "XXXX";
        Assert.assertEquals(value1.stringValue(), expected);
        String expected2 = "2222222";
        Assert.assertEquals(value2.stringValue(), expected2);
    }
}
