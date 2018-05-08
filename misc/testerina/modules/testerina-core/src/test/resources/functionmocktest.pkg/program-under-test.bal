
import functionmocktest2.pkg as sub1;

public function intAdd (int a, int b) returns (int) {
    return sub1:intAdd(a, b);
}

function floatAdd (float a, float b) returns (float) {
    return a + b;
}

function stringConcat (string a, string b) returns (string) {
    return a + b;
}
