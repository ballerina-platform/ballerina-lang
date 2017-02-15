package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test type casting of map elements.
 */
public class MapTypeCastExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        bFile = ParserUtils.parseBalFile("lang/expressions/map-type-cast-expr.bal", symScope);
    }

    @Test(description = "Test type cast native types inside a map")
    public void testMapNativeTypeCastExpr() {
        int input = 123456789;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = Functions.invoke(bFile, "mapPrimitiveCastTest", args);

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
        BValue[] returns = Functions.invoke(bFile, "mapStructCastTest", args);

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
        BValue[] returns = Functions.invoke(bFile, "mapCastWithExprTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actualResult = returns[0].stringValue();
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test type casting invalid types",
        expectedExceptions = {BallerinaException.class},
        expectedExceptionsMessageRegExp = "Could not find any type mapper to cast Employee to  Person")
    public void testIncorrectMapCastExpr() {
        BallerinaFile invalidFile = ParserUtils.parseBalFile("lang/expressions/invalid-map-type-cast.bal");
        String name1 = "alex";
        String name2 = "kuru";
        BValue[] args = {new BString(name1), new BString(name2)};
        Functions.invoke(invalidFile, "invalidMapStructCastTest", args);
    }

}
