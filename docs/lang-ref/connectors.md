# Connectors

A connector represents a participant in the integration and is used to interact with an external system or a service you've defined in Ballerina. Ballerina includes a set of standard connectors that allow you to connect to Twitter, Facebook, and more, and you can define additional connectors within your Ballerina programs.

A connector is defined as follows:
```
[ConnectorAnnotations]
connector ConnectorName ([ConnectorParamAnnotations]TypeName VariableName[(, TypeName VariableName)*]) {
    ConnectorDeclaration;*
    VariableDeclaration;*
    ActionDefinition;+
}
```
A connector defines a set of [actions](actions.md) that can be used when interacting with the external system or service. For example, the Twitter connector includes actions such as `tweet`, `retweet`, and `destroyStatus`, which allow you to perform those actions in Twitter from your Ballerina program.

Note that ConnectorAnnotations are designed to help the editor provide a better user experience for connector users.

Connectors are instantiated (by means of the `new` keyword) as follows:
```
[ConnectorPackageName:]ConnectorName ConnectorInstanceName = new [ConnectorPackageName:]ConnectorName (ValueList[, map]);
```
The newly created instance has the `ConnectorInstanceName` assigned.

Once a connector of name `ConnectorInstanceName` has been instantiated, actions can be invoked against that connector as follows:
```
[ConnectorPackageName:]ConnectorName.ActionName (ConnectorInstanceName, ValueList);
```
