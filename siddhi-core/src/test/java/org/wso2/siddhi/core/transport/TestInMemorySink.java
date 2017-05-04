package org.wso2.siddhi.core.transport;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;


@Extension(
        name = "testInMemory",
        namespace = "sink",
        description = "In-memory sink for testing distributed sink in multi client mode. This dummy " +
                "sink simply overrides getSupportedDynamicOptions return nothing so that when distributed " +
                "sink will identify it as a multi-client sink as there are no dynamic options",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic"),
        examples = @Example(
                value = "In the following example BarStream uses testInMemory transport which emit the Siddhi\n" +
                        "events internally without using external transport and transformation.\n" +
                        "@sink(type='testInMemory', @map(type='passThrough'),\n" +
                        "@distribution(strategy='roundRobin',\n" +
                        "@destination(topic = 'topic1'), \n" +
                        "@destination(topic = 'topic2')))\n" +
                        "define stream BarStream (symbol string, price float, volume long);"
        )
)
public class TestInMemorySink extends InMemorySink {
    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[0];
    }
}
