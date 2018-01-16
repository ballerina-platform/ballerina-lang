import org.test;

service<http> test {

    resource test () {
        test:testStruct ts = {};
        ts./*ref*/s:"";
    }
}
