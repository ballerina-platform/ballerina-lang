package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the readonly violation error consistency between BTuple type and BRecord type.
 *
 * @since 2201.9.0
 */
public class TupleVsArrayReadonlyErrorConsistencyTest {
    private CompileResult resultWithTupleUpdateMethod;
    private CompileResult resultWithoutTupleUpdateMethod;

    @BeforeClass
    public void setup() {
        resultWithTupleUpdateMethod =
                BCompileUtil.compile("test-src/types/readonly/test_tuple_vs_array_readonly_violation_consistency.bal");
        resultWithoutTupleUpdateMethod = BCompileUtil.compile(
                "test-src/types/readonly/test_tuple_vs_array_readonly_violation_consistency_without_tuple_Update_method.bal");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type '\\(Employee & readonly\\)'\".*")
    public void testWithTupleUpdateMethod() {
        BRunUtil.invoke(resultWithTupleUpdateMethod, "testFrozenAnyArrayElementUpdate");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type 'Employee & readonly'\".*")
    public void testWithoutTupleUpdateMethod() {
        BRunUtil.invoke(resultWithoutTupleUpdateMethod, "testFrozenAnyArrayElementUpdate");
    }

    @AfterClass
    public void tearDown() {
        resultWithTupleUpdateMethod = null;
        resultWithoutTupleUpdateMethod = null;
    }
}
