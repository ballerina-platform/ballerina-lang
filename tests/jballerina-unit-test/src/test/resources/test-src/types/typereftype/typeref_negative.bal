type ImmutableIntArray int[] & readonly;
type IntArray int[];
type IntOrBoolean int|boolean;
type Foo string;
type FooBar "foo"|1;
type FunctionTypeOne function (int i) returns string;
type FunctionTypeTwo function () returns string;

function testTypeReferenceNegative() {
    ImmutableIntArray intArr = "A";

    Foo foo = true;

    FunctionTypeOne func1 = function (IntOrBoolean x) returns int {
            return 1;
        };

    FunctionTypeTwo func2 = function (FooBar i) returns int {
        return 1;
    };

    var func3 = function (Foo foo1) returns int {
        return 1;
    };
    var res1 = func3(10);

    var func4 = function (Foo foo2) returns Foo {
        return 1;
    };
    var res2 = func4("foo");

    string _ = getImmutable();
}

function getImmutable() returns ImmutableIntArray {
    return [1,2, 3];
}

public class Student {
    string name;
    int id;
    float avg = 80.0;
    public function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

type StudentRef Student;

function testObjectTypeReferenceNegative() {
    StudentRef st1 = "A";

    StudentRef st2 = new ("John", 1);
    string a = st2;
}

type BTable table<map<int>>|BarTable;

type BarTable table<BarRec>key(a);

type BarRec record {
    readonly string a;
};

function testTableTypeReferenceNegative() {
    BarTable tab1 = "a";

    BTable tab2 = table key(a) [{a : "a"}];
    string a = tab2;
}
