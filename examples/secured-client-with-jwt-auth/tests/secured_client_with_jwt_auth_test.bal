// TODO: Resolve with https://github.com/ballerina-platform/ballerina-lang/issues/15487
//import ballerina/test;
//import ballerina/log;
//
//string log = "";
//
//// This is the mock function which will replace the real function
//@test:Mock {
//    moduleName: "ballerina/log",
//    functionName: "printInfo"
//}
//public function mockPrintInfo(string|(function () returns (string)) msg) {
//    if (msg is string) {
//        log = msg;
//    }
//}
//
//@test:Config
//function testFunc() {
//    // Invoking the main function
//    main();
//    test:assertEquals(log, "Hello, World!!!");
//}
