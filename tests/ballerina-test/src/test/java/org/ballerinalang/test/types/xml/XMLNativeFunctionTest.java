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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLNativeFunctionTest {

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
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
    }

    @Test
    public void testIsSingletonWithMultipleChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testIsSingletonWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);

    }
    
    @Test
    public void testIsEmpty() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithNoElementTextValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmptyWithNoElementTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithMultipleChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testIsEmptyWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }

    @Test
    public void testGetElementsFromSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsFromSequence");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetElementsByName() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameByPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDifferentPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNameByDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefixForDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testGetElementsByNamePrefixForDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
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
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is element seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is element seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is element seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
    
    @Test
    public void testGetChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetChildrenFromComplexXml() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildrenFromComplexXml");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetNonExistingChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testGetNonExistingChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSelectChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWithDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenPrefixedDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenPrefixedDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihSamePrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithSamePrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentNamespaces() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectChildrenWithDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is children seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is children seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is children seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is children seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<fname xmlns=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(children.get(1).stringValue(), "<lname xmlns=\"http://sample.com/test\">setunga</lname>");
        Assert.assertEquals(children.get(2).stringValue(),
                "<residency xmlns=\"http://sample.com/test\" citizen=\"true\">true</residency>");

        // Check attribute value
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[4]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithEmptyNamespace() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenEmptyNamespace");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\"><fname>supun</fname>"
                + "<lname>setunga</lname><residency xmlns=\"\" citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<fname xmlns=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(children.get(1).stringValue(), "<lname xmlns=\"http://sample.com/test\">setunga</lname>");
        Assert.assertEquals(children.get(2).stringValue(), "<residency citizen=\"true\">true</residency>");

        // Check attribute value
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[4]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithDifferentNamespaceForAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithDifferentNamespaceForAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\">" +
                "<fname>supun</fname><lname>setunga</lname><residency xmlns:nsncdom=\"http://sample.com/test/code\" " +
                "nsncdom:citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
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
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffNamespaceWithoutPrefix() {
        //related issue 3074
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithDiffNamespaceWithoutPrefix");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<residency xmlns:citizen=\"yes\">true</residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithAttributeDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency pre:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffElement() {
        BValue[] returns = BRunUtil.invoke(result, "testSetChildrenWithElementDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<pre:residency ns0:citizen=\"yes\">true</pre:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
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
        BRefValueArray children = ((BXMLSequence) ((BXML) returns[0]).children()).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children of the original xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray originalChildren = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(originalChildren.size(), 2);
        Assert.assertEquals(originalChildren.get(0).stringValue(),
                "<fname xmlns:ns0=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(originalChildren.get(1).stringValue(),
                "<lname xmlns:ns0=\"http://sample.com/test\">setunga</lname>");
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
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 4);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book-->     <bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
        
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 3);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
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
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
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
            expectedExceptionsMessageRegExp = ".*error, message: failed to slice xml: " +
                    "invalid indices: 4 < 1.*")
    public void testSliceInvalidIndex() {
        BRunUtil.invoke(result, "testSliceInvalidIndex");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error, message: failed to slice xml: " +
                    "index out of range: \\[4,10\\].*")
    public void testSliceOutOfRangeIndex() {
        BValue[] params = new BValue[] { new BInteger(4), new BInteger(10) };
        BRunUtil.invoke(result, "testSliceOutOfRangeIndex", params);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error, message: failed to slice xml: "
                    + "index out of range: \\[-4,10\\].*")
    public void testSliceOutOfRangeNegativeStartIndex() {
        BValue[] params = new BValue[] { new BInteger(-4), new BInteger(10) };
        BRunUtil.invoke(result, "testSliceOutOfRangeIndex", params);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error, message: failed to slice xml: "
                    + "index out of range: \\[4,-10\\].*")
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
    public void testToJsonForValue() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonForValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testToJsonForEmptyValue() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonForEmptyValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[]");
    }

    @Test
    public void testToJsonForComment() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonForComment");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForPI() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonForPI");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForSingleElement() {
        String xmlStr = "<key>value</key>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"value\"}");
    }

    @Test
    public void testToJsonForEmptySingleElement() {
        String xmlStr = "<key/>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"\"}");
    }

    @Test
    public void testToJsonForSimpleXML() {
        String xmlStr = "<person><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonForXMLWithTwoLevels() {
        String xmlStr = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}");
    }

    @Test
    public void testToJsonForXMLWithThreeLevels() {
        String xmlStr = "<persons><person><test><name>Jack</name><address>wso2</address></test></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"test\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAndAttributes() {
        String xmlStr = "<name test=\"5\">Jack</name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":{\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAttributesNamespace() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\">Jack</ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleEmptyElementAndAttributes() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\"></ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\"}}");
    }

    @Test
    public void testToJsonWithComplexObject() {
        String xmlStr = "<bookStore><storeName>foo</storeName><postalCode>94</postalCode><isOpen>true</isOpen><address>"
                + "<street>foo</street><city>94</city><country>true</country></address><codes><item>4</item>"
                + "<item>8</item><item>9</item></codes></bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"storeName\":\"foo\",\"postalCode\":\"94\","
                + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\",\"country\":\"true\"},"
                + "\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithXMLInMiddle() {
        String xmlStr = "<person><name>Jack</name><age>40</age><!-- some comment --></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithSimpleXMLAndAttributes() {
        String xmlStr = "<person id = \"5\"><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"@id\":\"5\",\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithMultipleAttributes() {
        String xmlStr = "<person id = \"5\"><name cat = \"A\">Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"person\":{\"@id\":\"5\",\"age\":\"40\",\"name\":{\"@cat\":\"A\",\"#text\":\"Jack\"}}}");
    }

    @Test
    public void testToJsonWithComplexObjectAttributes() {
        String xmlStr = "<bookStore status = \"online\"><storeName>foo</storeName><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address><street>foo</street><city code = \"A\">94</city><country>true"
                + "</country></address><codes quality=\"b\"><item>4</item><item>8</item><item>9</item></codes>"
                + "</bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\",\"postalCode\":\"94\","
                        + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"country\":\"true\","
                        + "\"city\":{\"@code\":\"A\",\"#text\":\"94\"}},\"codes\":{\"@quality\":\"b\","
                        + "\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithComplexObjectWithMultipleAttributes() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"@code\":\"A\",\"@reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"@quality\":\"b\",\"@type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithDifferentAttributeTag() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithOptions", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"#status\":\"online\",\"#id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"#code\":\"A\",\"#reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"#quality\":\"b\",\"#type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespaces() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@xmlns:ns1\":\"http://sample1.com/test\",\"@status\":\"online\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns3:postalCode\":{\"@xmlns:ns3\":\"http://sample3.com/test\","
                + "\"#text\":\"94\"},\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"ns2:street\":\"foo\",\"ns2:city\":\"111\",\"ns2:country\":\"true\"},"
                + "\"ns4:codes\":{\"@xmlns:ns4\":\"http://sample4.com/test\",\"ns4:item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespacesWithOptions() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"city\":\"111\",\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithObjectArray() {
        String xmlStr = "<books><item><bookName>book1</bookName><bookId>101</bookId></item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"bookName\":\"book1\",\"bookId\":\"101\"},{\"bookName\":\"book2\","
                        + "\"bookId\":\"102\"},{\"bookName\":\"book3\",\"bookId\":\"103\"}]}}");
    }

    @Test
    public void testToJsonWithChildElementsWithSameKey() {
        String xmlStr = "<books><item><item><bookName>book1</bookName><bookId>101</bookId></item></item><item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item></item><item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"item\":{\"bookName\":\"book1\",\"bookId\":\"101\"}},{\"item\":"
                        + "{\"bookName\":\"book2\",\"bookId\":\"102\"}},{\"item\":{\"bookName\":\"book3\","
                        + "\"bookId\":\"103\"}}]}}");
    }

    @Test
    public void testToJsonWithArray() {
        String xmlStr = "<books><item>book1</item><item>book2</item><item>book3</item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"books\":{\"item\":[\"book1\",\"book2\",\"book3\"]}}");
    }

    @Test
    public void testToJSONWithSequenceDistinctKeys() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceDistinctKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":\"value1\",\"key2\":\"value2\"}");
    }

    @Test
    public void testToJSONWithSequenceSimilarKeys() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceSimilarKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":[\"value1\",\"value2\",\"value3\"]}");
    }

    @Test
    public void testToJSONWithSequenceWithValueArray() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithValueArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void testToJSONWithSequenceWithMultipleElements() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithMultipleElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"},"
                + "\"metadata\":\"5\"}");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndText() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithElementAndText");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":\"value3\"}]");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndTextArray() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithElementAndTextArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentElements() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithDifferentElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"],"
                + "\"bookName\":\"Book1\",\"bookId\":[\"001\",\"001\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentComplexElements() {
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithSequenceWithDifferentComplexElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\","
                + "\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\","
                + "\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}},\"metaInfo\":\"some info\"}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" ns0:id = \"10\">"
                + "<ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen>"
                + "<ns2:address xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" "
                + "ns2:code= \"test\">srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":"
                + "\"http://sample0.com/test\",\"@status\":\"online\",\"@ns0:id\":\"10\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"@status\":\"online\",\"@ns0:id\":\"10\",\"@ns2:code\":\"test\",\"#text\":\"srilanka\"}}}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndNoPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" "
                + "ns0:id = \"10\"><ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen><ns2:address "
                + "xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" ns2:code= \"test\">"
                + "srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BRunUtil.invoke(result, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"storeName\":\"foo\",\"isOpen\":\"true\",\"address\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"@code\":\"test\",\"#text\":\"srilanka\"}}}");
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

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsWithEmptyNs() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectDescendantsWithEmptyNs");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(), "<name xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name>"
                + "<lname>Setunga</lname></name><name xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsFromSeq() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectDescendantsFromSeq");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 3);

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">Doe</name>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*failed to add attribute " +
                    "'a:text'. prefix 'a' is already bound to namespace 'yyy'.*")
    public void testUpdateAttributeWithDifferentUri() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdateAttributeWithDifferentUri");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name xmlns:a=\"yyy\" a:text=\"hello\"/>");
    }

    @Test(enabled = false)
    public void testParseXMLElementWithXMLDeclrEntity() {
        BValue[] returns = BRunUtil.invoke(result, "testParseXMLElementWithXMLDeclrEntity");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root>hello world</root>");
    }

    @Test(enabled = false)
    public void testParseXMLCommentWithXMLDeclrEntity() {
        BValue[] returns = BRunUtil.invoke(result, "testParseXMLCommentWithXMLDeclrEntity");
        Assert.assertEquals(returns[0], null);
        Assert.assertTrue(returns[1].stringValue().startsWith("{message:\"failed to create xml: " +
                "Unexpected EOF in prolog"));
        Assert.assertTrue(returns[1].stringValue().endsWith("at [row,col {unknown-source}]: [1,74]\", cause:null}"));
    }

    @Test
    public void testRemoveAttributeUsingStringName() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveAttributeUsingStringName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" xmlns:ns0=\"http://ballerina.com/aaa\" "
                        + "foo1=\"bar1\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test
    public void testRemoveAttributeUsinQName() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveAttributeUsinQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" xmlns:ns0=\"http://ballerina.com/aaa\" "
                        + "foo1=\"bar1\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test
    public void testRemoveNonExistingAttribute() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveNonExistingAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns1=\"http://ballerina.com/bbb\" xmlns:ns0=\"http://ballerina.com/aaa\" "
                        + "foo1=\"bar1\" ns0:foo1=\"bar2\" ns1:foo1=\"bar3\" ns0:foo2=\"bar4\"> hello world!</root>");
    }

    @Test
    public void testGetChildrenOfSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testGetChildrenOfSequence");
        Assert.assertEquals(returns.length, 2);

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<fname1>John</fname1><lname1>Doe</lname1><fname2>Jane</fname2><lname2>Doe</lname2>apple");
    }
}
