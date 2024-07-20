class EvenNumberGenerator {
    int i = 0;

    public isolated function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

isolated stream<int, error?> evenStream = new (object {
    int i = 0;
    public isolated function next() returns (record {|int value;|}|error)? {
        self.i += 2;
        return {value: self.i};
    }
});

function fn1() returns stream<int, error?> {
    lock {
        return evenStream;
    }
}
