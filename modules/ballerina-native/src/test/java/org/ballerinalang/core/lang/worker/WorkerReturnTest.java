package org.ballerinalang.core.lang.worker;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for usages of return statements within worker
 */
public class WorkerReturnTest {
    private ProgramFile bProgramFile;

    @BeforeClass
    public void setup() {
        bProgramFile = BTestUtils.getProgramFile("samples/worker-return-test.bal");
    }

    @Test(description = "Test returning from default worker")
    public void testReturnDefault() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnDefault", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test returning from non-default worker")
    public void testReturnWorker() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnWorker", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test returning from multiple workers")
    public void testReturnMultipleWorkers() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnMultipleWorkers", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
    }

    @Test(description = "Test returning void from multiple workers")
    public void testReturnVoidMultipleWorkers() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnVoidMultipleWorkers", args);
        Assert.assertEquals(returns.length, 0);
    }

    @Test(description = "Test negative condition of no worker returns",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "worker-return-negative-test.bal:3: " +
                    "missing return statement")
    public void testReturnWorkerNegative() {
        bProgramFile = BTestUtils.getProgramFile("samples/worker-return-negative-test.bal");
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnWorkerNegative", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test negative condition of no worker returns",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "worker-return-negative-condition-test.bal:3: " +
                    "missing return statement")
    public void testReturnWorkerNegativeCondition() {
        bProgramFile = BTestUtils.getProgramFile("samples/worker-return-negative-condition-test.bal");
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testReturnWorkerNegativeCondition", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }
}
