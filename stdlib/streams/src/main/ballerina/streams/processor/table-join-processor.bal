// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# The `TableJoinProcessor` object handles joining streams with in-memory tables in ballerina.
# `nextProcessor` is the `process` function of the next processor, which can be a `Select` processor, `Aggregator`
# processor, `Having` processor.. etc. The `streamName` is the stream of the join and its attached
# window is `'windowInstance`. The `tableName` is the name of the table with which the stream is joined. The
# `joinType` is the type of the join and it can be any value defined by `streams:JoinType`.
#
# + tableQuery - description
# + nextProcessor - description
# + windowInstance - description
# + streamName - description
# + tableName - description
# + joinType - description
# + lockField - description
public type TableJoinProcessor object {
    private function (StreamEvent s) returns map<anydata>[] tableQuery;
    private function (StreamEvent?[]) nextProcessor;
    public Window? windowInstance;
    public string streamName;
    public string tableName;
    public JoinType joinType;
    public int lockField = 0;

    public function __init(function (StreamEvent?[]) nextProcessor, JoinType joinType,
                           function (StreamEvent s) returns map<anydata>[] tableQuery) {
        self.nextProcessor = nextProcessor;
        self.joinType = joinType;
        self.tableQuery = tableQuery;
        self.windowInstance = ();
        self.streamName = "";
        self.tableName = "";
    }

    # Joins the incoming events to the stream with the given table.
    # + streamEvents - The stream events being joined with the table.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] joinedEvents = [];
        StreamEvent?[] outputEvents = [];
        lock {
            self.lockField += 1;
            int j = 0;
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent> evt;
                [StreamEvent?, StreamEvent?][] candidateEvents = [];
                int i = 0;
                function (StreamEvent s) returns map<anydata>[] tQuery = self.tableQuery;
                foreach var m in tQuery(event) {
                    StreamEvent resultEvent = new([self.tableName, m], "CURRENT", time:currentTime().time);
                    candidateEvents[i] = [event, resultEvent];
                    i += 1;
                }
                // with right/left/full joins, we need to emit an event even there're no candidate events in table.
                if (candidateEvents.length() == 0 && (self.joinType != "JOIN")) {
                    candidateEvents[0] = [event, ()];
                }

                foreach var e in candidateEvents {
                    joinedEvents[j] = self.joinEvents(e[0], e[1]);
                    j += 1;
                }
            }
            outputEvents = [];
            int i = 0;
            foreach var e in joinedEvents {
                if (e is StreamEvent) {
                    outputEvents[i] = e;
                    i += 1;
                }
            }
        }
        function (StreamEvent?[]) nProcessor = self.nextProcessor;
        nProcessor(outputEvents);
    }

    # Set the properties required for joining.
    # + tn - The name of the table being joined with the stream.
    # + sn - The name of the stream being joined with the table.
    # + wi - The window instance which is attached to the stream.
    public function setJoinProperties(string tn, string sn, Window wi) {
        self.tableName = tn;
        self.streamName = sn;
        self.windowInstance = wi;
    }

    function joinEvents(StreamEvent? lhsEvent, StreamEvent? rhsEvent) returns StreamEvent? {
        StreamEvent? joined = ();
        if (lhsEvent is StreamEvent) {
            joined = lhsEvent.copy();

            if (rhsEvent is StreamEvent && joined is StreamEvent) {
                joined.addData(rhsEvent.data);
            }
        }
        return joined;
    }
};

# Creates a `TableJoinProcessor` and return it.
#
# + nextProcessor - The function pointer to `process` function of the next processor, which can be a `Select` processor,
#                  `Aggregator` processor, `Having` processor.. etc
# + joinType - The type of the join and it can be any value defined by `streams:JoinType`.
# + tableQuery - The function pointer to a function which retrieves the records from the table and joins w ith
#               each stream event.
#
# + return - Returns a `TableJoinProcessor` object.
public function createTableJoinProcessor(function (StreamEvent?[])  nextProcessor, JoinType joinType,
                                         function (StreamEvent s) returns map<anydata>[] tableQuery)
                    returns TableJoinProcessor {
    TableJoinProcessor tableJoinProcessor = new(nextProcessor, joinType, tableQuery);
    return tableJoinProcessor;
}
