// Assigning var ref.
string s = "Ballerina";
const string name = s;
public const string name2 = s;

// Assigning var ref.
int a = 10;
const age = a;
public const age2 = a;

// Updating const.
const x = 10;
const int y = 20;

function testAssignment() {
    x = 1;
    y = 2;
}

// Updating const in worker.
function testWorkerInteractions() {
    worker w1 {
        x <- w2;
    }
    worker w2 {
        30 -> w1;
    }
}

const string sVar = 10;

const string m = { name: "Ballerina" };

// Redeclared constant.
const abc = "abc";

const abc = "Ballerina";

// Redeclared variable.
const def = "def";

function test() {
    string def = "def";
}
