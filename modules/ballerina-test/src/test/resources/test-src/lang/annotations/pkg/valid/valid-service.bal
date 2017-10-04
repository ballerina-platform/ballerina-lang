package lang.annotations.pkg.valid;

import lang.annotations.pkg.first;
import lang.annotations.pkg.second;

@first:Sample {value:"sample value"}
service<first> sampleService {
    resource abc (message m) {
        reply m;
    }
}

@first:SampleConfigSecond{value:"sample value"}
service<second> SampleServiceTwo {
    resource cde (message m) {
        reply m;
    }
}