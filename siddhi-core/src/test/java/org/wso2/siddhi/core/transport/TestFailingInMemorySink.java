package org.wso2.siddhi.core.transport;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;
import org.wso2.siddhi.core.util.transport.DynamicOptions;

@Extension(
        name = "testFailingInMemory",
        namespace = "sink",
        description = "In-memory sink for testing connection unavailable use-case",
        parameters = @Parameter(name = "topic", type = DataType.STRING, description = "Event will be delivered to all" +
                "the subscribers of the same topic"),
        examples = @Example(
                syntax = "@sink(type='inMemory', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long)",
                description = "In this example BarStream uses inMemory transport which emit the Siddhi " +
                        "events internally without using external transport and transformation."
        )
)
public class TestFailingInMemorySink extends InMemorySink {
    public static int errorCount = 0;
    private boolean isOkToConnect;

    public TestFailingInMemorySink() {
        this.isOkToConnect = true;
        this.errorCount = 0;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        if (!isOkToConnect) {
            errorCount++;
            throw new ConnectionUnavailableException("Connection unavailable during connection");
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        isOkToConnect = false;
        errorCount++;
        throw new ConnectionUnavailableException("Connection unavailable during publishing");
    }
}
