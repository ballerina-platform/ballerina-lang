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

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[35m";
    private static final String ANSI_BOLD = "\u001B[1m";

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
        LoggerFactory.getLogger(result.getTestClass().getRealClass().getSimpleName()).info(
                ANSI_BOLD + testCase + ": " + ANSI_CYAN + ANSI_BOLD + "RUNNING" + ANSI_RESET);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass().getSimpleName()).info(
                ANSI_BOLD + testCase + ": " + ANSI_GREEN + ANSI_BOLD + "PASSED" + ANSI_RESET);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        String testCase = tr.getName();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass()).info(
                ANSI_BOLD + testCase + ": " + ANSI_YELLOW + ANSI_BOLD + "SKIPPED" + ANSI_RESET);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        String testCase = tr.getName();
        Throwable e = tr.getThrowable();
        LoggerFactory.getLogger(tr.getTestClass().getRealClass())
                .error(ANSI_BOLD + testCase + ": " + ANSI_RED + ANSI_BOLD + "FAILED" + " -> " + e.getMessage() +
                               ANSI_RESET);
    }
}
