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

public type StreamEvent object {
    public EventType eventType;
    public int timestamp;
    public map data;

    public new((string, map) | map eventData, eventType, timestamp) {
        match eventData {
            (string, map) t => {
                foreach k, v in t[1] {
                    data[t[0] + DELIMITER + k] = v;
                }
            }
            map m => {
                data = m;
            }
        }
    }

    public function clone() returns StreamEvent {
        StreamEvent clone = new(cloneData(), eventType, timestamp);
        return clone;
    }

    public function addData(map eventData) {
        foreach k, v in eventData {
            data[k] = v;
        }
    }

    function cloneData() returns map {
        map dataClone;
        foreach k, v in data {
            dataClone[k] = v;
        }
        return dataClone;
    }
};