import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"This is the base connector we are going to decorate"}
connector TestConnector (int i) {
    action actionHello (TestConnector testConnector, string greeting) {
        system:println("Hello " + greeting);
    }
}

@doc:Description {value:"This is the filter connector which will be decorating the TestConnector"}
connector IntroductionConnector<TestConnector testC> (string j) {
    action actionHello (IntroductionConnector introConnector, string name) {
        system:println("I'm " + name);
        // Invoke the action of the base type connector
        testC.actionHello(j);
    }
}

function main(string[] args) {
    // Create the 'TestConnector' with 'IntroductionConnector' as the filter connector
    TestConnector tc = create TestConnector(5) with IntroductionConnector("Bob");

    // Invoke the action of the 'TestConnector' which will eventually call the 'IntroductionConnector' action
    tc.actionHello("Mike");
}