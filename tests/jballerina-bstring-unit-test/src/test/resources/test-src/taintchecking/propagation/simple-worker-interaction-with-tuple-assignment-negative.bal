public function main (string... args) {
    worker w1 {
        [string, string] data = [args[0], args[1]];
        data -> w2;
        data = <- w2;
        secureFunction(data[0], data[1]);
    }
    worker w2 {
        [string, string] dataW2;
        dataW2 = <- w1;
        secureFunction(dataW2[0], dataW2[1]);
        dataW2 -> w1;
    }

    f(...args);
}

public function f (string... args) {
    worker w1 {
        [string, string] data = [args[0], args[1]];
        data -> w2;
        data = <- w2;
        secureFunction(data[0], data[1]);
    }
    worker w2 {
        [string, string] dataW2;
        dataW2 = <- w1;
        secureFunction(dataW2[0], dataW2[1]);
        dataW2 -> w1;
    }
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}
