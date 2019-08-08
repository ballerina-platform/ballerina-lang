import TelemetryReporter from "vscode-extension-telemetry";
import { BallerinaExtension } from "../core";

// TODO generate a instrumentation key from azure with a production account
const INSTRUMENTATION_KEY = "d1155177-d396-4504-ad0f-c3570da84c3e";

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