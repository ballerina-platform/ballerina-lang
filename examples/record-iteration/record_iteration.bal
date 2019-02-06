import ballerina/io;

type Grades record {
    int maths;
    int physics;
    int chemistry;
    !...;
};

public function main() {
    // This creates a `Grades` record.
    Grades grades = {maths: 80, physics: 75, chemistry: 65};

    // The `foreach` variable is a 2-tuple, which consists of the field name and the value of the field.
    // The type of the field name is `string`. The variable type of the value depends on the types of fields of the record.
    // If the fields and the rest descriptor are of same type (the rest descriptor does not matter if it is a closed record),
    // the variable will also be of the same type. If not, the variable will be of type `any`.
    int total = 0;
    foreach var (subject, grade) in grades {
        total += grade;
        io:println(subject, " : ", grade);
    }

    io:println("Average grade: " + total/3.0);

    // Here, the integer grades are mapped to letter grades using the provided function and then
    // entries with either an "A" or "B" grade are filtered out. Iterable operations `map()` and `filter()`
    // always return a `map` when invoked on records. The constraint type of the returned `map` will depend on
    // the types of the values in the resultant `map`. In this case, all the values are `string`. Hence the
    // returned `map` will be of type `map<string>`.
    map<string> letterGrades = grades
                                .map(mapToLetterGrade)
                                .filter(function ((string, string) entry)
                                         returns boolean {
                                    if (entry[1] == "A" || entry[1] == "B") {
                                        return true;
                                    }
                                    return false;
                                });

    io:println("Mapped and filtered letter grades: ", letterGrades);
    io:println("Average grade using iterable ops: ", grades.average());
}

// The mapping function to be used to map integer grades to letter grades.
function mapToLetterGrade((string, int) entry) returns (string, string) {
    var (subject, grade) = entry;
    if (grade >= 80) {
        return (subject, "A");
    } else if (grade >=70) {
        return (subject, "B");
    } else if (grade >= 60) {
        return (subject, "C");
    } else if (grade >= 50) {
        return (subject, "D");
    }
    return (subject, "F");
}
