package ballerina.resiliency.fo;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.errors;

connector ClientConnector(http:ClientConnector[] testConnectorArray) {
    action post (string path, message m) (message) {
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.post(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action head (string path, message m) (message) {
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.head(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action put (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.put(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action execute (string httpVerb, string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.execute(httpVerb, path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action patch (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.patch(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action delete (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.delete(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }

    action get (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = 0;
        http:ClientConnector t1 = testConnectorArray[index];
        message response;
        int runningIndex = index;
        int startingIndex = -1;
        while (startingIndex != index) {
            try {
                startingIndex = runningIndex;
                index = index + 1;
                response = t1.get(path, m);
            } catch (errors:Error e) {
                if (arrayLength > index) {
                    t1 = testConnectorArray[index];
                } else {
                    if (startingIndex > 0) {
                        index = 0;
                    } else {
                        message errorMsg = {};
                        messages:setStringPayload(errorMsg, "All the load balanced endpoints failed. Last error was " +
                                                            e.msg);
                        http:setStatusCode(errorMsg, 500);
                        return errorMsg;
                    }
                }
            }
            if (response != null) {
                break;
            }
        }

        return response;
    }
}