# Configuration set for test functions.
#
# + enable - Flag to enable/disable test functions
# + groups - List of groups that this test function belongs to
# + dataProvider - Name of the function which will be used to feed data into this test
# + before - Name of the function to be run before the test is run
# + after - Name of the function to be run after the test is run
# + dependsOn - A list of function names the test function depends on, and will be run before the test
public type TestConfig record {
    boolean enable = true;
    string[] groups;
    string dataProvider;
    string before;
    string after;
    string[] dependsOn;
};

# Configuration of the function to be mocked.
#
# + packageName - Name of the package that the function to be mocked resides in
# + functionName - Name of the function to be mocked
public type MockConfig record {
    string packageName = ".";
    string functionName;
};

public annotation<function> Config TestConfig;

public annotation<function> Mock MockConfig;

# Identifies beforeSuite function.
public annotation<function> BeforeSuite;

# Identifies afterSuite function.
public annotation<function> AfterSuite;

# Identifies beforeTest function.
public annotation<function> BeforeEach;

# Identifies afterTest function.
public annotation<function> AfterEach;
