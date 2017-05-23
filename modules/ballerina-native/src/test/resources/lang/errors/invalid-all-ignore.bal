function test1()(string, float){
    string a = "a";
    float b = 0.0;
    _, _, _ = testMultiReturn1();
    return a,b;
}

function testMultiReturn1()(string, int, float){
    return "a", 1, 2.0;
}


