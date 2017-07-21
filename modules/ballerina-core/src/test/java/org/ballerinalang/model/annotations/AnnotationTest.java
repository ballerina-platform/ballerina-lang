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
package org.ballerinalang.model.annotations;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.util.TestHTTPServiceDispatcher;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined annotations in ballerina.
 */
public class AnnotationTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        ServiceDispatcher dispatcher = new TestHTTPServiceDispatcher();
        DispatcherRegistry.getInstance().registerServiceDispatcher(dispatcher);
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/foo/");
    }

    @Test(description = "Test function annotation")
    public void testFunctionAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getFunctions()[0].getAnnotations();
        
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "This is a test function");
        
        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "paramName");
        
        Assert.assertEquals(annottations[1].getValue(), "test @Args annotation");
    }

    @Test(description = "Test function parameter annotation")
    public void testParameterAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getFunctions()[0]
                .getParameterDefs()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "args: input parameter : type string");
    }
    
    @Test(description = "Test service annotation")
    public void testServiceAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getServices()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Pizza service");
    }
    
    @Test(description = "Test resource annotation")
    public void testResourceAnnotation() {
        Resource orderPizzaResource = bLangProgram.getEntryPackage().getServices()[0].getResources()[0];
        AnnotationAttachment[] annottations = orderPizzaResource.getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Order pizza");
        
        String paramAnnotVal = orderPizzaResource.getParameterDefs()[0].getAnnotations()[0].getValue();
        Assert.assertEquals(paramAnnotVal, "input parameter for oderPizza resource");
    }
    
    @Test(description = "Test typemapper annotation")
    public void testTypemapperAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getTypeMappers()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Length to boolean type mapper");
    }
    
    @Test(description = "Test connector annotation")
    public void testConnectorAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConnectors()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Test connector");
    }
    
    @Test(description = "Test action annotation")
    public void testActionAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConnectors()[0].getActions()[0]
                .getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Test action of test connector");
    }
    
    @Test(description = "Test struct annotation")
    public void testStructAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getStructDefs()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "User defined struct : Person");
    }

    @Test(description = "Test constant annotation")
    public void testConstantAnnotation() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getConsts()[0].getAnnotations();
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Constant holding the name of the current ballerina program");
    }
    
    @Test(description = "Test self annotating and annotation")
    public void testSelfAnnotating() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/annotations/doc/");
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getAnnotationDefs()[0]
                .getAnnotations();
        
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "Self annotating an annotation");
        
        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "first query param name");
    }
    
    @Test(description = "Test annotation array")
    public void testAnnotationArray() {
        AnnotationAttachment[] annottations = bLangProgram.getEntryPackage().getFunctions()[0].getAnnotations();
        
        AnnotationAttributeValue[] annotationArray = annottations[0].getAttribute("queryParamValue").getValueArray();
        Assert.assertEquals(annotationArray.length, 3, "Wrong annotation array length");
        
        String attributeValue = annotationArray[0].getAnnotationValue().getAttribute("name").getLiteralValue()
                .stringValue();
        Assert.assertEquals(attributeValue, "paramName");
        
        attributeValue = annotationArray[1].getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "paramName2");
        
        attributeValue = annotationArray[2].getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "paramName3");
        
    }

    @Test(description = "Test annotation attachment package valdation")
    public void testValidAnnoatationAttachmentPackage() {
        BTestUtils.parseBalFile("lang/annotations/pkg/valid");
    }

    // Negative tests
    
    @Test(description = "Test child annotation from a wrong package",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-child-annotation.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:Args', found 'Args'")
    public void testInvalidChildAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-child-annotation.bal");
    }

    @Test(description = "Test array value for a non-array type attribute",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-array-valued-attribute.bal:3: incompatible types: expected" +
                " a 'string', found an array")
    public void testInvalidArrayValuedAttribute() {
        BTestUtils.parseBalFile("lang/annotations/invalid-array-valued-attribute.bal");
    }
    
    @Test(description = "Test non-array value for a array type attribute",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-single-valued-attribute.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:QueryParam\\[\\]', found 'lang.annotations.doc:QueryParam'")
    public void testInvalidSingleValuedAttribute() {
        BTestUtils.parseBalFile("lang/annotations/invalid-single-valued-attribute.bal");
    }
    
    @Test(description = "Test multi-typed attribute value array",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "multityped-attribute-array.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:QueryParam', found 'string'")
    public void testMultiTypedAttributeArray() {
        BTestUtils.parseBalFile("lang/annotations/multityped-attribute-array.bal");
    }
    
    @Test(description = "Test an annotation attached in a wrong point",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "wrongly-attached-annot.bal:7: annotation 'Bar' is not allowed in" +
                " function")
    public void testWronglyAttachedAnnot() {
        BTestUtils.parseBalFile("lang/annotations/wrongly-attached-annot.bal");
    }
    
    @Test(description = "Test child annotation with an invalid attribute value",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-inner-attributes.bal:4: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidInnerAttribute() {
        BTestUtils.parseBalFile("lang/annotations/invalid-inner-attributes.bal");
    }
    
    @Test(description = "Test an invalid service annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-service-annotation.bal:4: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidServiceAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-service-annotation.bal");
    }
    
    @Test(description = "Test an invalid resource annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-resource-annotation.bal:12: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidResourceAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-resource-annotation.bal");
    }
    
    @Test(description = "Test an invalid typemapper annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-typemapper-annotation.bal:3: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidTypeMapperAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-typemapper-annotation.bal");
    }
    
    @Test(description = "Test an invalid connector annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-connector-annotation.bal:3: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidConnectorAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-connector-annotation.bal");
    }
    
    @Test(description = "Test an invalid action annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-action-annotation.bal:6: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidActionAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-action-annotation.bal");
    }
    
    @Test(description = "Test an invalid struct annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-struct-annotation.bal:3: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidStructAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-struct-annotation.bal");
    }
    
    @Test(description = "Test an invalid constant annotation",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-constant-annotation.bal:3: incompatible types: expected " +
                "'string', found 'int'")
    public void testInvalidConstantAnnotation() {
        BTestUtils.parseBalFile("lang/annotations/invalid-constant-annotation.bal");
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for given protocol package",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang/annotations/pkg/error1/invalid-service1.bal:6: annotation " +
                    "'lang.annotations.pkg.first:Sample' is not allowed in service<lang.annotations.pkg.second>")
    public void testInvalidAttachmentInServiceWithDifferentProtocolPkg() {
        BTestUtils.parseBalFile("lang/annotations/pkg/error1");
    }

    @Test(description = "Test invalid annotation attachment for service where annotation attachment is only valid" +
            "for annotation def protocol package",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang/annotations/pkg/error2/invalid-service2.bal:5: annotation " +
                    "'lang.annotations.pkg.first:SampleConfigSecond' is not " +
                    "allowed in service<lang.annotations.pkg.first>")
    public void testInvalidAttachmentInServiceWhenAttachPointIsDifferentPkg() {
        BTestUtils.parseBalFile("lang/annotations/pkg/error2");
    }
    
    @Test(description = "Test default values for annotation")
    public void testDefaultValues() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/annotations/default-values.bal");
        AnnotationAttachment[] annotations = bLangProgram.getEntryPackage().getFunctions()[0].getAnnotations();
        
        // check for default values for basic literal attributes
        Assert.assertEquals(annotations[0].getAttribute("value").getLiteralValue().stringValue(),
            "Description of the service/function");

        // check for default values for non-literal attributes
        Assert.assertEquals(annotations[0].getAttribute("queryParamValue"), null);

        // check for default values for nested annotations
        AnnotationAttachment nestedArgAnnot = annotations[0].getAttribute("args").getAnnotationValue();
        Assert.assertEquals(nestedArgAnnot.getValue(), "default value for 'Args' annotation in doc package");

        // check for default values for nested annotations arrays
        AnnotationAttachment nestedAnnot = annotations[0].getAttribute("queryParamValue2").getValueArray()[0]
                .getAnnotationValue();
        Assert.assertEquals(nestedAnnot.getAttribute("name").getLiteralValue().stringValue(), "default name");
        Assert.assertEquals(nestedAnnot.getAttribute("value").getLiteralValue().stringValue(), "default value");

        // check for default values for a local annotations
        Assert.assertEquals(annotations[1].getValue(), "default value for local 'Args' annotation");

        BValue status = annotations[3].getAttribute("status").getLiteralValue();
        Assert.assertTrue(status instanceof BInteger);
        Assert.assertEquals(((BInteger) status).intValue(), 200);
    }
}
