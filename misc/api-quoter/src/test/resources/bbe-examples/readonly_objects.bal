import ballerina/io;

// An object type descriptor.
type Controller object {
   int id;
   string[] codes;

   function getId() returns string;
};

// A `readonly` class definition.
// If the class definition includes `readonly`, all the fields in the
// object are effectively `final` and the values assigned to the fields
// have to be immutable.
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

public function main() {

    // A new expression used with a `readonly class` constructs an immutable value.
    // An object constructor expression or a new expression also constructs immutable values if
    // all of its fields are `final` fields and the types of each individual field is a subtype
    // of `readonly`.
    // In either case, the fields cannot be updated once the value is created.
    MainController mc = new MainController(123, ["AB"]);

    // A `readonly` intersection (`T & readonly`) can be used with an object type descriptor.
    // Only immutable values belong to such an intersection.
    // Since `MainController` is defined as a `readonly class`, the object value `mc` is considered
    // to be immutable. Thus, it can be used where a `readonly` value is expected.
    Controller & readonly immutableController = mc;

    io:println("immutableController ID: ", immutableController.getId());

    Controller controller = immutableController;
    io:println("controller is immutable: ",
                                        controller is readonly & Controller);
}
