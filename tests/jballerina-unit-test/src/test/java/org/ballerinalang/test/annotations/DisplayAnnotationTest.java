/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.annotations;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;

import java.util.List;

/**
 * Class to test display annotation.
 *
 * @since 2.0
 */
public class DisplayAnnotationTest {

    private CompileResult result;
    private CompileResult negative;

    @BeforeClass
    public void setup() {
        negative = BCompileUtil.compile("test-src/annotations/display_annot_negative.bal");
        result = BCompileUtil.compile("test-src/annotations/display_annot.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Compilation contain errors");
    }

    @Test
    public void testDisplayAnnotOnFunction() {
        BLangFunction fooFunction = (BLangFunction) ((List) ((BLangPackage) result.getAST()).functions).get(0);
        BLangAnnotationAttachment annot = (BLangAnnotationAttachment) ((List) fooFunction.annAttachments).get(0);
        Assert.assertEquals(getActualExpressionFromAnnotationAttachmentExpr(annot.expr).toString(),
                " {iconPath: fooIconPath.icon,label: Foo function}");
    }

    @Test
    public void testDisplayAnnotOnServiceDecl() {
        BLangService service = (BLangService) result.getAST().getServices().get(0);
        BLangAnnotationAttachment attachment = service.getAnnotationAttachments().get(0);
        Assert.assertEquals(getActualExpressionFromAnnotationAttachmentExpr(attachment.expr).toString(),
                " {iconPath: service.icon,label: service,misc: <anydata> Other info}");
    }

    @Test
    public void testDisplayAnnotOnObjectAndMemberFunction() {
        ClassDefinition clz = result.getAST().getClassDefinitions().get(0);
        List<? extends AnnotationAttachmentNode> objAnnot = clz.getAnnotationAttachments();
        Assert.assertEquals(objAnnot.size(), 1);
        Assert.assertEquals(objAnnot.get(0).getExpression().toString(),
                " {iconPath: barIconPath.icon,label: Bar class}");

        List<BLangAnnotationAttachment> attachedFuncAttachments =
                ((BLangClassDefinition) clz).functions.get(0).annAttachments;
        String annotAsString =
                getActualExpressionFromAnnotationAttachmentExpr(attachedFuncAttachments.get(0).getExpression())
                        .toString();
        Assert.assertEquals(annotAsString, " {label: k method}");
    }

    @Test void testDisplayAnnotationNegative() {
        BAssertUtil.validateError(negative, 0, "cannot specify more than one annotation value " +
                "for annotation 'ballerina/lang.annotations:0.0.0:display'", 17, 1);
        Assert.assertEquals(negative.getErrorCount(), 1);
    }

    private BLangExpression getActualExpressionFromAnnotationAttachmentExpr(BLangExpression expression) {
        if (expression.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            BLangTypeConversionExpr expr = (BLangTypeConversionExpr) expression;
            if (expr.getKind() == NodeKind.INVOCATION) {
                return ((BLangInvocation) expr.expr).expr;
            }
        }
        if (expression.getKind() == NodeKind.INVOCATION) {
            return ((BLangInvocation) expression).expr;
        }
        return expression;
    }
}
