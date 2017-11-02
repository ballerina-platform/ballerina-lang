# Create a Custom Client Connector

Now that you have [written a main program](../tutorials/main-program.md) and [written a passthrough service](../tutorials/passthrough-service.md) for integration scenarios, it is time to write a custom client connector to solve a problem.

> This tutorial provides instructions on how to write a custom connector that will invoke GitHub REST APIs.

This tutorial consists of the following main sections.

- [About connectors and actions](about-connectors-and-actions)

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
