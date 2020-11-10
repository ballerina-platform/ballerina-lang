// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# The `DummyObject` is a user-defined object.
# This `DummyObject` has two `string` data fields and a
# function definition (i.e., `doThatOnObject`), which performs a certain
# functionality on the associated `DummyObject` instance.
#
# + fieldOne - This is the description of the `DummyObject`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyObject`'s `fieldTwo` field.
public type DummyObject object {

    public string fieldOne;
    public string fieldTwo;
    # The `doThatOnObject` function is attached to the `DummyObject` object.
    #
    # + paramOne - This is the description of the parameter of
    #              the `doThatOnObject` function.
    # + return - This is the description of the return value of
    #            the `doThatOnObject` function.
    public function doThatOnObject(string paramOne) returns boolean;
};

# `DummyRecord` is a user-defined record.
# This `DummyRecord` has a `string` data field and an `int` data field.
#
# + fieldOne - This is the description of the `DummyRecord`'s `fieldOne` field.
# + fieldTwo - This is the description of the `DummyRecord`'s `fieldTwo` field.
public type DummyRecord record {
    string fieldOne;
    int fieldTwo;
};

# This function returns the `fieldTwo` field of the `DummyRecord`-typed
# record value passed as an argument.
#
# + recordValue - Parameter of type `DummyRecord`.
# + return - The `fieldTwo` field of the record value passed as an argument.
public function dummyFunction(DummyRecord recordValue) returns int {
    return recordValue.fieldTwo;
}

# Country code for Sri Lanka.
# # Deprecated
# This constant is deprecated. Use the constant `LK` instead.
@deprecated
public const string LKA = "LKA";

# New country code for Sri Lanka.
public const string LK = "LK";
# Country code for USA.
public const string USA = "USA";
public type CountryCode LK|LKA|USA;

@deprecated
public type Address record {|
    string street;
    string city;
    CountryCode countryCode;
|};

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

};
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
