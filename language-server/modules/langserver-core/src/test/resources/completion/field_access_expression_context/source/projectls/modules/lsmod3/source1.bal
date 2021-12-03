import projectls.lsmod2;

public type Document record {|
    string uri;
    int id;
|};

function testFunction() {
    lsmod2:Range | lsmod2:Edit | Document doc = {
        u
    }
}
