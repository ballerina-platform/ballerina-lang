public class ExampleObject {
    string taintedData = "";
}

public type ExampleRecord record {
    string taintedData = "";
};

public function secureFunction(@untainted any secureIn, any insecureIn) {

}

public function taintExampleObject (ExampleObject ex, string data) {
    ex.taintedData = data;
}

public function taintExampleRecord (ExampleRecord ex, string data) {
    ex.taintedData = data;
}

public function main (string... args) {
    ExampleObject ex1 = new;
    ex1.taintedData = "static";
    secureFunction(ex1, ex1);

    ExampleObject ex2 = new;
    taintExampleObject(ex2, "static");
    secureFunction(ex2, ex2);

    ExampleRecord ex3 = {};
    ex3.taintedData = "static";
    secureFunction(ex3, ex3);

    ExampleRecord ex4 = {};
    taintExampleRecord(ex4, "static");
    secureFunction(ex4, ex4);
}
