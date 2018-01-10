string output = "";

function concatString (string value) {
    output = output + value;
}

function concatIntString (int i, string v) {
    output = output + i + ":" + v + " ";
}

struct Week {
    string[] days;
}

function testNestedForeach () (string) {
    Week w = {days:["mon", "tue", "wed", "thu", "fri"]};
    string[] people = ["tom", "bob", "sam"];
    output = "";
    foreach i, s in w.days {
        concatIntString(i, s);
        foreach _, k in people {
            concatIntString(i, k);
        }
        concatString("\n");
    }
    return output;
}