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
    error? err = trap test:assertEquals("hello user","hello userr");
    error result = <error>err;
    test:assertTrue(result.message().toString().endsWith("--- expected\n+++ " +
    "actual \n \n @@ -1,1 +1,1 @@ \n \n -hello" +
    " userr\n+hello user\n"));
}
