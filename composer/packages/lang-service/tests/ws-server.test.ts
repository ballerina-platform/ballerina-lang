import { createWSLangClient, IBallerinaLangClient, WSBallerinaLangServer } from "../src";

let client: IBallerinaLangClient;
let server: WSBallerinaLangServer;

beforeAll((done) => {
    server = new WSBallerinaLangServer(9099);
    server.start();
    // tslint:disable-next-line:no-empty
    createWSLangClient(9099, () => {}, () => {})
        .then((wsClient) => {
            client = wsClient;
            done();
        });
});

test("WS server init success", (done) => {
    expect(client).toBeTruthy();
    if (client) {
        client.init()
            .then((result) => {
                expect(result.capabilities).toBeTruthy();
                expect(result.capabilities.experimental.astProvider).toBeTruthy();
                expect(result.capabilities.experimental.examplesProvider).toBeTruthy();
                done();
            });
    }
});

afterAll((done) => {
    client.close();
    server.shutdown();
    done();
});
