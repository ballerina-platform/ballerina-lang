type BaseRecord record {|
    string name;
    string:RegExp pattern1;
    RegExpRef2 pattern2;
|};

type FinalRecordType record {|
    *BaseRecord;
    TableTypeRef tbl;
|};

type RegExpRef1 string:RegExp;

type RegExpRef2 RegExpRef1;

type Foo record {|
    readonly int id;
    string name;
|};

type TableType table<Foo> key(id);

type TableTypeRef TableType;

function testFillRecordCodeAction() {
    FinalRecordType rec = {};
}
