type AnnotationProcessor function (string name, function f) returns boolean|error;

AnnotationProcessor[] annotationProcessors = [
    processConfigAnnotation,
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation,
    processBeforeGroupsAnnotation,
    processAfterGroupsAnnotation
];

function processAnnotation(string name, function f) returns error? {
    boolean annotationProcessed = false;
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if (check annotationProcessor(name, f)) {
            annotationProcessed = true;
            break;
        }
    }

    // Process the register functions under the test factory method.
    // Currently the dynamic registration does not support groups filtration.
    if !annotationProcessed && options.groups.length() == 0 && checkTest(name) {
        testRegistry.addFunction(name = name, executableFunction = f);
    }
}

function processConfigAnnotation(string name, function f) returns boolean|error {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        if !config.enable || !(options.groups.length() > 0 ? checkGroup(config.groups) : true && checkTest(name)) {
            return true;
        }
        DataProviderReturnType? params = ();
        if config.dataProvider != () {
            DataProviderReturnType|error providerOutput = trap config.dataProvider();
            if providerOutput is error {
                return error(string `Failed to execute the dataProvider for '${name}', ${providerOutput.message()}`);
            } else {
                params = providerOutput;
            }
        }
        config.groups.forEach(group => groupStatusRegistry.incrementTotalTest(group));
        testRegistry.addFunction(name = name, executableFunction = f, params = params,
            before = config.before, after = config.after, groups = config.groups);
        return true;
    }
    return false;
}

function processBeforeSuiteAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@BeforeSuite;
    if isTrue == true {
        beforeSuiteRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterSuiteAnnotation(string name, function f) returns boolean|error {
    AfterSuiteConfig? config = (typeof f).@AfterSuite;
    if config != () {
        afterSuiteRegistry.addFunction(name = name, executableFunction = f, alwaysRun = config.alwaysRun);
        return true;
    }
    return false;
}

function processBeforeEachAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@BeforeEach;
    if isTrue == true {
        beforeEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processAfterEachAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@AfterEach;
    if isTrue == true {
        afterEachRegistry.addFunction(name = name, executableFunction = f);
        return true;
    }
    return false;
}

function processBeforeGroupsAnnotation(string name, function f) returns boolean|error {
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

function processAfterGroupsAnnotation(string name, function f) returns boolean|error {
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

function checkGroup(string[] groups) returns boolean {
    foreach string group in groups {
        if options.groups.indexOf(group) is int {
            return true;
        }
    }
    return false;
}

function checkTest(string name) returns boolean =>
    options.tests.length() > 0 ? (options.tests.indexOf(name) is int) : true;
