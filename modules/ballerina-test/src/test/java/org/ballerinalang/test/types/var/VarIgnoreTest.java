package org.ballerinalang.test.types.var;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VarIgnoreTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/types/var/var-ignore.bal");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerValue() {
        BValue[] returns = BTestUtils.invoke(result, "m", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }
}
