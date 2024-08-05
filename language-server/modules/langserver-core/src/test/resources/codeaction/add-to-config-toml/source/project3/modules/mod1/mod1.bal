configurable string xc = "host";
configurable boolean bool = false;
configurable int i = ?;
configurable int:Signed16  pp = ?;
configurable byte b = ?;
configurable float f = ?;
configurable decimal d = ?;
configurable string s = ?;
configurable xml x =

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
