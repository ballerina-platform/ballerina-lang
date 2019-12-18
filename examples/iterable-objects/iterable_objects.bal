import ballerina/io;

// The subtype of Iterator<int>
type FibIterator object {
    private int fib1;
    private int fib2;
    private int n;
    private int currentFib;
    private int cursor;

    public function __init(int n) {
        self.cursor = 0;
        self.n = n;
        self.fib1 = 0;
        self.fib2 = 1;
        self.currentFib = 0;
    }

    public function hasNext() returns boolean {
        return self.cursor < self.n;
    }

    // `next` function which generate the sequence of values of type `int`
    public function next() returns record {|
        int value;
    |}? {
        // Fibonacci series generation logic
        if (self.hasNext()) {
            if (self.cursor == 0) {
                self.currentFib = self.fib1;
            } else if (self.cursor == 1) {
                self.currentFib = self.fib2;
            } else {
                self.currentFib = self.fib1 + self.fib2;
                self.fib1 = self.fib2;
                self.fib2 = self.currentFib;
            }
            record {|int value;|} nextVal = {value : self.currentFib};
            self.cursor += 1;
            return nextVal;
        }

        return ();
    }
};

// The subtype of Iterable<int>
type FibGenerator object {
    private int n;

    public function __init(int n) {
        self.n = n;
    }

    // __iterator built-in function always returns a new iterator<int>
    public function __iterator() returns abstract object {
            public function next() returns record {|int value;|}?;} {
        return new FibIterator(self.n);
    }
};

public function main() {
    FibGenerator fibGen = new(10);
    int i = 0;
    foreach var item in fibGen {
	    i += 1;
	    io:println(i, "th fib number: ", item);
    }
}
