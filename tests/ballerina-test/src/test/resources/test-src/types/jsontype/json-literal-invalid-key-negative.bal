function testJSONWithUnsupportedKey () returns (json) {
    json j = { foo(): "supun"};
    return j;
}

function foo() returns (string) {
  return "name";
}
