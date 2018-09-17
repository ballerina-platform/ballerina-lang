int gInt = 5;
string gStr = "str";
boolean gBool = true;
byte gByte = 255;

public function main(string... args) {
    int x = 10;
    int z = gInt + x;
    int y = x + z;
    Foo foo = { count: 5, last: "last" };
    Person p = new;
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
