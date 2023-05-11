package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.ballerinalang.testerina.test.utils.CommonUtils.replaceExecutionTime;

public class TestparallelizationTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("parallelisation-test").toString();
    }

    @Test
    public void testParallelization() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "parallelisation-simple-test"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParallelization_w1.txt", output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "parallelisation-simple-test", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParallelization_w30.txt", output);

        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);

    }

    @Test
    public void testNonParallelizable() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "parallelisation-parallelizable"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParallelizable_w1.txt", output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "parallelisation-parallelizable", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParallelizable_w30.txt", output);

        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }


    @Test
    public void testParalallelizableTupleDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "parallelisation-tuple-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParalallelizableTupleDataProvider_w1.txt", output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "parallelisation-tuple-data-provider", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParalallelizableTupleDataProvider_w30.txt", output);

        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);
    }

    @Test
    public void testParalallelizableMapDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "parallelisation-map-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParalallelizableMapDataProvider_w1.txt", output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "parallelisation-map-data-provider", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testParalallelizableMapDataProvider_w30.txt", output);

        Assert.assertTrue(executionTimeW1 / 3 > executionTimeW30);
    }

    @Test
    public void testNonParalallelizableTupleDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "non-parallelisation-tuple-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParalallelizableTupleDataProvider_w1.txt",
//        output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "non-parallelisation-tuple-data-provider", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParalallelizableTupleDataProvider_w30.txt",
//        output);

        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    @Test
    public void testNonParalallelizableMapDataProvider() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"--workers=30", "non-parallelisation-map-data-provider"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW30 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParalallelizableMapDataProvider_w1.txt", output);


        args = mergeCoverageArgs(new String[]{"--workers=1", "parallelisation-map-data-provider", });
        output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Float executionTimeW1 = getTimeForTestExecution(output);
        output = replaceExecutionTime(output);
//        AssertionUtils.assertOutput("TestparallelizationTest-testNonParalallelizableMapDataProvider_w30.txt", output);

        Assert.assertTrue(executionTimeW1 - executionTimeW30 < 1000);
    }

    private float getTimeForTestExecution(String output) {
        int firstPos = output.indexOf("Test execution time :") + ("Test execution time :").length();
        int lastPos = output.indexOf("ms");
        String executionTime = output.substring(firstPos, lastPos);
        return Float.parseFloat(executionTime);
    }
}
