package org.ballerinalang.langlib.testutils;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;

import java.util.List;

public class CTestRunner {

    public static void testRunner(CTestGroup testGroup){
        System.out.println("Running Test group " + testGroup.getName());
        List<CTests> tests = testGroup.getTests();

        for (CTests test: tests) {
            List<TestSteps> functionList = test.getTestFunctions();
             CompileResult compileResult = BCompileUtil.compile(test.getPath());

            for (TestSteps functiontorun: functionList) {
                BRunUtil.invoke(compileResult, functiontorun.getFunctionName());
            }
        }
    }
}
