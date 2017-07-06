# Siddhi Extensions

Siddhi supports an extension architecture to support custom code and functions to be incorporated with Siddhi in a seamless manner. Extension will follow the following syntax;

`<namespace>:<function name>(<parameter1>, <parameter2>, ... )`

Here the namespace will allow Siddhi to identify the function as an extension and its extension group, the function name will denote the extension function within the given group, and the parameters will be the inputs that can be passed to the extension for evaluation and/or configuration.
E.g. A window extension created with namespace foo and function name unique can be referred as follows:

```
from StockExchangeStream[price >= 20]#window.foo:unique(symbol)
select symbol, price
insert into StockQuote
```

## Extension Types

Siddhi supports following five type of extensions:

### Function Extension

For each event it consumes zero or more parameters and output a single attribute as an output. This could be used to manipulate event attributes to generate new attribute like Function operator. Implemented by extending "org.wso2.siddhi.core.executor.function.FunctionExecutor".

E.g. `math:sin(x)` here the sin function of math extension will return the sin value its parameter x.

### Aggregate Function Extension

For each event it consumes zero or more parameters and output a single attribute having an aggregated results based in the input parameters as an output. This could be used with conjunction with a window in order to find the aggregated results based on the given window like Aggregate Function operator. Implemented by extending "org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator".

E.g. `custom:std(x)` here the std aggregate function of custom extension will return the standard deviation of value x based on the assigned window to its query.

### Window Extension

Allows events to be collected and expired without altering the event format based on the given input parameters like the Window operator. Implemented by extending "org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor".

E.g. `custom:unique(key)` here the unique window of custom extension will return all events as current events upon arrival as current events and when events arrive with the same value based on the "key" parameter the corresponding to a previous event arrived the previously arrived event will be emitted as expired event.

### Stream Function Extension

Allows events to be altered by adding one or more attributes to it. Here events could be outputted upon each event arrival. Implemented by extending "org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor".

E.g. `custom:pol2cart(theta,rho)` here the pol2cart function of custom extension will return all events by calculating the cartesian coordinates x & y and adding them as new attributes to the existing events.

### Stream Processor Extension

Allows events to be collected and expired with altering the event format based on the given input parameters. Implemented by extending "oorg.wso2.siddhi.core.query.processor.stream.StreamProcessor".

E.g. `custom:perMinResults(arg1, arg2, ...)` here the perMinResults function of custom extension will return all events by adding one or more attributes the events based on the conversion logic and emitted as current events upon arrival as current events and when at expiration expired events could be emitted appropriate expiring events attribute values for matching the current events attributes counts and types.

## Available Extensions

Siddhi currently have several prewritten extensions as follows;

<#macro displayRepositoriesList title extensionRepositories>
<#list extensionRepositories>
### ${title}
<#items as extensionRepository>
1. <a target="_blank" href="https://github.com/${extensionsOwner}/${extensionRepository}/blob/master/docs/documentation.md">${extensionRepository?replace("^siddhi-gpl-", "", "rf")?replace("^siddhi-", "", "rf")?replace("-", " ")?capitalize}</a><br>
</#items>
</#list>

</#macro>
<@displayRepositoriesList title="Extensions released under GPL License" extensionRepositories=gplExtensionRepositories/>
<@displayRepositoriesList title="Extensions released under Apache 2.0 License" extensionRepositories=apacheExtensionRepositories/>
