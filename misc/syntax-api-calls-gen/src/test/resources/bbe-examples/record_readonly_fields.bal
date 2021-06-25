import ballerina/io;

type Details record {|
    int id;
    string country;
|};

// A record with both read-only and non-read-only fields.
type Employee record {|
    string department;
    // `details` is a `readonly` field which cannot be updated once the value is created.
    // `details` also expects an immutable value, and thus the effective contextually expected type for
    // `details` is `Details & readonly`.
    readonly Details details;
|};

// A record with all read-only fields.
type Identifier record {|
   readonly int id;
   readonly string[] codes;
|};

public function main() {

    // Define an immutable `Details` value.
    Details & readonly immutableDetails = {
        id: 112233,
        country: "Sri Lanka"
    };

    Employee empOne = {
        department: "IT",
        // Since `immutableDetails` was created as an immutable value, it can be used as the value for the `readonly`
        // record field `details`.
        details: immutableDetails
    };

    Employee empTwo = {
        department: "finance",
        // Alternatively, if the constructor expression is defined in-line for the `readonly` field, the value created
        // would be created as an immutable value.
        details: {
            id: 212141,
            country: "USA"
        }
    };

    // `empTwo` is not immutable.
    io:println("empTwo is immutable: ", empTwo.isReadOnly());

    Details details = empTwo.details;
    // But, the `details` field of `empTwo` is immutable.
    io:println("details is immutable: ", details.isReadOnly());

    // Define an `Identifier` value.
    Identifier identifier = {
        id: 1234,
        codes: ["AS", "KL"]
    };

    // If all the fields in a record are `read-only` fields, the record value itself is considered to be immutable.
    // Thus, `identifier` belongs to `readonly`.
    readonly readonlyValue = identifier;
    // The immutable check for `identifier` also evaluates to true.
    io:println("identifier is immutable: ", identifier.isReadOnly());

    record {
        string department;
        Details details;
    } anyEmployee = empOne;

    // Attempting to update a `readonly` field results in a panic.
    anyEmployee.details = {
        id: 222288,
        country: "Sri Lanka"
    };
}
