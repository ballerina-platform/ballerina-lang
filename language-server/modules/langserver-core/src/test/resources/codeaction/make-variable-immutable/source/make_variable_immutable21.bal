isolated class Counter {
    isolated function next() returns int => 0;
}

Counter counter = new;

public isolated function main() {
    int _ = counter.next();
}
