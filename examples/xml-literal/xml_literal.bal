import ballerina/io;

public function main() {

    // A complex XML defined using the literal syntax, which contains nested elements of different types.
    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    io:println(x1);

    // Defines namespaces. These are visible to all the XML literals defined from this point onwards.  
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/aa" as ns0;

    // Creates an XML element. Previously-defined namespaces will be added to the element. 
    // The defined prefixes can be applied to elements and attributes inside the element. 
    xml x2 = xml `<book ns0:status="available">
                    <ns0:name>Sherlock Holmes</ns0:name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    io:println(x2);

    // XML can be interpolated with expressions using the `${}` notation.
    // The expression can be a previously-defined variable, arithmetic expressions, or even a function call. 
    // These expressions are evaluated during the runtime.
    string title = "(Sir)";

    xml x3 = xml `<ns0:newBook>
                    <name>Sherlock Holmes</name>
                    <author>${title} Arthur Conan Doyle</author>
                    <!--Price: $${ 40 / 5 + 4 }-->
                  </ns0:newBook>`;
    io:println(x3);
}
