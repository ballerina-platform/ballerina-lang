function testJSONWithUnsupportedKey () (json) {
    json j = { foo(): "supun"};
    return j;
}

function foo() (string) {
  return "name";
}
