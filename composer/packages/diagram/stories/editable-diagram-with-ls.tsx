// tslint:disable-next-line:no-submodule-imports
import { createWSLangClient } from "@ballerina/lang-service/lib/src/client/ws";

async function test(): Promise<number> {
  // tslint:disable-next-line:no-empty
  const client = await createWSLangClient(8081, () => {}, () => {});
  const samples = await client.fetchExamples({});
  // tslint:disable-next-line:no-console
  console.log(samples);
  return 0;
}

test();
