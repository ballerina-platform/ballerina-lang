import ballerina/io;
import ballerina/test;

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
    test:assertEquals(outputs[0], "ToUpper: LION IN TOWN. CATCH THE LION ");
    test:assertEquals(outputs[1], "ToLower: lion in town. catch the lion ");
    test:assertEquals(outputs[2], "EqualsIgnoreCase: true");
    test:assertEquals(outputs[3], "SubString: Lion");
    test:assertEquals(outputs[4], "Contains: true");
    test:assertEquals(outputs[5], "IndexOf: 2");
    test:assertEquals(outputs[6], "LastIndexOf: 26");
    test:assertEquals(outputs[7], "ValueOf: 5.8");
    test:assertEquals(outputs[8], "ReplaceFirst: Tiger in Town. Catch the Lion ");
    test:assertEquals(outputs[9], "Replace: Tiger in Town. Catch the Tiger ");
    test:assertEquals(outputs[10], "ReplaceAll: Li0n in T0wn. Catch the Li0n ");
    test:assertEquals(outputs[11], "Length: 29");
    test:assertEquals(outputs[12], "Trim: Lion in Town. Catch the Lion");
    test:assertEquals(outputs[13], "HasSuffix: true");
    test:assertEquals(outputs[14], "HasPrefix: true");
    test:assertEquals(outputs[15], "Unescape: Lion in Town. Catch the Lion ");
    test:assertEquals(outputs[16], "Split: Lion");
    test:assertEquals(outputs[17], "Split: in");
    test:assertEquals(outputs[18], "Split: Town.");
    test:assertEquals(outputs[19], "Bytes: Lion in Town. Catch the Lion ");
}
