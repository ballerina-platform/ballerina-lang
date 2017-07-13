package org.ballerinalang.connectors;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
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
        programFile = BTestUtils.getProgramFile("samples/connectors/composite-connector-test.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCompositeConnector");
        Assert.assertEquals(returns.length, 1);
        BInteger actionReturned = (BInteger) returns[0];
        int expected = 500;
        Assert.assertEquals(actionReturned.intValue(), expected);
    }
}
