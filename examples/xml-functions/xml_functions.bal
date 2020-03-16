import ballerina/io;
import ballerina/lang.'xml as xmllib;

public function main() {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    xmllib:Element book = <xmllib:Element> xml `<book/>`;

    // Gets the name of an XML element.
    xmllib:Element bookNameElem = <xmllib:Element> bookName;
    io:println(bookNameElem.getName());

    // Concatenates xml and string values.
    xml concat = xmllib:concat(someText, bookName, bookComment);
    io:println(concat);
    io:println(content == concat);

    // Get the number of xml items in a sequence.
    io:println(concat.length());

    // Gets a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println(x);

    // Gets all the element-type items in an XML sequence.
    x = content.elements();
    io:println(x);

    // Sets the children elements of an XML element.
    book.setChildren(content);
    io:println(book);

    // Strips the insignificant parts of the an xml value.
    // Comment items, processing instruction items are considered insignificant.
    x = content.strip();
    io:println(x);
}
