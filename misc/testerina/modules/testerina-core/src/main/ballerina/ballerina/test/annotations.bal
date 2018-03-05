package ballerina.test;

@Description { value:"Configuration set for test functions."}
@Field {value:"disabled: Flag to disable test functions"}
@Field {value:"groups: List of groups, the test function is included"}
@Field {value:"valueSet: List of valueSets to be parsed for data-driven tests"}
public annotation config attach function {
    boolean disabled;
    string[] groups;
    string dataProvider;
    string before;
    string after;
    string[] dependsOn;

}

@Description { value:"Identoifies beforeSuite function."}
public annotation beforeSuite attach function {}

@Description { value:"Identoifies afterSuite function."}
public annotation afterSuite attach function {}

@Description { value:"Identoifies beforeTest function."}
public annotation beforeEach attach function {}

@Description { value:"Identoifies afterTest function."}
public annotation afterEach attach function {}

