import * as path from 'path';
import { sync as globSync } from 'glob';
import { startBallerinaLangServer, MinimalLangClient, balToolsPath, BallerinaAST } from './server';
import URI from 'vscode-uri';
import * as React from 'react';
import { BallerinaDiagramWrapper } from '@ballerina/diagram';
import { create } from 'react-test-renderer';

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

function testDiagramRendering(ast: BallerinaAST,  uri: string) {
    test('Diagram renders properly', () => {
        function getAST(url: string) {
            return Promise.resolve({ ast });
        }
        function parseFragment(fragment: any) {
        }
        function goToSource(pos: any) {
        }
        function onChange(ast: any) {
        }
        function getEndpoints() {
        }

        const component = create(
            <BallerinaDiagramWrapper 
                docUri={uri}
                getAST={getAST}
                onChange={onChange}
                width={1000}
                height={1000}
                getEndpoints={getEndpoints}
                parseFragment={parseFragment}
                goToSource={goToSource}
            />
        );
        let tree = component.toJSON();
        console.log(tree);
    });
}

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
            testDiagramRendering(resp.ast, uri);
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