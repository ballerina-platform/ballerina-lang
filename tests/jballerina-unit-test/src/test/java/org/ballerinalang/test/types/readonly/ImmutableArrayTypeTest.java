package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImmutableArrayTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/readonly/test_immutable_array_type.bal");
    }

    @Test(dataProvider = "immutableArrayTypesTestFunctions")
    public void testImmutableArrayTypes(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "immutableArrayTypesTestFunctions")
    public Object[][] immutableArrayTypesTestFunctions() {
        return new Object[][]{
                {"testCastOfReadonlyIntArrayToByteArray"},
                {"testCastOfReadonlyIntArrayToByteArrayNegative"},
                {"testCastOfReadonlyAnyToByteArray"},
                {"testCastOfReadonlyArrayToUnion"},
                {"testCastOfReadonlyUnionArrayToByteArray"},
                {"testCastOfReadonlyRecord"},
                {"testCastOfReadonlyStringArrayToStringConstantArray"}
        };
    }

    @Test(expectedExceptions = org.ballerinalang.core.util.exceptions.BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: '\\(Foo & readonly\\)' cannot be cast to 'Bar'\"\\}.*")
    public void testCastOfReadonlyRecordNegative() {
        BRunUtil.invoke(result, "testCastOfReadonlyRecordNegative");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
