import ballerina/io;

// An object that is a subtype of `Iterator<int>`.
type ArrayIterator object {
    private int[] integers = [0, 1, 1, 2, 3, 5, 8, 13, 21, 34];
    private int cursor = -1;

    // `next` method which generates the sequence of values of type `int`.
    public function next() returns record {|int value;|}? {
        self.cursor += 1;
        if (self.cursor < self.integers.length()) {
            record {|int value;|} nextVal = {value: self.integers[self.cursor]};
            return nextVal;
        }
        return ();
    }
};

//  An object that is a subtype of `Iterable<int>`.
type IteratorGenerator object {

    // The `__iterator()` method should return a new `Iterator<T>`.
    public function __iterator() returns abstract object {
            public function next() returns record {|int value;|}?;} {
        return new ArrayIterator();
    }
};

public function main() {
    IteratorGenerator itrGen = new;
    int i = 0;
    foreach var item in itrGen {
        i += 1;
        io:println("Element ", i, ": ", item);
    }
}
