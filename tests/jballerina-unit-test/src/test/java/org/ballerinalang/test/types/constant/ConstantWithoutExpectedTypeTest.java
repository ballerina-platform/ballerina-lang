package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Constant expression without expected type test cases.
 *
 * @since 2201.6.0
 */
public class ConstantWithoutExpectedTypeTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/constant_without_expected_type.bal");
    }

    @Test(dataProvider = "constantWithoutExpectedTypeFunctions")
    public void testConstantWithoutExpectedType(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider(name = "constantWithoutExpectedTypeFunctions")
    public Object[] constantWithoutExpectedTypeFunctions() {
        return new Object[] {
                "testConstAdditions",
                "testConstSubtracts",
                "testConstMultiplications",
                "testConstDivisions",
                "testConstGrouping",
                "testMapAccessReference",
                "testBitwiseConstExpressions",
                "testConstUnaryExpressions",
                "testConstRemainderOperation"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
