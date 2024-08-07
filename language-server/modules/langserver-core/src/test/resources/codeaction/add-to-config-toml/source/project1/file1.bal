configurable boolean bool = false;
configurable int i = ?;
configurable int:Signed16  pp = ?;
configurable xml[] xmls = ?;
configurable boolean[] bools = ?;
configurable decimal[] decimals = ?;
configurable string[] strs = ?;
configurable [string, float, float...] tuple = ?;

configurable map<int> intMap = ?;
configurable map<int>[] mapArray = ?;

type Person record {|
    string id;
    string name;
|};

configurable Person person = ?;

type Bake record {|
    string bake;
    string cake;
|};

configurable Person[] people = ?;

configurable table<map<string>> users = ?;

configurable table<map<string>>[] userTeams = ?;

enum Country {
    LK = "Sri Lanka",
    US = "United States"
}

configurable Country country = ?;
