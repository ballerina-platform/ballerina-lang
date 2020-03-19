public function main() {
    int ans = intAdd(5, 3);
}


//
// FUNCTIONS
//

// This should be mocked
public function intAdd(int a, int b) returns (int) {
    return a+b;
}

// This should NOT be mocked
public function intSubtract(int a, int b) returns (int) {
    return a-b;
}
