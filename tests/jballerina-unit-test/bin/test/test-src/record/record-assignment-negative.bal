public function invalidRecordAssignment() returns error? {
    int x = 10;
    record { int i; record { string name;}j;} a = { i : 10, j : { name : "a"}};
    a = x;
}
