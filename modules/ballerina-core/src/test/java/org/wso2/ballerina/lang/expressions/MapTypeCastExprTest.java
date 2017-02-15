package org.wso2.ballerina.lang.expressions;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test type casting of map elements.
 */
public class MapTypeCastExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        Path programPath = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        bLangProgram = new BLangProgramLoader().loadLibrary(programPath,
                Paths.get("lang/expressions/map-type-cast-expr.bal"));
    }

    @Test(description = "Test type cast native types inside a map")
    public void testMapNativeTypeCastExpr() {
        int input = 123456789;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapPrimitiveCastTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, (double) input);
    }

    @Test(description = "Test type casting struct types of map elements")
    public void testMapStructTypeCastExpr() {
        String name1 = "alex";
        String name2 = "kuru";
        BValue[] args = {new BString(name1), new BString(name2)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapStructCastTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualResult = returns[0].stringValue();
        Assert.assertEquals(actualResult, name1);
    }

    @Test(description = "Test type casting map elements with an expression")
    public void testMapCastWithExpr() {
        int input = 123456789;
        String expectedResult = "output" + input;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapCastWithExprTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualResult = returns[0].stringValue();
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test type casting invalid types",
        expectedExceptions = {BallerinaException.class},
        expectedExceptionsMessageRegExp = "incompatible types: 'Employee' cannot be cast to  'Person'")
    public void testIncorrectMapCastExpr() {
        Path programPath = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        BLangProgram program = new BLangProgramLoader().loadLibrary(programPath,
                Paths.get("lang/expressions/invalid-map-type-cast.bal"));
        String name1 = "alex";
        String name2 = "kuru";
        BValue[] args = {new BString(name1), new BString(name2)};
        BLangFunctions.invoke(program, "invalidMapStructCastTest", args);
    }

}
