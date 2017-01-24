package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * This contains methods to test different behaviours of the if-else statement
 *
 * @since 1.0.0
 */
public class BreakStatementTest {
    
    @Test(description = "Test Break statment with, tested inner break generic break loop and innter break")
    public void testWhileStmtConditionTrue() {
        BallerinaFile bFile = ParserUtils.parseBalFile("lang/statements/break-stmt.bal");
        BValue[] args = { new BInteger(10), new BInteger(1) };
        BValue[] returns = Functions.invoke(bFile, "testBreakStatement1", args);
        Assert.assertEquals(returns.length, 1);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 40;
        Assert.assertEquals(actual, expected);
        
        returns = Functions.invoke(bFile, "testBreakStatement2", args);
        Assert.assertEquals(returns.length, 1);
        actual = ((BInteger) returns[0]).intValue();
        expected = 5;
        Assert.assertEquals(actual, expected);
    }
    
}
