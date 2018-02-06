function foo() (string) {
  return "name";
}

function testMapWithUnsupportedKey () (map) {
    map m = { foo: "supun"};
    return m;
}
