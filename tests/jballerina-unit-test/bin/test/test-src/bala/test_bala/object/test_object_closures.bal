import testorg/foo;
import testorg/utils;

public function testObjectWithClosuresFromFoo() {
    object {
        public int number;
        public function getNumber() returns int;
    } obj = foo:createObject();
    utils:assertEquality(300, obj.number);
    utils:assertEquality(500, obj.getNumber());
}
