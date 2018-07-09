import ballerina/io;
import ballerina/test;

any[] outputs = [];
int counter = 0;
// This is the mock function which will replace the real function
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
    // Invoking the main function
    main();
    test:assertEquals("ToUpper: LION IN TOWN. CATCH THE LION ", outputs[0]);
    test:assertEquals("ToLower: lion in town. catch the lion ", outputs[1]);
    test:assertEquals("EqualsIgnoreCase: true", outputs[2]);
    test:assertEquals("SubString: Lion", outputs[3]);
    test:assertEquals("Contains: true", outputs[4]);
    test:assertEquals("IndexOf: 2", outputs[5]);
    test:assertEquals("LastIndexOf: 26", outputs[6]);
    test:assertEquals("ValueOf: 5.8", outputs[7]);
    test:assertEquals("ReplaceFirst: Tiger in Town. Catch the Lion ", outputs[8]);
    test:assertEquals("Replace: Tiger in Town. Catch the Tiger ", outputs[9]);
    test:assertEquals("ReplaceAll: Li0n in T0wn. Catch the Li0n ", outputs[10]);
    test:assertEquals("Length: 29", outputs[11]);
    test:assertEquals("Trim: Lion in Town. Catch the Lion", outputs[12]);
    test:assertEquals("HasSuffix: true", outputs[13]);
    test:assertEquals("HasPrefix: true", outputs[14]);
    test:assertEquals("Unescape: Lion in Town. Catch the Lion ", outputs[15]);
    test:assertEquals("Split: Lion", outputs[16]);
    test:assertEquals("Split: in", outputs[17]);
    test:assertEquals("Split: Town.", outputs[18]);
    test:assertEquals("Bytes: Lion in Town. Catch the Lion ", outputs[19]);
}
