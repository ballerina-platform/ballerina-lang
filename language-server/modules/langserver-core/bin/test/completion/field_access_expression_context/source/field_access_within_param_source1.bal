type Club record {|
    string name;
    string country;
|};

type Player record {|
    string name;
    string nationality;
    Club club?;

    int age;

    int appearances?;
    int internationalCaps?;
    int goals?;
|};

public function main() {
    Player p = {
        nationality: "England",
        name: "Wayne Rooney",
        age: 29,
        club: {
            name: "Man Utd",
            country: "England"
        },
        appearances: 100
    };

    int val = 100;
    float ab = 120.2;
    string str = "";
    val = doDomething(p.)
}

function doDomething(string p) returns int {
    return 0;
}
