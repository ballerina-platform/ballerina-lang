import * as path from 'path';
import { sync as globSync } from 'glob';
import { startBallerinaLangServer, MinimalLangClient, balToolsPath, BallerinaAST } from './server';
import URI from 'vscode-uri';
import * as React from 'react';
import { BallerinaDiagramWrapper } from '@ballerina/diagram';
import SizingUtil from '@ballerina/diagram/lib/plugins/ballerina/diagram/views/default/sizing-util';
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

    var getTextWidth = jest.fn();
    getTextWidth.mockReturnValue(100);
    jest
        .spyOn(SizingUtil.prototype, 'getTextWidth')
        .mockImplementation(getTextWidth);

    var consoleLog = jest.fn();
    jest
        .spyOn(console, 'log')
        .mockImplementation(consoleLog);

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
    component.toJSON();
    expect(getTextWidth).toHaveBeenCalled();
}

var bbeFiles = globSync(path.join(bbeDir, '**', 'hello_world_client.bal'), {});
bbeFiles.forEach((file) => {
    const uri = URI.file(file).toString();
    test('Parsing and Rendering BBE: ' + path.basename(file), (done) => {
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