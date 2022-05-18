public type Record record {|
    int[] arr;
|};

public type Location record {|
    int line;
    int column;
|};

public type ErrorDetail record {|
    string message;
    Location[] locations?;
    (int|string)[] path?;
    map<anydata> extensions?;
|};

public type ServerError distinct error<record {| json? data?; ErrorDetail[] errors; map<json>? extensions?; |}>;

public function fn() returns ServerError {
    return error("", errors = []);
}

public type Tuple int|[string, Tuple];

public type RecordTwo record {
    object {
        int i;
    } ob;
};

public type TupleTwo RecordTwo|map<RecordTwo>;

public type Foo record {
    readonly Bar a;
    record {
        Bar d;
    } b;
};

public type Bar record {
    int[] c;
};
