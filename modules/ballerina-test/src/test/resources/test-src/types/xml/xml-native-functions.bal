import ballerina.lang.xmls;

function toString(xml msg) (string) {
    string s = <string> msg;
    return s;
}

function testIsSingleton() (boolean, boolean) {
    var x1, _ = <xml> "<!-- outer comment -->";
    var x2, _ = <xml> "<name>supun</name>";
    xml x3 = x1 + x2;
    boolean b1 = xmls:isSingleton(x3);
    boolean b2 = xmls:isSingleton(x2);
    return b1, b2;
}

function testIsSingletonWithMultipleChildren() (boolean) {
    var x1, _ = <xml> "<order><orderid>123</orderid><noOfItems>5</noOfItems></order>";
    boolean b = xmls:isSingleton(x1);
    return b;
}
 
function testIsEmpty() (boolean) {
    var x, _ = <xml> "<name>supun</name>";
    boolean b = xmls:isEmpty(x);
    return b;
}

function testIsEmptyWithNoElementTextValue() (boolean) {
    var x, _ = <xml> "<name/>";
    boolean b = xmls:isEmpty(x);
    return b;
}

function testIsEmptyWithMultipleChildren() (boolean) {
    var x, _ = <xml> "<order><orderid>123</orderid><noOfItems>5</noOfItems></order>";
    boolean b = xmls:isEmpty(x);
    return b;
}

function testGetItemType() (string, string, string) {
    var x1, _  = <xml> "<name>supun</name>";
    var x2, _  = <xml> "<!-- outer comment -->";
    var x3, _  = <xml> "<name gender=\"male\">supun</name>";
    xml x4 = x2 + x1 + x3;
    string s1 = xmls:getItemType(x1);
    string s2 = xmls:getItemType(x3);
    string s3 = xmls:getItemType(x4);
    return s1, s2, s3;
}

function testGetItemTypeForElementWithPrefix() (string) {
    var x, _ = <xml> "<cre:name xmlns:cre=\"http://xmlns.oracle.com/apps/ozf\" cre:gender=\"male\">supun</cre:name>";
    string s1 = xmls:getItemType(x);
    return s1;
}

function testGetItemTypeForElementWithDefaultNamespace() (string) {
    var x, _ = <xml> "<name xmlns=\"http://xmlns.oracle.com/apps/ozf\" gender=\"male\">supun</name>";
    string s1 = xmls:getItemType(x);
    return s1;
}

function testGetElementName() (string) {
    var x, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\">supun</ns0:name>";
    string s = xmls:getElementName(x);
    return s;
}

function testGetElementNameForElementWithDefaultNamespace() (string) {
    var x, _ = <xml> "<name xmlns=\"http://sample.com/test\">supun</name>";
    string s = xmls:getElementName(x);
    return s;
}

function testGetElementNameForElementWithoutNamespace() (string) {
    var x, _  = <xml> "<name xmlns=\"http://sample.com/test/core\" xmlns:ns0=\"http://sample.com/test\" ns0:gender=\"male\">supun</name>";
    string s = xmls:getElementName(x);
    return s;
}

function testGetTextValue() (string) {
    var x, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\">supun</ns0:name>";
    string s = xmls:getTextValue(x);
    return s;
}

function testGetTextValueDefaultNamespace() (string) {
    var x, _ = <xml> "<name xmlns=\"http://sample.com/test\">supun</name>";
    string s = xmls:getTextValue(x);
    return s;
}

function testGetChildren() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>";
    xml x2 = xmls:children(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testGetChildrenFromComplexXml() (xml, boolean, boolean) {
    xml x1 = xml `<name gender="male" xmlns="http://sample.com/test"><ns0:fname ns0:preferred="true" xmlns:ns0="http://sample.com/test/code">supun</ns0:fname><fname>supun</fname><lname>setunga</lname></name>`;
    xml x2 = xmls:children(x1);

    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);

    return x2, isEmpty, isSingleton;
}

function testGetNonExistingChildren() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"></ns0:name>";
    xml x2 = xmls:children(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testSelectChildren() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>";
              
    xml x2 = xmls:selectChildren(x1, "{http://sample.com/test}fname");
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testSelectChildrenWithDefaultNamespace() (xml, boolean, boolean) {
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = xmls:selectChildren(x1, "{http://sample.com/test}fname");
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);

    return x2, isEmpty, isSingleton;
}

function testSelectChildrenPrefixedDefaultNamespace() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = xmls:selectChildren(x1, pre:fname);
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);

    return x2, isEmpty, isSingleton;
}

function testSelectChildrenWithSamePrefix() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as ns0;
    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;

    xml x2 = xmls:selectChildren(x1, ns0:fname);
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);

    return x2, isEmpty, isSingleton;
}

function testSelectChildrenWithDifferentPrefix() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;

    xml x2 = xmls:selectChildren(x1, pre:fname);
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);

    return x2, isEmpty, isSingleton;
}

function testSelectChildrenWithDifferentNamespaces() (xml, xml, boolean, boolean, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    xmlns "http://sample.com/test/code" as cre;
    xml x1 = xml `<name xmlns="http://sample.com/test"><ns0:fname xmlns:ns0="http://sample.com/test/code">supun</ns0:fname><fname>thilina</fname><lname>setunga</lname></name>`;

    xml x2 = xmls:selectChildren(x1, pre:fname);
    xml x3 = xmls:selectChildren(x1, cre:fname);

    boolean isEmpty_1 = xmls:isEmpty(x2);
    boolean isSingleton_1 = xmls:isSingleton(x2);

    boolean isEmpty_2 = xmls:isEmpty(x3);
    boolean isSingleton_2 = xmls:isSingleton(x3);

    return x2, x3, isEmpty_1, isSingleton_1, isEmpty_2, isSingleton_2;
}

function testGetElements() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>";
    xml x2 = xmls:elements(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testGetElementsFromSequence() (xml, boolean, boolean){
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>";
    var x2, _ = <xml> "<order xmlns=\"http://sample.com/test/core\"><orderid>123</orderid><noOfItems>5</noOfItems></order>";
    xml x3 = x1 + x2;
    xml x4 = xmls:elements(x3);

    boolean isEmpty = xmls:isEmpty(x4);
    boolean isSingleton = xmls:isSingleton(x4);
    return x4, isEmpty, isSingleton;

}

function testGetElementsByName() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, "{http://sample.com/test}fname");
    
    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);
    
    return x3, isEmpty, isSingleton;
}

function testGetElementsByNameWithDefaultNamespace() (xml, boolean, boolean) {
    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, "{http://sample.com/test}fname");

    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);

    return x3, isEmpty, isSingleton;
}

function testGetElementsByNameByPrefix() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as ns0;
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, ns0:fname);

    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);

    return x3, isEmpty, isSingleton;
}

function testGetElementsByNameByDifferentPrefix() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, pre:fname);

    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);

    return x3, isEmpty, isSingleton;
}

function testGetElementsByNameEmptyNamespace() (xml, boolean, boolean) {
    xmlns "";
    xml x1 = xml `<name xmlns=""><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>`;
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, "{}fname");

    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);

    return x3, isEmpty, isSingleton;
}

function testGetElementsByNamePrefixForDefaultNamespace() (xml, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><fname>supun</fname><fname>thilina</fname><lname>setunga</lname></name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, pre:fname);

    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);

    return x3, isEmpty, isSingleton;
}

function testGetElementsByNameDifferentNamespaces() (xml, xml, boolean, boolean, boolean, boolean) {
    xmlns "http://sample.com/test" as pre;
    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><ns0:fname xmlns:ns0=\"http://sample.com/test/code\">supun</ns0:fname><fname>thilina</fname><lname>setunga</lname></name>";
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, pre:fname);
    xml x4 = xmls:select(x2, "{http://sample.com/test/code}fname");

    boolean isEmpty_1 = xmls:isEmpty(x3);
    boolean isSingleton_1 = xmls:isSingleton(x3);

    boolean isEmpty_2 = xmls:isEmpty(x4);
    boolean isSingleton_2 = xmls:isSingleton(x4);

    return x3, x4, isEmpty_1, isSingleton_1, isEmpty_2, isSingleton_2;
}

function testConcat() (xml, boolean, boolean) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>";
    var x2, _ = <xml> "<ns1:address xmlns:ns1=\"http://sample.com/test\"><country>SL</country><city>Colombo</city></ns1:address>";
    
    xml x3 = x1 + x2;
    
    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);
    
    return x3, isEmpty, isSingleton;
}

function testSetChildren() (xml, boolean, boolean, xml) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>";
    var x2, _ = <xml> "<newFname>supun-new</newFname>";
    var x3, _ = <xml> "<newMname>thilina-new</newMname>";
    var x4, _ = <xml> "<newLname>setunga-new</newLname>";
    
    xml children = x2 + x3 + x4;
    xmls:setChildren(x1, children);
    
    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    
    return x1, isEmpty, isSingleton, xmls:children(x1);
}

function testSetChildrenDefaultNamespace() (xml, boolean, boolean, xml, string) {
    xmlns "http://sample.com/test";

    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></name>";
    string elemantName = "residency";
    string attributeName = "citizen";
    string elementValue = "true";
    string attributeValue = "true";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{}residency");

    return x1, isEmpty, isSingleton, xmls:children(x1), x5@["citizen"];
}

function testSetChildrenWithDifferentNamespaceForAttribute() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test";

    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></name>";
    string elemantName = "residency";
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "true";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{}residency");

    return x1, isEmpty, isSingleton, x5@["{http://sample.com/test/code}citizen"];
}

function testSetChildrenWithPrefixedAttribute() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test/code" as pre;
    xmlns "http://sample.com/test";

    var x1, _ = <xml> "<name xmlns=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></name>";
    string elemantName = "residency";
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "true";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{}residency");


    return x1, isEmpty, isSingleton, x5@[pre:citizen];
}

function testSetChildrenWithSameNamespace() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test" as ns0;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test}residency";
    string attributeName = "{http://sample.com/test}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{http://sample.com/test}residency");

    return x1, isEmpty, isSingleton, x5@[ns0:citizen];
}

function testSetChildrenWithDifferentNamespace() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test/code" as ns0;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test/code}residency";
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{http://sample.com/test/code}residency");

    return x1, isEmpty, isSingleton, x5@[ns0:citizen];
}

function testSetChildrenWithDiffNamespaceWithoutPrefix() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test/code";

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test/code}residency";
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{http://sample.com/test/code}residency");

    return x1, isEmpty, isSingleton, x5@["{http://sample.com/test/code}citizen"];
}

function testSetChildrenWithAttributeDiffNamespace() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test" as ns0;
    xmlns "http://sample.com/test/code" as pre;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test}residency";
    string attributeName = "{http://sample.com/test/code}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{http://sample.com/test}residency");

    return x1, isEmpty, isSingleton, x5@["{http://sample.com/test/code}citizen"];
}

function testSetChildrenWithElementDiffNamespace() (xml, boolean, boolean, string) {
    xmlns "http://sample.com/test" as ns0;
    xmlns "http://sample.com/test/code" as pre;

    xml x1 = xml `<ns0:name xmlns:ns0="http://sample.com/test"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>`;
    string elemantName = "{http://sample.com/test/code}residency";
    string attributeName = "{http://sample.com/test}citizen";
    string elementValue = "true";
    string attributeValue = "yes";
    xml x2 = xml `<{{elemantName}} {{attributeName}}="{{attributeValue}}">{{elementValue}}</{{elemantName}}>`;
    xml x3 = xmls:children(x1);
    xml x4 = x3 + x2;
    xmls:setChildren(x1, x4);

    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    xml x5 = xmls:selectChildren(x1, "{http://sample.com/test/code}residency");

    return x1, isEmpty, isSingleton, x5@[ns0:citizen];
}

function testCopy() (xml, boolean, boolean, xml) {
    var x1, _ = <xml> "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>";
    var x2, _ = <xml> "<newFname>supun-new</newFname>";
    var x3, _ = <xml> "<newMname>thilina-new</newMname>";
    var x4, _ = <xml> "<newLname>setunga-new</newLname>";
    
    xml children = x2 + x3 + x4;
    
    xml copy = xmls:copy(x1);
    
    xmls:setChildren(copy, children);
    
    boolean isEmpty = xmls:isEmpty(copy);
    boolean isSingleton = xmls:isSingleton(copy);
    
    return copy, isEmpty, isSingleton, xmls:children(x1);
}

function testToString() (string) {
    var bookComment, _ = <xml> "<!-- comment about the book-->";
    var bookName, _ = <xml> "<bookName>Book1</bookName>";
    var bookId, _ = <xml> "<bookId>001</bookId>";
    var bookAuthor, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var bookMeta, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml book = bookComment + bookName + bookId + bookAuthor + bookMeta;
    
    string s = <string> book;
    return s;
}

function testStrip() (xml, xml) {
    var x1, _  = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "     ";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6, xmls:strip(x6);
}

function testStripSingleton() (xml, xml) {
    var x1, _ = <xml> "<bookId>001</bookId>";
    return x1, xmls:strip(x1);
}

function testStripEmptySingleton() (xml, xml, boolean) {
    var x1, _ = <xml> "";
    xml x2 = xmls:strip(x1);
    boolean isEmpty = xmls:isEmpty(x2);
    
    return x1, xmls:strip(x2), isEmpty;
}

function testSlice() (xml) {
    var x1, _ = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "<bookName>Book1</bookName>";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, 1, 4);
}

function testSliceAll() (xml) {
    var x1, _ = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "<bookName>Book1</bookName>";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, -1, -1);
}

function testSliceInvalidIndex() (xml) {
    var x1, _ = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "<bookName>Book1</bookName>";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, 4, 1);
}

function testSliceOutOfRangeIndex(int startIndex, int endIndex) (xml) {
    var x1, _ = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "<bookName>Book1</bookName>";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, startIndex, endIndex);
}

function testSliceSingleton() (xml) {
    var x1, _ = <xml> "<bookName>Book1</bookName>";
    return xmls:slice(x1, -1, -1);
}

function testSeqCopy()(xml, xml) {
    var x1, _ = <xml> "<!-- comment about the book-->";
    var x2, _ = <xml> "<bookName>Book1</bookName>";
    var x3, _ = <xml> "<bookId>001</bookId>";
    var x4, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x5, _ = <xml> "<?word document=\"book.doc\" ?>";
    
    xml original = x1 + x2 + x3 + x4 + x5;
    xml copy = xmls:copy(original);

    var x7, _ = <xml> "Updated Book ID";
    xmls:setChildren(x3, x7);
    
    return original, copy;
}

function testSetChildrenToElemntInDefaultNameSpace() (xml) {
    xml x1 = xml `<name xmlns="http://sample.com/test"><fname>supun</fname></name>`;
    xml x2 = xml `<newFname>supun-new</newFname>`;

    xmls:setChildren(x1, x2);

    return x1;
}


function testToJsonForValue() (json) {
    xmls:Options options = {};
    var x, _ = <xml> "value";
    return xmls:toJSON(x, options);
}

function testToJsonForEmptyValue() (json) {
    xmls:Options options = {};
    var x, _ = <xml> "";
    return xmls:toJSON(x, options);
}

function testToJsonForComment() (json) {
    xmls:Options options = {};
    var x, _ = <xml> "<!-- value -->";
    return xmls:toJSON(x, options);
}

function testToJsonForPI() (json) {
    xmls:Options options = {};
    var x, _ = <xml> "<?doc document=\"book.doc\"?>";
    return xmls:toJSON(x, options);
}


function testToJSON(xml msg) (json) {
    xmls:Options options = {};
    return xmls:toJSON(msg, options);
}

function testToJSONWithOptions(xml msg) (json) {
    xmls:Options options = {attributePrefix : "#"};
    return xmls:toJSON(msg, options);
}

function testToJSONWithoutNamespace(xml msg) (json) {
    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(msg, options);
}

function testToJSONWithSequenceDistinctKeys() (json) {
    var x1, _ = <xml> "<key1>value1</key1>";
    var x2, _ = <xml> "<key2>value2</key2>";
    xml x3 = x1 + x2;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x3, options);
}

function testToJSONWithSequenceSimilarKeys() (json) {
    var x1, _ = <xml> "<key>value1</key>";
    var x2, _ = <xml> "<key>value2</key>";
    var x3, _ = <xml> "<key>value3</key>";
    xml x = x1 + x2 + x3;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithValueArray() (json) {
    var x1, _ = <xml> "a";
    var x2, _ = <xml> "b";
    var x3, _ = <xml> "c";
    xml x = x1 + x2 + x3;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithMultipleElements() (json) {
    var x1, _ = <xml> "<person><name>Jack</name><age>40</age></person>";
    var x2, _ = <xml> "<metadata>5</metadata>";
    xml x = x1 + x2;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithElementAndText() (json) {
    var x1, _ = <xml> "a";
    var x2, _ = <xml> "b";
    var x3, _ = <xml> "<key>value3</key>";
    xml x = x1 + x2 + x3;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithElementAndTextArray() (json) {
    var x1, _ = <xml> "a";
    var x2, _ = <xml> "b";
    var x3, _ = <xml> "<key>value3</key>";
    var x4, _ = <xml> "<key>value4</key>";
    var x5, _ = <xml> "<key>value4</key>";
    xml x = x1 + x2 + x3 + x4 + x5;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithDifferentElements()(json) {
    var x1, _ = <xml> "a";
    var x2, _ = <xml> "b";
    var x3, _ = <xml> "<key>value3</key>";
    var x4, _ = <xml> "<key>value4</key>";
    var x5, _ = <xml> "<key>value4</key>";
    var x6, _ = <xml> "<!-- comment about the book-->";
    var x7, _ = <xml> "<bookName>Book1</bookName>";
    var x8, _ = <xml> "<bookId>001</bookId>";
    var x9, _ = <xml> "<bookAuthor>Author01</bookAuthor>";
    var x10, _ = <xml> "<?word document=\"book.doc\" ?>";

    xml x = x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + x8 + x10;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testToJSONWithSequenceWithDifferentComplexElements()(json) {
    var x1, _ = <xml> ("<bookStore status=\"online\"><storeName>foo</storeName><postalCode>94</postalCode>" +
                        "<isOpen>true</isOpen><address><street>foo</street><city>94</city><country>true</country>" +
                        "</address><codes><item>4</item><item>8</item><item>9</item></codes></bookStore>");
    var x2, _ = <xml> "<!-- some comment -->";
    var x3, _ = <xml> "<?doc document=\"book.doc\"?>";
    var x4, _ = <xml> "<metaInfo>some info</metaInfo>";

    xml x = x1 + x2 + x3 + x4;

    xmls:Options options = {preserveNamespaces : false};
    return xmls:toJSON(x, options);
}

function testSelectChildrenWithEmptyNs() (xml, xml) {
    xml x1 = xml `<name><fname>supun</fname><fname xmlns="">thilina</fname><lname>setunga</lname></name>`;
              
    xml x2 = xmls:selectChildren(x1, "fname");
    xml x3 = xmls:selectChildren(x1, "{}fname");
    
    return x2, x3;
}

function testSelectElementsWithEmptyNs() (xml, xml) {
    xml x1 = xml `<name><fname>supun</fname><fname xmlns="">thilina</fname><lname>setunga</lname></name>`;
    xml x2 = xmls:children(x1);
    
    xml x3 = xmls:select(x2, "fname");
    xml x4 = xmls:select(x2, "{}fname");
    
    return x3, x4;
}

function testSelectDescendants() (xml) {
    xmlns "http://ballerinalang.org/";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info>`;
    xml x2 = xmls:selectDescendants(x1, "{http://ballerinalang.org/}name");
    return x2;
}

function testSelectDescendantsWithEmptyNs() (xml) {
    xmlns "";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info>`;
    xml x2 = xmls:selectDescendants(x1, "{}name");
    return x2;
}

function testSelectDescendantsFromSeq() (xml) {
    xmlns "http://ballerinalang.org/";
    xmlns "http://ballerinalang.org/aaa" as ns0;
    
    xml x1 = xml `<info1><employee><name><name>Supun</name><lname>Setunga</lname></name><ns0:name>Jane</ns0:name></employee><name>John</name></info1>`;
    xml x2 = xml `<info2><name>Doe</name></info2>`;
    xml x3 = x1 + x2;
    xml x4 = xmls:selectDescendants(x3, "{http://ballerinalang.org/}name");
    return x4;
}

function testUpdateAttributeWithDifferentUri() (xml) {
    xmlns "xxx" as a;

    xml x1 = xml `<name xmlns:a="yyy" a:text="hello"></name>`;
    x1@[a:text] = "hello world";
    
    return x1;
}

function testParseXMLElementWithXMLDeclrEntity() (xml) {
    var x, _ = <xml> "<?xml version='1.0' encoding='UTF-8' standalone='no'?><root>hello world</root><!-- comment node-->";
    return x;
}

function testParseXMLCommentWithXMLDeclrEntity() (xml, TypeConversionError) {
    var x, e = <xml> "<?xml version='1.0' encoding='UTF-8' standalone='no'?><!-- comment node-->";
    return x, e;
}

function testRemoveAttributeUsingStringName() (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  xmls:removeAttribute(x, "{http://ballerina.com/aaa}foo1");
  return x;
}

function testRemoveAttributeUsinQName() (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  xmls:removeAttribute(x, ns0:foo1);
  return x;
}

function testRemoveNonExistingAttribute() (xml) {
  xmlns "http://ballerina.com/aaa" as ns0;
  xml x = xml `<root xmlns:ns1="http://ballerina.com/bbb" foo1="bar1" ns0:foo1="bar2" ns1:foo1="bar3" ns0:foo2="bar4"> hello world!</root>`;
  xmls:removeAttribute(x, "{http://ballerina.com/aaa}foo555");
  return x;
}