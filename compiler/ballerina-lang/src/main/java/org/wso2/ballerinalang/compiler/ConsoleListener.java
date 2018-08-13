package org.wso2.ballerinalang.compiler;

import java.io.PrintStream;
import java.util.List;

/**
 * Console Listener to write to the standard output.
 */
public class ConsoleListener implements Listener {
    private PrintStream outStream = System.out;

    @Override
    public void compileStarted() {
        outStream.println("Compiling source");
    }

    @Override
    public void executablesGenerated() {
        outStream.println("Generating executables");
    }

    @Override
    public void executableGenerated() {
        outStream.println("Generating executable");
    }

    @Override
    public void executableGenerated(String fileName) {
        outStream.println("    " + fileName);
    }

    @Override
    public void packageCompiled(String pkgName) {
        outStream.println("    " + pkgName);
    }

    @Override
    public void testsCompleted() {
        outStream.println("Running tests");
    }

    @Override
    public void testsCompiled() {
        outStream.println("Compiling tests");
    }

    @Override
    public void testsFailed(String functionName, String error) {
        outStream.println("\t[fail] " + functionName + ":");
        outStream.println("\t    " + error);
    }

    @Override
    public void testsPassed(String functionName) {
        outStream.println("\t[pass] " + functionName);
    }

    @Override
    public void testsNotFound() {
        outStream.println("    No tests found");
    }

    @Override
    public void testsNotFoundInSuite() {
        outStream.println("\tNo tests found\n");
    }

    @Override
    public void testSuiteResultGenerated(int passed, int failed, int skipped) {
        outStream.println("\t" + passed + " passing");
        outStream.println("\t" + failed + " failing");
        outStream.println("\t" + skipped + " skipped");
    }

    @Override
    public void noTestGroupsAvailable() {
        outStream.println("There are no groups available!");
    }

    @Override
    public void testGroupsAvailable(List<String> groupList) {
        outStream.println("Following groups are available : ");
        outStream.println(groupList);
    }

    @Override
    public void beforeAndAfterTestsFailed(String errorMsg) {
        outStream.println(errorMsg);
    }

    @Override
    public void lineBreak() {
        outStream.println();
    }
}
