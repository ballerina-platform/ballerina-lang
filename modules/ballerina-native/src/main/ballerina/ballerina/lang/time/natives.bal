package ballerina.lang.time;

struct Timezone {
    string zoneId;
    int zoneOffset;
}


struct Time {
    int time;
    Timezone zone;
}
