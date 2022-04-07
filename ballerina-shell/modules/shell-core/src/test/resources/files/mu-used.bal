type Entry map<json>;

type RoMap readonly & map<Entry>;

function loadMap() returns RoMap {
    //...
    return {"Hello": {"msg": "world"}};
}

final RoMap m = loadMap();

function lookup(string s) returns readonly & Entry? {
    // OK because m is final and type of m is subtype of readonly
    return m[s];
}
