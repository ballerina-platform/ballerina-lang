import ballerina/test;

public class Foo {
    public function hello() returns string {
        return "hello";
    }
}

public function main() {
    Foo obj = test:mock(Foo);
    assertDiff();
}

function assertDiff() {
    error? err = trap test:assertEquals("hello userr","hello user");
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- actual\n+++ " +
    "expected \n \n @@ -1,1 +1,1 @@ \n \n -hello" +
    " userr\n+hello user\n"));
}
