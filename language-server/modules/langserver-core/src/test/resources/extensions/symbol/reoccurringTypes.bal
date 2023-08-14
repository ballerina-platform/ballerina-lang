public type IntOrString int|string;

public type RecA record {
    string name;
};

public type SampleRecord record {|
    IntOrString[] field1?;
    IntOrString[] field2?;
    RecA rec1?;
    RecA rec2?;
|};

function transform(string s) returns SampleRecord => {};
