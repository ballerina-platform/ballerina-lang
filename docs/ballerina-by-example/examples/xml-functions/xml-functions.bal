import ballerina/io;

function main (string[] args) {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    // Other than the XML literal syntax, XML can be parsed using a string. The string should be a valid XML.
    string xmlString = "<book/>";
    xml book = <xml>xmlString;

    // Get the type of the XML element.
    io:println(bookComment.getItemType());

    // Get the name of an XML element.
    io:println(bookName.getElementName());

    // Get the text content of an XML element.
    io:println(bookName.getTextValue());

    // Check if the XML element is emtpy.
    io:println(content.isEmpty());

    // Check if the XML element has only one value.
    io:println(content.isSingleton());

    // Get a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println(x);

    // All the element-type items are taken from an XML sequence.
    x = content.elements();
    io:println(x);

    // An XML element with a particular name can be retrieved.
    x = content.select("name");
    io:println(x);

    // Set the children of an XML element.
    book.setChildren(content);
    io:println(book);

    // Get all the children of an XML element.
    x = book.*;
    io:println(x);

    // Get a particular child of an XML element.
    x = book.selectChildren("name");
    io:println(x);

    // Remove any text items from an XML sequence that are all whitespace.
    x = content.strip();
    io:println(x);

    // Make a copy of an XML element.
    x = bookComment.copy();
    io:println(x);
}
