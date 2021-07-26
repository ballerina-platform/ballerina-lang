import ballerina/module1;

public function main() {
    int varInt = 1;
    string varString = "Hello";
    IntTypeDef varIntTypeDef = 1;
    RecordTypeDec varRecTypeDef = {
        field1: 0,
        field2: 0
    };

    string hello = string `test ${functionwithIntReturn()}`;
}

function functionwithIntReturn() returns int {
    return 123;
}

function functionwithRecordTypeDecReturn() returns RecordTypeDec {
    return {
        field1: 0,
        field2: 0
    };
}

type IntTypeDef int;

type RecordTypeDec record {
    int field1;
    int field2;
};

public function main2() {
    int a = 10;
    string val = "My Str";
    string another = string `Str is:`;
    string url = "https://ballerina.io";
    xml last = xml `<books>
        <book>
            <title gender="${another}">${g}</title>
        </book>
    </books>`;
}

function getXml() returns xml:Element {
    xml:Element e = xml `<test></test>`;
    
    return e;
}
