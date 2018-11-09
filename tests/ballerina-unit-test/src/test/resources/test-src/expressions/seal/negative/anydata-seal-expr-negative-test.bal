
type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

//-----------------------Anydata Type Seal Negative Test cases --------------------------------------------------

function sealAnyToObject() returns PersonObj? {

    anydata anyValue = new PersonObj();
    PersonObj? personObj = anyValue.seal(PersonObj);

    return personObj;
}

