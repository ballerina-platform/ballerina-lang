import ballerina/io;

public function main() {
    // An array-typed binding pattern, which declares the variables `score1`, `score2`, and
    // `score3` of type `float`. The members of the array value given by the assignment
    // expression provides the values for the variables.
    float[3] scores = [9.8, 9.6, 9.5];
    [float, float, float] [score1, score2, score3] = scores;
    io:println("Score 1: ", score1, " Score 2: ", score2, " Score 3: ", score3);
}
