import ballerina/io;

function main(string... args) {

    // A complex XML defined using the literal syntax and containing nested elements of different types.
    xml x1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    io:println(x1);

    // Define namespaces. These are visible to all the XML literals defined from this point onwards.  
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/aa" as ns0;

    // Create an XML element. Previously defined namespaces will be added to the element. 
    // The defined prefixes can be applied to elements and attributes inside the element. 
    xml x2 = xml `<book ns0:status="available">
                    <ns0:name>Sherlock Holmes</ns0:name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    io:println(x2);

    // XML interpolated with expressions using the `{{}}` notation. 
    // The expression can be a previously defined variable, arithmetic expressions, or even a function call. 
    // These expressions get evaluated during runtime.
    string rootTag = "{http://ballerina.com/aa}newBook";
    string title = "(Sir)";

    xml x3 = xml `<{{rootTag}}>
                    <name>Sherlock Holmes</name>
                    <author>{{title}} Arthur Conan Doyle</author>
                    <!--Price: ${{ 40 / 5 + 4 }}-->
                  </{{rootTag}}>`;
    io:println(x3);
}
