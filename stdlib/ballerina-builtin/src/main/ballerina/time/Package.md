`ballerina/time` package contains implementations related to time, date and durations. 

It has 2 main Types, `Time` and `Timezone`. The type `Time` represents a particular time with its associated timezone and has `time` and `zone` attributes. 

`time` - Time in milliseconds since January 1, 1970, 00:00:00 GMT<br/>
`zone` - The `Timezone` of the time

The type `Timezone` represents the timezone information associated with a particular time, and has `zoneId` and `zoneOffset` attributes.

`zoneId` - Short ID of the zone (eg. "America/Panama") or offset string (eg. "-05:00")<br/> 
`zoneOffset` - The offset in seconds


## Get current time/date

```ballerina
time:Time time = time:currentTime(); 
int timeValue = time.time;   // eg. 1523513039 
string zoneId = time.zone.zoneId; // eg. “America/Panama”
int zoneoffset = time.zone.zoneOffset; // eg. -05:00

int nanoTime = time:nanoTime(); //eg. 2426115697486340

int year = time.year(); //eg. 2018
int month = time.month(); //eg. 3
int day = time.day(); //eg. 1
int hour = time.hour(); //eg. 18 
int minute = time.minute(); //eg. 56 
int second = time.second(); //eg. 23
int milliSecond = time.milliSecond(); //eg. 555 
string weekday = time.weekday(); //eg. “TUESDAY”
```

## Create a time/date object

```ballerina
//create a time object with zone 
time:Timezone zoneIdValue = {zoneId:"America/Panama"};
time:Time time1 = new (1498488382000, zoneValue);

//create a time object with zone offset 
time:Timezone zoneOffsetValue = {zoneId:"-05:00"};
time:Time time2 =new (1498488382000, zoneValue);

//create a time object without zone 
time:Timezone noZoneValue = {zoneId:""};
time:Time time3 = new (1498488382000, zoneValue);

//create a time object with time and date. eg. 2018-03-28T23:42:45.554-05:00  
time:Time dateTime = time:createTime(2018, 3, 28, 23, 42, 45, 554, "America/Panama");
```


## Format a time/date to string

```ballerina
time:Timezone zoneValue = {zoneId:"America/Panama"};
time:Time time = new (1498488382444, zoneValue);

//Format a time to string of a given format
string time1 = time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); //”2017-06-26T09:46:22.444-0500”

//Format a time to string of a RFC-1123 format
string time2 = time.formatTo(time:TIME_FORMAT_RFC_1123); //"Mon, 26 Jun 2017 09:46:22 -0500”

//Call toString() function
string time3 = time.toString(); //”2017-06-26T09:46:22.444-05:00”
```

## Parse a string to time/date

```ballerina
//parse a time string of a given format
time:Time time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

//parse a time string of RFC-1123 format
time:Time time2 = time:parseTo("Wed, 28 Mar 2018 11:56:23 +0530", time:TIME_FORMAT_RFC_1123);
```

## Time durations

```ballerina
//Add a time duration
time:Time time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
time1 = time.addDuration(1, 1, 1, 1, 1, 1, 1); // Adds 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second and 1 millisecond

//Subtract a time duration
time:Time time2 = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
time2 = time.subtractDuration(1, 1, 1, 1, 1, 1, 1);  // Subtracts 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second and 1 millisecond

//Get duration between 2 time values
int diffInMillis = time1.time - time2.time;
```
