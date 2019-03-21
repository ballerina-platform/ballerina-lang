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

    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] joinedEvents = [];
        StreamEvent?[] outputEvents = [];
        lock {
            self.lockField += 1;
            int j = 0;
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent> evt;
                (StreamEvent?, StreamEvent?)[] candidateEvents = [];
                int i = 0;
                foreach var m in self.tableQuery.call(event) {
                    StreamEvent resultEvent = new((self.tableName, m), "CURRENT", time:currentTime().time);
                    candidateEvents[i] = (event, resultEvent);
                    i += 1;
                }
                // with right/left/full joins, we need to emit an event even there're no candidate events in table.
                if (candidateEvents.length() == 0 && (self.joinType != "JOIN")) {
                    candidateEvents[0] = (event, ());
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
        self.nextProcessor.call(outputEvents);
    }

    public function setJoinProperties(string tn, string sn, Window wi) {
        self.tableName = tn;
        self.streamName = sn;
        self.windowInstance = wi;
    }

    function joinEvents(StreamEvent? lhsEvent, StreamEvent? rhsEvent) returns StreamEvent? {
        StreamEvent? joined = ();
        if (lhsEvent is StreamEvent) {
            joined = lhsEvent.copy();

            if (rhsEvent is StreamEvent) {
                joined.addData(rhsEvent.data);
            }
        }
        return joined;
    }
};

public function createTableJoinProcessor(function (StreamEvent?[])  nextProcessor, JoinType joinType,
                                         function (StreamEvent s) returns map<anydata>[] tableQuery)
                    returns TableJoinProcessor {
    TableJoinProcessor tableJoinProcessor = new(nextProcessor, joinType, tableQuery);
    return tableJoinProcessor;
}
