
import lang.annotations.pkg.second;

annotation Sample attach service<>, resource {
    string value;
}

annotation SampleConfig attach service<> {
    string value;
}

annotation SampleConfigSecond attach service<second> {
    string value;
}