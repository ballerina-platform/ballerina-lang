import ballerina.lang.xmls;
import ballerina.lang.system;

function getString(xml msg, string xPath) (string) {
    return xmls:getString(msg, xPath);
}

function getXML(xml msg, string xPath) (xml) {
    return xmls:getXml(msg, xPath);
}

function setString(xml msg, string xPath, string value) (xml) {
    xmls:setString(msg, xPath, value);
    return msg;
}

function setXML(xml msg, string xPath, xml value) (xml) {
    xmls:setXml(msg, xPath, value);
    return msg;
}

function addElement(xml msg, string xPath, xml value) (xml) {
    xmls:addElement(msg, xPath, value);
    return msg;
}

function addAttribute(xml msg, string xPath, string name, string value) (xml) {
    xmls:addAttribute(msg, xPath, name, value);
    return msg;
}

function remove(xml msg, string xPath) (xml) {
    xmls:remove(msg, xPath);
    return msg;
}

function toString(xml msg) (string) {
    return xmls:toString(msg);
}


function xmlSetString1()(xml) {
     xml payload;
     string doctorName;
     doctorName = "DName1";
     payload = xmls:parse("<CheckAvailability><doctorName></doctorName><appointmentDate></appointmentDate></CheckAvailability>");
     xmls:setString(payload, "/CheckAvailability/doctorName", doctorName);
     return payload;
 }

 function xmlSetString2()(xml) {
     xml payload;
     string doctorName;
     doctorName = "DName2";
     payload = xmls:parse("<CheckAvailability><doctorName>NValue</doctorName><appointmentDate></appointmentDate></CheckAvailability>");
     xmls:setString(payload, "/CheckAvailability/doctorName/text()", doctorName);
     return payload;
 }

function testIsSingleton() (boolean, boolean) {
    xml x1 = xmls:parse("<!-- outer comment -->");
    xml x2 = xmls:parse("<name>supun</name>");
    xml x3 = x1 + x2;
    boolean b1 = xmls:isSingleton(x3);
    boolean b2 = xmls:isSingleton(x2);
    return b1, b2;
}
 
function testIsEmpty() (boolean) {
    xml x = xmls:parse("<name>supun</name>");
    boolean b = xmls:isEmpty(x);
    return b;
}

function testGetItemType() (string, string) {
    xml x1 = xmls:parse("<name>supun</name>");
    xml x2 = xmls:parse("<!-- outer comment -->");
    xml x3 = x2 + x1;
    string s1 = xmls:getItemType(x1);
    string s2 = xmls:getItemType(x3);
    return s1, s2;
}

function testGetElementName() (string) {
    xml x = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\">supun</ns0:name>");
    string s = xmls:getElementName(x);
    return s;
}

function testGetTextValue() (string) {
    xml x = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\">supun</ns0:name>");
    string s = xmls:getTextValue(x);
    return s;
}

function testGetChildren() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>");
    xml x2 = xmls:children(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testGetNonExistingChildren() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"></ns0:name>");
    xml x2 = xmls:children(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testSelectChildren() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>");
              
    xml x2 = xmls:selectChildren(x1, "{http://sample.com/test}fname");
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testGetElements() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>");
    xml x2 = xmls:elements(x1);
    
    boolean isEmpty = xmls:isEmpty(x2);
    boolean isSingleton = xmls:isSingleton(x2);
    
    return x2, isEmpty, isSingleton;
}

function testGetElementsByName() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><ns0:fname>supun</ns0:fname><ns0:fname>thilina</ns0:fname><ns0:lname>setunga</ns0:lname></ns0:name>");
    xml x2 = xmls:children(x1);
    xml x3 = xmls:select(x2, "{http://sample.com/test}fname");
    
    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);
    
    return x3, isEmpty, isSingleton;
}

function testConcat() (xml, boolean, boolean) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>");
    xml x2 = xmls:parse("<ns1:address xmlns:ns1=\"http://sample.com/test\"><country>SL</country><city>Colombo</city></ns1:address>");
    
    xml x3 = x1 + x2;
    
    boolean isEmpty = xmls:isEmpty(x3);
    boolean isSingleton = xmls:isSingleton(x3);
    
    return x3, isEmpty, isSingleton;
}

function testSetChildren() (xml, boolean, boolean, xml) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>");
    xml x2 = xmls:parse("<newFname>supun-new</newFname>");
    xml x3 = xmls:parse("<newMname>thilina-new</newMname>");
    xml x4 = xmls:parse("<newLname>setunga-new</newLname>");
    
    xml children = x2 + x3 + x4;
    xmls:setChildren(x1, children);
    
    boolean isEmpty = xmls:isEmpty(x1);
    boolean isSingleton = xmls:isSingleton(x1);
    
    return x1, isEmpty, isSingleton, xmls:children(x1);
}

function testCopy() (xml, boolean, boolean, xml) {
    xml x1 = xmls:parse("<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun</fname><lname>setunga</lname></ns0:name>");
    xml x2 = xmls:parse("<newFname>supun-new</newFname>");
    xml x3 = xmls:parse("<newMname>thilina-new</newMname>");
    xml x4 = xmls:parse("<newLname>setunga-new</newLname>");
    
    xml children = x2 + x3 + x4;
    
    xml copy = xmls:copy(x1);
    
    xmls:setChildren(copy, children);
    
    boolean isEmpty = xmls:isEmpty(copy);
    boolean isSingleton = xmls:isSingleton(copy);
    
    return copy, isEmpty, isSingleton, xmls:children(x1);
}

function testToString() (string) {
    xml bookComment = xmls:parse("<!-- comment about the book-->");
    xml bookName = xmls:parse("<bookName>Book1</bookName>");
    xml bookId = xmls:parse("<bookId>001</bookId>");
    xml bookAuthor = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml bookMeta = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml book = bookComment + bookName + bookId + bookAuthor + bookMeta;
    
    return xmls:toString(book);
}

function testSetAttribute() (xml) {
    xml book = xmls:parse("<book>Book1</book>");
    xmls:setAttribute(book, "{http://sample/com}id", "ns0", "1234");
    return book;
}

function testSetAttributeWithoutPrefix() (xml) {
    xml book = xmls:parse("<book xmlns=\"http://sample/com\">Book1</book>");
    xmls:setAttribute(book, "{http://sample/com}id", "", "1234");
    return book;
}

function testSetAttributeWithoutNamspaceUri() (xml) {
    xml book = xmls:parse("<book xmlns=\"http://sample/com\">Book1</book>");
    xmls:setAttribute(book, "id", "ns1","1234");
    return book;
}

function testSetAttributeWithoutNamspaceAndPrefix() (xml) {
    xml book = xmls:parse("<book>Book1</book>");
    xmls:setAttribute(book, "id", "", "1234");
    return book;
}

function testSetAttributeWithoutLocalName() (xml) {
    xml book = xmls:parse("<book>Book1</book>");
    xmls:setAttribute(book, "{http://sample/com}", "ns0", "1234");
    return book;
}

function testGetAttribute() (string) {
    xml book = xmls:parse("<book xmlns:ns0=\"http://sample/com\" ns0:id=\"1234\">Book1</book>");
    string attribute = xmls:getAttribute(book, "{http://sample/com}id");
    system:println(attribute);
    return attribute;
}

function testGetNonExistingAttribute() (string) {
    xml book = xmls:parse("<book xmlns:ns0=\"http://sample/com\" ns0:id=\"1234\">Book1</book>");
    string attribute = xmls:getAttribute(book, "{http://sample/com}status");
    system:println(attribute);
    return attribute;
}

function testGetAttributeWithoutNamespace() (string) {
    xml book = xmls:parse("<book xmlns:ns0=\"http://sample/com\" ns0:id=\"1234\" id=\"789\">Book1</book>");
    string attribute = xmls:getAttribute(book, "{}id");
    system:println(attribute);
    return attribute;
}

function testGetAttributeWithoutLocalname() (string) {
    xml book = xmls:parse("<book xmlns:ns0=\"http://sample/com\" ns0:id=\"1234\" id=\"789\">Book1</book>");
    string attribute = xmls:getAttribute(book, "{}");
    system:println(attribute);
    return attribute;
}

function testStrip() (xml, xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("     ");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return x6, xmls:strip(x6);
}

function testStripSingleton() (xml, xml) {
    xml x1 = xmls:parse("<bookId>001</bookId>");
    return x1, xmls:strip(x1);
}

function testStripEmptySingleton() (xml, xml, boolean) {
    xml x1 = xmls:parse("");
    xml x2 = xmls:strip(x1);
    boolean isEmpty = xmls:isEmpty(x2);
    
    return x1, xmls:strip(x2), isEmpty;
}

function testSlice() (xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("<bookName>Book1</bookName>");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, 1, 4);
}

function testSliceAll() (xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("<bookName>Book1</bookName>");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, -1, -1);
}

function testSliceInvalidIndex() (xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("<bookName>Book1</bookName>");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, 4, 1);
}

function testSliceOutOfRangeIndex() (xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("<bookName>Book1</bookName>");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml x6 = x1 + x2 + x3 + x4 + x5;
    return xmls:slice(x6, 4, 10);
}

function testSliceSingleton() (xml) {
    xml x1 = xmls:parse("<bookName>Book1</bookName>");
    return xmls:slice(x1, -1, -1);
}

function testXPathOnCopiedXML() (xml, xml) {
    xml original = xmls:parse("<root><bookName>Book1</bookName><bookId>001</bookId><bookAuthor>Author01</bookAuthor></root>");
    xml copy = xmls:copy(original);

    xmls:remove(original,"/root/bookName");
    xmls:remove(copy,"/root/bookAuthor");

    return original, copy;
}

function testSeqCopy()(xml, xml) {
    xml x1 = xmls:parse("<!-- comment about the book-->");
    xml x2 = xmls:parse("<bookName>Book1</bookName>");
    xml x3 = xmls:parse("<bookId>001</bookId>");
    xml x4 = xmls:parse("<bookAuthor>Author01</bookAuthor>");
    xml x5 = xmls:parse("<?word document=\"book.doc\" ?>");
    
    xml original = x1 + x2 + x3 + x4 + x5;
    xml copy = xmls:copy(original);

    xml x7 = xmls:parse("Updated Book ID");
    xmls:setChildren(x3, x7);
    
    return original, copy;
}