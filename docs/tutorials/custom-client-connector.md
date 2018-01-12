# Create a Custom Client Connector

Now that you have [written a main program](../tutorials/main-program.md) and [written a passthrough service](../tutorials/passthrough-service.md) for integration scenarios, it is time to write a custom client connector to solve a problem.

> This tutorial provides instructions on how to write a custom connector that invokes [GitHub REST APIs](https://developer.github.com/v3/guides/getting-started/).

This tutorial consists of the following main sections.

- [About connectors and actions](about-connectors-and-actions)
- [About custom client connectors](about-custom-client-connectors)
- [Create a custom client connector](create-a-custom-client-connector)

> **Prerequisites**: Download Ballerina and set it up. For instructions on how to do this, see the [Quick Tour](../quick-tour.md). Use the Quick Tour to also get an understanding of the Ballerina Composer and how it works. It is also recommended to try to [write a main program](../tutorials/main-program.md) and [write a passthrough service](../tutorials/passthrough-service.md) before trying this out. This helps you to get familiar with Ballerina and how it can help achieve integration scenarios.

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

Any variables declared at the connector level is visible to all actions. The lifetime of the connector also defines the lifetime of the variables and they are local to each connector instance.

The structure of an action definition is as follows.

```
[ActionAnnotations]
action actionName ([Input parameters]) ([Output parameters]){
}
```

The execution semantics of an action are the same as that of a function: it runs using the caller's thread and the caller blocks until the action completes execution.

## About custom client connectors

When creating a custom client connector, you need to be aware of the following.

- The capabilities you need to expose through the connector and logical separation. These is represented by separate actions when the connector is being created.
- Supportive extensions needed. These can be achieved by Ballerina itself or by integrating to an existing entity through Ballerina
- Packaging the connector 

In order to demonstrate the above aspects, this tutorial uses a connector that can invoke GitHub REST APIs. GitHub contains a set of REST APIs that can be used to achieve a vast set of functionalities. Out of the collection, REST APIs that cover the following functionalities are used.

- [View repositories for the authenticated user](https://developer.github.com/v3/repos/#list-your-repositories).
- [List repositories for an organization](https://developer.github.com/v3/repos/#list-organization-repositories).
- [List issues per repository according to the state](https://developer.github.com/v3/issues/#list-issues)

The above can be categorized as the capabilities expected from the connector and these are represented by separate actions.

Furthermore, in order to invoke the APIs, an authentication header must be sent that encompasses the base64 encoded value of the username and the token. This is a supportive extension and can be powered by Ballerina itself by using its inbuilt [base64encoder](https://ballerinalang.org/docs/api/0.94.1/ballerina.util.html#base64encode).

After creating the connector, you need to package it in a way that the connector can be distributed. As a comprehensive packaging mechanism is work in progress, currently we will be using the available packaging model to demonstrate the usage of the connector. The reason for using the package is that, when the inbuilt packaging is available, the structure available now can be extended and packaged to a distributable mode.

## Create a custom client connector

This section of the tutorial explains the way a sample custom client connector is built.

1. On the tool palette, click **More Connectors** to see a list of all available connectors.

    ![alt text](../images/MoreConnectors.png)

1. From the list that appears, expand **ballerina.net.http** and drag a **ClientConnector** onto the canvas.
1. Provide the connector parameter list. In order to connect to GitHub APIs, this connector uses an authenticated user and a token obtained for that user.  Furthermore, you need to initialize the connection to the GitHub API as shown below.
    ```Ballerina
    public connector ClientConnector (string username, string token) {
         endpoint<http:HttpClient> gitEP { create http:HttpClient("https://api.github.com", {});}
    }
    ```
1. Prepare the base64 encoded key value to be sent in authorization header to the back end. The Ballerina built-in base64encoder is used for this purpose and the logic in this scenario is written in another function that resides in the same package as the connector.
    ```Ballerina
    package org.wso2.ballerina.connectors.github;
 
    import ballerina.util;
 
    function getBase64EncodedKey (string value1,string value2) (string encodedString) {
	    string toEncode = value1 + ":" + value2;
	    encodedString = <string>util:base64encode(toEncode);
	    return;
    }
    ```
1. The above function is called within the connector to obtain the encoded string as shown below.
    ```ballerina
    public connector ClientConnector (string username, string token) {
        endpoint<http:HttpClient> gitEP { create http:HttpClient("https://api.github.com", {});}
        string authHeader = getBase64EncodedKey(username, token);
    }
    ```
1. The first action retrieves the list of repositories per organization. The action defined takes in the desired organization as a parameter and returns a HTTP response and, within the action, the relevant REST API is invoked.
    ```Ballerina
    action getReposOfOrg (string orgnization) (http:Response, http:HttpConnectorError) {
    	http:Request request = {};
    	string gitPath = string `/orgs/{{orgnization}}/repos`;
    	request.setHeader("Authorization", "Basic "+ authHeader);
    	http:Response response = {};
    	http:HttpConnectorError err;
    	response, err = gitEP.get(gitPath, request);
    	return response, err;
 	}
    ```
    Other actions can be configured in a similar manner.

## Using the connector

In order to test the connector, you must have a Ballerina program with either a main function or a service. This tutorial uses a service for the purpose. This test service is created in another package.

1. Import the client connector made above to the file where the service is written.
    ```Ballerina
    package com.test.sample;
    import org.wso2.ballerina.connectors.github;
    ```
1. Inside a resource initialize and invoke the client connector as follows.
    ```Ballerina
    resource getReposForOrganization (http:Request req,http:Response res, string org) {
        endpoint<github:ClientConnector> gitHubConnector{
        }
        string username;
        string tokenEnc;
        username, _ = req.getHeader("Username");
        tokenEnc, _ = req.getHeader("Token");
        github:ClientConnector gitHubConn = create github:ClientConnector(username, tokenEnc);
        bind gitHubConn with gitHubConnector;
        http:Response gitHubResponse = {};
        gitHubResponse, _ = gitHubConnector.getReposOfOrg(org);
        res.forward(gitHubResponse);
    }
    ```
The following is the full code of the sample.

```Ballerina
package org.wso2.ballerina.connectors.github;

import ballerina.net.http;

@Description {
    value:"GitHub Client Connector"
}
@Param {
    value:"username: github username"
}
@Param {
    value:"token: github personal token"
}
public connector ClientConnector (string username, string token) {
    endpoint<http:HttpClient> gitEP { create http:HttpClient("https://api.github.com", {});}

    string authHeader = getBase64EncodedKey(username, token);
    @Description {
        value:"Retrieve repositories for the authenticated user"
    }
    @Return {
        value:"Response object"
    }
    
    action getReposOfUser () (http:Response, http:HttpConnectorError) {
         http:Request request = {};
         string gitPath = "/user/repos";
         request.setHeader("Authorization", "Basic "+ authHeader);
         http:Response response = {};
         http:HttpConnectorError err;
         response, err = gitEP.get(gitPath, request);
         return response, err;
    }
    @Description {
        value:"List repositories for an organization"
    }
    @Param {
        value:"organization: name of the orgnization on which repositories should be fetched"
    }
    @Return {
        value:"Response object"
    }
    action getReposOfOrg (string orgnization) (http:Response, http:HttpConnectorError) {
        http:Request request = {};
        string gitPath = string `/orgs/{{orgnization}}/repos`;
        request.setHeader("Authorization", "Basic "+ authHeader);
        http:Response response = {};
        http:HttpConnectorError err;
        response, err = gitEP.get(gitPath, request);
        return response, err;
    }

    @Description {
        value:"List all issues under a given repository"
    }
    @Param {
        value:"organization: name of the orgnization on which issues should be fetched"
    }
    @Return {
        value:"Response object"
    }
    action getIssuesOfRepoByState (string orgnization, string repository, string state) (http:Response, http:HttpConnectorError) {
        http:Request request = {};
        string gitPath = string `/repos/{{orgnization}}/{{repository}}/issues?state={{state}}`;
        request.setHeader("Authorization", "Basic "+ authHeader);
        http:Response response = {};
        http:HttpConnectorError err;
        response, err = gitEP.get(gitPath, request);
        return response, err;
    }
}
```
