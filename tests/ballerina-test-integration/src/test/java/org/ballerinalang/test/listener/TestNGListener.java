package org.ballerinalang.test.listener;

import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.PrintStream;
import java.util.HashSet;

/**
 * Test listener for listening to test events
 *
 * @since 0.965.0
 */
public class TestNGListener extends TestListenerAdapter {

    private static final HashSet<String> processedTestCases = new HashSet<>();

    @Override
    public void beforeConfiguration(ITestResult tr) {
        PrintStream printStream = new PrintStream(System.out);
        String testClassName = tr.getTestClass().getRealClass().getSimpleName();

        if (tr.getMethod().isBeforeClassConfiguration() && !processedTestCases.contains(testClassName)) {
            printStream.println("\n// Start Running " + testClassName + " ...\n");
            processedTestCases.add(testClassName);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testCase = result.getName();
        LoggerFactory.getLogger(result.getTestClass().getRealClass()).info("Test running: " + testCase);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass()).info("Test successful: " + testCase);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass()).info("Test skipped: " + testCase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        String testCase = tr.getName();
        Throwable e = tr.getThrowable();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass()).error(
                "Test failed: " + testCase + "-> " + e.getMessage());
    }
}
