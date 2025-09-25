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
    Department dept = {
        name: "Engineering",
        code: IT  // Correct enum value access in Ballerina
    };
    
    // This will be tested via the Compiler API
    // to verify that DepartmentCode field returns TYPE_REFERENCE, not UNION
}