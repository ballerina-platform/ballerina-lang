package org.ballerinalang.test.taintchecking.connectors;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test IO related natives for taint checking operations.
 */
public class IOTest {

    @Test
    public void testCharacterIO() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/connectors/character-io.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testCharacterIONegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/connectors/character-io-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'path'", 8, 43);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'accessMode'", 8, 53);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'numberOfChars'", 20, 40);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'sensitiveValue'", 22, 18);
    }
    
    @Test
    public void testFileIONegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/connectors/file-read-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 5);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'accessMode'", 6, 17);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'accessMode'", 8, 50);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'numberOfBytes'", 14, 34);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'offset'", 14, 42);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'sensitiveValue'", 16, 18);
    }
}
