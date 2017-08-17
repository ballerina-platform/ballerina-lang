package ballerina.resiliency.lb;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.errors;

int count = 0;

connector ClientConnector(http:ClientConnector[] testConnectorArray, string algorithm, boolean failover) {
    action post (string path, message m) (message) {
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.post(path, m);
        }

        return response;
    }

    action head (string path, message m) (message) {
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.head(path, m);
        }

        return response;
    }

    action put (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.put(path, m);
        }

        return response;
    }

    action execute (string httpVerb, string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.execute(httpVerb, path, m);
        }

        return response;
    }

    action patch (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.patch(path, m);
        }

        return response;
    }

    action delete (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.delete(path, m);
        }

        return response;
    }

    action get (string path, message m) (message){
        int arrayLength = lengthof (testConnectorArray);
        int index = count % arrayLength;
        http:ClientConnector t1 = testConnectorArray[index];
        count = count + 1;
        message response;
        int runningIndex = index;
        int startingIndex = -1;

        if (failover) {
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
        } else {
            response = t1.get(path, m);
        }

        return response;
    }
}