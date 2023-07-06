import ballerina/io;

type Details record {|
    int id;
    string country;
|};

// A class definition with both `final` and non-`final` fields.
class Employee {
    string department;
    // `details` is a `final` field to which a value cannot be assigned
    // once the object value is created. However, since the type of `details` is
    // a mutable type, updates can be attempted on the `details` field.
    final Details details;

    function init(string department, Details details) {
        self.department = department;
        self.details = details;
    }
}

// A class definition for a `readonly` object.
// If the class definition includes `readonly`, all the fields in the
// object are considered to be `final` fields.
// The effective type of each field is the intersection of the
// specified type and `readonly`.
readonly class MainController {
    int id;
    string[] codes;

    function init(int id, readonly & string[] codes) {
        self.id = id;
        // The expected type for `codes` is `readonly & string[]`.
        self.codes = codes;
    }

    function getId() returns string {
        return string `Main: ${self.id}`;
    }
}

int currentId = 1000;

public function main() {

    // Define a mutable `Details` value.
    Details details = {
        id: 112233,
        country: "Sri Lanka"
    };

    // Use `details` as the value for the `final` `details` field in `Employee`.
    Employee emp = new ("IT", details);

    io:println("ID: ", emp.details.id);

    // Even though the `details` field of `Employee` is `final`,
    // it can hold a mutable value. Thus, we can attempt updating
    // the `id` field of the `details` field.
    Details empDetails = emp.details;
    empDetails.id = 223344;
    io:println("new ID: ", emp.details.id);

    // Since all the fields in the object constructor expression are `final`
    // and the types are subtypes of `readonly`, it constructs an immutable
    // value, which can be assigned to a variable of type `MainController`
    // which expects immutable values.
    MainController controller = object {
        final int id;
        // A value is set for the `final` field `codes` using an initializer
        // expression.
        // A value cannot be set for this field via the `init` method or
        // once the value is created.
        final string[] & readonly codes = ["AB", "CD"];

        function init() {
            self.id = currentId;
            currentId = currentId + 1;
        }

        function getId() returns string => string `Default: ${self.id}`;
    };

    // Whether a field is `final` does not affect the type.
    // Thus, an object value belongs to a type even if the type
    // specifies `final` for a field for which the source object value
    // does not.
    Employee emp2 = object {
        string department = "finance";
        // The `details` field is not `final` here even though it is
        // a `final` field in `Employee`.
        Details details = {
            id: 1001,
            country: "Sri Lanka"
        };
    };
}
