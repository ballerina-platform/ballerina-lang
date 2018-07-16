
import lang.annotations.pkg.first;
import lang.annotations.pkg.second;

@first:Sample {value:"sample value"}
service<first> sampleService {
    resource abc (string s) {
    }
}

@first:SampleConfigSecond{value:"sample value"}
service<second> SampleServiceTwo {
    resource cde (string s) {
    }
}