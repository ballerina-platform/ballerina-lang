import ballerina/observe;
import ballerina/io;
import ballerina/time;

function main (string... args) {

	map tags = {"event_type":"test"};

	//Create a counter
	observe:Counter counter = new("event_total", "Total count of events.", tags);
	io:println("---Counter---");

	//Increment the counter by 1
	counter.incrementByOne();
	io:println("count: " + counter.count());

	//Increment the counter by 5
	counter.increment(5);
	io:println("count: " + counter.count());
	io:println("");

	//Create a gauge
	observe:Gauge gauge = new("event_queue_size", "Size of an event queue.", tags);
	io:println("---Gauge---");

	//Increment the gauge by one
	gauge.incrementByOne();
	io:println("gauge: " + gauge.value());

	//Increment the gauge by three
	gauge.increment(3);
	io:println("gauge: " + gauge.value());

	//Decrement the gauge by one
	gauge.decrementByOne();
	io:println("gauge: " + gauge.value());

	//Decrement the gauge by one
	gauge.decrement(2);
	io:println("gauge: " + gauge.value());

	//Create a gauge and set the value to current system time
	observe:Gauge currentTimeGauge = new("current_system_time", "Current time of the system.", ());
	time:Time time = time:currentTime();
	int currentTimeMills = time.time;
	currentTimeGauge.setValue(currentTimeMills);
	io:println("current system time in milliseconds: " + currentTimeGauge.value());
	io:println("");

	//Create a summary
	observe:Summary summary = new("event_size", "Size of an event.", tags);
	io:println("---Summary---");

	//Record events in the summary
	summary.record(5);	
	summary.record(1);
	summary.record(8);
	summary.record(3);
	summary.record(4);

	//Count the number of recorded events in the summary
	io:println("count : " + summary.count());

	//Return the maximum value of events recorded in the summary
	io:println("max: " + summary.max());

	//Return the mean value of events recorded in the summary 
	io:println("mean: " + summary.mean());

	//Return the values at different percentiles
	io:print("percentile values: ");
	io:println(summary.percentileValues());
	io:println("");

	//Create a timer
	observe:Timer timer = new("event_duration", "Duration of an event.", tags);
	io:println("---Timer---");

	//Record times in the timer
	timer.record(1000, observe:TIME_UNIT_NANOSECONDS);
	timer.record(200, observe:TIME_UNIT_MICROSECONDS);
	timer.record(30, observe:TIME_UNIT_MILLISECONDS);
	timer.record(4, observe:TIME_UNIT_SECONDS);
	timer.record(1, observe:TIME_UNIT_MINUTES);

	//Return the number of times that record has been called since this timer was created.
	io:println("count: " + timer.count());

	//Return the maximum time recorded in the timer
	io:println("max: " + timer.max(observe:TIME_UNIT_SECONDS) + " seconds");

	//Return the mean value of times recorded in the timer
	io:println("mean: " + timer.mean(observe:TIME_UNIT_SECONDS) + " seconds");

	//Return the latencies at specific percentiles.
	io:print("percentile values: ");
	io:println(timer.percentileValues(observe:TIME_UNIT_SECONDS));
}
