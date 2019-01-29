function testJSONWithExpressionKey () returns (json) {
    string a = "key1";
    json j = {a:"Lion", (a):"Cat", getKey():"Dog"};
    return j;
}

function getKey() returns (string) {
  return "key2";
}
