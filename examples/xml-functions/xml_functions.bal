import ballerina/io;

function main(string... args) {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    xml book = xml `<book/>`;

    // Get the type of the XML element.
    io:println(bookComment.getItemType());

    // Get the name of an XML element.
    io:println(bookName.getElementName());

    // Get the text content of an XML element.
    io:println(bookName.getTextValue());

    // Check if the XML element is empty.
    io:println(content.isEmpty());

    // Check if the XML element has only one value.
    io:println(content.isSingleton());

    // Get a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println(x);

    // Get all the element-type items in an XML sequence.
    x = content.elements();
    io:println(x);

    // Retrieve an XML element by name.
    x = content.select("name");
    io:println(x);

    // Set the child elements of an XML element.
    book.setChildren(content);
    io:println(book);

    // Get all the child elements of an XML element.
    x = book.*;
    io:println(x);

    // Retrieve a particular child of an XML element by name.
    x = book.selectDescendants("name");
    io:println(x);

    // Remove any text items from an XML sequence that are all whitespace.
    x = content.strip();
    io:println(x);

    // Make a copy of an XML element.
    x = bookComment.copy();
    io:println(x);
}
