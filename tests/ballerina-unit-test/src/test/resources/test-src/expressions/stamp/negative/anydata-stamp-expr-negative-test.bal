
type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

//-----------------------Anydata Type Stamp Negative Test cases --------------------------------------------------

function stampAnyToObject() returns PersonObj? {

    anydata anyValue = new PersonObj();
    PersonObj? personObj = anyValue.stamp(PersonObj);

    return personObj;
}

