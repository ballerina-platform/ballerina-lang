/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for selectively immutable values with the `readonly` type.
 *
 * @since 1.3.0
 */
public class SelectivelyImmutableTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/readonly/test_selectively_immutable_type.bal");
    }

    @Test(dataProvider = "immutableTypesTestFunctions")
    public void testImmutableTypes(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "immutableTypesTestFunctions")
    public Object[][] immutableTypesTestFunctions() {
        return new Object[][]{
                {"testSimpleInitializationForSelectivelyImmutableXmlTypes"},
                {"testSimpleInitializationForSelectivelyImmutableListTypes"},
                {"testSimpleInitializationForSelectivelyImmutableMappingTypes"},
                {"testSimpleInitializationForSelectivelyImmutableTableTypes"},
                {"testRuntimeIsTypeForSelectivelyImmutableBasicTypes"},
                {"testRuntimeIsTypeNegativeForSelectivelyImmutableTypes"},
                {"testImmutabilityOfNestedXmlWithAttributes"},
                {"testImmutableTypedRecordFields"},
                {"testImmutabilityForSelfReferencingType"},
                {"testImmutableRecordWithDefaultValues"},
                {"testImmutableObjects"},
                {"testImmutableJson"},
                {"testImmutableAnydata"},
                {"testImmutableAny"},
                {"testImmutableUnion"},
                {"testDefaultValuesOfFields"},
                {"testUnionReadOnlyFields"},
                {"testReadOnlyCastConstructingReadOnlyValues"},
                {"testReadOnlyCastConstructingReadOnlyValuesPropagation"},
                {"testValidInitializationOfReadOnlyClassIntersectionWithReadOnly"},
                {"testValidInitializationOfNonReadOnlyClassIntersectionWithReadOnly"},
                {"testFunctionWithReturnTypeAnyToReadonly"},
                {"testReadOnlyIntersectionWithNever"},
                {"testReadOnlyIntersectionWithNeverExplicitlyInType"},
                {"testReadOnlyIntersectionWithRecordThatHasAnOptionalNeverReadOnlyField"},
                {"testReadOnlyIntersectionWithRecordThatHasANeverReadOnlyRestField"},
                {"testTypeDefinitionForReadOnlyIntersectionWithBuiltinType"},
                {"testIsReadonlyWithInBuiltUnionType"},
                {"testReadOnlyIntersectionWithJsonAndAnydata"}
        };
    }

    @Test
    public void testImmutableTypesNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/types/readonly/test_selectively_immutable_type_negative.bal");
        int index = 0;

        // Assignment and initialization.
        validateError(result, index++, "incompatible types: expected 'Student', found '(Person & readonly)'", 41, 17);
        validateError(result, index++, "incompatible types: expected '(int|string)', found '(PersonalDetails & " +
                "readonly)'", 42, 20);
        validateError(result, index++, "incompatible types: expected 'Student', found '(Person & readonly)?'", 43, 17);
        validateError(result, index++, "incompatible types: expected 'string', found 'int'", 44, 16);
        validateError(result, index++, "incompatible types: expected '(Student & readonly)', found 'Student'", 57, 30);
        validateError(result, index++, "incompatible types: expected '(PersonalDetails & readonly)', found " +
                "'PersonalDetails'", 60, 18);
        validateError(result, index++, "incompatible types: expected '(ABAny & readonly)', found 'Obj'", 78, 26);
        validateError(result, index++, "incompatible types: expected 'anydata & readonly', found 'string[]'", 81, 28);
        validateError(result, index++, "incompatible types: expected 'any & readonly', found 'future'", 83, 24);
        validateError(result, index++, "incompatible types: expected '((Obj & readonly)|(int[] & readonly))', found " +
                "'string[]'", 85, 44);
        validateError(result, index++, "incompatible types: expected '(PersonalDetails & readonly)', found " +
                "'PersonalDetails'", 112, 18);
        validateError(result, index++, "incompatible types: expected '(Department & readonly)' for field 'dept', " +
                "found 'Department'", 113, 12);

        // Updates.
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 136, 5);
        validateError(result, index++, "cannot update 'readonly' record field 'details' in 'Employee'", 140, 5);
        validateError(result, index++, "incompatible types: expected '(Department & readonly)', found 'Department'",
                      145, 14);
        validateError(result, index++, "incompatible types: expected '(Department & readonly)', found 'Department'",
                      146, 17);

        validateError(result, index++, "invalid intersection type with 'readonly', 'table<Bar> key(name)' can never " +
                              "be 'readonly'", 159, 5);
        validateError(result, index++, "cannot infer type of the object from 'other'", 160, 26);
        validateError(result, index++, "invalid intersection type with 'readonly', 'Baz' can never be 'readonly'", 171,
                      5);
        validateError(result, index++, "cannot update 'readonly' value of type '(Config & readonly)'", 194, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'MyConfig'", 197, 5);

        validateError(result, index++, "invalid intersection type '(DEF & readonly)': no intersection", 201, 5);
        validateError(result, index++, "invalid intersection type with 'readonly', 'JKL' can never be 'readonly'", 209,
                5);
        validateError(result, index++, "invalid intersection type with 'readonly', 'JKL' can never be 'readonly'", 211,
                19);
        validateError(result, index++, "incompatible types: expected 'int[] & readonly', found 'int[]'", 230, 12);
        validateError(result, index++, "incompatible types: expected 'int[] & readonly', found 'int[]'", 231, 9);
        validateError(result, index++, "incompatible types: expected '(int[] & readonly)', found 'int[]'", 232, 12);
        validateError(result, index++, "incompatible types: expected 'int', found 'future<int>'", 238, 48);
        validateError(result, index++, "incompatible types: expected 'string', found 'stream<float>'", 242, 46);
        validateError(result, index++, "incompatible types: expected 'string', found 'stream<float>'", 242, 51);
        validateError(result, index++, "incompatible types: expected 'NeverImmutable', found 'readonly'", 244, 25);
        validateError(result, index++, "incompatible types: expected 'readonly', found 'future<int>'", 244, 40);

        validateError(result, index++, "cannot update 'readonly' value of type " +
                "'record {| readonly int i; (anydata & readonly)...; |} & readonly'", 259, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'object { final int j; } & readonly'",
                      262, 5);

        validateError(result, index++, "invalid intersection type with 'readonly', 'NeverReadOnlyClass' can never be " +
                "'readonly'", 276, 5);
        validateError(result, index++, "cannot initialize abstract object '(ReadOnlyClass & readonly)'", 279, 32);
        validateError(result, index++, "cannot initialize abstract object '(ReadOnlyClass & readonly)'", 282, 36);
        validateError(result, index++, "cannot initialize abstract object '(NonReadOnlyClass & readonly)'", 289, 35);
        validateError(result, index++, "cannot initialize abstract object '(NonReadOnlyClass & readonly)'", 292, 39);
        validateError(result, index++, "incompatible types: expected '(ReadOnlyClass & readonly)', found " +
                "'NonReadOnlyClass'", 298, 35);
        validateError(result, index++, "incompatible types: expected '(NonReadOnlyClass & readonly)', found " +
                "'NonReadOnlyClass'", 299, 38);

        validateError(result, index++, "cannot define a variable of type 'never' or equivalent to type 'never'",
                      303, 5);
        validateError(result, index++, "incompatible types: expected 'never?', found 'int'", 305, 52);

        validateError(result, index++, "invalid intersection type with 'readonly', 'Grault' can never be 'readonly'",
                      313, 5);

        validateError(result, index++, "incompatible types: expected 'never?', found 'stream<int>'", 321, 27);
        validateError(result, index++, "incompatible types: expected 'record {| never a?; |}', " +
                "found '(R1 & readonly)'", 322, 32);
        validateError(result, index++, "incompatible types: expected 'never', found 'int'", 331, 35);
        validateError(result, index++, "incompatible types: expected 'never', found 'stream<int>'", 331, 43);
        validateError(result, index++, "incompatible types: expected 'record {| never a?; |}', " +
                "found '(R2 & readonly)'", 332, 32);
        validateError(result, index++, "missing non-defaultable required record field 'a'", 333, 23);
        validateError(result, index++, "incompatible types: expected 'never', found 'int'", 333, 29);
        validateError(result, index++, "invalid intersection type with 'readonly', 'R3' can never be 'readonly'",
                      345, 6);
        validateError(result, index++, "invalid intersection type with 'readonly', 'R4' can never be 'readonly'",
                      346, 5);
        validateError(result, index++, "incompatible types: expected 'ImmutableXmlElement', found 'xml:Text'",
                      352, 29);
        validateError(result, index++, "incompatible types: expected 'ImmutableXmlElement', found 'xml:Element'",
                      355, 29);
        validateError(result, index++, "incompatible types: expected '(xml:Text|ImmutableXmlElement)', found " +
                "'xml:Element'", 363, 12);
        validateError(result, index++, "incompatible types: expected '(string|error)', found '(anydata & readonly)'",
                      372, 22);
        validateError(result, index++, "incompatible types: expected '(string|error)', found '(json & readonly)'", 375,
                      22);
        validateError(result, index++, "incompatible types: expected 'json & readonly', found '(anydata & readonly)'"
                , 384, 25);
        validateError(result, index++, "incompatible types: expected '(json & readonly)[]', found 'MyJson[]'", 403, 29);
        validateError(result, index++, "incompatible types: expected '(MyJson & readonly)[]', found 'json[]'", 405, 31);
        validateError(result, index++, "incompatible types: expected '(xml & readonly)[]', found " +
                "'(json & readonly)[]'", 407, 28);
        validateError(result, index++, "incompatible types: expected '(json & readonly)[]', found " +
                "'(xml & readonly)[]'", 408, 29);

        assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testInvalidImmutableValueUpdateViaLangLibMethod() {
        CompileResult result = BCompileUtil.compile(
                "test-src/types/readonly/test_selectively_immutable_type_langlib_negative.bal");
        int index = 0;

        // lang.array - array, tuple.
        validateError(result, index++, "cannot update 'readonly' value of type 'int[] & readonly'", 32, 14);
        validateError(result, index++, "cannot update 'readonly' value of type 'int[] & readonly'", 33, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'int[] & readonly'", 34, 5);
        validateError(result, index++, "cannot update 'readonly' value of type '[(Details & readonly),string...] & " +
                "readonly'", 35, 9);
        validateError(result, index++, "cannot update 'readonly' value of type 'int[] & readonly'", 36, 9);
        validateError(result, index++, "cannot update 'readonly' value of type '[(Details & readonly),string...] & " +
                "readonly'", 37, 25);
        validateError(result, index++, "cannot update 'readonly' value of type '[(Details & readonly),string...] & " +
                "readonly'", 38, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'int[] & readonly'", 39, 14);
        validateError(result, index++, "cannot update 'readonly' value of type '[(Details & readonly),string...] & " +
                "readonly'", 40, 5);

        // lang.map - map, record.
        validateError(result, index++, "cannot update 'readonly' value of type 'map<string> & readonly'", 47, 17);
        validateError(result, index++, "cannot update 'readonly' value of type '(Details & readonly)'", 48, 18);
        validateError(result, index++, "cannot update 'readonly' value of type 'map<string> & readonly'", 49, 5);
        validateError(result, index++, "cannot update 'readonly' value of type '(Details & readonly)'", 50, 5);

        // lang.table
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(map<anydata> & readonly)> & " +
                "readonly'", 64, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(Details & readonly)> key(name)" +
                " & readonly'", 65, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(Details & readonly)> key(name)" +
                " & readonly'", 66, 18);
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(Details & readonly)> key(name)" +
                " & readonly'", 67, 19);
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(map<anydata> & readonly)> & " +
                "readonly'", 68, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'table<(Details & readonly)> key(name)" +
                " & readonly'", 69, 5);

        // lang.value
        validateError(result, index++, "cannot update 'readonly' value of type 'map<(json & readonly)> & readonly'",
                      77, 20);
        validateError(result, index++, "cannot update 'readonly' value of type 'map<(json & readonly)> & readonly'",
                      78, 20);

        // lang.xml
        validateError(result, index++, "cannot update 'readonly' value of type 'xml:Element & readonly'", 85, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'xml:Element & readonly'", 86, 5);

        assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
