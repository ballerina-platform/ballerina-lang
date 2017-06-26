import ballerina.lang.system;
import ballerina.lang.xmls;

function main (string[] args) {

    // Declare an un-initialized XML. Value would be null
    xml x;

    // Create a XML text
    xml someText = xmls:parse("Hello, World!");

    // Create a XML element
    xml book = xmls:parse("<book/>");
    xml bookName = xmls:parse("<name>Book1</name>");

    // Create a XML comment 
    xml bookComment = xmls:parse("<!-- comment about the book-->");

    // Create a processing instruction
    xml bookMeta = xmls:parse("<?doc document=\"book.doc\" ?>");

    // Get the type of the XML
    system:println(xmls:getItemType(bookComment));

    // Get the name of an XML element
    system:println(xmls:getElementName(bookName));

    // Get the text content of an XML
    system:println(xmls:getTextValue(bookName));

    // Set an attribute of an XML element
    xmls:setAttribute(book, "{http://ballerina.com/}year", "ns0", "2017");
    system:println(book);

    // Get an attribute of an XML element
    string year = xmls:getAttribute(book, "{http://ballerina.com/}year");
    system:println(year);

    // Concatenate XML. This will produce an XML sequence
    xml content = someText + bookName + bookComment + bookMeta;
    system:println(content);

    // Check whether an XML is exmpty
    system:println(xmls:isEmpty(content));

    // Check whether an XML sequence has only one element in it
    system:println(xmls:isSingleton(content));

    // Get a subsequence of an XML sequence
    x = xmls:slice(content, 2, 4);
    system:println(x);

    // Get all the element-type items from an XML sequence
    x = xmls:elements(content);
    system:println(x);

    // Get a particular element-type item from an XML sequence
    x = xmls:select(content, "name");
    system:println(x);

    // Set the children of an XML
    xmls:setChildren(book, content);
    system:println(book);

    // Get all the children of an XML
    x = xmls:children(book);
    system:println(x);

    // Get a particular child of an XML
    x = xmls:selectChildren(book, "name");
    system:println(x);

    // Remove any text items from an XML sequence that are all whitespace
    x = xmls:strip(content);
    system:println(x);

    // Make a copy of an XML
    x = xmls:strip(bookComment);
    system:println(x);
}
