import { WSBallerinaLangServer } from "@ballerina/lang-service";

const langServer = new WSBallerinaLangServer(8081);
langServer.start();
