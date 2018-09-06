
public function main(string... args) {
    var a = testObjectWithInterface();
}

public function testObjectWithInterface () returns (int, string) {
    Person p = new Person();
    p.init(6);
    Person p1 = new Person();
    p1.init("");
    Person p2 = new Person();
    p2.init(true);
    Foo foo = {count:5, last:" last"};
    Person p3 = new Person();
    p3.init(foo);
    return (p.attachInterface(7), p.month);
}


type Person object {

    public int age = 10,
    public string name,


    string month = "february";


    new (age = 6, string n = "llll") {
        self.name = n;
        int value  = 8 + 7;
    }

    function init(int | string | boolean | Foo inVal) {
        match inVal {
            int a => {
                age = age + a;
            }
            string b => {
                name = name + b;
            }
            boolean c => {
                age = age + 10;
                name = name + " hello";
            }
            Foo d => {
                age = age + d.count;
                name = name + d.last;
            }
        }
    }

    function incrementCount(int increment) returns int {
        int retVal = age;
        if (age > increment) {
            retVal = retVal + increment;
        }
        return retVal;
    }

    function attachInterface(int add) returns int;
};


function Person::attachInterface(int add) returns int {
    int count = self.age + add;
    count  = count + self.incrementCount(100);
    return count;
}

type Foo record {
    int count,
    string last,
};