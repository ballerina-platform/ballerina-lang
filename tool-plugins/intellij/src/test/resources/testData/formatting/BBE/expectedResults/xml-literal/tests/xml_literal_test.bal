import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function, which will replace the real function.
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    xml xml1 = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    xml xml2 = xml `<book xmlns="http://ballerina.com/" xmlns:ns0="http://ballerina.com/aa" ns0:status="available">
                    <ns0:name>Sherlock Holmes</ns0:name>
                    <author>Sir Arthur Conan Doyle</author>
                    <!--Price: $10-->
                  </book>`;
    xml xml3 = xml `<ns0:newBook xmlns:ns0="http://ballerina.com/aa" xmlns="http://ballerina.com/">
                    <name>Sherlock Holmes</name>
                    <author>(Sir) Arthur Conan Doyle</author>
                    <!--Price: $12-->
                  </ns0:newBook>`;

    test:assertEquals(xml1, outputs[0]);
    test:assertEquals(xml2, outputs[1]);
    test:assertEquals(xml3, outputs[2]);
}
