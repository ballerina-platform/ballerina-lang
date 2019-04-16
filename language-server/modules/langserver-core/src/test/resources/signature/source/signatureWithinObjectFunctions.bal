import ballerina/io;

type TestObject1 object {
    function testOb1Signature1();

    function testOb1Function1() {
        testSignatureAndHover()
    }
};

type TestObject2 object {
    function testOb2Signature1();

    function testOb2Function1() {
    }
};

function TestObject1.testOb1Signature1() {
    // This is the implementation for the signature 1
}


# This function shows the hover and signature content
#
# + param1 - This is the description for `param1`
# + param2 - This is the description for `param2`
function testSignatureAndHover(int param1, string param2) {

}
