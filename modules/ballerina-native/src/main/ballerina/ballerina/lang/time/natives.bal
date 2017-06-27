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

native function year(Time time)(int);

native function month(Time time)(int);

native function day(Time time)(int);

native function hour(Time time)(int);

native function minute(Time time)(int);

native function second(Time time)(int);

native function milliSecond(Time time)(int);

native function date(Time time)(int, int, int);

native function time(Time time)(int, int, int, int);