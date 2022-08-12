type AnnotationProcessor function (string name, function f) returns boolean;

AnnotationProcessor[] annotationProcessors = [
    processConfigAnnotation,
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation,
    processBeforeGroupsAnnotation,
    processAfterGroupsAnnotation
];

function processAnnotation(string name, function f) {
    boolean annotationProcessed = false;
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if (annotationProcessor(name, f)) {
            annotationProcessed = true;
            break;
        }
    }

    // Process the register functions under the test factory method.
    // Currently the dynamic registration does not support groups filtration.
    if !annotationProcessed && filterGroups.length() == 0 {
        testRegistry.addFunction(name = name, executableFunction = f);
    }
}

function processConfigAnnotation(string name, function f) returns boolean {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        if !config.enable || (filterGroups.length() == 0 ? false : !hasGroup(config.groups, filterGroups))
            || (filterDisableGroups.length() == 0 ? false : hasGroup(config.groups, filterDisableGroups)) {
            return true;
        }
        DataProviderReturnType? params = ();
        error? diagnostics = ();
        if config.dataProvider != () {
            DataProviderReturnType|error providerOutput = trap config.dataProvider();
            if providerOutput is error {
                diagnostics = error(
                    string `Failed to execute the data provider for '${name}', ` + "\n" + providerOutput.message());
            } else {
                params = providerOutput;
            }
        }
        config.groups.forEach(group => groupStatusRegistry.incrementTotalTest(group));
        testRegistry.addFunction(name = name, executableFunction = f, params = params, before = config.before,
        after = config.after, groups = config.groups, diagnostics = diagnostics);
        return true;
    }
    return false;
}

function processBeforeSuiteAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@BeforeSuite;
    if isTrue == true {
        beforeSuiteRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterSuiteAnnotation(string name, function f) returns boolean {
    AfterSuiteConfig? config = (typeof f).@AfterSuite;
    if config != () {
        afterSuiteRegistry.addFunction(name = name, executableFunction = f, alwaysRun = config.alwaysRun);
        return true;
    }
    return false;
}

function processBeforeEachAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@BeforeEach;
    if isTrue == true {
        beforeEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterEachAnnotation(string name, function f) returns boolean {
    boolean? isTrue = (typeof f).@AfterEach;
    if isTrue == true {
        afterEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processBeforeGroupsAnnotation(string name, function f) returns boolean {
    BeforeGroupsConfig? config = (typeof f).@BeforeGroups;
    if config != () {
        TestFunction testFunction = {
            name: name,
            executableFunction: f
        };
        config.value.forEach(group => beforeGroupsRegistry.addFunction(group, testFunction));
        return true;
    }
    return false;
}

function processAfterGroupsAnnotation(string name, function f) returns boolean {
    AfterGroupsConfig? config = (typeof f).@AfterGroups;
    if config != () {
        TestFunction testFunction = {
            name: name,
            executableFunction: f,
            alwaysRun: config.alwaysRun
        };
        config.value.forEach(group => afterGroupsRegistry.addFunction(group, testFunction));
        return true;
    }
    return false;
}

function hasGroup(string[] groups, string[] filter) returns boolean {
    foreach string group in groups {
        if filter.indexOf(group) is int {
            return true;
        }
    }
    return false;
}
