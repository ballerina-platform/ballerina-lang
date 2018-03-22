import ballerina/io;

function main (string[] args) {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    // Other than the XML literal syntax, XML can be parsed using a string. The string should be a valid XML.
    string xmlString = "<book/>";
    xml book =? <xml>xmlString;

    // Get the type of the XML.
    io:println(bookComment.getItemType());

    // Get the name of an XML element.
    io:println(bookName.getElementName());

    // Get the text content of an XML.
    io:println(bookName.getTextValue());

    // Is the XML emtpy?
    io:println(content.isEmpty());

    // Does the element have only one element?
    io:println(content.isSingleton());

    // Get a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println(x);

    // All the element-type items are taken from an XML sequence.
    x = content.elements();
    io:println(x);

    // An element with a particular name can also be retrieved.
    x = content.select("name");
    io:println(x);

    // Set the children of an XML.
    book.setChildren(content);
    io:println(book);

    // Get all the children of an XML.
    x = book.children();
    io:println(x);

    // Get a particular child of an XML.
    x = book.selectChildren("name");
    io:println(x);

    // Remove any text items from an XML sequence that are all whitespace.
    x = content.strip();
    io:println(x);

    // Make a copy of an XML.
    x = bookComment.copy();
    io:println(x);
}
