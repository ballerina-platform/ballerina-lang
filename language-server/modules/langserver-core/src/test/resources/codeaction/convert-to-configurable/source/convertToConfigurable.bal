public int code = 12500;
boolean isAdmin = true;
int port = 9000;
byte bVal = 44;
float height = 5.6;
decimal salary = 50.56;
string host = "localhost";
string url = string `http://www.ballerina.io/"`;
xml book = xml `<book>Ballerina</book>`;

string[] strs = ["toml", "yaml", "json"];
[string, int, boolean] tuple = ["ballerina", 2024, true];

map<string> man = {
    name: "Anna",
    city: "Paris"
};

Person person = {
    name: "Anna",
    age: 30,
    i: ()
};

table<map<string>> users = table [];

int|string value = "string";
anydata data = "123";
json j = 12;

type Person record {
    string name;
    int age;
    () i;
};
