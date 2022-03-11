import ballerina/test;

@test:Config {}
function test_cut_1() {
    test:assertEquals(cut(""), ["", ""]);
    test:assertEquals(cut(":"), ["", ""]);
    test:assertEquals(cut("::"), ["", ":"]);
    test:assertEquals(cut("key"), ["key", ""]);
    test:assertEquals(cut("key:"), ["key", ""]);
    test:assertEquals(cut("key:value"), ["key","value"]);
}

@test:Config {}
function test_cut_2() {
    test:assertEquals(cut("", ":separator:"), ["", ""]);
    test:assertEquals(cut(":separator:", ":separator:"), ["", ""]);
    test:assertEquals(cut(":separator::separator:", ":separator:"), ["", ":separator:"]);
    test:assertEquals(cut("key", ":separator:"), ["key", ""]);
    test:assertEquals(cut("key:separator:", ":separator:"), ["key", ""]);
    test:assertEquals(cut("key:separator:value", ":separator:"), ["key", "value"]);
}

@test:Config {}
function test_concat_1() {
    test:assertEquals(concat(["a"], ["b"]), ["a","b"]);
}

@test:Config {}
function test_stringBinaryNumberToInt_1() {
    test:assertEquals(stringBinaryNumberToInt("1"),  1);
    test:assertEquals(stringBinaryNumberToInt("10"), 2);
    test:assertEquals(stringBinaryNumberToInt("11"), 3);
    test:assertEquals(stringBinaryNumberToInt("100"), 4);
    test:assertEquals(stringBinaryNumberToInt("001011100001"), 737);
    test:assertEquals(stringBinaryNumberToInt("100111110011"), 2547);    
}

@test:Config {}
function test_unpad_1() {
    test:assertEquals(unpad(" a b"), "a b");
    test:assertEquals(unpad("a  b "), "a b");
    test:assertEquals(unpad("  a  b    c "), "a b c");
}