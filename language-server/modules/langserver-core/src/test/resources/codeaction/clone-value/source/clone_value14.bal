type Row record {|
    readonly int id;
    string value;
    [int, string][] values;
|};

isolated table<Row> tbl = table [];

function fn1(table<Row> a) {
    lock {
        tbl  = a;
    }
}
