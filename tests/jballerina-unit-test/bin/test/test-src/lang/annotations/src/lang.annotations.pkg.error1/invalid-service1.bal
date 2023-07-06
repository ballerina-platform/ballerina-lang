
import lang.annotations.pkg.first;
import lang.annotations.pkg.second;

// File not used in any of the test cases
@first:Sample {value:"sample value"}
service<second> sampleService {
    resource abc (string m) {
    }
}
