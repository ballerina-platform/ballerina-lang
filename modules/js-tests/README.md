How to make changes to source gen

Full build method
-----------------

1) Make the changes in tree.g in `web` module.
2) Run `npm run gen-source-gen`
3) Run `mvn install` in `web`
4) Keep backend running
5) In `js-test` run `mvn clean install` to copy the artifacts, you may `Ctrl+C` during test run
6) Run tests using `npm run test`
   Eg: `npm run test:debug -- --grep 'src/test/resources/passing/annotation-1.bal file serializ'`
