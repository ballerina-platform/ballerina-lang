package lang.errors.runtime;

function arrayIndexOutOfBoundTest() {
    string name;
    string[] animals;

    animals = ["Lion", "Cat"];
    name = animals[5];
    return;
}

function testStackTrace() {
  string[] fruits;
  string apple;
  apple = getFruit1(fruits);
}

function getFruit1(string[] fruits) (string) {
  return getFruit2(fruits);
}

function getFruit2(string[] fruits) (string) {
  return getApple(fruits);
}

function getApple(string[] fruits) (string) {
  return fruits[24];
}

function testStackOverflow() {
	infiniteRecurse();
}

function infiniteRecurse() {
	infiniteRecurse();
}

function testTypeCastException() {
    string x = "value";

    int y = (int) x;
}