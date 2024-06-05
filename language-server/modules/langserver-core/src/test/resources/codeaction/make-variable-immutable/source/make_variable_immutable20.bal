readonly class Counter {
    isolated function next() returns int => 0;
    function prev() returns int => -1;
}

Counter counter = new;

public isolated function main() {
    int _ = counter.next();
}
