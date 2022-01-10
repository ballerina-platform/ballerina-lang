package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test cases for import user defined type.
 */
public class ImportUserDefinedTypeTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_import_type_with_null/test_import_project");

        this.result = BCompileUtil.compile("test-src/bala/test_projects/test_import_type_with_null/main.bal");
    }

    @Test
    public void importUserDefinedTypeWithNullTest() {
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
