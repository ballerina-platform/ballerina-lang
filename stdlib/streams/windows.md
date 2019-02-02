    # Ballerina Streaming Windows

Windows allow you to capture a subset of events based on a specific criterion from an input stream for calculation. 
Each input stream can only have a maximum of one window.

In ballerina there are a bunch of inbuilt windows that you can use out of the box(As well you can define your own 
custom windows).

Following are the inbuilt windows shipped with Ballerina Streams.

1.  time window

    `time(int windowTime)`

    A sliding time window that holds events that arrived during the last `windowTime` period at a given time, and 
    gets updated for each event arrival and expiry.

2.  timeBatch window

    `timeBatch(int windowTime)`

    A batch (tumbling) time window that holds events that arrive during `windowTime` periods, and gets updated for 
    each `windowTime`.

3. timeLength window

    `timelength(int windowTime, int windowLength)`

    A sliding time window that, at a given time holds the last `windowLength` events that arrived during last 
    `windowTime` period, and gets updated for every event arrival and expiry.

4. length window
    `length(int windowLength)`

    A sliding length window that holds the last `windowLength` events at a given time, and gets updated for each 
    arrival and expiry.

5. lengthBatch window

    `lengthBatch(int windowLength)`

    A batch (tumbling) length window that holds a number of events specified as the `windowLength`. The window is 
    updated each time a batch of events that equals the number specified as the `windowLength` arrives.

6. externalTime window

    `externalTime(timeStamp, int windowTime)`

    A sliding time window based on external time. It holds events that arrived during the last `windowTime` period 
    from the external `timestamp`, and gets updated on every monotonically increasing `timestamp`. Here the 
    `timeStamp` should be an attribute of the record which is used as the constraint type of relevant input stream. 
    As the `timeStamp` parameter you should pass `<streamName>.<attiributeName>`.

7. externalTimeBatch window

    `externalTimeBatch(timeStamp, int windowTime, int? startTime, int? timeout)`

    A batch (tumbling) time window based on external time, that holds events arrived during `windowTime` periods, and
     gets updated for every `windowTime`. Here the `timeStamp` should be an attribute of the record which is used as 
     the constraint type of relevant input stream. As the `timeStamp` parameter you should pass `<streamName>
     .<attiributeName>`. Parameters `startTime` and `timeout` are optional parameters. `startTime` can be used to 
     specify a user defined time to start the first batch. `timeout` is time to wait for arrival of new event, before
      flushing and giving output for events belonging to a specific batch. Usually `timeout` is greater than 
      `windowTime`.

8. uniqueLength window
    
    `uniqueLength(uniqueAttribute, int windowLength)`

    A sliding length window that returns unique events within the `windowLength` based on the given `uniqueAttribute`
    . Here the `uniqueAttribute` should be an attribute of the record which is used as the constraint type of 
    relevant input stream.

9. delay window
    
    `delay(int delayTime)`

    A delay window holds events for a specific time period(`delayTime`) that is regarded as a delay period before 
    processing them.

10. sort window
    
    `sort(int windowLength, attributeName, string order)`

    A sort sort window holds a batch of events that equal the number specified as the `windowLength` and sorts them 
    in the given `order` of given `attributeName`. Here the `attributeName` should be an attribute of the record 
    which is used as the constraint type of relevant input stream. You can have multiple `attributeName` fields 
    followed by it's `order`.

11. timeAccum window

    `timeAccum(int timePeriod)`
    
    A sliding window that accumulates events until no more events arrive within the `timePeriod`, and only then 
    releases the accumulated events.
    
12. hopping window

    `hopping(int windowTime, int hoppingTime)`
    
    A hopping window holds the events arrived within last `windowTime` and release them in every `hoppingTime` period.
    
13. timeOrder window

    `timeOrder(timestamp, int windowTime, boolean dropOlderEvents)`
    
    A timeOrder window orders events that arrive out-of-order, using timestamp values provided by `timestamp`, and 
    bycomparing that `timestamp` value to system time. `windowTime` is the window duration. `dropOlderEvents` flag 
    determines whether to drop the events which has timestamp value less than the tail-time of current window. 
    Tail-time is the time, an amount of `windowTime` before the system time. Here the `timeStamp` should be an 
    attribute of the record which is used as the constraint type of relevant input stream. As the `timeStamp` 
    parameter you should pass `<streamName>.<attiributeName>`.