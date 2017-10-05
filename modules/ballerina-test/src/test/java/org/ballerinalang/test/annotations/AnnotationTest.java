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


//import org.ballerinalang.model.AnnotationAttachment;
//import org.ballerinalang.model.AnnotationAttributeValue;
//import org.ballerinalang.model.BLangProgram;
//import org.ballerinalang.model.Resource;

//import org.ballerinalang.model.values.BInteger;
//import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.test.utils.BTestUtils;
//import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.test.utils.TestHTTPServiceDispatcher;

//import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
//import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined annotations in ballerina.
 */
public class AnnotationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        ServiceDispatcher dispatcher = new TestHTTPServiceDispatcher();
        DispatcherRegistry.getInstance().registerServiceDispatcher(dispatcher);
        //compileResult = BTestUtils.compile("lang/annotations/foo");
    }

    @Test(description = "Test function annotation")
    public void testFunctionAnnotation() {
//        AnnotationAttributeInfo info = compileResult.getProgFile().getEntryPackage().getFunctionInfoEntries()[1].getAttributeInfoEntries()[0]
//
//        compileResult.getProgFile();
//        AnnotationAttachment[] annottations = compileResult.getProgFile().getEntryPackage().getFunctionInfoEntries()[1].getAttributeInfoEntries()[0];
//
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "This is a test function");
//
//        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
//        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "paramName");
//
//        Assert.assertEquals(annottations[1].getValue(), "test @Args annotation");
    }

//    @Test(description = "Test function parameter annotation")
//    public void testParameterAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getFunctions()[0]
//                .getParameterDefs()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "args: input parameter : type string");
//    }
//
//    @Test(description = "Test service annotation")
//    public void testServiceAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getServices()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Pizza service");
//    }
//
//    @Test(description = "Test resource annotation")
//    public void testResourceAnnotation() {
//        Resource orderPizzaResource = bLangProgram.getEntryPackage().getServices()[0].getResources()[0];
//        AnnotationAttachment[] annottations = orderPizzaResource.getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Order pizza");
//
//        String paramAnnotVal = orderPizzaResource.getParameterDefs()[0].getAnnotations()[0].getValue();
//        Assert.assertEquals(paramAnnotVal, "input parameter for oderPizza resource");
//    }
//
//    @Test(description = "Test typemapper annotation")
//    public void testTypemapperAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getTypeMappers()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Length to boolean type mapper");
//    }
//
//    @Test(description = "Test connector annotation")
//    public void testConnectorAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConnectors()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Test connector");
//    }
//
//    @Test(description = "Test action annotation")
//    public void testActionAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConnectors()[0].getActions()[0]
//                .getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Test action of test connector");
//    }
//
//    @Test(description = "Test struct annotation")
//    public void testStructAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getStructDefs()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "User defined struct : Person");
//    }
//
//    @Test(description = "Test constant annotation")
//    public void testConstantAnnotation() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConsts()[0].getAnnotations();
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Constant holding the name of the current ballerina program");
//    }
//
//    @Test(description = "Test self annotating and annotation")
//    public void testSelfAnnotating() {
//        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/annotations/doc/");
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getAnnotationDefs()[0]
//                .getAnnotations();
//
//        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "Self annotating an annotation");
//
//        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
//        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "first query param name");
//    }
//
//    @Test(description = "Test annotation array")
//    public void testAnnotationArray() {
//        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getFunctions()[0].getAnnotations();
//
//        AnnotationAttributeValue[] annotationArray = annottations[0].getAttribute("queryParamValue").getValueArray();
//        Assert.assertEquals(annotationArray.length, 3, "Wrong annotation array length");
//
//        String attributeValue = annotationArray[0].getAnnotationValue().getAttribute("name").getLiteralValue()
//                .stringValue();
//        Assert.assertEquals(attributeValue, "paramName");
//
//        attributeValue = annotationArray[1].getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "paramName2");
//
//        attributeValue = annotationArray[2].getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
//        Assert.assertEquals(attributeValue, "paramName3");
//
//    }
//
    @Test(description = "Test annotation attachment package valdation")
    public void testValidAnnoatationAttachmentPackage() {
        Assert.assertNotNull(BTestUtils.compile("lang/annotations/pkg/valid").getProgFile());
    }

    @Test(description = "Test constant as annotation attribute value")
    public void testConstAsAttributeValue() {
        Assert.assertNotNull(BTestUtils.compile("lang/annotations/constant-as-attribute-value.bal").getProgFile());
    }

    // Negative tests

    @Test(description = "Test child annotation from a wrong package")
    public void testInvalidChildAnnotation() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/invalid-child-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0, "incompatible types: expected 'lang.annotations.doc:Args', " +
                "found 'Args'", 3, 23);
    }

    @Test(description = "Test array value for a non-array type attribute")
    public void testInvalidArrayValuedAttribute() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/invalid-array-valued-attribute.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0, "incompatible types: expected a 'string', found an array", 3, 0);
    }

    @Test(description = "Test non-array value for a array type attribute")
    public void testInvalidSingleValuedAttribute() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/invalid-single-valued-attribute.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'lang.annotations.doc:QueryParam[]', " +
                        "found 'lang.annotations.doc:QueryParam'", 3, 34);
    }

    @Test(description = "Test multi-typed attribute value array")
    public void testMultiTypedAttributeArray() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/multityped-attribute-array.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'lang.annotations.doc:QueryParam', found 'string'", 5, 41);

    }

    @Test(description = "Test an annotation attached in a wrong point")
    public void testWronglyAttachedAnnot() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/wrongly-attached-annot.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "annotation 'Bar' is not allowed in function", 7, 0);
    }

    @Test(description = "Test child annotation with an invalid attribute value")
    public void testInvalidInnerAttribute() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/invalid-inner-attributes.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 4, 15);
    }

    @Test(description = "Test an invalid service annotation")
    public void testInvalidServiceAnnotation() {
        CompileResult resultNegative = BTestUtils.compile("lang/annotations/invalid-service-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 5, 23);
    }

    @Test(description = "Test an invalid resource annotation")
    public void testInvalidResourceAnnotation() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/invalid-resource-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 13, 27);
    }

    @Test(description = "Test an invalid connector annotation")
    public void testInvalidConnectorAnnotation() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/invalid-connector-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 23);
    }

    @Test(description = "Test an invalid action annotation")
    public void testInvalidActionAnnotation() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/invalid-action-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 6, 27);
    }

    @Test(description = "Test an invalid struct annotation")
    public void testInvalidStructAnnotation() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/invalid-struct-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 23);
    }

    @Test(description = "Test an invalid constant annotation")
    public void testInvalidConstantAnnotation() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/invalid-constant-annotation.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 3, 23);
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for given protocol package")
    public void testInvalidAttachmentInServiceWithDifferentProtocolPkg() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/pkg/error1");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative,
                0, "annotation 'lang.annotations.pkg.first:Sample' is not allowed in " +
                        "service<lang.annotations.pkg.second>", 6, 0);
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for annotation def protocol package")
    public void testInvalidAttachmentInServiceWhenAttachPointIsDifferentPkg() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/pkg/error2");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative,
                0, "annotation 'lang.annotations.pkg.first:SampleConfigSecond' is not allowed in " +
                        "service<lang.annotations.pkg.first>", 5, 0);
    }

    @Test(description = "Test global variable as annotation attribute value")
    public void testVariableAsAttributeValue() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/variable-as-attribute-value.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "annotation attribute value should be either constant reference or a basic literal", 4, 0);
    }

    @Test(description = "Test type mismatch in annotation attribute value")
    public void testTypeMismatchInAttributeValue() {
        CompileResult resultNegative =  BTestUtils.compile("lang/annotations/attribute-value-type-mismatch.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BTestUtils.validateError(resultNegative, 0,
                "incompatible types: expected 'string', found 'int'", 4, 14);
    }
//
//    @Test(description = "Test default values for annotation")
//    public void testDefaultValues() {
//        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/annotations/default-values.bal");
//        AnnotationAttachment[] annotations = bLangProgram.getEntryPackage().getFunctions()[0].getAnnotations();
//
//        // check for default values for basic literal attributes
//        Assert.assertEquals(annotations[0].getAttribute("value").getLiteralValue().stringValue(),
//                "Description of the service/function");
//
//        // check for default values for non-literal attributes
//        Assert.assertEquals(annotations[0].getAttribute("queryParamValue"), null);
//
//        // check for default values for nested annotations
//        AnnotationAttachment nestedArgAnnot = annotations[0].getAttribute("args").getAnnotationValue();
//        Assert.assertEquals(nestedArgAnnot.getValue(), "default value for 'Args' annotation in doc package");
//
//        // check for default values for nested annotations arrays
//        AnnotationAttachment nestedAnnot = annotations[0].getAttribute("queryParamValue2").getValueArray()[0]
//                .getAnnotationValue();
//        Assert.assertEquals(nestedAnnot.getAttribute("name").getLiteralValue().stringValue(), "default name");
//        Assert.assertEquals(nestedAnnot.getAttribute("value").getLiteralValue().stringValue(), "default value");
//
//        // check for default values for a local annotations
//        Assert.assertEquals(annotations[1].getValue(), "default value for local 'Args' annotation");
//
//        BValue status = annotations[3].getAttribute("status").getLiteralValue();
//        Assert.assertTrue(status instanceof BInteger);
//        Assert.assertEquals(((BInteger) status).intValue(), 200);
//    }
}
