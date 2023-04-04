# Test connector to test all the Ballerina syntaxes
# array, tuple, map, record, inline record, closed record
#
# + ci - Class integer field
# + ca - Class record field
@display {label: "Test Client"}
public client class Client {
    int ci;
    A ca;

    // Lines starting with `#` contain structured documentation in Markdown format.
    # Client constructor
    #
    // Documents the parameters of the function.
    # + ci - First Parameter - `int` type
    # + ca - Second Parameter - `A` *record* type
    // Documents the return parameter of the function
    # + return - Return Value Description
    public isolated function init(int ci, A ca) returns error? {
        self.ci = ci;
        self.ca = ca;
    }

    # Remote function - no parameters no return
    @display {label: "Test client empty function"}
    remote isolated function fun1() {
    }

    # Resource function - no query parameter or path parameters
    # + return - Return json or error
    @display {label: "Test client get function"}
    resource isolated function get .() returns json|error? {
    }

    resource isolated function get users/[string name](@display {label: "query param i"}int i, string s) returns string {
        return s;
    }

    resource isolated function post paths/[string... rsts](int i, string... s) returns string {
        return "";
    }

    resource isolated function put path1/path2/path3(float f, decimal d, boolean b, xml? x, json j = {}) returns json|error? {
        return j;
    }

    remote isolated function fun4(int[] iarr, [string, int] tple, map<string> maps, table<A> key(i) tbl) returns int[]|error? {
        return;
    }

    remote isolated function fun5(string|int unon, stream<int, error?> strm, any 'any, anydata anyd, error? eop) returns stream<int, error?>|() {
        return;
    }

    remote isolated function fun6(Color c, record {|int i; string s;|} rec, typedesc<record {}>? tdesc) returns typedesc<record {}>|error? {
        return;
    }

    remote isolated function fun7(A a, B b, Obj obj) returns A|error? { // TODO: Obj is not supported
        return;
    }

    remote isolated function fun8(C c, D d, *E x) returns [string, int]|error? {
        return;
    }

    resource isolated function put .(F f, G g) returns json {
        return {"i": g.i, "f": g.h.j};
    }

}

# Description
#
# + i - Field Description  
# + f - Field Description  
public type A record {|
    readonly int i;
    float f;
|};

# Description
#
# + s - Field Description
public type B record {|
    *A;
    string s;
|};

# Description
#
# + a - Field Description  
# + b - Field Description  
# + d - Field Description
public type C record {
    *B;
    A a;
    boolean b;
    decimal d?;
};

# Description
#
# + x - Field Description  
# + farr - Field Description  
# + str2darr - Field Description  
# + tple - Field Description  
# + rsttple - Field Description  
# + maps - Field Description  
# + tbl - Field Description  
# + unon - Field Description
public type D record {
    xml x;
    float[] farr;
    string[3][2] str2darr;
    [string, int] tple;
    [int...] rsttple;
    map<string> maps;
    table<A> key(i) tbl;
    string|int unon;
};

# Description
#
# + strm - Field Description  
# + er - Field Description  
# + eop - Field Description  
# + anyd - Field Description  
# + 'any - Field Description  
# + tdesc - Field Description
public type E record {
    stream<int, error?> strm;
    error er;
    error? eop;
    anydata anyd;
    any 'any;
    typedesc<record {}> tdesc?;
};

# Description
#
# + j - Field Description  
# + rec - Field Description
public type F record {
    json j;
    record {|
        int i;
        string s;
    |} rec;
};

# Cyclic relation record
# G -> H -> J -> G
#
# + i - Field Description  
# + h - Field Description
public type G record {|
    int i;
    H h;
|};

# Description
#
# + f - Field Description  
# + j - Field Description
public type H record {|
    float f;
    J j;
|};

# Description
#
# + d - Field Description  
# + g - Field Description
public type J record {|
    decimal d;
    G g;
|};

# Description
#
public class Obj {
    # Object field description
    public string s;

    function init(string s) {
        self.s = s;
    }

    function getS() returns string {
        return self.s;
    }
}

# Color type
# enum type template - ```enum {value1, value2, value3}```
# For more information, see [Ballerina learn](https://ballerina.io/learn/)
public enum Color {
    RED,
    GREEN,
    BLUE
}
