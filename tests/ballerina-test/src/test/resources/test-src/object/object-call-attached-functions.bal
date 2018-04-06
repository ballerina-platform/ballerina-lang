
public function testObjectCallAttachedFunctions () returns (string, string, string, string) {
    Person p = new Person();
    return (p.getNameWrapperInside1(), p.getNameWrapperInside2(), p.getNameWrapperOutside1(), p.getNameWrapperOutside2());
}

type Person object {
    public {
        int age = 10,
        string name = "sample name";
    }

    private {
        int year = 50;
        string month = "february";
    }

    function getName() returns string {
        return name;
    }

    function getNameWrapperInside1() returns string {
        return getName();
    }

    function getNameWrapperInside2() returns string {
        return getNameOut();
    }

    function getNameOut() returns string;

    function getNameWrapperOutside1() returns string;

    function getNameWrapperOutside2() returns string;

};

function Person::getNameOut() returns string {
    return name;
}

function Person::getNameWrapperOutside1() returns string {
    return getName();
}

function Person::getNameWrapperOutside2() returns string {
    return getNameOut();
}



