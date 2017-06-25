import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"The (int, int) in this function indicates that it returns 2 int values."}
function swap (int a, int b) (int x, int y) {
    x = b;
    y = a;
    return x, y;
}

@doc:Description {value:"Named return variables are treated as local variables and are initialized to their default values."}
function defaultValues () (int a, float b, boolean c, string d) {
    return a, b, c, d;
}

@doc:Description {value:"When named returns are present, it is possible to simply use 'return' without explicitly naming them."}
@doc:Description {value:"However, it is not possible to have a mix of named return and normal returns [i.e: (int x, int)]."}
function optionalReturnStmt () (int x, int y) {
    x = 8;
    y = 13;
    return;
}

function main (string[] args) {
    int x = 2;
    int y = 3;

    // The returned values can be assigned to individual variables.
    x, y = swap(x, y);
    system:println("x=" + x + ", y=" + y);

    int a;
    float b;
    boolean c;
    string d;

    a, b, c, d = defaultValues();

    system:println("Default int return value: <" + a + ">");
    system:println("Default float return value: <" + b + ">");
    system:println("Default boolean return value: <" + c + ">");
    system:println("Default string return value: <" + d + ">");

    x, y = optionalReturnStmt();
    system:println("x=" + x + ", y=" +y);
}
