package ballerina.test;

@Description { value:"Configuration set for test functions."}
@Field {value:"disabled: Flag to disable test functions"}
@Field {value:"groups: List of groups, the test function is included"}
@Field {value:"valueSet: List of valueSets to be parsed for data-driven tests"}
public annotation config attach function {
    boolean disabled;
    string[] groups;
    string dataProvider;
    string beforeFn;
    string afterFn;
    string[] dependsOnFns;

}

@Description { value:"Identoifies beforeSuite function."}
public annotation beforeSuite attach function {}

@Description { value:"Identoifies afterSuite function."}
public annotation afterSuite attach function {}

@Description { value:"Identoifies beforeTest function."}
public annotation before attach function {}

@Description { value:"Identoifies afterTest function."}
public annotation after attach function {}

@Description { value:"Identoifies beforeTest function."}
public annotation beforeEach attach function {}

@Description { value:"Identoifies afterTest function."}
public annotation afterEach attach function {}

