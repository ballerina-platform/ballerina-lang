# Create a Custom Client Connector

Now that you have [written a main program](../tutorials/main-program.md) and [written a passthrough service](../tutorials/passthrough-service.md) for integration scenarios, it is time to write a custom client connector to solve a problem.

> This tutorial provides instructions on how to write a custom connector that invokes [GitHub REST APIs](https://developer.github.com/v3/guides/getting-started/).

This tutorial consists of the following main sections.

- [About connectors and actions](about-connectors-and-actions)
- [Create your own connector](create-your-own-connector)

> **Prerequisites**: Download Ballerina and set it up. For instructions on how to do this, see the [Quick Tour](../quick-tour.md). it is also recommended to try to [write a main program](../tutorials/main-program.md) and [write a passthrough service](../tutorials/passthrough-service.md) before trying this out. This helps you to get familiar with Ballerina and how it can help achieve integration scenarios.

## About connectors and actions

When integrating and making a robust application, a need arises to make messaging channels in between various interfaces and weave a structure to encompass them together. When interacting with commonly available interfaces, rather than creating the structure of the messaging channel each and every time through code, it is worth keeping a programmed unit that can be reused. This component interacts with the given interface with ease and less complexity. In a way, such a unit can be described as a facade as it will mask any complexities that exist when interacting with the real interface and can be a facade to any party connecting to the particular interface.

Ballerina brings out a concept called connectors that makes the above possible with ease. Using Ballerina connectors, a custom Ballerina implementation can be made that can be used to communicate with a given interface. This custom connector can be made out-of-the-box just using Ballerina itself. The use of these custom connectors can span from a database to a REST endpoint and can even extend to a JMS queue as the requirement demands.

Connectors represents network services that are used by a Ballerina program. The term "network services" here is meant in the broadest possible sense - Ballerina is designed to connect with everything from a database to a JMS queue to an OAuth protected API to LDAP servers and more. Therefore, connectors are the representation of any such remote service in Ballerina.

Graphically, connectors are modeled as a separate lifeline to represent their independent and parallel execution. However, that lifeline is not a programmable thread of execution for the Ballerina developer - it only exists to represent the remote system.

Connectors may need to have usage specific parameters and, hence, must be instantiated prior to use. For example, an HTTP connector needs at least the URL to connect to.

A connector comes with a set of actions that represent the interactions that one can have with the remote service. Actions can be one directional or bi-directional and represent blocking from the calling worker's perspective. That is, the calling thread is blocked until the action has completed its remote interaction.

A connector contains the following syntax in Ballerina.

```
[ConnectorAnnotations]
[public] connector ConnectorName ([Connector initialize parameters]) {
    VariableDeclaration;*
    ActionDefinition;+
}

```

Any variables declared at the connector level are visible to all actions. The lifetime of the connector also defines the lifetime of the variables and they are of course local to each connector instance.

The structure of an action definition is as follows:

```
[ActionAnnotations]
action actionName ([Input parameters]) ([Output parameters]){
}

```

The execution semantics of an action are the same as that of a function: it runs using the caller's thread and the caller blocks until the action completes execution.

## Create your own connector

When creating a custom client connector, you need to be aware of the following.

- The capabilities you need to expose through the connector and logical separation. These is represented by separate actions when the connector is being created.
- Supportive extensions needed. These can be achieved by Ballerina itself or by integrating to an existing entity through Ballerina
- Packaging the connector 

In order to demonstrate the above aspects, this tutorial uses a connector that can invoke GitHub REST APIs. GitHub contains a set of REST APIs that can be used to achieve a vast set of functionalities. Out of the collection, REST APIs that cover the following functionalities are used.

- [View repositories for the authenticated user](https://developer.github.com/v3/repos/#list-your-repositories).
- [List repositories for an organization](https://developer.github.com/v3/repos/#list-organization-repositories).
- [List issues per repository according to the state](https://developer.github.com/v3/issues/#list-issues)

The above can be categorized as the capabilities expected from the connector and three separate actions represent the above.
