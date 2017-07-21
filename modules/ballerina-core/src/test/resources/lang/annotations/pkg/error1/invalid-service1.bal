package lang.annotations.pkg.error1;

import lang.annotations.pkg.first;
import lang.annotations.pkg.second;

@first:Sample {value:"sample value"}
service<second> sampleService {
    resource abc (message m) {
        reply m;
    }
}
