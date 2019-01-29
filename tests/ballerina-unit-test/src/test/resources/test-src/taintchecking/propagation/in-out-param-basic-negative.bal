type ExampleObject object {
    string taintedData = "";
};

type ExampleRecord record {
    string taintedData = "";
};

public function secureFunction(@sensitive any secureIn, any insecureIn) {

}

public function taintExampleObject (ExampleObject ex, string data) {
    ex.taintedData = data;
}

public function taintExampleRecord (ExampleRecord ex, string data) {
    ex.taintedData = data;
}

public function inOutParamWithAnnotation (@tainted ExampleObject ex) {

}

public function main (string... args) {
    ExampleObject ex1 = new;
    ex1.taintedData = args[0];
    secureFunction(ex1, ex1);

    ExampleObject ex2 = new;
    taintExampleObject(ex2, args[0]);
    secureFunction(ex2, ex2);

    ExampleRecord ex3 = {};
    ex3.taintedData = args[0];
    secureFunction(ex3, ex3);

    ExampleRecord ex4 = {};
    taintExampleRecord(ex4, args[0]);
    secureFunction(ex4, ex4);

    ExampleObject ex5 = new;
    inOutParamWithAnnotation(ex5);
    secureFunction(ex5, ex5);
}
