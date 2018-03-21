function testJSONWithExpressionKey () (json) {
    string a = "key1";
    json j = {a:"Lion", (a):"Cat", getKey():"Dog"};
    return j;
}

function getKey() (string) {
  return "key2";
}
