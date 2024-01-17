function testFunction(int param1, int param2) returns int {
    foreach int i in 0...9 {
        if i==1 {
            return param1 * param2;
        }
    }

    return 0;
}
