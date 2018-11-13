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
    private function (StreamEvent s) returns map[] tableQuery;
    private function (any) nextProcessor;
    public Window? windowInstance;
    public string streamName;
    public string tableName;
    public JoinType joinType;

    public new(nextProcessor, joinType, tableQuery) {
        self.windowInstance = ();
        self.streamName = "";
        self.tableName = "";
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent?[] joinedEvents = [];
        int j = 0;
        foreach event in streamEvents {
            (StreamEvent?, StreamEvent?)[] candidateEvents = [];
            foreach i, m in self.tableQuery(event) {
                StreamEvent resultEvent = new((self.tableName, m), "CURRENT", time:currentTime().time);
                candidateEvents[i] = (event, resultEvent);
            }
            // with right/left/full joins, we need to emit an event even there're no candidate events in table.
            if (candidateEvents.length() == 0 && (self.joinType != "JOIN")) {
                candidateEvents[0] = (event, ());
            }

            foreach e in candidateEvents {
                joinedEvents[j] = self.joinEvents(e[0], e[1]);
                j += 1;
            }
        }
        StreamEvent[] outputEvents = [];
        int i = 0;
        foreach e in joinedEvents {
            match e {
                StreamEvent s => {
                    outputEvents[i] = s;
                    i += 1;
                }
                () => {
                }
            }
        }
        self.nextProcessor(outputEvents);
    }

    public function setJoinProperties(string tn, string sn, Window wi) {
        self.tableName = tn;
        self.streamName = sn;
        self.windowInstance = wi;
    }

    function joinEvents(StreamEvent? lhsEvent, StreamEvent? rhsEvent) returns StreamEvent? {
        StreamEvent? joined = ();
        match lhsEvent {
            StreamEvent lhs => {
                joined = lhs.copy();
                match rhsEvent {
                    StreamEvent rhs => {
                        joined.addData(rhs.data);
                    }
                    () => {
                        // nothing to do.
                    }
                }
            }
            () => {
                // nothing to do.
            }
        }
        return joined;
    }
};

public function createTableJoinProcessor(function (any) nextProcessor, JoinType joinType,
                                         function (StreamEvent s) returns map[] tableQuery)
                    returns TableJoinProcessor {
    TableJoinProcessor tableJoinProcessor = new(nextProcessor, joinType, tableQuery);
    return tableJoinProcessor;
}
