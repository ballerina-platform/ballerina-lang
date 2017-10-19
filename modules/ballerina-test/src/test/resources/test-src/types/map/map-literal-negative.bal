function testMapWithUnsupportedKey () (map) {
    map m = { foo(): "supun"};
    return m;
}

function foo() (string) {
  return "name";
}
