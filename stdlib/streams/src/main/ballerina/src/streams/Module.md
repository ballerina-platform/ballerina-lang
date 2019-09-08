## Module overview

The Streams module provides the stream processing capabilities to Ballerina:

Note: Ballerina Streaming capabilities are shipped as an experimental
feature in the latest release. Please use the `--experimental` flag when
compiling Ballerina files which have streaming constructs.

The following topics explain the high-level concepts of Ballerina
streaming.

* [Stream](#stream)
* [Forever Statement](#forever-statement)
* [Query](#query)

### Stream

A stream is a logical series of events ordered in time. Its schema is
defined/constrained via the **record definition**. A record definition
contains a unique name and a set of uniquely-identifiable attributes
with specific types within the record. All the events of a specific
stream have the same schema (i.e., have the same attributes in the same
order).

###### Syntax

The syntax for defining a new stream is as follows.

```ballerina
type <record name> record {
    <attribute type> <attribute name>;
    <attribute type> <attribute name>;
    <attribute type> <attribute name>;
    ...
};

stream<record name> <stream name> = new;
```
The following parameters are configured in a stream definition.

| Parameter     | Description |
| ------------- |-------------|
| `stream name`      | The name of the created stream. |
| `record name`      | The name of the record that constrains the stream. |
| `attribute name`   | The uniquely-identifiable attribute name. The schema of a record is defined by its attributes.|
| `attribute type`   | The type of each attribute defined in the record. |

###### Example
```ballerina
type Employee record {
    string name;
    int age;
    string status;
};

stream<Employee> employeeStream = new;
```

The code given above creates a stream named `employeeStream` that is
constrained by the `Employee` type with the following attributes.

+ `name` of type `string`
+ `age` of type `int`
+ `status` of type `string`

### Forever Statement
The `forever` statement block can include one or more streaming queries
defining stream processing and complex event processing rules. The
`forever` statement block lets streaming queries to run continuously
till the Ballerina program is exited. Here each streaming query within
the `forever` block executes as an independent isolated processing unit.

###### Sample query

This query filters out the sensor events, which have the temperature
greater than 30 celsius, and for every 100 sensor events, it groups them
based on their type, count, number of sensor events for each type. Next,
it publishes all the types that have more than one event to the
`highTemperatureSensorStream` stream.

```ballerina
    forever {
        from sensorTemperatureStream
            where sensorTemperatureStream.temperature > 30
            window lengthBatch (100)
        select sensorTemperatureStream.type, count() as totalCount
            group by sensorTemperatureStream.type
            having totalCount > 1
        =>  (HighTemperature [] values) {
                foreach var value in values {
                    highTemperatureSensorStream.publish(value);
                }
            }
    }
```

### Query

Each streaming query can consume one or more streams, process the events continuously in a streaming manner, 
and generate the output simultaneously.
A query enables you to perform complex event processing and stream processing operations by processing incoming events
one by one in the order they arrive.

###### Syntax

Each query contains an input and an output section. Some also contain a projection section. 
The following is a simple query with all three sections.

```ballerina
from <input stream>
select <attribute name>, <attribute name>, ...
=> (<array type> <parameter name>) {
      ...
      ...
}
```

###### Example

This query consumes events from the `tempStream` stream (that is already
defined) and sends the room temperature and the room number to the
`roomTempStream` stream as the output.

```ballerina
type temperature record {
  int deviceID;
  int roomNo;
  float value;
};

type roomTemperature record {
  int roomNo;
  float value;
};

stream<temperature> tempStream = new;
stream<roomTemperature> roomTempStream = new;

public function initQuery() {
    forever {
        from tempStream
        select tempStream.roomNo, tempStream.value
        => (roomTemperature[] temperatures) {
            foreach var value in temperatures {
                roomTempStream.publish(value);
            }
        }
    }
}
```