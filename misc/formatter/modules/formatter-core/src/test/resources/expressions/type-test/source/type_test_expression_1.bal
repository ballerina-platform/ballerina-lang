function getVal(int v) returns any {
   return v;
}

public function foo() {
   boolean f = getVal(8)   is    float;
}

function process(map<Details> entities) {
    string[] entitiesWithLocation = from var [name, details] in entities.entries() where details?.location is string select name;
}
