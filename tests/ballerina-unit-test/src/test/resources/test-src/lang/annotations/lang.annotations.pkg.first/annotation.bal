
import lang.annotations.pkg.second;

public annotation Sample attach service<>, resource {
    string value;
}

public annotation SampleConfig attach service<> {
    string value;
}

public annotation SampleConfigSecond attach service<second> {
    string value;
}