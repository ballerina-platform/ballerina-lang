import { startBallerinaLangServer, MinimalLangClient } from './server';

let langClient : MinimalLangClient;

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

afterAll(() => {
    if (langClient) {
        langClient.kill();
    }
});