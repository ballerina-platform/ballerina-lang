import ballerina/io;

public function main() {
    // A tuple-typed binding pattern, which declares the variables `name`, `age`, and
    // `address` of type `string`, `int`, and `string` respectively. The members of the tuple value
    // given by the assignment expression provides the values for the variables.
    [string, int, string] [name, age, address] =
                       ["Jack Smith", 23, "380 Lakewood Dr. Desoto, TX 75115"];
    io:println("Name: ", name, " Age: ", age, " Address: ", address);

    // The binding pattern can also be used by providing variables that were declared beforehand.
    float latitude;
    float longitude;
    [latitude, longitude] = [37.773972, -122.431297];
    io:println("Latitude: ", latitude, " Longitude: ", longitude);

    // Binding patterns can be nested arbitrarily.
    [string, [float, float, float]][eventId, [score1, score2, score3]] =
                                                     ["E335", [9.8, 9.6, 9.5]];
    io:println("Event Id: ", eventId, " Score 1: ", score1,
                " Score 2: ", score2, " Score 3: ", score3);
}
