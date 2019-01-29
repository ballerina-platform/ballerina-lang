import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function, which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
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

    test:assertEquals(outputs[0], xml1);
    test:assertEquals(outputs[1], xml2);
    test:assertEquals(outputs[2], xml3);
}
