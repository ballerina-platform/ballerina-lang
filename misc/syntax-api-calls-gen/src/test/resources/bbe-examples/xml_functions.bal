import ballerina/io;
import ballerina/lang.'xml as xmllib;

public function main() {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    xmllib:Element book = <xmllib:Element> xml `<book/>`;

    // Get the name of an XML element.
    xmllib:Element bookNameElem = <xmllib:Element> bookName;
    io:println("Element name: ", bookNameElem.getName());

    // Concatenate XML and string values.
    xml concat = xmllib:concat(someText, bookName, bookComment);
    io:println("Concat: ", concat);
    io:println("Equals: ", content == concat);

    // Get the number of XML items in a sequence.
    io:println("Length: ", concat.length());

    // Get a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    io:println("Subsequence: ", x);

    // Get all the element-type items in an XML sequence.
    x = content.elements();
    io:println("All XML elements: ", x);

    // Set the child elements of an XML element.
    book.setChildren(content);
    io:println("Child elements set: ", book);

    // Strip the insignificant parts of an XML value.
    // Comments and processing instruction items are considered as insignificant.
    x = content.strip();
    io:println("Stripped: ", x);
}
