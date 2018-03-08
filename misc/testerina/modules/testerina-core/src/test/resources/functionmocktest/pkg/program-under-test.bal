package src.test.resources.functionmocktest.pkg;

import src.test.resources.functionmocktest2.pkg as sub1;

public function intAdd (int a, int b) (int) {
    return sub1:intAdd(a, b);
}

function floatAdd (float a, float b) (float) {
    return a + b;
}

function stringConcat (string a, string b) (string) {
    return a + b;
}
