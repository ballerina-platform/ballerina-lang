import org.test;

function test () {
    test:Name name = {firstName:""};
    test:Person person = {};
    person./*ref*/name=name;
}
