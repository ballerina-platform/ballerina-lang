public function testFpRestParameterWarning() {
   var bar = foo; 
   _ = bar("", [1,2,3]);
   _ = bar("", getArray());
}

function foo(string b, int... c) returns string {
    return b;
}

function getArray() returns int[] {
    return [1,2,3];
}
