import ballerina/io;

type Student record {
    string name;
    int age;
    Grades grades;
    string...
};

type Grades record {
    int maths;
    int physics;
    int chemistry;
    !...
};

public function main() {
    // This creates an instance of the `Student` record with an empty record literal. The implicit initial value of each
    // field type is assigned to the corresponding field.
    Student s1 = {};
    io:println(s1);

    // This creates an instance of the `Student` record with a non-empty record literal.
    Student s2 = { name: "John Doe", age: 17, grades: {maths: 80, physics: 75, chemistry: 65}};
    io:println(s2);

    // This fetches the value of a specific field in this record. To access fields, use the dot (.) or the index operator.
    io:println(s2.name);
    io:println(s2["name"]);

    // This fetches a field of a nested record.
    io:println(s2.grades.maths);

    // This sets the value of a field.
    s1.name = "Peter";
    s1.age = 19;
    io:println(s1);
    io:println(s2);

    // This adds an additional field not defined in the record type definition above.
    // Note that an attempt to add additional fields to a closed record results in compile errors.
    // e.g., `s1.grades.ict = 77;`
    s1.department = "Computer Science";
    io:println(s1);

    // Records are iterable. Therefore, records can be used with foreach loops. Either the value of the field or
    // both the name and the value of the field can be used as the foreach variable(s).
    // The type of the field name is string. The variable type for the value depends on the types of fields of the record.
    // If the fields and the rest descriptor are of same type (or if it is a closed record, the rest descriptor does not matter),
    // the variable will also be of that same type. If not, the variable will be an `any` variable.
    int total = 0;
    foreach subj, grade in s2.grades {
        total += grade;
        io:println(subj + " : " + grade);
    }

    io:println("Average grade: " + total/3.0);

    // Since records are iterable, the iterable operations `foreach()`, `map()`, `filter()`, `count()` can also be performed on records.
    // In addition to these, if all the fields (including the rest descriptor, if it is an open record) are either of type `int` or `float`,
    // the iterable operations `sum()`, `average()`, `max()` and `min()` are allowed as well.
    map<string> letterGrades = s2.grades.map(mapToLetterGrade)
                                        .filter(function ((string, string) entry) returns boolean {
                                            if (entry[1] == "A" || entry[1] == "B") {
                                                return true;
                                            }
                                            return false;
                                        });

    io:println(letterGrades);
    io:println("Average grade using iterable ops: " + s2.grades.average());
}

function mapToLetterGrade((string, int) entry) returns (string, string) {
    var (subj, grade) = entry;
    if (grade >= 80) {
        return (subj, "A");
    } else if (grade >=70) {
        return (subj, "B");
    } else if (grade >= 60) {
        return (subj, "C");
    } else if (grade >= 50) {
        return (subj, "D");
    }
    return (subj, "F");
}
