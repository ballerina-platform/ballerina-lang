import TelemetryReporter from "vscode-extension-telemetry";
import { BallerinaExtension } from "../core";

// TODO generate a working instrumentation key from azure
const INSTRUMENTATION_KEY = "ballerina.ext.instrumentation.key";

export function createTelemetryReporter(ext: BallerinaExtension): TelemetryReporter {
    const reporter = new TelemetryReporter(ext.getID(), ext.getVersion(), INSTRUMENTATION_KEY); 
    if (ext.context) {
        ext.context.subscriptions.push(reporter);
    }
    return reporter;
}

export * from "./events";
export * from "./exceptions";
export * from "./components";