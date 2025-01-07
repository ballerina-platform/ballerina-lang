package org.ballerinalang.test.typechecker;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * Tests the optimizations done for large BDD operations.
 */
public class BddOptimizationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(Path.of("test-src/typechecker/bdd_optimizations.bal").toString());
    }

    @Test
    public void test() {
        BRunUtil.invoke(compileResult, "test");
    }
}
