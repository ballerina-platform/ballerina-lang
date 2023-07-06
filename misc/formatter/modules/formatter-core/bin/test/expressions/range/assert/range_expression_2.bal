public function foo() {
    object {
        public function __iterator() returns object {
            public isolated function next() returns record {|int value;|}?;
        };
    } iterableObj = 25 ..< 28;
}
