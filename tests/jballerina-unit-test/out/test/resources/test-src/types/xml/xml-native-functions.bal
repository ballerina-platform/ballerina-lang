//function toString(xml msg) returns (string) {
//    string s = <string> msg;
//    return s;
//}

function testIsSingleton() returns [boolean, boolean] {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>supun</name>`;
    xml x3 = x1 + x2;
    boolean b1 = x3.isSingleton();
    boolean b2 = x2.isSingleton();
    return [b1, b2];
}

function testIsSingletonWithMultipleChildren() returns (boolean) {
    var x1 = xml `<order><orderid>123</orderid><noOfItems>5</noOfItems></order>`;
    boolean b = x1.isSingleton();
    return b;
}
 
function testIsEmpty() returns (boolean) {
    var x = xml `<name>supun</name>`;
    boolean b = x.isEmpty();
    return b;
}

function testIsEmptyWithNoElementTextValue() returns (boolean) {
    var x = xml `<name/>`;
    boolean b = x.isEmpty();
    return b;
}

function testIsEmptyWithMultipleChildren() returns (boolean) {
    var x = xml `<order><orderid>123</orderid><noOfItems>5</noOfItems></order>`;
    boolean b = x.isEmpty();
    return b;
}

function testGetItemType() returns [string, string, string, string] {
    var x1 = xml `<name>supun</name>`;
    var x2 = xml `<!-- outer comment -->`;
    var x3 = xml `<name gender="male">supun</name>`;
    xml x4 = x2 + x1 + x3;
    string s1 = x1.getItemType();
    string s2 = x2.getItemType();
    string s3 = x3.getItemType();
    string s4 = x4.getItemType();
    return [s1, s2, s3, s4];
}

function testGetItemTypeForElementWithPrefix() returns (string) {
    var x = xml `<cre:name xmlns:cre="http://xmlns.oracle.com/apps/ozf" cre:gender="male">supun</cre:name>`;
    string s1 = x.getItemType();
    return s1;
}

function testGetItemTypeForElementWithDefaultNamespace() returns (string) {
    var x = xml `<name xmlns="http://xmlns.oracle.com/apps/ozf" gender="male">supun</name>`;
    string s1 = x.getItemType();
    return s1;
}

function testGetElementName() returns (string) {
    var x = xml `<ns0:name xmlns:ns0="http://sample.com/test">supun</ns0:name>`;
    string s = x.getElementName();
    return s;
}

function testGetElementNameForElementWithDefaultNamespace() returns (string) {
    var x = xml `<name xmlns="http://sample.com/test">supun</name>`;
    string s = x.getElementName();
    return s;
}

function testGetElementNameForElementWithoutNamespace() returns (string) {
    var x = xml `<name xmlns="http://sample.com/test/core" xmlns:ns0="http://sample.com/test" ns0:gender="male">supun</name>`;
    string s = x.getElementName();
    return s;
}

function testGetTextValue() returns (string) {
    var x = xml `<ns0:name xmlns:ns0="http://sample.com/test">supun</ns0:name>`;
    string s = x.getTextValue();
    return s;
}

function testGetTextValueDefaultNamespace() returns (string) {
    var x = xml `<name xmlns="http://sample.com/test">supun</name>`;
    string s = x.getTextValue();
    return s;
}

function testGetChildren() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></ns0:name>`;
    xml x2 = x1.*;
    
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();
    
    return [x2, isEmpty, isSingleton];
}

function testGetChildrenFromComplexXml() returns [xml, boolean, boolean] {
    xml x1 = xml `<name gender="male" xmlns="http://sample.com/test"><ns0:fname ns0:preferred="true" xmlns:ns0="http://sample.com/test/code">supun</ns0:fname><fname>supun</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;

    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();

    return [x2, isEmpty, isSingleton];
}

function testGetNonExistingChildren() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"></ns0:name>`;
    xml x2 = x1.*;
    
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();
    
    return [x2, isEmpty, isSingleton];
}

function testSelectChildren() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
              
    xml x2 = x1["{http://sample.com/test}fname"];
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();
    
    return [x2, isEmpty, isSingleton];
}

function testSelectChildrenWithDefaultNamespace() returns [xml, boolean, boolean] {
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = x1["{http://sample.com/test}fname"];
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();

    return [x2, isEmpty, isSingleton];
}

function testSelectChildrenPrefixedDefaultNamespace() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = x1[pre:fname];
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();

    return [x2, isEmpty, isSingleton];
}

function testSelectChildrenWithSamePrefix() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as ns0;
    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;

    xml x2 = x1[ns0:fname];
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();

    return [x2, isEmpty, isSingleton];
}

function testSelectChildrenWithDifferentPrefix() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;

    xml x2 = x1[pre:fname];
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();

    return [x2, isEmpty, isSingleton];
}

function testSelectChildrenWithDifferentNamespaces() returns [xml, xml, boolean, boolean, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    xmlns "http://sample.com/test/code" as cre;
    xml x1 = xml `<name xmlns="http://sample.com/test"><ns0:fname xmlns:ns0="http://sample.com/test/code">supun</ns0:fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = x1[pre:fname];
    xml x3 = x1[cre:fname];

    boolean isEmpty_1 = x2.isEmpty();
    boolean isSingleton_1 = x2.isSingleton();

    boolean isEmpty_2 = x3.isEmpty();
    boolean isSingleton_2 = x3.isSingleton();

    return [x2, x3, isEmpty_1, isSingleton_1, isEmpty_2, isSingleton_2];
}

function testGetElements() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></ns0:name>`;
    xml x2 = x1.elements();
    
    boolean isEmpty = x2.isEmpty();
    boolean isSingleton = x2.isSingleton();
    
    return [x2, isEmpty, isSingleton];
}

function testGetElementsFromSequence() returns [xml, boolean, boolean]{
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    var x2 = xml `<order xmlns="http://sample.com/test/core"><orderid>123</orderid><noOfItems>5</noOfItems></order>`;
    xml x3 = x1 + x2;
    xml x4 = x3.elements();

    boolean isEmpty = x4.isEmpty();
    boolean isSingleton = x4.isSingleton();
    return [x4, isEmpty, isSingleton];

}

function testGetElementsByName() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    xml x2 = x1.*;
    xml x3 = x2.select("{http://sample.com/test}fname");
    
    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();
    
    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNameWithDefaultNamespace() returns [xml, boolean, boolean] {
    var x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;
    xml x3 = x2.select("{http://sample.com/test}fname");

    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();

    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNameByPrefix() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as ns0;
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    xml x2 = x1.*;
    xml x3 = x2.select(ns0:fname);

    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();

    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNameByDifferentPrefix() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    xml x2 = x1.*;
    xml x3 = x2.select(pre:fname);

    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();

    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNameEmptyNamespace() returns [xml, boolean, boolean] {
    xmlns "";
    xml x1 = xml `<name xmlns=""><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;
    xml x3 = x2.select("{}fname");

    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();

    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNamePrefixForDefaultNamespace() returns [xml, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    var x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;
    xml x3 = x2.select(pre:fname);

    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();

    return [x3, isEmpty, isSingleton];
}

function testGetElementsByNameDifferentNamespaces() returns [xml, xml, boolean, boolean, boolean, boolean] {
    xmlns "http://sample.com/test" as pre;
    var x1 = xml `<name xmlns="http://sample.com/test"><ns0:fname xmlns:ns0="http://sample.com/test/code">supun</ns0:fname><fname>thilina</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;
    xml x3 = x2.select(pre:fname);
    xml x4 = x2.select("{http://sample.com/test/code}fname");

    boolean isEmpty_1 = x3.isEmpty();
    boolean isSingleton_1 = x3.isSingleton();

    boolean isEmpty_2 = x4.isEmpty();
    boolean isSingleton_2 = x4.isSingleton();

    return [x3, x4, isEmpty_1, isSingleton_1, isEmpty_2, isSingleton_2];
}

function testConcat() returns [xml, boolean, boolean] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></ns0:name>`;
    var x2 = xml `<ns1:address xmlns:ns1="http://sample.com/test"><country>SL</country><city>Colombo</city></ns1:address>`;
    
    xml x3 = x1 + x2;
    
    boolean isEmpty = x3.isEmpty();
    boolean isSingleton = x3.isSingleton();
    
    return [x3, isEmpty, isSingleton];
}

function testSetChildren() returns [xml, boolean, boolean, xml] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></ns0:name>`;
    var x2 = xml `<newFname>supun-new</newFname>`;
    var x3 = xml `<newMname>thilina-new</newMname>`;
    var x4 = xml `<newLname>setunga-new</newLname>`;
    
    xml children = x2 + x3 + x4;
    x1.setChildren(children);
    
    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    
    return [x1, isEmpty, isSingleton, x1.*];
}

function testSetChildrenDefaultNamespace() returns [xml, boolean, boolean, xml, string?] {
    xmlns "http://sample.com/test";

    var x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></name>`;
    string elementValue = "true";
    string attributeValue = "true";
    xml x2 = xml `<residency citizen="${attributeValue}">${elementValue}</residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test}residency"];

    return [x1, isEmpty, isSingleton, x1.*, x5@["citizen"]];
}

function testSetChildrenWithDifferentNamespaceForAttribute() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test";

    var x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></name>`;
    string elemantName = "residency";
    string elementValue = "true";
    string attributeValue = "true";
    xml x2 = xml `<residency xmlns:nsncdom="http://sample.com/test/code" citizen="${attributeValue}">${elementValue}</residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test}residency"];

    return [x1, isEmpty, isSingleton, x5@["citizen"]];
}

function testSetChildrenWithPrefixedAttribute() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test";

    var x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></name>`;
    string elementValue = "true";
    string attributeValue = "true";

    xmlns "http://sample.com/test/code" as pre;
    xml x2 = xml `<residency pre:citizen="${attributeValue}">${elementValue}</residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test}residency"];


    return [x1, isEmpty, isSingleton, x5@[pre:citizen]];
}

function testSetChildrenWithSameNamespace() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test" as ns0;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<ns0:residency ns0:citizen="${attributeValue}">${elementValue}</ns0:residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test}residency"];

    return [x1, isEmpty, isSingleton, x5@[ns0:citizen]];
}

function testSetChildrenWithDifferentNamespace() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test/code" as ns0;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<ns0:residency ns0:citizen="${attributeValue}">${elementValue}</ns0:residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1[ns0:residency];

    return [x1, isEmpty, isSingleton, x5@[ns0:citizen]];
}

function testSetChildrenWithDiffNamespaceWithoutPrefix() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test/code";

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<residency citizen="${attributeValue}">${elementValue}</residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test/code}residency"];

    return [x1, isEmpty, isSingleton, x5@["citizen"]];
}

function testSetChildrenWithAttributeDiffNamespace() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test" as ns0;
    xmlns "http://sample.com/test/code" as pre;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<ns0:residency pre:citizen="${attributeValue}">${elementValue}</ns0:residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test}residency"];

    return [x1, isEmpty, isSingleton, x5@["{http://sample.com/test/code}citizen"]];
}

function testSetChildrenWithElementDiffNamespace() returns [xml, boolean, boolean, string?] {
    xmlns "http://sample.com/test" as ns0;
    xmlns "http://sample.com/test/code" as pre;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test/code}residency";
    string attributeName = "{http://sample.com/test}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<pre:residency ns0:citizen="${attributeValue}">${elementValue}</pre:residency>`;
    xml x3 = x1.*;
    xml x4 = x3 + x2;
    x1.setChildren(x4);

    boolean isEmpty = x1.isEmpty();
    boolean isSingleton = x1.isSingleton();
    xml x5 = x1["{http://sample.com/test/code}residency"];

    return [x1, isEmpty, isSingleton, x5@[ns0:citizen]];
}

function testCopy() returns [xml, boolean, boolean, xml] {
    var x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><fname>supun</fname><lname>setunga</lname></ns0:name>`;
    var x2 = xml `<newFname>supun-new</newFname>`;
    var x3 = xml `<newMname>thilina-new</newMname>`;
    var x4 = xml `<newLname>setunga-new</newLname>`;
    
    xml children = x2 + x3 + x4;
    
    xml copy = x1.copy();
    
    copy.setChildren(children);
    
    boolean isEmpty = copy.isEmpty();
    boolean isSingleton = copy.isSingleton();
    
    return [copy, isEmpty, isSingleton, x1.*];
}

function testToString() returns (string) {
    var bookComment = xml `<!-- comment about the book-->`;
    var bookName = xml `<bookName>Book1</bookName>`;
    var bookId = xml `<bookId>001</bookId>`;
    var bookAuthor = xml `<bookAuthor>Author01</bookAuthor>`;
    var bookMeta = xml `<?word document="book.doc" ?>`;
    
    xml book = bookComment + bookName + bookId + bookAuthor + bookMeta;
    return book.toString();
}

function testStrip() returns [xml, xml] {
    var x1 = xml `<!-- comment about the book-->`;
    xml x2 = xml `     `;
    var x3 = xml `<bookId>001</bookId>`;
    xml x4 = xml ` `;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return [x6, x6.strip()];
}

function testStripSingleton() returns [xml, xml] {
    var x1 = xml `<bookId>001</bookId>`;
    return [x1, x1.strip()];
}

function testStripEmptySingleton() returns [xml, xml, boolean] {
    xml x1 = xml ` `;
    xml x2 = x1.strip();
    boolean isEmpty = x2.isEmpty();
    
    return [x1, x2.strip(), isEmpty];
}

function testSlice() returns (xml) {
    var x1 = xml `<!-- comment about the book-->`;
    var x2 = xml `<bookName>Book1</bookName>`;
    var x3 = xml `<bookId>001</bookId>`;
    var x4 = xml `<bookAuthor>Author01</bookAuthor>`;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6.slice(1, 4);
}

function testSliceAll() returns (xml) {
    var x1 = xml `<!-- comment about the book-->`;
    var x2 = xml `<bookName>Book1</bookName>`;
    var x3 = xml `<bookId>001</bookId>`;
    var x4 = xml `<bookAuthor>Author01</bookAuthor>`;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6.slice(-1, -1);
}

function testSliceInvalidIndex() returns (xml) {
    var x1 = xml `<!-- comment about the book-->`;
    var x2 = xml `<bookName>Book1</bookName>`;
    var x3 = xml `<bookId>001</bookId>`;
    var x4 = xml `<bookAuthor>Author01</bookAuthor>`;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6.slice(4, 1);
}

function testSliceOutOfRangeIndex(int startIndex, int endIndex) returns (xml) {
    var x1 = xml `<!-- comment about the book-->`;
    var x2 = xml `<bookName>Book1</bookName>`;
    var x3 = xml `<bookId>001</bookId>`;
    var x4 = xml `<bookAuthor>Author01</bookAuthor>`;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6.slice(startIndex, endIndex);
}

function testSliceSingleton() returns (xml) {
    var x1 = xml `<bookName>Book1</bookName>`;
    return x1.slice(-1, -1);
}

function testSeqCopy() returns [xml, xml] {
    var x1 = xml `<!-- comment about the book-->`;
    var x2 = xml `<bookName>Book1</bookName>`;
    var x3 = xml `<bookId>001</bookId>`;
    var x4 = xml `<bookAuthor>Author01</bookAuthor>`;
    var x5 = xml `<?word document="book.doc" ?>`;
    
    xml original = x1 + x2 + x3 + x4 + x5;
    xml copy = original.copy();

    var x7 = xml `Updated Book ID`;
    x3.setChildren(x7);
    
    return [original, copy];
}

function testSetChildrenToElemntInDefaultNameSpace() returns (xml) {
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname></name>`;
    xml x2 = xml `<newFname>supun-new</newFname>`;

    x1.setChildren(x2);

    return x1;
}

function testSelectChildrenWithEmptyNs() returns [xml, xml] {
    xml x1 = xml `<name><fname>supun</fname><fname xmlns="">thilina</fname><lname>setunga</lname></name>`;
              
    xml x2 = x1["fname"];
    xml x3 = x1["{}fname"];
    
    return [x2, x3];
}

function testSelectElementsWithEmptyNs() returns [xml, xml] {
    xml x1 = xml `<name><fname>supun</fname><fname xmlns="">thilina</fname><lname>setunga</lname></name>`;
    xml x2 = x1.*;
    
    xml x3 = x2.select("fname");
    xml x4 = x2.select("{}fname");
    
    return [x3, x4];
}

function testSelectDescendants() returns (xml) {
    xmlns "http://ballerinalang.org/";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info>`;
    xml x2 = x1.selectDescendants("{http://ballerinalang.org/}name");
    return x2;
}

function testSelectDescendantsWithEmptyNs() returns (xml) {
    xmlns "";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info>`;
    xml x2 = x1.selectDescendants("{}name");
    return x2;
}

function testSelectDescendantsFromSeq() returns (xml) {
    xmlns "http://ballerinalang.org/";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info1><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info1>`;
    xml x2 = xml `<info2><name>Doe</name></info2>`;
    xml x3 = x1 + x2;
    xml x4 = x3.selectDescendants("{http://ballerinalang.org/}name");
    return x4;
}

function testUpdateAttributeWithDifferentUri() returns (xml) {
    xmlns "xxx" as a;

    xml x1 = xml `<name xmlns:a="yyy" a:text="hello"></name>`;
    x1@[a:text] = "hello world";
    
    return x1;
}

function testRemoveAttributeUsingStringName() returns (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  x.removeAttribute("{http://ballerina.com/aaa}foo1");
  return x;
}

function testRemoveAttributeUsinQName() returns (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  x.removeAttribute(ns0:foo1);
  return x;
}

function testRemoveNonExistingAttribute() returns (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  x.removeAttribute("{http://ballerina.com/aaa}foo555");
  return x;
}

function testGetChildrenOfSequence() returns [int, xml] {
    xml x1 = xml `<name1><fname1>John</fname1><lname1>Doe</lname1></name1>`;
    xml x2 = xml `<name2><fname2>Jane</fname2><lname2>Doe</lname2></name2>`;
    xml x3 = x1 + x2 + xml `<foo>apple</foo>`;
    xml x4 = x3.*;
    return [x4.length(), x4];
}

function testAddChildren() returns [xml, xml] {
    xml x1 = xml `<name><fname>John</fname><lname>Doe</lname></name>`;
    xml children = x1.*;
    xml x2 = xml `<age>50</age>`;
    xml x3 = xml `<city>Colombo</city>`;
    xml x4 = xml `<country>SL</country>`;
    xml x5 = xml `<!-- unknown person -->`;
    xml x6 = xml `marital status: unknown`;
    xml x7 = x3 + x4;
    x1.appendChildren(x2);
    x1.appendChildren(x5);
    x1.appendChildren(x6);
    x1.appendChildren(x7);
    return [x1, children];
}

function testRemoveSingleChild() returns [xml, xml] {
    xml x1 = xml `<name><fname>John</fname><lname>Doe</lname></name>`;
    xml children = x1.*;
    xml x2 = xml `<age>50</age>`;
    x1.appendChildren(x2);
    x1.removeChildren("foo"); // remove non existing child
    x1.removeChildren("lname");
    return [children, x1.*];
}

function testRemoveChildren() returns [xml, xml] {
    xml x1 = xml `<person><name>John</name><name>Jane</name><age>50</age><name>Doe</name></person>`;
    xml children = x1.*;
    x1.removeChildren("foo"); // remove non existing child
    x1.removeChildren("name");
    return [children, x1.*];
}

function testRemoveChildrenWithNamesapces() returns [xml, xml] {
    xmlns "http://wso2.com" as ns0;
    xml x1 = xml `<person><name>John</name><ns0:name>Foo</ns0:name><age>50</age><name>Doe</name></person>`;
    xml children = x1.*;
    x1.removeChildren(ns0:name);
    return [children, x1.*];
}

function testRemoveComplexChildren() returns [xml, xml] {
    xml x1 = xml `<person><name>John</name><address><street>Palm Grove</street><city>Colombo 03</city><country><name>Sri Lanka</name><code>LK</code></country></address><age>50</age></person>`;
    xml children = x1.*;
    x1.removeChildren("address");
    return [children, x1.*];
}

function testRemoveInnerChildren() returns [xml, xml] {
    xml x1 = xml `<person><name>John</name><address><street>Palm Grove</street><city>Colombo 03</city><country><name>Sri Lanka</name><code>LK</code></country></address><age>50</age></person>`;
    xml children = x1.*;
    x1.address.country.removeChildren("code");
    return [children, x1.*];
}

function testXMLLength() returns [int, int, int, int] {
    xml a = xml `xml1`;
    xml b = xml `b✅`;
    xml c = xml `<elem>cont</elem>`;
    xml d = a + b + c;
    return [d.length(), a.length(), b.length(), c.length()];
}
