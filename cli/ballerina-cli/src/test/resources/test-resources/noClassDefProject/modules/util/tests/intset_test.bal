import ballerina/test;

@test:Config {}
function test_IntSet_1() {
    IntSet s = new;

    s.add(1);
    s.add(2);
    s.add(1);
    s.add(2);

    test:assertEquals(s.has(1), true);
    test:assertEquals(s.has(2), true);
    test:assertEquals(s.has(3), false);

    test:assertEquals(s.values(), [1,2]);
}

@test:Config {}
function test_IntSet_2() {
    IntSet s = new;

    s.add(1);
    s.add(2);
    s.add(3);
    s.add(4);

    s.remove(0);
    s.remove(2);
    s.remove(2);
    s.remove(4);
    s.remove(4);
    s.remove(5);

    test:assertEquals(s.values(), [1,3]);
}
