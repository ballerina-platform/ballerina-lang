import ballerina/jballerina.java;

public const string LKA = "LKA";
public const string LK = "LK";
public const string USA = "USA";

public type CountryCode LK|LKA|USA;

# Address record
#
# + street - street of the address
# + city - city of the address
# + countryCode - country code of the address
public type Address record {|
    string street;
    string city;
    CountryCode countryCode;
|};

# Person record
public type Person record {|
    # name of the person
    string name;
    # age of the person
    int age;
    # country code of the person
    CountryCode countryCode;
|};

# `Apartment` record in the *town*
# `test` documentation row
#
# + number - number of the apartment
# + street - street of the apartment
# + countryCode - country code of the apartment
public type Apartment record {|
    # apartment no
    int number;
    # apartment street
    string street;
    # apartment country-code
    CountryCode countryCode;
|};

# Student object
#
# + name - student name
# + age - student age
public class Student {
    public string name = "Saman";
    public int age = 15;
}

# `Teacher` object in *school* located in **New York**
# `Senior` teacher of the school
public class Teacher {
    # Teacher name
    public string name = "Saman";
    # Teacher age
    public int age = 15;
}


# Employee object
#
# + empNo - employee number
# + age - employee age
public class Employee {
    # funny number
    public string empNo = "E100546";
    # funny age
    public int age = 15;
}

# Prints `Hello World`.
public function main() {
    println("Hello World!");
}

// helper functions

function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
