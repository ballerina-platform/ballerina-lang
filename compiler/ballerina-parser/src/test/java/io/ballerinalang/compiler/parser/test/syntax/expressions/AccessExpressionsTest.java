/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing access expressions.
 * 
 * @since 2.0.0
 */
public class AccessExpressionsTest extends AbstractExpressionsTest {

    @Test
    public void testFieldAccessExpression() {
        test("x + a.b.c.d + y", "access-expr/field_access_expr_assert_01.json");
    }

    @Test
    public void testMethodCallExpression() {
        test("x + a.b().c.d() + y", "access-expr/field_access_expr_assert_02.json");
    }

    @Test
    public void testNestedMethodCallExpression() {
        test("x + a.b(p.q(s.t()) + r).c.d() + y", "access-expr/field_access_expr_assert_03.json");
    }

    @Test
    public void testSimpleMemberAccessExpression() {
        test("a[b]", "access-expr/member_access_expr_assert_01.json");
    }

    @Test
    public void testFieldAccessInMemberAccess() {
        test("x.y.z[a.b.c]", "access-expr/member_access_expr_assert_02.json");
    }

    @Test
    public void testComplexMemberAccessExpression() {
        test("x[y[z]].call()[3]", "access-expr/member_access_expr_assert_04.json");
    }

    @Test
    public void testSimpleAnnotAccess() {
        test("a .@ b", "access-expr/annot_access_expr_assert_01.json");
        test("a .@ b:c", "access-expr/annot_access_expr_assert_02.json");
    }

    @Test
    public void testAnnotAccess() {
        test("a + b.@c.@d.@e + f", "access-expr/annot_access_expr_assert_03.json");
        test("a + b .@c:d .@e:f .@g .h + k", "access-expr/annot_access_expr_assert_04.json");
    }

    @Test
    public void testMultiKeyedMemberAccess() {
        test("foo[a, 4, bar()]", "access-expr/member_access_expr_assert_08.json");
    }

    @Test
    public void testSimpleOptionalFieldAccess() {
        test("a ?. b", "access-expr/optional_field_access_expr_assert_01.json");
    }

    @Test
    public void testOptionalFieldAccess() {
        test("a + b ?.c ?.d ?.e + f", "access-expr/optional_field_access_expr_assert_02.json");
    }

    @Test
    public void testXMLOptionalAttributeAccess() {
        test("a ?. b:c", "access-expr/xml_optional_attribute_access_expr_assert_01.json");
        test("a + b ?.c:d ?.e:f ?.g .h + k", "access-expr/xml_optional_attribute_access_expr_assert_02.json");

    }

    @Test
    public void testXMLRequiredAttributeAccess() {
        test("a . b:c", "access-expr/xml_required_attribute_access_expr_assert_01.json");
        test("a + b .c:d .e:f .g .h + k", "access-expr/xml_required_attribute_access_expr_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testMethodCallRecovery() {
        test("x + a b() c.d() + y", "access-expr/field_access_expr_assert_04.json");
    }

    @Test
    public void testAccessExpressionRecovery() {
        test("x + a b() c. d", "access-expr/field_access_expr_assert_05.json");
    }

    @Test
    public void testMissingKeyExprInMemberAccess() {
        test("x[].y", "access-expr/member_access_expr_assert_03.json");
    }

    @Test
    public void testMissingCloseBracketInMemberAccess() {
        test("x[y[z].call()[3]", "access-expr/member_access_expr_assert_05.json");
    }

    @Test
    public void testMissingFieldNameInMemberAccess() {
        test("foo.bar.[baz]", "access-expr/member_access_expr_assert_06.json");
    }

    @Test
    public void testMissingFuncNameInMethodCall() {
        test("foo.bar.(baz)", "access-expr/member_access_expr_assert_07.json");
    }

    @Test
    public void testAnnotAccessWithInvalidAnnotTagReference() {
        test("a .@", "access-expr/annot_access_expr_assert_05.json");
        test("a .@:b", "access-expr/annot_access_expr_assert_06.json");
    }

    @Test
    public void testAnnotAccessWithMissingExpression() {
        test(".@ a", "access-expr/annot_access_expr_assert_07.json");
        test("{foo : .@ a }", "access-expr/annot_access_expr_assert_08.json");
        test("[foo, .@ a]", "access-expr/annot_access_expr_assert_09.json");
        test("let int a = .@ a in c", "access-expr/annot_access_expr_assert_10.json");
        test("from int a in b where .@ select .@", "access-expr/annot_access_expr_assert_11.json");
    }

    @Test
    public void testRecoveryInMultiKeyedMemberAccess() {
        test("foo[a, 4, ,bar()", "access-expr/member_access_expr_assert_09.json");
    }

    @Test
    public void testOptionalFieldAccessWithMissingFieldName() {
        test("a ?.", "access-expr/optional_field_access_expr_assert_03.json");
    }

    @Test
    public void testOptionalFieldAccessWithMissingExpression() {
        test("?. a;", "access-expr/optional_field_access_expr_assert_04.json");
        test("{foo : ?. a };", "access-expr/optional_field_access_expr_assert_05.json");
        test("[foo, ?. a];", "access-expr/optional_field_access_expr_assert_06.json");
        test("let int a = ?. b in c;", "access-expr/optional_field_access_expr_assert_07.json");
        test("from int a in b where ?. select ?. ;", "access-expr/optional_field_access_expr_assert_08.json");
    }
}
