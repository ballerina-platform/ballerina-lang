import testproject.module2 as mod2;
import ballerina/module1;

function testFunc() {
    mod2:Teacher teacher = createTeacher("Teacher 1");
    int count = addTeacher(teacher, "string");
    
    int port = 9090;
    module1:Listener l = createListener(port);
}
