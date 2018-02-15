package ballerina.test;

@Description { value:"Configuration set for test functions."}
@Field {value:"disabled: Flag to disable test functions"}
@Field {value:"groups: List of groups, the test function is included"}
@Field {value:"valueSet: List of valueSets to be parsed for data-driven tests"}
public annotation config attach function {
    boolean disabled;
    string[] groups;
    string[] valueSets;
}
