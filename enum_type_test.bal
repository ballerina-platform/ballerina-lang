// Test file to reproduce enum type reference issue
enum DepartmentCode {
    HR,
    IT,
    FINANCE
}

type Department record {
    string name;
    DepartmentCode code;
};

public function main() {
    Department dept = {name: "Engineering", code: IT};
}