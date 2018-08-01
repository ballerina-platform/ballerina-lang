int gInt = 5;
string gStr = "str";
boolean gBool = true;
float gFloat = 12.4;
byte gByte = 255;
Foo gRec = { count: 5, last: "last" };
Person gObj = new;
@final int gConst = 5;

function main(string... args) {
    gObj.name = "abc";
    gObj.age = 21;
    gInt = gInt + 5;
    int y = gInt + 10;
}

type Foo record {
    int count,
    string last,
};

type Person object {
public int age,
public string name,
public Person? parent,
private string email = "default@abc.com",
};