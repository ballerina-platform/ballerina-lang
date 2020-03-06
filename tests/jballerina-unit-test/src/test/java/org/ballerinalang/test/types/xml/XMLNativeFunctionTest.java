/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.types.xml;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLNativeFunctionTest {

    private static final String ERROR_FAILED_TO_SLICE_XML_INDEX_OUT_OF_RANGE =
            "error: \\{ballerina/lang.xml\\}XMLOperationError message=Failed to slice xml: index out of range:";
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-native-functions.bal");
    }

    @Test
    public void testIsSingleton() {
        BValue[] returns = BRunUtil.invoke(result, "testIsSingleton");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testIsSingletonWithMultipleChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testIsSingletonWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

    }
    
    @Test
    public void testIsEmpty() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIsEmptyWithNoElementTextValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmptyWithNoElementTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIsEmptyWithMultipleChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmptyWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }
    
    @Test
    public void testGetItemType() {
        BValue[] returns = BRunUtil.invoke(result, "testGetItemType");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
        
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "comment");

        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[2].stringValue(), "element");
        
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "sequence");
    }

    @Test
    public void testGetItemTypeForElementWithPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testGetItemTypeForElementWithPrefix");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }

    @Test
    public void testGetItemTypeForElementWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetItemTypeForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }
    
    @Test
    public void testGetElementName() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementName");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

    @Test
    public void testGetElementNameWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementNameForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

     @Test
    public void testGetElementNameWithoutNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementNameForElementWithoutNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test/core}name");
    }
    
    @Test
    public void testGetTextValue() {
        BValue[] returns = BRunUtil.invoke(result, "testGetTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }

    @Test
    public void testGetTextValueDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetTextValueDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }
    
    @Test
    public void testGetElements() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElements");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsFromSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsFromSequence");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }
    
    @Test
    public void testGetElementsByName() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameWithPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameByPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameWithDifferentPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameByDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameEmptyNamespace() {
        //related issue 3062
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameEmptyNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameWithPrefixForDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNamePrefixForDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetElementsByNameWithDifferentNamespaces() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is element seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());

        // is element seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());

        // is element seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());

        // is element seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[5]).booleanValue());
    }
    
    @Test
    public void testGetChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetChildrenFromComplexXml() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildrenFromComplexXml");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }
    
    @Test
    public void testGetNonExistingChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testGetNonExistingChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }
    
    @Test
    public void testSelectChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testSelectChildrenWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testSelectChildrenPrefixedDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenPrefixedDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testSelectChildrenWtihSamePrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithSamePrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testSelectChildrenWtihDifferentPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testSelectChildrenWtihDifferentNamespaces() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals((returns[0]).size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals((returns[1]).size(), 1);

        // is children seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());

        // is children seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());

        // is children seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());

        // is children seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[5]).booleanValue());
    }
    
    @Test
    public void testConcat() {
        BValue[] returns = BRunUtil.invoke(result, "testConcat");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun" +
                "</fname><lname>setunga</lname></ns0:name><ns1:address xmlns:ns1=\"http://sample.com/test\">" +
                "<country>SL</country><city>Colombo</city></ns1:address>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }
    
    @Test
    public void testSetChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildren");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        
        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.getRefValue(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.getRefValue(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.getRefValue(2).stringValue(), "<newLname>setunga-new</newLname>");
    }

    @Test
    public void testSetChildrenWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenDefaultNamespace");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\"><fname>supun</fname>"
                + "<lname>setunga</lname><residency citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.getRefValue(0).stringValue(),
                "<fname xmlns=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(children.getRefValue(1).stringValue(),
                "<lname xmlns=\"http://sample.com/test\">setunga</lname>");
        Assert.assertEquals(children.getRefValue(2).stringValue(),
                "<residency xmlns=\"http://sample.com/test\" citizen=\"true\">true</residency>");

        // Check attribute value
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertEquals(returns[4].stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithDifferentNamespaceForAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithDifferentNamespaceForAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<name xmlns=\"http://sample.com/test\">" +
                        "<fname>supun</fname>" +
                        "<lname>setunga</lname>" +
                        "<residency xmlns:nsncdom=\"http://sample.com/test/code\" citizen=\"true\">true</residency>" +
                        "</name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithPrefixedAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithPrefixedAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\">" +
                "<fname>supun</fname><lname>setunga</lname><residency xmlns:pre=\"http://sample.com/test/code\" " +
                "pre:citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "true");
    }

    @Test
    public void testSetChildrenSameNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithSameNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDifferentNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithDifferentNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency xmlns:ns0=\"http://sample.com/test/code\" " +
                "ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffNamespaceWithoutPrefix() {
        //related issue 3074
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithDiffNamespaceWithoutPrefix");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<residency citizen=\"yes\">true</residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithAttributeDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(),
                "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname>" +
                        "<ns0:lname>setunga</ns0:lname>" +
                        "<ns0:residency xmlns:pre=\"http://sample.com/test/code\" pre:citizen=\"yes\">" +
                        "true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffElement() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithElementDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(),
                "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                        "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                        "<pre:residency xmlns:pre=\"http://sample.com/test/code\" ns0:citizen=\"yes\">" +
                        "true</pre:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[3].stringValue(), "yes");
    }

    @Test
    public void testCopy() {
        BValue[] returns = BRunUtil.invoke(result, "testCopy");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // Check children of the copied xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BValueArray children = ((BXMLSequence) ((BXML) returns[0]).children()).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.getRefValue(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.getRefValue(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.getRefValue(2).stringValue(), "<newLname>setunga-new</newLname>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        
        // Check children of the original xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BValueArray originalChildren = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(originalChildren.size(), 2);
        Assert.assertEquals(originalChildren.getRefValue(0).stringValue(),
                "<fname>supun</fname>");
        Assert.assertEquals(originalChildren.getRefValue(1).stringValue(),
                "<lname>setunga</lname>");
    }
    
    @Test
    public void testToString() {
        BValue[] returns = BRunUtil.invoke(result, "testToString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStrip() {
        BValue[] returns = BRunUtil.invoke(result, "testStrip");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book-->     <bookId>001</bookId> " +
                "<?word document=\"book.doc\" ?>");
        
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);
        Assert.assertEquals(returns[1].stringValue(), "<bookId>001</bookId>");
    }
    
    @Test
    public void testStripSingleton() {
        BValue[] returns = BRunUtil.invoke(result, "testStripSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<bookId>001</bookId>");
        Assert.assertEquals(returns[1].stringValue(), "<bookId>001</bookId>");
    }
    
    @Test
    public void testStripEmptySingleton() {
        BValue[] returns = BRunUtil.invoke(result, "testStripEmptySingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), " ");
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }
    
    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invoke(result, "testSlice");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 3);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName><bookId>001</bookId><bookAuthor>" +
                "Author01</bookAuthor>");
    }
    
    @Test
    public void testSliceAll() {
        BValue[] returns = BRunUtil.invoke(result, "testSliceAll");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.xml\\}XMLOperationError message=Failed to slice" +
                  " xml: invalid indices: 4 < 1.*")
    public void testSliceInvalidIndex() {
        BRunUtil.invoke(result, "testSliceInvalidIndex");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ERROR_FAILED_TO_SLICE_XML_INDEX_OUT_OF_RANGE + " \\[4,10\\].*")
    public void testSliceOutOfRangeIndex() {
        BValue[] params = new BValue[] { new BInteger(4), new BInteger(10) };
        BRunUtil.invoke(result, "testSliceOutOfRangeIndex", params);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ERROR_FAILED_TO_SLICE_XML_INDEX_OUT_OF_RANGE + " \\[-4,10\\].*")
    public void testSliceOutOfRangeNegativeStartIndex() {
        BValue[] params = new BValue[] { new BInteger(-4), new BInteger(10) };
        BRunUtil.invoke(result, "testSliceOutOfRangeIndex", params);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ERROR_FAILED_TO_SLICE_XML_INDEX_OUT_OF_RANGE + " \\[4,-10\\].*")
    public void testSliceOutOfRangeNegativeEndIndex() {
        BValue[] params = new BValue[] { new BInteger(4), new BInteger(-10) };
        BRunUtil.invoke(result, "testSliceOutOfRangeIndex", params);
    }

    @Test
    public void testSliceSingleton() {
        BValue[] returns = BRunUtil.invoke(result, "testSliceSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName>");
    }
    
    @Test
    public void testSeqCopy() {
        BValue[] returns = BRunUtil.invoke(result, "testSeqCopy");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>Updated Book ID</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }

    @Test
    public void testSetChildrenToElemntInDefaultNameSpace() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenToElemntInDefaultNameSpace");
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(),
                "<name xmlns=\"http://sample.com/test\"><newFname xmlns=\"\">supun-new</newFname></name>");
    }

    @Test
    public void testSelectChildrenWithEmptyNs() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectElementsWithEmptyNs() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectElementsWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectDescendants() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectDescendants");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(),
                "<name xmlns=\"http://ballerinalang.org/\"><name>Supun</name><lname>Setunga</lname></name>" +
                        "<name xmlns=\"http://ballerinalang.org/\">John</name>");
    }

    @Test
    public void testSelectDescendantsWithEmptyNs() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectDescendantsWithEmptyNs");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(),
                "<name><name>Supun</name><lname>Setunga</lname></name><name>John</name>");
    }

    @Test
    public void testSelectDescendantsFromSeq() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectDescendantsFromSeq");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 3);

        Assert.assertEquals(seq.stringValue(),
                "<name xmlns=\"http://ballerinalang.org/\"><name>Supun</name><lname>Setunga</lname></name>" +
                        "<name xmlns=\"http://ballerinalang.org/\">John</name>" +
                        "<name xmlns=\"http://ballerinalang.org/\">Doe</name>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*failed to add attribute " +
                    "'a:text'. prefix 'a' is already bound to namespace 'yyy'.*")
    public void testUpdateAttributeWithDifferentUri() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateAttributeWithDifferentUri");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name xmlns:a=\"yyy\" a:text=\"hello\"/>");
    }

    @Test
    public void testRemoveAttributeUsingStringName() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveAttributeUsingStringName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" " +
                        "xmlns:ns0=\"http://ballerina.com/aaa\" " +
                        "foo1=\"bar1\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test
    public void testRemoveAttributeUsinQName() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveAttributeUsinQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" xmlns:ns0=\"http://ballerina.com/aaa\" " +
                        "foo1=\"bar1\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test
    public void testRemoveNonExistingAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveNonExistingAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" " +
                        "xmlns:ns0=\"http://ballerina.com/aaa\" foo1=\"bar1\" " +
                        "ns0:foo1=\"bar2\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test(enabled = false)
    public void testGetChildrenOfSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildrenOfSequence");
        Assert.assertEquals(returns.length, 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<fname1>John</fname1><lname1>Doe</lname1><fname2>Jane</fname2><lname2>Doe</lname2>apple");
    }

    @Test
    public void testAddChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testAddChildren");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name><fname>John</fname><lname>Doe</lname><age>50</age>" +
                "<!-- unknown person -->marital status: unknown<city>Colombo</city><country>SL</country></name>");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<fname>John</fname><lname>Doe</lname>");
    }

    @Test
    public void testRemoveSingleChild() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveSingleChild");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<fname>John</fname><lname>Doe</lname>");
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<fname>John</fname><age>50</age>");
    }

    @Test
    public void testRemoveChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveChildren");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<name>John</name><name>Jane</name><age>50</age><name>Doe</name>");
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<age>50</age>");
    }

    @Test
    public void testRemoveChildrenWithNamesapces() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveChildrenWithNamesapces");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<name>John</name><ns0:name xmlns:ns0=\"http://wso2.com\">Foo</ns0:name><age>50</age><name>Doe</name>");
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<name>John</name><age>50</age><name>Doe</name>");
    }
    
    @Test
    public void testRemoveComplexChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveComplexChildren");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name>John</name><address><street>Palm Grove</street><city>" +
                "Colombo 03</city><country><name>Sri Lanka</name><code>LK</code></country></address><age>50</age>");
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<name>John</name><age>50</age>");
    }
    
    @Test
    public void testRemoveInnerChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveInnerChildren");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<name>John</name><address><street>Palm Grove</street><city>" +
                "Colombo 03</city><country><name>Sri Lanka</name></country></address><age>50</age>");
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<name>John</name><address><street>Palm Grove</street><city>" +
                "Colombo 03</city><country><name>Sri Lanka</name></country></address><age>50</age>");
    }

    @Test
    public void testXMLCharacterLiteralLength() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLLength");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1);
    }
}
