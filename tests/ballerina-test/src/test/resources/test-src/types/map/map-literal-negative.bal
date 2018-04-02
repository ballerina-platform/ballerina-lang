function testMapWithUnsupportedKey () returns (map) {
    map m = { foo(): "supun"};
    return m;
}

function foo() returns (string) {
  return "name";
}
