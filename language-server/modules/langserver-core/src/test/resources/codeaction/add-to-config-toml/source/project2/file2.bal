configurable byte b = ?;
configurable float f = ?;
configurable decimal d = ?;
configurable string s = ?;
configurable xml x = ?;
configurable int[] ints = ?;
configurable byte[] bytes = ?;
configurable float[] floats = ?;

type Food record {
    string name;
    int cal;
    Bake b;
};

type Diet record {
    Food food;
    int age;
};

configurable Diet input = ?;

configurable int|string code = ?;
configurable anydata data = ?;
configurable json payload = ?;
