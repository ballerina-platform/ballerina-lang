import ballerina/io;

public function main() {

    // The XML element with nested children.
    xml bookXML = xml `<book>
                <name>Sherlock Holmes</name>
                <author>
                    <fname title="Sir">Arthur</fname>
                    <mname>Conan</mname>
                    <lname>Doyle</lname>
                </author>
                <bar:year xmlns:bar="http://ballerina.com/a">2019</bar:year>
                <!--Price: $10-->
                </book>`;
    
    // You can access child XML items using XML step expressions.
    io:println(bookXML/<author>/<fname>);

    // Accessing a non-existing child will return an empty `xml` sequence.
    io:println(bookXML/<ISBN>/<code>);

    // You can also retrieve attributes of the resulting child XML.
    io:println(bookXML/<author>/<fname>.title);

    // You can match descendant elements using the following stepping access syntax.
    io:println(bookXML/**/<fname>);

    // Select all children using the bellow syntax.
    io:println(bookXML/*);

    // Select all children elements using the bellow syntax.
    io:println(bookXML/<*>);

    // Select all the children belonging to a specific namespace.
    xmlns "http://ballerina.com/a" as bar;
    io:println(bookXML/<bar:*>/*);

    xml seq = bookXML/*;
    // XML sequences can be filtered using XML filter expressions.
    io:println(seq.<name>);
    io:println(seq.<bar:year>);
}
