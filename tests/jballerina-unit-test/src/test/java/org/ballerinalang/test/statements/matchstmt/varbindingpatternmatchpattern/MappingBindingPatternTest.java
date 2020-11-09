package org.ballerinalang.test.statements.matchstmt.varbindingpatternmatchpattern;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the var binding-pattern mapping-binding-pattern.
 *
 * @since 2.0.0
 */
@Test
public class MappingBindingPatternTest {
    private CompileResult result, restMatchPatternResult, resultNegative;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";
    private String unreachableCode = "unreachable code";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern/mapping-binding" +
                "-pattern.bal");
        restMatchPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping-binding-pattern-with-rest-binding-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping-binding-pattern-negative.bal");
    }

    @Test
    public void testMappingBindingPattern1() {
        BRunUtil.invoke(result, "testMappingBindingPattern1");
    }

    @Test
    public void testMappingBindingPattern2() {
        BRunUtil.invoke(result, "testMappingBindingPattern2");
    }

    @Test
    public void testMappingBindingPattern3() {
        BRunUtil.invoke(result, "testMappingBindingPattern3");
    }

    @Test
    public void testMappingBindingPattern4() {
        BRunUtil.invoke(result, "testMappingBindingPattern4");
    }

    @Test
    public void testMappingBindingPattern5() {
        BRunUtil.invoke(result, "testMappingBindingPattern5");
    }

    @Test
    public void testMappingBindingPattern6() {
        BRunUtil.invoke(result, "testMappingBindingPattern6");
    }

    @Test
    public void testMappingBindingPattern7() {
        BRunUtil.invoke(result, "testMappingBindingPattern7");
    }

    @Test
    public void testMappingBindingPattern8() {
        BRunUtil.invoke(result, "testMappingBindingPattern8");
    }

    @Test
    public void testMappingBindingPattern9() {
        BRunUtil.invoke(result, "testMappingBindingPattern9");
    }

    @Test
    public void testMappingBindingPattern10() {
        BRunUtil.invoke(result, "testMappingBindingPattern10");
    }

    @Test
    public void testMappingBindingPattern11() {
        BRunUtil.invoke(result, "testMappingBindingPattern11");
    }

    @Test
    public void testMappingBindingPattern12() {
        BRunUtil.invoke(result, "testMappingBindingPattern12");
    }

    @Test
    public void testMappingBindingPattern13() {
        BRunUtil.invoke(result, "testMappingBindingPattern13");
    }

    @Test
    public void testMappingBindingPattern14() {
        BRunUtil.invoke(result, "testMappingBindingPattern14");
    }

    @Test
    public void testMappingBindingPatternWithRest1() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest1");
    }

    @Test
    public void testMappingBindingPatternWithRest2() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest2");
    }

    @Test
    public void testMappingBindingPatternWithRest3() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest3");
    }

    @Test
    public void testMappingBindingPatternWithRest4() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest4");
    }

    @Test
    public void testMappingBindingPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 7);

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 20, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 27, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 38, 32);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 42, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 46, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 54, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 61, 9);
    }
}
