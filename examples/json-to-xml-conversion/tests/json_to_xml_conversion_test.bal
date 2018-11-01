import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();

    xml xml1 = xml `<Store id="AST">
                           <name>Anne</name>
                           <address>
                              <street>Main</street>
                              <city>94</city>
                           </address>
                           <codes>
                              <item>4</item>
                              <item>8</item>
                           </codes>
                      </Store>`;

    xml xml2 = xml `<Store id="AST">
                           <name>Anne</name>
                           <address>
                              <street>Main</street>
                              <city>94</city>
                           </address>
                           <codes>
                              <wrapper>4</wrapper>
                              <wrapper>8</wrapper>
                           </codes>
                        </Store>`;

    test:assertEquals(outputs[0], xml1);
    test:assertEquals(outputs[1], xml2);
}
