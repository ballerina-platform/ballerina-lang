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
    }

    @Test(description = "Test Basic annotation")
    public void testBasicAnnotation() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/foo/");
        AnnotationAttachment[] annottations = bLangProgram.getLibraryPackages()[0].getFunctions()[0].getAnnotations();
        
        String attributeValue = annottations[0].getAttribute("value").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "This is a test function");
        
        AnnotationAttributeValue firstElement = annottations[0].getAttribute("queryParamValue").getValueArray()[0];
        attributeValue = firstElement.getAnnotationValue().getAttribute("name").getLiteralValue().stringValue();
        Assert.assertEquals(attributeValue, "paramName");
        
        Assert.assertEquals(annottations[1].getValue(), "args: input parameter");
    }

    @Test(description = "Test child annotation from a wrong package",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-child-annotation.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:Args', found 'Args'")
    public void testInvalidChildAnnotation() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/invalid-child-annotation.bal");
    }
    
    @Test(description = "Test array value for a non-array type attribute",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-array-valued-attribute.bal:3: incompatible types: expected" +
                " a 'string', found an array")
    public void testInvalidArrayValuedAttribute() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/invalid-array-valued-attribute.bal");
    }
    
    @Test(description = "Test non-array value for a array type attribute",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-single-valued-attribute.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:QueryParam\\[\\]', found 'lang.annotations.doc:QueryParam'")
    public void testInvalidSingleValuedAttribute() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/invalid-single-valued-attribute.bal");
    }
    
    @Test(description = "Test multi-typed attribute value array",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "multityped-attribute-array.bal:3: incompatible types: expected " +
                "'lang.annotations.doc:QueryParam', found 'string'")
    public void testMultiTypedAttributeArray() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/multityped-attribute-array.bal");
    }
    
    @Test(description = "Test an annotation attached in a wrong point",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "wrongly-attached-annot.bal:7: annotation 'Bar' is not allowed in" +
                " functions")
    public void testWronglyAttachedAnnot() {
        bLangProgram = BTestUtils.parseBalFile("lang/annotations/wrongly-attached-annot.bal");
    }
    
}
