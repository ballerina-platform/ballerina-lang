import { ChildProcess } from "child_process";
import { createStdioLangClient, IBallerinaLangClient, StdioBallerinaLangServer } from "../src";

let client: IBallerinaLangClient;
let server: StdioBallerinaLangServer;

beforeAll((done) => {
    server = new StdioBallerinaLangServer();
    server.start();
    // tslint:disable-next-line:no-empty
    createStdioLangClient(server.lsProcess as ChildProcess, () => {}, () => {})
        .then((stdioClient) => {
            client = stdioClient;
            done();
        });
});

test("stdio server init success", (done) => {
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
