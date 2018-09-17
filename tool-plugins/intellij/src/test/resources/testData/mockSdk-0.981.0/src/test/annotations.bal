
@Description { value:"Configuration set for test functions."}
@Field {value:"enable: Flag to enable/disable test functions"}
@Field {value:"groups: List of groups that this test function belongs to"}
@Field {value:"dataProvider: Name of the function which will be used to feed data into this test"}
@Field {value:"before: Name of the function to be run before the test is run"}
@Field {value:"after: Name of the function to be run after the test is run"}
@Field {value:"dependsOn: A list of function names the test function depends on, and will be run before the test"}
public type TestConfig record {
    boolean enable = true;
    string[] groups;
    string dataProvider;
    string before;
    string after;
    string[] dependsOn;
};

@Description { value:"Configuration of the function to be mocked."}
@Field {value:"packageName: Name of the package that the function to be mocked resides in"}
@Field {value:"functionName: Name of the function to be mocked"}
public type MockConfig record {
    string packageName = ".";
    string functionName;
};

public annotation <function> Config TestConfig;

public annotation <function> Mock MockConfig;

@Description { value:"Identifies beforeSuite function."}
public annotation <function> BeforeSuite;

@Description { value:"Identifies afterSuite function."}
public annotation <function> AfterSuite;

@Description { value:"Identifies beforeTest function."}
public annotation <function> BeforeEach;

@Description { value:"Identifies afterTest function."}
public annotation <function> AfterEach;

