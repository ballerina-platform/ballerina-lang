package lang.annotations.pkg.error2;

import lang.annotations.pkg.first;

@first:SampleConfigSecond {value:"sample value"}
service<first> sampleService {
    resource abc (string m) {
    }
}
