package org.wso2.ballerinalang.compiler;

import java.util.List;

/**
 * Listener interface.
 */
public interface Listener {
    void compileStarted();

    void executablesGenerated();

    void executableGenerated();

    void executableGenerated(String fileName);

    void packageCompiled(String pkgName);

    // Methods required for Testerina
    void testsCompleted();

    void testsCompiled();

    void testsFailed(String functionName, String error);

    void testsPassed(String functionName);

    void testsNotFound();

    void testsNotFoundInSuite();

    void testSuiteResultGenerated(int passed, int failed, int skipped);

    void noTestGroupsAvailable();

    void testGroupsAvailable(List<String> groupList);

    void beforeAndAfterTestsFailed(String errorMsg);

    void lineBreak();
}
