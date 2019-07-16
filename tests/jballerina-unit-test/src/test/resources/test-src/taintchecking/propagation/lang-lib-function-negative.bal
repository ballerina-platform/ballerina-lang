type Person record {
    string name;
};

function getTaintedParson() returns @tainted Person {
    return { name: "Arthur Dent" };
}

function sensitiveF(@untainted Person p) {
    // pass
}

function driver() {
    Person p = getTaintedParson();
    json jp = <json> json.constructFrom(p);
    Person sameP = <Person> Person.constructFrom(jp);
    sensitiveF(sameP);
    sensitiveF(sameP.cloneReadOnly());
}

function driver2() {
    Person p = getTaintedParson();
    sensitiveF(p.cloneReadOnly());
}
