int gInt = 5;
string gStr = "str";
boolean gBool = true;
string gNewStr = callFunc();
json gJson = null;
Person gPerson = new;
byte gByte = 255;

public function main(string... args) {
    int x = 10;
    int z = gInt + x;
    int y = x + z;
    Foo foo = { count: 5, last: "last" };
    Person p = new;
}

type Foo record {
    int count;
    string last;
};

class Person {
    public int age = 0;
    public string name = "";
    public Person? parent = ();
    private string email = "default@abc.com";

    function init() {

    }
}

function callFunc() returns (string) {
    string newStr = "ABCDEFG";
    newStr = newStr + " HIJ";
    return newStr;
}