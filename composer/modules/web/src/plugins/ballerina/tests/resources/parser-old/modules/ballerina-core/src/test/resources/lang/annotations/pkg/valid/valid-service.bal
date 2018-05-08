
import lang.annotations.pkg.first;
import lang.annotations.pkg.second;

@first:Sample {value:"sample value"}
service<first> sampleService {
    resource abc (message m) {
        response:send(m);
    }
}

@first:SampleConfigSecond{value:"sample value"}
service<second> SampleServiceTwo {
    resource cde (message m) {
        response:send(m);
    }
}