// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/config;
import ballerina/file;
import ballerina/filepath;
import ballerina/io;
import ballerina/lang.'int as langint;
import ballerina/log;
import ballerina/task;
import ballerina/time;

# Abstract Snapshotable to be referenced by all snapshotable objects.
public type Snapshotable abstract object {

    # Function to return the current state of a snapshotable object.
    # + return - A `map<any>` that represents the current state of
    # the snapshotable instance.
    public function saveState() returns map<any>;

    # Function to restore a previous state intoa a snapshotable object.
    # + state - A `map<any>` state that can be used to restore
    # snapshotable instance to a previous state.
    public function restoreState(map<any> state);
};

// Global variables to be used with snapshot persistence.
task:Scheduler persistScheduler = new({ intervalInMillis: 1 });
map<map<any>> streamsPersistanceState = {};
map<Snapshotable> snapshotables = {};
string persistanceDirectory = "snapshots";
int persistanceIntervalInMillis = 30000;
boolean stateLoaded = false;
map<boolean> loadedStates = {};

# Native function to deserialize a serialized snapshot.
# This function is deprecated since v1.1.
# + str - A `string` of serialized content.
# + return - A deserialized `map<any>` state.
function deserialize(string str) returns map<any> {
    error e = error("Deserializing is not supported.");
    panic e;
}

# Native function to serialize a snapshot.
# This function is deprecated since v1.1.
# + data - A `map<any>` state to be serialized.
# + return - A `string` of serialized state.
function serialize(map<any> data) returns string {
    error e = error("Serializing is not supported.");
    panic e;
}

# Function to read given number of characters from an io:ReadableCharacterChannel.
# + rch - A `ReadableCharacterChannel` instance.
# + numberOfCharacters - A `int` indicating number of chars to read.
# + return - A `string` of content or an `error`.
function readCharacters(io:ReadableCharacterChannel rch, int numberOfCharacters) returns @tainted string|error {
    return rch.read(numberOfCharacters);
}

# Function to read all characters as a string from an io:ReadableCharacterChannel.
# + rch - A `ReadableCharacterChannel` instance.
# + return - A `string` of content or an `error`.
function readAllCharacters(io:ReadableCharacterChannel rch) returns @tainted string|error? {
    int fixedSize = 128;
    boolean isDone = false;
    string result = "";
    while (!isDone) {
        var readResult = readCharacters(rch, fixedSize);
        if (readResult is string) {
            result = result + readResult;
        } else {
            if (readResult is io:EofError) {
                isDone = true;
            } else {
                return readResult;
            }
        }
    }
    return result;
}

# Function to write a string to a file using an io:WritableCharacterChannel.
# + wch - A `WritableCharacterChannel` instance.
# + content - The `string` content to be written.
# + startOffset - A `int` indicating start offset.
# + return - A `int` indicating number of chars got written or an `error`.
function writeCharacters(io:WritableCharacterChannel wch, string content, int startOffset) returns int|error? {
    return wch.write(content, startOffset);
}

# Function to initialize an io:ReadableCharacterChannel.
# + filePath - A `string` path to the file.
# + encoding - A `string` indicating the encoding type.
# + return - The `ReadableCharacterChannel` which got initilized.
function createReadableCharacterChannel(string filePath, string encoding) returns io:ReadableCharacterChannel? {
    io:ReadableCharacterChannel? rch = ();
    io:ReadableByteChannel|error byteChannel = trap io:openReadableFile(filePath);
    if (byteChannel is io:ReadableByteChannel) {
        rch = <@untainted> new io:ReadableCharacterChannel(byteChannel, encoding);
    }
    return rch;
}

# Function to initialize an io:WritableCharacterChannel.
# + filePath - A `string` path to the file.
# + encoding - A `string` indicating the encoding type.
# + return - The `WritableCharacterChannel` which got initilized.
function createWritableCharacterChannel(string filePath, string encoding) returns io:WritableCharacterChannel? {
    io:WritableCharacterChannel? wch = ();
    io:WritableByteChannel|error byteChannel = trap io:openWritableFile(filePath);
    if (byteChannel is io:WritableByteChannel) {
        wch = <@untainted> new io:WritableCharacterChannel(byteChannel, encoding);
    }
    return wch;
}

# Function to close writable/readable character channels.
# + c - Character channel to be closed.
function closeCharChannel(any c) {
    error? e = ();
    if (c is io:ReadableCharacterChannel) {
        e = c.close();
    } else if (c is io:WritableCharacterChannel) {
        e = c.close();
    }
    if (e is error) {
        log:printError("Couldn't close char channel.", e);
    }
}

# Function to read a text file.
# + filePath - A `string` path to the file.
# + encoding - A `string` indicating the encoding type.
# + return - The `string` content of the file.
function readFile(string filePath, string encoding) returns @tainted string {
    string content = "";
    io:ReadableCharacterChannel? rch = createReadableCharacterChannel(filePath, encoding);
    if (rch is io:ReadableCharacterChannel) {
        string|error? c = readAllCharacters(rch);
        closeCharChannel(rch);
        if (c is string) {
            content = c;
        }
    }
    return content;
}

# Function to write text to a file.
# + filePath - A `string` path to the file.
# + encoding - A `string` indicating the encoding type.
# + content - A `string` content to be written.
# + return - An `int` indicating how many chars got written.
function writeToFile(string filePath, string encoding, string content) returns int {
    int written = 0;
    io:WritableCharacterChannel? wch = createWritableCharacterChannel(filePath, encoding);
    if (wch is io:WritableCharacterChannel) {
        int|error? i = writeCharacters(wch, content, 0);
        closeCharChannel(wch);
        if (i is int) {
            written = i;
        }
    }
    return written;
}

# Function to serialize a Snapshotable state and write that to given file.
# + persistancePath - A `string` path of the persistance directory.
# + return - An `error` if state cannot be written to a file.
function writeStateToFile(string persistancePath) returns error? {
    string? snapshotFile = ();
    time:Time ct = time:currentTime();
    int currentTimeMillis = ct.time;

    if (!file:exists(persistancePath)) {
        string|error e = file:createDir(persistancePath, true);
    }
    string path = check filepath:build(persistancePath, currentTimeMillis.toString());
    if (!file:exists(path)) {
        string|error filepath = file:createFile(path);
        if (filepath is string) {
            snapshotFile = filepath;
        }
    }
    if (snapshotFile is string) {
        string serialized = serialize(streamsPersistanceState);
        int written = writeToFile(snapshotFile, "UTF8", serialized);
        if (written > 0) {
            return;
        } else {
            error e = error("Error while writing streaming state to a file.");
            return e;
        }
    } else {
        error e = error("Error while creating snapshot file: " + path);
        return e;
    }
}

# Function to get all snapshot files in persistance path
# + persistancePath - A `string` path of the persistance directory.
# + return - An `array` containing absolute paths of all snapshot files.
function getSnapshotFiles(string persistancePath) returns string[] {
    string[] snapshotFiles = [];
    if (file:exists(persistancePath)) {
        var files = file:readDir(persistancePath);
        int[] timestamps = [];
        if (files is file:FileInfo[]) {
            foreach file:FileInfo p in files {
                int|error t = langint:fromString(p.getName());
                if (t is int) {
                    timestamps[timestamps.length()] = t;
                }
            }
        }
        IntSort s = new;
        s.sort(timestamps);
        foreach int t in timestamps {
            string|error sf = filepath:build(persistancePath, t.toString());
            if (sf is string) {
                snapshotFiles[snapshotFiles.length()] = sf;
            }
        }
    }
    return snapshotFiles;
}

# Function to get latest snapshot file in persistance path
# + persistancePath - A `string` path of the persistance directory.
# + return - A `string` absolute path of the latest snapshot file.
function getLatestSnapshotFile(string persistancePath) returns string? {
    string[] files = getSnapshotFiles(persistancePath);
    string? latestFile = ();
    if (files.length() > 0) {
        latestFile = files[files.length() - 1];
    }
    return latestFile;
}

# Function to purge/delete old snapshot files from persistance path
# + persistancePath - A `string` path of the directory where the states should get persisted.
function purgeOldSnapshotFiles(string persistancePath) {
    int revisionsToKeep = 5;
    string[] files = getSnapshotFiles(persistancePath);
    if (files.length() > revisionsToKeep) {
        int i = 0;
        while (i < (files.length() - revisionsToKeep)) {
            string f = files[i];
            if (file:exists(f)) {
                error? e = file:remove(f);
                if (e is error) {
                    log:printError("Couldn't delete snapshot.", e);
                }
            }
            i += 1;
        }
    }
}

# Function to restore a persisted snapshotable states from a file.
function restoreStates() {
    if (!stateLoaded) {
        string? lastSnapshotFile = getLatestSnapshotFile(persistanceDirectory);
        if (lastSnapshotFile is string) {
            string stateStr = readFile(lastSnapshotFile, "UTF8");
            var retrievedState = trap deserialize(stateStr);
            if (retrievedState is map<map<any>>) {
                streamsPersistanceState = retrievedState;
                stateLoaded = true;
            }
        }
    }
    foreach var [k, v] in streamsPersistanceState.entries() {
        boolean loaded = loadedStates[k] ?: false;
        if (!loaded) {
            Snapshotable? s = snapshotables[k];
            if (s is Snapshotable) {
                s.restoreState(v);
                loadedStates[k] = true;
            }
        }
    }
}

# Function to iterate through all the snapshotables, and persist their states into a file.
# + return - An `error` if the state cannot be persisted into a file.
function persistStatesAndPurgeOldSnapshots() returns error? {
    foreach var [k, v] in snapshotables.entries() {
        streamsPersistanceState[k] = v.saveState();
    }
    error? e = writeStateToFile(persistanceDirectory);
    purgeOldSnapshotFiles(persistanceDirectory);
    return e;
}

# Function to initilize a Scheduler task and start periodic snapshotting.
function startPersisting() {
    persistScheduler = new({
            intervalInMillis: persistanceIntervalInMillis,
            initialDelayInMillis: persistanceIntervalInMillis
        }
    );
    checkpanic persistScheduler.attach(persistanceSchedulerService);
    checkpanic persistScheduler.start();
}

# Scheduler service for persisting states.
service persistanceSchedulerService = service {
    resource function onTrigger() {
        error? e = persistStatesAndPurgeOldSnapshots();
        if (e is error) {
            log:printError("Couldn't persist state and purge old snapshots.", e);
        }
    }
};

# Function to restore state of a given object.
# + key - An unique `string` identifier for the snapshotable reference.
# + reference - The snapshotable reference to be restored.
public function restoreState(string key, any reference) {
    var state = streamsPersistanceState[key];
    if (state is map<any> && reference is Snapshotable) {
        reference.restoreState(state);
    }
}

# Function to clear an existing state.
# + key - An unique `string` identifier for the snapshotable reference.
# + return - A `boolean` indicating whether the state for the given key removed successfully.
public function removeState(string key) returns boolean {
    if (snapshotables.hasKey(key) && streamsPersistanceState.hasKey(key)) {
        var snapshotableRemoved = snapshotables.remove(key);
        var stateRemoved = streamsPersistanceState.remove(key);
        return true;
    } else {
        return false;
    }
}

# Function to register Snapshotables.
# + key - An unique `string` identifier for the snapshotable reference.
# + reference - The snapshotable reference to be registered.
public function registerSnapshotable(string key, any reference) {
    if (reference is Snapshotable) {
        snapshotables[key] = reference;
    }
}

# Function to initialize and start snapshotting.
public function initPersistence() {
    boolean enabled = config:getAsBoolean("b7a.streaming.persistence.enabled");
    if (enabled) {
        persistanceDirectory = config:getAsString("b7a.streaming.persistence.directory", "snapshots");
        int interval = config:getAsInt("b7a.streaming.persistence.interval", 30);
        persistanceIntervalInMillis = interval * 1000;
        restoreStates();
        startPersisting();
    }
}
