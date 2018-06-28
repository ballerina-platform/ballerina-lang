import ballerina/io;

function main(string... args) {
    io:println("Documentation attachments are non executable constructs in Ballerina.");
}

// This is the documentation attachment for object `DummyObject`. You may use Markdown syntax to style the text in these descriptions.
documentation {
    `DummyObject` is a user defined object type in Ballerina program. This
    `DummyObject` is used to hold two string data fields and
    one attached function definition, `doThatOnObject` performs that functionality
    on the associated `DummyObject` instance.

    F{{fieldOne}} This is the description for the `DummyObject`'s field `fieldOne`.
                  `F` doc prefix is used to mark a field on Object.
    F{{fieldTwo}} This is the description for the `DummyObject`'s field `fieldTwo`.
                  `F` doc prefix is used to mark a field on Object.
}
public type DummyObject object {

    public {
        string fieldOne;
        string fieldTwo;
    }

    // This is the documentation attachment for function `doThatOnObject`. You may use Markdown syntax to style the text in these descriptions.
    documentation {
        `doThatOnObject` is an attached function for the object `DummyObject`.

        P{{paramOne}} This is the description for the `doThatOnObject` function
                      parameter. `P` doc prefix is used to mark a parameter for
                      a function.
        R{{}} This is the description for the `doThatOnObject` function Return
              value. `R` doc prefix is used to mark a return value for a function.
    }
    public function doThatOnObject(string paramOne) returns (boolean);

};

// This is the documentation attachment for record `DummyType`. You may use Markdown syntax to style the text in these descriptions.
documentation {
   `DummyType` is a user defined record type in Ballerina program.
              This `DummyType` is used to hold a string type data field and an int type
              data field.

    F{{fieldOne}} This is the description for the `DummyType`'s field `fieldOne`.
                  `F` doc prefix is used to mark a field on Record.
    F{{fieldTwo}} This is the description for the `DummyType`'s field `fieldTwo`.
                  `F` doc prefix is used to mark a field on Record.
}
public type DummyType record {
    string fieldOne,
    int fieldTwo,
};
