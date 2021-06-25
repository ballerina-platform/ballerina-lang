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
    io:println("First name: ", bookXML/<author>/<fname>);

    // Accessing a non-existing child will return an empty `xml` sequence.
    io:println("ISBN Code: ", bookXML/<ISBN>/<code>);

    // You can also retrieve attributes of the resulting child XML.
    io:println("Name title: ", bookXML/<author>/<fname>.title);

    // You can match descendant elements using the stepping access syntax.
    io:println("First name (match descendants): ", bookXML/**/<fname>);

    // Select all the child items using this syntax.
    io:println("Book child items: ", bookXML/*);

    // Select all the child elements using this syntax.
    io:println("Book child elements: ", bookXML/<*>);

    // Select all the child elements belonging to a specific namespace and then select all of their child items.
    xmlns "http://ballerina.com/a" as bar;
    io:println("Book children in ns: ", bookXML/<bar:*>/*);

    xml seq = bookXML/*;
    // XML sequences can be filtered using XML filter expressions.
    io:println("XML sequence filter name: ", seq.<name>);
    io:println("XML sequence filter year: ", seq.<bar:year>);
}
