# Connectors & Actions

Connectors represents network services that are used by a Ballerina program. The term "network services" here is meant in the broadest possible sense - Ballerina is designed to connect with everything from a database to a JMS queue to an OAuth protected API to LDAP servers and more. Thus connectors are the representation of any such remote service in Ballerina.

Graphically, connectors are modeled as a separate lifeline to represent its independent and parallel execution. However, that lifeline is not a programmable thread of execution for the Ballerina developer - it only exists to represent the remote system.

Connectors may need to have usage specific parameters and hence must be instantiated prior to use. For example, an HTTP connector will need the URL to connect to in the minimum.

A connector comes with a set of actions which represent the interactions that one can have with the remote service. Actions can be one directional or bi-directional and represent are blocking from the calling worker's perspective. That is, the calling thread is blocked until the action has completed its remote interaction.

A `connector` is defined as follows:
```
[ConnectorAnnotations]
[native] connector ConnectorName ([ConnectorParamAnnotations] TypeName VariableName[(, [ConnectorParamAnnotations] TypeName VariableName)*]) {
    VariableDeclaration;*
    ActionDefinition;+
}
```

> NOTE: The `native` keyword has been used in 0.8 to signify a connector implemented using native code and not Ballerina itself. In a future release all connectors will be written in Ballerina and the use of the native keyword for connectors will be removed.

Any variables declared at the connector level are visible to all actions. The lifetime of the connector also defines the lifetime of the variables and they are of course local to each connector instance.

The  structure of an `action` definition is as follows:

```
[ActionAnnotations]

[native] action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
        [throws exception] {
    WorkerDeclaration;*
    Statement;+
}
```

The first argument of any action must be a parameter declared to be of the same type of the enclosing connector.

The `native` keyword says that the action's implementation is not in Ballerina. In that case the body of the action is not given as it is opaque.

The execution semantics of an action are the same as that of a function: it runs using the caller's thread and the caller blocks until the action completes execution.

## Instantiating Connectors

Connectors are instantiated using the `create` keyword as follows:

```
[ConnectorPackageName:]ConnectorName ConnectorVarName = create [ConnectorPackageName:]ConnectorName (ValueList);
```

## Invoking Actions

Actions are invoked as follows:
```
[ConnectorPackageName:]ConnectorName.ActionName (ConnectorVarName, ValueList);
```

> NOTE: A future version of the language will likely introduce the syntax `ConnectorVarName.ActionName (ValueList)` to invoke actions. That will also likely result in removing the required first argument for action definitions.

## Nesting Connectors

A powerful feature of Ballerina is the ability to use one connector to write another connector. See examples such as BasicAuthConnector, OAuthConnector and TwitterConnector to see different usages of this capability.

## Built-in Connectors

Ballerina comes with a set of built-in connectors for several protocols as part of the standard library.
