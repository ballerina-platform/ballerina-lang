public function main() {
    int v01_intVar = 10;
    int v02_intVar = 1;
    int v03_intVar = 5;
    int z = 0;

    // debug engage in 'if' statement
    if (v01_intVar == 10 && v02_intVar == 1) {
        z = 1;
    }

    // debug engage in 'else' statement
    if (v01_intVar < v02_intVar && v03_intVar == 5) {
        z = 2;
    } else {
        z = 3;
    }

    // debug engage in 'else-if' statement
    if (v02_intVar < 0 && v03_intVar == 5) {
        z = 4;
    } else if (v02_intVar > 0 && v03_intVar == 5) {
        z = 5;
    } else {
        z = 6;
    }

    // debug engage in 'while' loop
    int v04_intVar = 0;
    while (true) {
        if (v04_intVar >= 1) {
            break;
        }
        v04_intVar = v04_intVar + 1;
        z += 1;
    }

    // debug engage in 'foreach' loop
    string[] v05_fruits = ["apple", "orange"];
    foreach string v in v05_fruits {
        string fruit = v;
        z += 1;
    }

    // debug engage in 'match' statement
    int[1] v06_intArray = [7];
    int v07_intVar;
    foreach var counter in v06_intArray {
        match counter {
            7 => {
                v07_intVar = 7;
            }
        }
    }

    // debug engage in lambda - iterable arrow operation
    map<string> v08_words = { a: "ant"};
    map<string> v09_animals = v08_words.'map(
        word => word.toUpperAscii()
    );

    // debug engage in Asynchronous function call (Non-blocking calls)
    future<int> v10_future = start sum(40, 50);
    _ = wait v10_future;
}

function sum(int a, int b) returns int {
    return a + b;
}
