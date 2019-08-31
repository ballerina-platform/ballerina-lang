import TelemetryReporter from "vscode-extension-telemetry";
import { BallerinaExtension } from "../core";

const INSTRUMENTATION_KEY = "3a82b093-5b7b-440c-9aa2-3b8e8e5704e7";

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
