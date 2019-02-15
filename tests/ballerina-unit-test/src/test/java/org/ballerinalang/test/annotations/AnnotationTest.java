/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined annotations in ballerina.
 */
public class AnnotationTest {

    @BeforeClass
    public void setup() {
        BCompileUtil.compile(this, "test-src/lang/annotations", "lang.annotations.foo");
    }

    @Test(description = "Test constant annotation", enabled = false)
    public void testConstantAnnotation() {
//        if (varNode.symbol.flags == Flags.FINAL) {
//            PackageVarInfo varInfo = currentPkgInfo.pkgVarInfoMap.get(varNode.getName().getValue());
//
//            int annotationAttribNameIndex = addUTF8CPEntry(currentPkgInfo,
//                    AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE.value());
//            AnnotationAttributeInfo attributeInfo = new AnnotationAttributeInfo(annotationAttribNameIndex);
//            varNode.annAttachments.forEach(annt -> visitServiceAnnotationAttachment(annt, attributeInfo));
//            varInfo.addAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE, attributeInfo);
//        }
//
//        AnnotationAttributeInfo annotationInfo = (AnnotationAttributeInfo) compileResult.getProgFile()
//                .getEntryPackage().getPackageInfoEntries()[0]
//                .getAttributeInfo(AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);
//        AnnAttachmentInfo[] attachmentInfos = annotationInfo.getAttachmentInfoEntries();
//        String attributeValue = attachmentInfos[0].getAttributeValue("value").getStringValue();
//        Assert.assertEquals(attributeValue, "Constant holding the name of the current ballerina program");
    }

    @Test(description = "Test self annotating and annotation", enabled = false)
    public void testSelfAnnotating() {
        CompileResult bLangProgram = BCompileUtil.compile(this, "test-src/lang/annotations",
                "lang.annotations.doc1");
        // TODO Annotation definitions are not available complied program entry package
//        AnnotationAttachment[] annottations = bLangProgram.getProgFile().getEntryPackage().[0]
//                .getAnnotations();
//
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Self annotating an annotation");
//
//        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
//        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "first query param name");
    }

    @Test(description = "Test annotation attachment package valdation", enabled = false)
    public void testValidAnnoatationAttachmentPackage() {
        Assert.assertNotNull(BCompileUtil.compile(this, "test-src/lang/annotations",
                "lang.annotations.pkg.valid").getProgFile());
    }

    @Test(description = "Test constant as annotation attribute value", enabled = false)
    public void testConstAsAttributeValue() {
        Assert.assertNotNull(BCompileUtil
                .compile("test-src/lang/annotations/constant-as-attribute-value.bal").getProgFile());
    }

    // Negative tests

    @Test(description = "Test child annotation from a wrong package")
    public void testInvalidChildAnnotation() {
        CompileResult resNegative = BCompileUtil.compile("test-src/lang/annotations/invalid-child-annotation.bal");
        Assert.assertEquals(resNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resNegative, 0, "invalid usage of record literal with type 'anydata'", 1, 12);
        BAssertUtil.validateError(resNegative, 1, "missing non-defaultable required record field 'value'", 1, 6);
        BAssertUtil.validateError(resNegative, 2, "missing non-defaultable required record field 'prop'", 1, 6);
    }

    @Test(description = "Test array value for a non-array type attribute", enabled = false)
    public void testInvalidArrayValuedAttribute() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-array-valued-attribute.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected a 'string', found an array", 3, 1);
    }

    @Test(description = "Test non-array value for a array type attribute", enabled = false)
    public void testInvalidSingleValuedAttribute() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-single-valued-attribute.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'lang.annotations.doc1:QueryParam[]', " +
                        "found 'lang.annotations.doc1:QueryParam'", 3, 35);
    }

    @Test(description = "Test multi-typed attribute value array", enabled = false)
    public void testMultiTypedAttributeArray() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/multityped-attribute-array.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'lang.annotations.doc1:QueryParam', found 'string'",
                5, 42);

    }

    @Test(description = "Test an annotation attached in a wrong point", enabled = false)
    public void testWronglyAttachedAnnot() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/wrongly-attached-annot.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "annotation 'Bar' is not allowed in function", 7, 1);
    }

    @Test(description = "Test child annotation with an invalid attribute value", enabled = false)
    public void testInvalidInnerAttribute() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-inner-attributes.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 4, 16);
    }

    @Test(description = "Test an invalid service annotation", enabled = false)
    public void testInvalidServiceAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-service-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 4, 24);
    }

    @Test(description = "Test an invalid resource annotation", enabled = false)
    public void testInvalidResourceAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-resource-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 13, 28);
    }

    @Test(description = "Test an invalid connector annotation", enabled = false)
    public void testInvalidConnectorAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-connector-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 24);
    }

    @Test(description = "Test an invalid action annotation", enabled = false)
    public void testInvalidActionAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-action-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 6, 28);
    }

    @Test(description = "Test an invalid struct annotation", enabled = false)
    public void testInvalidStructAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-struct-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 24);
    }

    @Test(description = "Test an invalid constant annotation", enabled = false)
    public void testInvalidConstantAnnotation() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/invalid-constant-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 24);
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for given protocol package", enabled = false)
    public void testInvalidAttachmentInServiceWithDifferentProtocolPkg() {
        CompileResult resultNegative = BCompileUtil.compile(this, "test-src/lang/annotations",
                "lang.annotations.pkg.error1");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative,
                0, "annotation 'lang.annotations.pkg.first:Sample' is not allowed in " +
                        "service<lang.annotations.pkg.second>", 6, 1);
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for annotation def protocol package", enabled = false)
    public void testInvalidAttachmentInServiceWhenAttachPointIsDifferentPkg() {
        CompileResult resultNegative = BCompileUtil.compile(this, "test-src/lang/annotations",
                "lang.annotations.pkg.error2");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative,
                0, "annotation 'lang.annotations.pkg.first:SampleConfigSecond' is not allowed in " +
                        "service<lang.annotations.pkg.first>", 5, 1);
    }

    @Test(description = "Test global variable as annotation attribute value", enabled = false)
    public void testVariableAsAttributeValue() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/variable-as-attribute-value.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "annotation attribute value should be either constant reference or a basic literal",
                4, 1);
    }

    @Test(description = "Test type mismatch in annotation attribute value", enabled = false)
    public void testTypeMismatchInAttributeValue() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/lang/annotations/attribute-value-type-mismatch.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 4, 15);
    }

}
