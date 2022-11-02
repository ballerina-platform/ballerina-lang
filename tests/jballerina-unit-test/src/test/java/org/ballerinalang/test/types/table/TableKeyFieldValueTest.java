/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.table;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test table key field value.
 *
 * @since 2.0.0
 */
public class TableKeyFieldValueTest {
    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_key_field_value_test.bal");
        negativeResult = BCompileUtil.compile("test-src/types/table/table-key-field-value-negative.bal");
    }

    @Test(dataProvider = "dataToTestTableKeyFieldValue", description = "Test table key field value")
    public void testTableKeyFieldValue(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestTableKeyFieldValue() {
        return new Object[]{
                "testLiteralAsKeyValue",
                "testStringTemplateExprAsKeyValue",
                "testXmlTemplateExprAsKeyValue",
                "testListConstructorExprAsKeyValue",
                "testTableConstructorExprAsKeyValue",
                "testMappingConstructorExprAsKeyValue",
                "testConstRefExprAsKeyValue",
                "testTypeCastExprAsKeyValue",
                "testUnaryExprAsKeyValue",
                "testMultiplicativeExprAsKeyValue",
                "testAdditiveExprAsKeyValue",
                "testShiftExprAsKeyValue",
                "testRelationalExprAsKeyValue",
                "testIsExprAsKeyValue",
                "testEqualityExprAsKeyValue",
                "testBinaryBitwiseExprAsKeyValue",
                "testLogicalExprAsKeyValue",
                "testConditionalExprAsKeyValue",
                "testGroupExprAsKeyValue",
                "testKeyCollision"
        };
    }

    @Test
    public void testDuplicateKeysInTableConstructorExpr() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 50, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 55, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 60, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 65, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 70, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 75, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 76, 9);
        BAssertUtil.validateError(negativeResult, index++, "using default values for key " +
                "fields in table types is not yet supported", 77, 9);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }
}
