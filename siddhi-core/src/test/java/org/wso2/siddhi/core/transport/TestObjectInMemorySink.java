package org.wso2.siddhi.core.transport;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;

@Extension(
        name = "testStringInMemory",
        namespace = "sink",
        description = "In-memory sink for testing connection unavailable use-case",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic"),
        examples = @Example(
                syntax = "@sink(type='testStringInMemory', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long)",
                description = "In this example BarStream uses inMemory transport which emit the Siddhi " +
                        "events internally without using external transport and transformation."
        )
)
public class TestObjectInMemorySink extends InMemorySink {
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class};
    }
}
