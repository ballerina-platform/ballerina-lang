package org.ballerinalang.test.worker;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class WorkerMultipleReceiveTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/worker-multiple-receive.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    /*
     * Happy path test cases for the Worker Multiple Receive feature.
     * */

    @Test
    public void recordOfTwoUnordered() {
        BValue[] returns = BRunUtil.invoke(result, "recordOfTwoUnordered", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{a:100, b:250}");
    }

    @Test
    public void recordOfTwoUnorderedWithSingleReceive() {
        BValue[] returns = BRunUtil.invoke(result, "recordOfTwoUnorderedWithSingleReceive", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{a:100, b:250}");
    }

    @Test
        public void singleStringMap() {
        BValue[] returns = BRunUtil.invoke(result, "singleStringMap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{w1:\"abc\"}");
    }

    @Test
    public void multipleStringMap() {
        BValue[] returns = BRunUtil.invoke(result, "multipleStringMap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{w1:\"100\", w3:\"200\", w4:\"300\"}");
    }

    @Test
    public void singleIntMap() {
        BValue[] returns = BRunUtil.invoke(result, "singleIntMap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{w1:150}");
    }

    @Test
    public void multipleIntMap() {
        BValue[] returns = BRunUtil.invoke(result, "multipleIntMap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{w1:50, w3:250, a:350, w5:500}");
    }

    @Test
    public void mixedTypesMap() {
        BValue[] returns = BRunUtil.invoke(result, "mixedTypesMap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);

        Assert.assertEquals(returns[0].stringValue(), "{c:100, d:\"350\"}");
    }

    /*
     * Negative test cases of the Worker Multiple Receive feature.
     * */

    @Test
    public void workerMultipleReceiveNonWorkerError() {
        CompileResult result = BCompileUtil.compile(
                "test-src/workers/worker-multiple-receive-non-worker-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "unsupported worker reference 'map'",
                9, 31);
    }


    @Test
    public void workerMultipleReceiveSymbolError() {
        CompileResult result = BCompileUtil.compile(
                "test-src/workers/worker-multiple-receive-symbol-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "undefined worker 'w1'",
                7, 31);
    }

    @Test
    public void workerMultipleReceiveIncompatibleTypesError() {
        CompileResult result = BCompileUtil.compile(
                "test-src/workers/worker-multiple-receive-incompatible-types-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'map<int>', found 'record {| string w1; |}'",
                11, 26);
    }

    @Test
    public void workerMultiReceiveIncompatibleTypesRecordError() {
        CompileResult result = BCompileUtil.compile(
                "test-src/workers/worker-multi-receive-incompatible-types-record-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'record {| int a; string b; |}', " +
                        "found 'record {| int a; int b; |}'",
                20, 16);
    }
}
