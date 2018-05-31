package org.ballerinalang.test.variable.shadowing;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests variable shadowing in Ballerina.
 *
 * @since 0.972.1
 */
public class VariableShadowingTest {

    @Test(description = "Test private field access")
    public void testPrivateFieldAccess() {
        CompileResult compileResult = BCompileUtil.compile("test-src/variable/shadowing/variable-shadowing.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 4);

        BAssertUtil.validateError(compileResult, 0, "redeclared symbol 'name'", 3, 20);
        BAssertUtil.validateError(compileResult, 1, "redeclared symbol 'name'", 4, 9);
        BAssertUtil.validateError(compileResult, 2, "redeclared symbol 'name'", 7, 13);
        BAssertUtil.validateError(compileResult, 3, "redeclared symbol 'name'", 20, 9);
    }
}
