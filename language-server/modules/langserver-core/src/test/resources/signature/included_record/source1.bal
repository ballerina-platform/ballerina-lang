
# Description
#
# + field1 - Field1 Description
# + field2 - Field2 Description
type Record1 record {|
    int field1;
    int field2;
|};

# Description
#
# + r2field1 - Field21 Description
# + r2field2 - Field22 Description
type Record2 record {|
    int r2field1;
    string r2field2;
|};


function testFunction1(int arg1, int arg2) {

}

function testFunction2(int arg1, int arg2, *Record1 rec1, *Record2 rec2) {

}

function testFunction3(*Record1 rec1, *Record2 rec2) {

}

public function main() {
    testFunction1(1, 2);
    testFunction2(1, 2, field1 = 0, field2 = 0, r2field1 = 0, r2field2 = "");
    testFunction3(field1 = 0, field2 = 0, r2field1 = 0, r2field2 = "");
}
