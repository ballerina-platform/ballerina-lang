package org.ballerinalang.test.types.string;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BRunUtil.IS_STRING_VALUE_PROP;

/**
 * Tests for the generateNewXML* functions for StringValue.
 */
public class StringValueXmlTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty(IS_STRING_VALUE_PROP, "true");
        result = BCompileUtil.compile("test-src/types/string/string-value-xml-test.bal");
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlComment() {
        testAndAssert("testXmlComment", 12);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlQName() {
        testAndAssert("testXmlQName", 13);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlText() {
        testAndAssert("testXmlText", 19);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlProcessingIns() {
        testAndAssert("testXmlProcessingIns", 12);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlStr() {
        testAndAssert("testXmlStr", 8);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testComplexXml() {
        testAndAssert("testComplexXml", 202);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlNamespace() {
        testAndAssert("testXmlNamespace", 334);
    }

    @Test (groups = "brokenOnJBallerina")
    public void testXmlInterpolation() {
        testAndAssert("testXmlInterpolation", 249);
    }

    private void testAndAssert(String funcName, int i) {
        BValue[] returns = BRunUtil.invoke(result, funcName);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), i);
    }

    @AfterClass
    public void down() {
        System.clearProperty(IS_STRING_VALUE_PROP);
    }
}
