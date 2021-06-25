import testproject.module2 as mod2;

function testFunc() {
    mod2:Teacher teacher = createTeacher("Teacher 1");
    int count = addTeacher(teacher, "string");
}
