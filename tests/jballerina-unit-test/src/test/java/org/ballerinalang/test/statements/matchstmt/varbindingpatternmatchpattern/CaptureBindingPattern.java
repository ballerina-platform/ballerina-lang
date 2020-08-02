package org.ballerinalang.test.statements.matchstmt.varbindingpatternmatchpattern;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CaptureBindingPattern {
    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture-binding" +
                "-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/capture" +
                "-binding-pattern-negative.bal");
    }

    @Test
    public void testCaptureBindingPattern1() {
        BRunUtil.invoke(result, "testCaptureBindingPattern1");
    }

    @Test
    public void testCaptureBindingPattern2() {
        BRunUtil.invoke(result, "testCaptureBindingPattern2");
    }

    @Test
    public void testCaptureBindingPattern3() {
        BRunUtil.invoke(result, "testCaptureBindingPattern3");
    }

    @Test
    public void testCaptureBindingPatternNegative1() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "unreachable pattern", 22, 9);
    }
}
