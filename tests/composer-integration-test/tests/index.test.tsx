import * as path from 'path';
import { sync as globSync } from 'glob';
import { balToolsPath } from './server';
import URI from 'vscode-uri';
import * as React from "react";
import { Diagram, DiagramMode } from '@ballerina/composer/packages/diagram';
import { DiagramUtils } from '@ballerina/composer/packages/diagram/lib/src/diagram/diagram-utils';
import * as fontUtils from '@ballerina/composer/packages/diagram/lib/src/utils';
import * as controllerPanel from '@ballerina/composer/packages/diagram/lib/src/diagram/controllers/controller-panel';
import { create } from 'react-test-renderer';
import { StdioBallerinaLangServer, createStdioLangClient,
    IBallerinaLangClient, BallerinaAST } from '@ballerina/composer/packages/lang-service';
import { CompilationUnit } from '@ballerina/composer/packages/ast-model/lib/src';

let langServer: StdioBallerinaLangServer;
let langClient: IBallerinaLangClient;
const bbeDir = path.join(balToolsPath, 'examples');

beforeAll((done) => {
    langServer = new StdioBallerinaLangServer(balToolsPath);
    langServer.start();
    if (langServer.lsProcess) {
        createStdioLangClient(langServer.lsProcess, () => {}, () => {})
            .then((client) => {
                langClient = client;
                done();
            }, (reason) => {
                console.log('Could not connect to LS properly');
                console.log(reason);
                done();
            });
    }
});

test('Lang-server is started properly', (done) => {
    langClient.init()
        .then((result) => {
            expect(result.capabilities.experimental.astProvider).toBe(true);
            done();
        });
});

function testDiagramRendering(ast: BallerinaAST,  uri: string) {

    const getTextWidth = jest.fn();
    getTextWidth.mockReturnValue(100);
    jest
        .spyOn(DiagramUtils, 'getTextWidth')
        .mockImplementation(getTextWidth);

    const getCodePoint = jest.fn();
    getCodePoint.mockReturnValue("");
    jest
        .spyOn(fontUtils, 'getCodePoint')
        .mockImplementation(getCodePoint);
    
    jest.spyOn(controllerPanel, "ControllerPanel")
            .mockImplementation(jest.fn().mockReturnValue(<div />));
    jest.spyOn(console, "error")
        .mockImplementation(jest.fn());

    const component = create(
        <Diagram
            ast={ast as CompilationUnit}
            width={1000}
            height={1000}
            mode={DiagramMode.DEFAULT}
            zoom={1}
        />
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
}

var bbeFiles = globSync(path.join(bbeDir, '**', '*.bal'), {});
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
        langClient.close();
        langServer.shutdown();
    }
});