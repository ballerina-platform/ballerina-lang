package org.ballerinalang.test.typedefs;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DFS based type resolution
 */
public class TypeDefinitionDFSResolutionTest {

    private CompileResult recordCompileResult;

    @BeforeClass
    public void setup() {
        recordCompileResult = BCompileUtil.compile("test-src/typedefs/record_acyclic_case_1.bal");
    }

    @DataProvider(name = "FunctionListRecord")
    public Object[][] getTestRecordFunctions() {
        return new Object[][]{
                {"foo"},
        };
    }

    @Test(description = "Test DFS based record type definition resolutions", dataProvider = "FunctionListRecord")
    public void testRecordTypeDefinitions(String funcName) {
        BRunUtil.invoke(recordCompileResult, funcName);
    }

    @AfterClass
    public void tearDown() {
        recordCompileResult = null;
    }
}
