package ballerina.lang.time;

struct Timezone {
    string zoneId;
    int zoneOffset;
}


struct Time {
    int time;
    Timezone zone;
}

native function currentTime()(Time);

native function createTime(int timeValue, string zoneId)(Time);

native function parse(string data, string format) (Time);

native function toString(Time time)(string);

native function format(Time time, string format)(string);