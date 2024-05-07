/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.test.internal.types.semtype;

import io.ballerina.runtime.api.types.SemType.SubType;
import io.ballerina.runtime.internal.types.semtype.BBooleanSubType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BBooleanSubTypeTests {

    private static final BBooleanSubType TRUE = BBooleanSubType.from(true);
    private static final BBooleanSubType FALSE = BBooleanSubType.from(false);

    @Test
    public static void testSimpleUnion() {
        SubType res = TRUE.union(FALSE);
        Assert.assertTrue(res.isAll());
        Assert.assertFalse(res.isNothing());
        Assert.assertFalse(res.isNothing());

        res = FALSE.union(TRUE);
        Assert.assertTrue(res.isAll());
        Assert.assertFalse(res.isNothing());
        Assert.assertFalse(res.isNothing());
    }

    @Test
    public static void testSimpleIntersection() {
        SubType res = TRUE.intersect(FALSE);
        Assert.assertFalse(res.isAll());
        Assert.assertTrue(res.isEmpty());
        Assert.assertTrue(res.isNothing());

        res = FALSE.intersect(TRUE);
        Assert.assertFalse(res.isAll());
        Assert.assertTrue(res.isEmpty());
        Assert.assertTrue(res.isNothing());

        res = TRUE.intersect(TRUE);
        Assert.assertFalse(res.isAll());
        Assert.assertFalse(res.isEmpty());
        Assert.assertFalse(res.isNothing());
    }

    @Test
    public static void testSimpleDiff() {
        SubType res = TRUE.diff(FALSE);
        Assert.assertFalse(res.isAll());
        Assert.assertFalse(res.isEmpty());
        Assert.assertFalse(res.isNothing());

        res = TRUE.diff(TRUE);
        Assert.assertFalse(res.isAll());
        Assert.assertTrue(res.isEmpty());
        Assert.assertTrue(res.isNothing());

        SubType all = TRUE.union(FALSE);
        res = all.diff(TRUE);
        Assert.assertFalse(res.isAll());
        Assert.assertFalse(res.isEmpty());
        Assert.assertFalse(res.isNothing());
    }

    @Test
    public static void testSimpleComplement() {
        SubType all = TRUE.union(FALSE);
        SubType nothing = all.complement();
        Assert.assertTrue(nothing.isNothing());

        SubType res = TRUE.complement();
        Assert.assertFalse(res.isAll());
        Assert.assertFalse(res.isEmpty());
        Assert.assertFalse(res.isNothing());

        SubType otherNothing = TRUE.intersect(FALSE);
        Assert.assertTrue(otherNothing == nothing); // Boolean subtype is interned
    }
}
