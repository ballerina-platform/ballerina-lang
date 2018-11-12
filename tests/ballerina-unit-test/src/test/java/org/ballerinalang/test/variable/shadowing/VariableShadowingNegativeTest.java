package org.ballerinalang.test.variable.shadowing;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests variable shadowing in Ballerina.
 *
 * @since 0.974.0
 */
public class VariableShadowingNegativeTest {

    @Test(description = "Test shadowed variables")
    public void testShadowedVariables() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/variable/shadowing/variable-shadowing-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 12);

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 7, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 10, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 15, 13);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 21, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 31, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 34, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 36, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 37, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 43, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 46, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 48, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 49, 9);
    }
}
