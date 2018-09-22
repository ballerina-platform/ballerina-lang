import ballerina/http;
import ballerina/log;
import ballerina/websub;

@final string WEBSUB_TOPIC_ONE = "http://one.websub.topic.com";
@final string WEBSUB_TOPIC_TWO = "http://two.websub.topic.com";

boolean remoteTopicRegistered;

websub:WebSubHub webSubHub = startHubAndRegisterTopic();

endpoint websub:Client websubHubClientEP {
    url: webSubHub.hubUrl
};

endpoint http:Listener publisherServiceEP {
    port:8080
};

service<http:Service> publisher bind publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    discover(endpoint caller, http:Request req) {
        http:Response response;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_TOPIC_ONE);
        response.statusCode = 202;
        caller->respond(response) but {
            error e => log:printError("Error responding on ordering", err = e)
        };
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    notify(endpoint caller, http:Request req) {
        remoteRegisterTopic();
        json jsonPayload = check req.getJsonPayload();
        string mode = jsonPayload.mode.toString();

        http:Response response;
        response.statusCode = 202;
        caller->respond(response) but {
            error e => log:printError("Error responding on notify request", err = e)
        };


        if (mode == "direct") {
            json payload = {"action":"publish","mode":"internal-hub"};
            webSubHub.publishUpdate(WEBSUB_TOPIC_ONE, payload) but {
                error e => log:printError("Error publishing update directly", err = e)
            };
        } else {
            json payload = {"action":"publish","mode":"remote-hub"};
            websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, payload) but {
                error e => log:printError("Error publishing update remotely", err = e)
            };
        }
    }
}

function startHubAndRegisterTopic() returns websub:WebSubHub {
    websub:WebSubHub internalHub = websub:startHub(9191, remotePublishingEnabled = true) but {
        websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub
    };
    internalHub.registerTopic(WEBSUB_TOPIC_ONE) but {
        error e => log:printError("Error registering topic directly", err = e)
    };
    return internalHub;
}

function remoteRegisterTopic()  {
    if (remoteTopicRegistered) {
        return;
    }
    websubHubClientEP->registerTopic(WEBSUB_TOPIC_TWO) but {
        error e => log:printError("Error registering topic remotely", err = e)
    };
    remoteTopicRegistered = true;
}
