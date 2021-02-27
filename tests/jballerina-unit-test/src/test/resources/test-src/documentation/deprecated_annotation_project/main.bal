import ballerina/io;

//========== MODULE-LEVEL CONST DECL ========================

# Country code for Sri Lanka.
# # Deprecated
# This constant is deprecated. Use the constant `LK` instead.
@deprecated
public const string LKA = "LKA";

# New country code for Sri Lanka.
public const string LK = "LK";
# Country code for USA.
public const string USA = "USA";

//========== MODULE-LEVEL TYPE DECL =========================
//========== 1.union types ==================================
@deprecated
public type CountryCode LK|LKA|USA;

public type NewCountryCode LK|USA;

//========= 2.finite types ==================================
@deprecated
public type State "on"|"off";

public type NewState "on"|"off";


//========= 3.objects =======================================
// also used for object member methods test

@deprecated
public class Person {
    public string firstName = "John";
    public string lastName = "Doe";

    Address addr = {
        street: "Palm Grove",
        city: "Colombo 3",

        countryCode: LKA
    };

    @deprecated
    public function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    public function getCity() returns string {
        return self.addr.city;
    }
}

public class Student {
    @deprecated
    public string name = "Saman";
    public int age = 15;

    @deprecated
    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }
}

//========= 4.records =======================================

# Address record
#
# + street - street Parameter Description
# + city - city Parameter Description
# + countryCode - countryCode Parameter Description
# # Deprecated
@deprecated
public type Address record {|
    string street;
    string city;
    CountryCode countryCode;
|};

# Bank record
#
# + street - street Parameter Description
# + city - city Parameter Description
# + countryCode - countryCode Parameter Description
public type Bank record {|
    string street;
    string city;
    CountryCode countryCode;
|};

//========= 5.errors ========================================

const INVALID_ACC_TYPE = "InvalidAccountType";
type InvalidAccountTypeErrorData record {
    string message?;
    error cause?;
    string accountType;
};

# Invalid account error
# # Deprecated
@deprecated
public type InvalidAccountTypeError error<InvalidAccountTypeErrorData>;

# Invalid bank error
public type InvalidBankTypeError error<InvalidAccountTypeErrorData>;

//========= MODULE_LEVEL ANNOTATION DECL ====================

# This record defines the fields of the @hello:Greeting annotation.
#
# + salutation - The greeting message
public type HelloConfiguration record {|
    string salutation = "Hello!";
|};

# This record defines the fields of the @hello:Bye annotation.
#
# + salutation - The greeting message
public type ByeConfiguration record {|
    string salutation = "Hello!";
|};

# Define an annotation named `Greeting`. Its type is `HelloConfiguration` and it can be
# attached to functions.
public annotation HelloConfiguration Greeting on function;

# Define an annotation named `Bye`. Its type is `ByeConfiguration` and it can be
# attached to functions.
#
# # Deprecated
@deprecated
public annotation ByeConfiguration Bye on function;

//========= MODULE-LEVEL FUNCTION DEF =======================

# Creates and returns a `Person` object given the parameters.
#
# + fname - First name of the person
# + lname - Last name of the person
# + street - Street the person is living at
# + city - The city the person is living in
# + countryCode - The country code for the country the person is living in
# + return - A new `Person` object
#
# # Deprecated
# This function is deprecated since the `Person` type is deprecated.
@deprecated
public function createPerson(string fname, string lname, string street,
                             string city, CountryCode countryCode) returns Person {
    Person p = new;
    p.firstName = fname;
    p.lastName = lname;
    p.addr = {street, city, countryCode};
    return p;
}

# say hello
#
# + name - name person want to say hello
public function sayHello(string name) {
    io:println("Hello " + name);
}

//========= REQUIRED AND DEFAULTABLE FUNCTION PARAMS ======

# Create full name
#
# + title - title of the person
# + fName - first name
# + mName - middle name
# + lName - last name
# + return - full name
public function getFullName(string title, @deprecated string fName,  string mName = "Chris",
                                                    @deprecated string lName = "Wood") returns string {
    return fName + mName + lName;
}

//========= OBJECT MEMBERS ================================

public class Player {
    @deprecated
    public string name = "Saman";
    public int age = 15;

    @deprecated
    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }
}

// main method

public function main() {
    Person p = createPerson("Jane", "Doe", "Castro Street", "Mountain View", USA);
    io:println(p.getFullName());
}
