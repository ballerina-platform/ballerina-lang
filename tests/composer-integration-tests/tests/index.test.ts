import * as path from 'path';
import { sync as globSync } from 'glob';
import { startBallerinaLangServer, MinimalLangClient, balToolsPath } from './server';
import URI from 'vscode-uri';

let langClient : MinimalLangClient;

const bbeDir = path.join(balToolsPath, 'examples');

beforeAll((done) => {
    startBallerinaLangServer()
    .then((languageClient) => {
        if (languageClient) {
            langClient = languageClient;
        } else {
            console.log('Could not start LS properly');
        }
        done();
    }, (reason) => {
        console.log('Could not start LS properly');
        console.log(reason);
        done();
    });
});

test('Lang-server is started properly', () => {
    expect(langClient.initializedResult.capabilities.experimental.astProvider).toBe(true);
});

var bbeFiles = globSync(path.join(bbeDir, '**', '*.bal'), {});
bbeFiles.forEach((file) => {
    const uri = URI.file(file).toString();
    test('Parsing BBE: ' + path.basename(file), (done) => {
        langClient.getAST({
            documentIdentifier: {
                uri 
            }
        }).then((resp) => {
            expect(resp.parseSuccess).toBeTruthy();
            expect(resp.ast).toBeDefined();
            done();
        }, (reason) => {
            console.log('Error: ' + reason);
            done();
        });
    });
});

afterAll(() => {
    if (langClient) {
        langClient.kill();
    }
});