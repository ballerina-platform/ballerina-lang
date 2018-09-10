import { Uri, ExtensionContext } from "vscode";
import { join } from "path";

export function getWebViewResourceRoot(context: ExtensionContext): Uri {
    return (process.env.DIAGRAM_DEBUG === "true") ? 
                Uri.parse("http://localhost:8081")
                : Uri.file(join(context.extensionPath, 'resources', 'diagram')).with({ scheme: 'vscode-resource' });
}