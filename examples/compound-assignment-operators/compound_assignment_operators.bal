import ballerina/io;

public function main() {
    int value = 1;
    io:println("initial value: ", value);

    // Compound `add` operator.
    value += 7;
    io:println("add: ", value);

    // Compound `subtract` operator.
    value -= 2;
    io:println("subtract: ", value);

    // Compound `divide` operator.
    value /= 3;
    io:println("divide: ", value);

    // Compound `multiply` operator.
    value *= 2;
    io:println("multiply: ", value);

    // Compound `and` operator.
    value &= 4;
    io:println("and: ", value);

    // Compound `or` operator.
    value |= 3;
    io:println("or: ", value);

    // Compound `xor` operator.
    value ^= 5;
    io:println("xor: ", value);

    // Compound `left shift` operator.
    value <<= 1;
    io:println("left shift: ", value);

    // Compound `right shift` operator.
    value >>= 1;
    io:println("right shift: ", value);

    // Compound `logical shift` operator.
    value >>>= 1;
    io:println("logical shift: ", value);
}
