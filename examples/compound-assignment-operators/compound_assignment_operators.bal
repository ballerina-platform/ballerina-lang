import ballerina/io;

public function main() {
    int value = 1;
    io:println("value: ", value);

    // Compound `add` operator.
    value += 7;
    io:println("value += 7: ", value);

    // Compound `subtract` operator.
    value -= 2;
    io:println("value -= 2: ", value);

    // Compound `divide` operator.
    value /= 3;
    io:println("value /= 3: ", value);

    // Compound `multiply` operator.
    value *= 2;
    io:println("value *= 2: ", value);

    // Compound `and` operator.
    value &= 4;
    io:println("value &= 4: ", value);

    // Compound `or` operator.
    value |= 3;
    io:println("value |= 3: ", value);

    // Compound `xor` operator.
    value ^= 5;
    io:println("value ^= 5: ", value);

    // Compound `left shift` operator.
    value <<= 1;
    io:println("value <<= 1: ", value);

    // Compound `right shift` operator.
    value >>= 1;
    io:println("value >>= 1: ", value);

    // Compound `logical shift` operator.
    value >>>= 1;
    io:println("value >>>= 1: ", value);
}
