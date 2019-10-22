import ballerina/file;
import ballerina/time;

function testFunction1() {
    string testString = "Hello Ballerina!";
    var modifiedTime = getFileInfo.modifiedTime.
}

function getFileInfo() returns file:FileInfo {
	time:Time tm = {
        time: 0,
        zone: {
            id: "",
            offset: 0
        }
    };
    	
    file:FileInfo fInfo = new("test", 12, tm, false);
    return fInfo;
}