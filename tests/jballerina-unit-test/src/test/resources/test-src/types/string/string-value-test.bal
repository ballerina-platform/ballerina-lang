function concatBMP() returns string {
    string prefix = "red ";
    string s = "apple";
    return prefix + s;
}

function nonBMPLength() returns (int) {
    string smiley = "h😀llo";
    return smiley.length();
}

function recordStringValuePut() returns () {
    string smiley = "h😀llo";
    record {| string myField; |} r = {myField: smiley};
    //TODO: return r
}

function testError() returns int {
    string smiley = "h🤷llo";
    error err = error(smiley);
    return err.reason().length();
}

function testArrayStore() returns int {
    string[] arr = [];
    string[][] arr2 = [["h🤷llo", "h🤷llo", "h🤷llo"], ["h🤷llo", "h🤷llo", "h🤷llo"]];
    arr[0] = "h🤷llo";
    return arr[0].length() + arr2[0][1].length();
}

type Person object {
    string helloField = "h🤷llo";
    string lastName;

    function __init(string last) {
        self.lastName = last;
    }

    function appendName(string append) returns string {
        return self.helloField + append;
    }
};


function testObjects() returns int {
    Person p = new("h😀llo");
    string s = p.appendName("h😀llo") + p.lastName;
    return s.length();
}