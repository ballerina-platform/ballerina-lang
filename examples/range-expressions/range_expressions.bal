import ballerina/io;

public function main() {
    // A range expression `x ... y` can be used to iterate integers
    // from `x` to `y` including `x` and `y` in ascending order.
    io:println("foreach for 25 ... 28");
    foreach int i in 25 ... 28 {
        io:println(i);
    }

    // A range expression `x ..< y` can be used to iterate integers
    // from `x` to `y` including `x` but excluding `y` in ascending order.
    io:println("\nforeach for 25 ..< 28");
    foreach int i in 25 ..< 28 {
        io:println(i);
    }

    // The result of a range expression can also be used by assigning it to
    // an object belonging to the abstract object type `Iterable<int>`.
    abstract object {
        public function __iterator() returns
            abstract object {
                public function next() returns record {|int value;|}?;
            };
    } iterableObj = 25 ..< 28;

    abstract object {
            public function next() returns (record {|int value;|}?);
    } iterator = iterableObj.__iterator();

    io:println("\niterable object for 25 ..< 28");
    while (true) {
        record {| int value; |}? r = iterator.next();
        if (r is record {| int value; |}) {
            io:println(r.value);
        } else {
            // `r` is `()` implying the end of the iteration.
            break;
        }
    }
}
