type Row record {|
    readonly int id;
    string value;
    [int, string][] values;
|};

isolated table<Row> tbl = table [];

function fn1() returns table<Row> {
    lock {
        return tbl;
    }
}
