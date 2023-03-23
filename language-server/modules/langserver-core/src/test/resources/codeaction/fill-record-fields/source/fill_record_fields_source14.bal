type Result record {|
    string name;
    string college;
    int grade;
|};

type Student record {|
    string name;
    int age;
|};

function filterResults(Student[] students, map<int> grades) returns Result[] {
    Result[] results = from var student in students
                        let int lgrade = (grades[student.name] ?: 0), string targetCollege = "Stanford"
                        where lgrade > 75
                        select {};
    return results;
}
