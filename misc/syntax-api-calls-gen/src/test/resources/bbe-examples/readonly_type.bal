import ballerina/io;

public function main() {
    // A value belonging to an inherently immutable basic type can be used directly
    // where an immutable `readonly` value is expected.
    readonly a = 5;
    io:println(a);

    string ballerina = "ballerina";
    readonly b = ballerina;
    io:println(b);

    // A value of a selectively immutable type can be created as immutable, by using
    // an intersection type with `readonly` as the contextually expected type.
    map<int> & readonly marks = {
        math: 80,
        physics: 85,
        chemistry: 75
    };

    // If the inherent type of a value is `T & readonly`, its read-only bit is set,
    // and the value will belong to the `readonly` type.
    readonly c = marks;
    // The `.isReadOnly()` check for an immutable value returns `true`.
    io:println(c);
    io:println(marks.isReadOnly());
}
