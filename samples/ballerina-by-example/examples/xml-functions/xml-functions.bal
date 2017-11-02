function main (string[] args) {
    xml bookName = xml `<name>Book1</name>`;
    xml bookComment = xml `<!--some comment-->`;
    xml someText = xml `Hello, World!`;
    xml content = someText + bookName + bookComment;

    // Other than the XML literal syntax, XML can be parsed using a string. The string should be a valid XML.
    string xmlString = "<book/>";
    var book, _ = <xml>xmlString;

    // Get the type of the XML.
    println(bookComment.getItemType());

    // Get the name of an XML element.
    println(bookName.getElementName());

    // Get the text content of an XML.
    println(bookName.getTextValue());

    // Is the XML emtpy?
    println(content.isEmpty());

    // Does the element have only one element?
    println(content.isSingleton());

    // Get a subsequence of an XML sequence.
    xml x = content.slice(2, 3);
    println(x);

    // All the element-type items from an taken from an XML sequence.
    x = content.elements();
    println(x);

    // An element with a particular name can also be retrieved.
    x = content.select("name");
    println(x);

    // Set the children of an XML.
    book.setChildren(content);
    println(book);

    // Get all the children of an XML.
    x = book.children();
    println(x);

    // Get a particular child of an XML.
    x = book.selectChildren("name");
    println(x);

    // Remove any text items from an XML sequence that are all whitespace.
    x = content.strip();
    println(x);

    // Make a copy of an XML.
    x = bookComment.copy();
    println(x);
}
