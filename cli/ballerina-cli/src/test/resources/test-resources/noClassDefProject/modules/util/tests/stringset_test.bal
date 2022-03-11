import ballerina/test;

@test:Config {}
function test_StringSet_1() {
    StringSet s = new;

    s.add("a");
    s.add("b");
    s.add("a");
    s.add("b");

    test:assertEquals(s.has("a"), true);
    test:assertEquals(s.has("b"), true);
    test:assertEquals(s.has("c"), false);

    test:assertEquals(s.values(), ["a", "b"]);
}

@test:Config {}
function test_StringSet_2() {
    StringSet s = new;

    s.add("a");
    s.add("b");
    s.add("c");
    s.add("d");

    s.remove("");
    s.remove("b");
    s.remove("b");
    s.remove("d");
    s.remove("d");
    s.remove("e");

    test:assertEquals(s.values(), ["a", "c"]);
}

@test:Config {}
function test_StringSet_union_1() {
    StringSet a = new;

    a.add("a");
    a.add("b");
    a.add("c");
    a.add("d");

    StringSet b = new;

    a.add("c");
    a.add("d");
    b.add("e");
    b.add("f");

    StringSet c = union(a, b);

    test:assertEquals(c.values(), ["a", "b", "c", "d", "e", "f"]);
}

@test:Config {}
function test_StringSet_intersection_1() {
    StringSet a = new;

    a.add("a");
    a.add("b");
    a.add("c");
    a.add("d");

    StringSet b = new;

    b.add("c");
    b.add("d");
    b.add("e");
    b.add("f");

    StringSet c = intersection(a, b);

    test:assertEquals(c.values(), ["c", "d"]);
}

@test:Config {}
function test_StringSet_intersection_2() {
    StringSet a = new;

    a.add("a");
    a.add("b");

    StringSet b = new;

    b.add("e");
    b.add("f");

    StringSet c = intersection(a, b);

    test:assertEquals(c.values(), []);
}

@test:Config {}
function test_StringSet_difference_1() {
    StringSet a = new;

    a.add("a");
    a.add("b");
    a.add("c");
    a.add("d");

    StringSet b = new;

    b.add("c");
    b.add("d");
    b.add("e");
    b.add("f");

    StringSet c = difference(a, b);

    test:assertEquals(c.values(), ["a", "b"]);
}
