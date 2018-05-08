import ballerina/lang.system;
import ballerina/lang.xmls;

function main (string... args) {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    // Other than the XML literal syntax, XML can be parsed using a string. The string should be a valid XML.
    xml book = xmls:parse("<book/>");

    // Get the type of the XML.
    system:println(xmls:getItemType(bookComment));

    // Get the name of an XML element.
    system:println(xmls:getElementName(bookName));

    // Get the text content of an XML.
    system:println(xmls:getTextValue(bookName));

    // Is the XML emtpy?
    system:println(xmls:isEmpty(content));

    // Does the element have only one element?
    system:println(xmls:isSingleton(content));

    // Get a subsequence of an XML sequence.
    xml x = xmls:slice(content, 2, 3);
    system:println(x);

    // All the element-type items from an taken from an XML sequence.
    x = xmls:elements(content);
    system:println(x);

    // An element with a particular name can also be retrieved.
    x = xmls:select(content, "name");
    system:println(x);

    // Set the children of an XML.
    xmls:setChildren(book, content);
    system:println(book);

    // Get all the children of an XML.
    x = xmls:children(book);
    system:println(x);

    // Get a particular child of an XML.
    x = xmls:selectChildren(book, "name");
    system:println(x);

    // Remove any text items from an XML sequence that are all whitespace.
    x = xmls:strip(content);
    system:println(x);

    // Make a copy of an XML.
    x = xmls:strip(bookComment);
    system:println(x);
}
