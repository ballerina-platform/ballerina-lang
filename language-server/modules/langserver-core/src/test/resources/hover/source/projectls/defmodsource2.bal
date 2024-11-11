import projectls.lsmod2;
import ballerina/module1;

public function main() {
    testDefaultModuleFunction1({field1: 1, field2: {}}, {}, {});
    lsmod2:Person person = new();
    string name = person.name;
    lsmod2:functionWithDefaultableParam();
    module1:function4()
}
