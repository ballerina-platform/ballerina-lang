import ballerina/io;

public function main() {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    xml book = xml `<book/>`;

    // Gets the type of the XML element.
    io:println(bookComment.getItemType());

    // Gets the name of an XML element.
    io:println(bookName.getElementName());

    // Gets the text content of an XML element.
    io:println(bookName.getTextValue());

    // Checks if the XML element is empty.
    io:println(content.isEmpty());

    // Checks if the XML element has only one value.
    io:println(content.isSingleton());

    // Gets a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println(x);

    // Gets all the element-type items in an XML sequence.
    x = content.elements();
    io:println(x);

    // Retrieves an XML element by its name.
    x = content.select("name");
    io:println(x);

    // Sets the children elements of an XML element.
    book.setChildren(content);
    io:println(book);

    // Gets all the children elements of an XML element.
    x = book.*;
    io:println(x);

    // Retrieves a particular child of an XML element by its name.
    x = book.selectDescendants("name");
    io:println(x);

    // Removes any text items from an XML sequence that are all whitespaces.
    x = content.strip();
    io:println(x);

    // Makes a copy of an XML element.
    x = bookName.copy();
    io:println(x);
}
