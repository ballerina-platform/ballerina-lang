package org.ballerinalang.model.arraysofarrays;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user arrays of arrays in ballerina.
 */
public class ArraysOfArraysTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/arraysofarrays/arraysOfArrays.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testValueAssignmentAndRetrieval() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "valueAssignmentAndRetrieval");
        Assert.assertEquals(returns[0].stringValue(), "3");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayInitializationAndRetrieval() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayInitializationAndRetrieval");
        Assert.assertEquals(returns[0].stringValue(), "1");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayToArrayAssignment() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayToArrayAssignment");
        Assert.assertEquals(returns[0].stringValue(), "9");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayTest() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarray");
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayValueAccess() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarrayValueAccess");
        Assert.assertEquals(returns[0].stringValue(), "99");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayStringValueAccess() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "threeDarrayStringValueAccess");
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayFunctionCalltest() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "twoDarrayFunctionCalltest");
        Assert.assertEquals(returns[0].stringValue(), "4");
    }

    @Test(description = "Test setting incorrect type",
          expectedExceptions = {BallerinaException.class})
    public void testAssignIncorrectValue() {
        bLangProgram = BTestUtils.parseBalFile("lang/arraysofarrays/arraysOfArraysFailures.bal");
    }
}
