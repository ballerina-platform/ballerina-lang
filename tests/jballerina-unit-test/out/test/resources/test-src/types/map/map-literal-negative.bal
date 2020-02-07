function testMapWithUnsupportedKey () returns (map<any>) {
    map<any> m = { foo(): "supun"};
    return m;
}

function foo() returns (string) {
  return "name";
}
