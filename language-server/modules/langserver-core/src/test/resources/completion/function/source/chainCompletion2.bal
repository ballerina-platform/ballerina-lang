import ballerina/file;
import ballerina/time;

function testFunction1() {
	time:Time tm = {
		time: 0,
		zone: {
			id: "",
			offset: 0
		}
	};
	
	file:FileInfo fInfo = new("test", 12, tm, false);
    fInfo.modifiedTime.
    string testString = "Hello Ballerina!";
}