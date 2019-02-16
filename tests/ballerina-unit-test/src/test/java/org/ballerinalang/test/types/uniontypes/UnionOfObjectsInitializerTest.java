/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.uniontypes;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains test cases for object initialization when LHS is a union.
 */
public class UnionOfObjectsInitializerTest {
    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/uniontypes/object_type_union.bal");
        resultNegative = BCompileUtil.compile("test-src/types/uniontypes/object_type_union_negative.bal");
    }

    @Test(description = "Test initialization of object union")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInit() {
        BValue[] a = BRunUtil.invoke(result, "getObj4");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("val")).intValue(), 4);

        BValue[] ab = BRunUtil.invoke(result, "getObj0");
        BMap<String, BValue> bMap = (BMap<String, BValue>) ab[0];
        Assert.assertEquals(((BInteger) bMap.get("val")).intValue(), 0);

        BValue[] tupple = BRunUtil.invoke(result, "getLocals");
        BMap<String, BValue> localObj4 = (BMap<String, BValue>) tupple[0];
        Assert.assertEquals(((BInteger) localObj4.get("val")).intValue(), 4);

        BMap<String, BValue> localObj0 = (BMap<String, BValue>) tupple[1];
        Assert.assertEquals(((BInteger) localObj0.get("val")).intValue(), 0);

        BMap<String, BValue> localObj3 = (BMap<String, BValue>) tupple[2];
        Assert.assertEquals(((BInteger) localObj3.get("val")).intValue(), 3);
    }

    @Test(description = "Test initialization of object union with union members of mixed types")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInitMixedMemmber() {
        BValue[] a = BRunUtil.invoke(result, "getMixedUnionMembers");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("val")).intValue(), 0);
    }

    @Test()
    public void testNegativeUnionTypeInit() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "ambiguous type 'Obj|Obj2|Obj3|Obj4'", 48, 25);
        BAssertUtil.validateError(resultNegative, 1, "ambiguous type 'Obj|Obj2|Obj3|Obj4'", 49, 25);
    }
}
