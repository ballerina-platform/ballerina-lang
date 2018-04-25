import ballerina/io;

function main(string... args) {
    io:println("Documentation attachments are non executable constructs in Ballerina.");
}

documentation {
    `DummyObject` is a user defined object type in Ballerina program. You may use Markdown syntax to style text on these Descriptions.

    F{{fieldOne}} This is the description for the DummyObject's field `fieldOne`. `F` doc prefix is used to mark a field on Object.
    F{{fieldTwo}} This is the description for the DummyObject's field `fieldTwo`. `F` doc prefix is used to mark a field on Object.
}
public type DummyObject object {

    public {
        string fieldOne;
        string fieldTwo;
    }

    documentation {
        `doThatOnObject` is an attached function for the object `DummyObject`. You may use Markdown syntax to style text on these Descriptions.

        P{{paramOne}} This is the description for the `doThatOnObject` function parameter. `P` doc prefix is used to mark a parameter for a function.
        R{{}} This is the description for the `doThatOnObject` function Return value. `R` doc prefix is used to mark a return value for a function.
    }
    public function doThatOnObject(string paramOne) returns (boolean);

};

documentation {
   `DummyType` is a user defined record type in Ballerina program. You may use Markdown syntax to style text on these Descriptions.

    F{{fieldOne}} This is the description for the DummyType's field `fieldOne`. `F` doc prefix is used to mark a field on Record.
    F{{fieldTwo}} This is the description for the DummyType's field `fieldTwo`. `F` doc prefix is used to mark a field on Record.
}
public type DummyType {
    string fieldOne,
    int fieldTwo,
};