import ballerina/lang.regexp;

type Type2 record {|
    regexp:RegExp pattern;
    string target;
|};

function fillRecordFieldsCodeAction2() {
    Type2 type2 = {};
}
