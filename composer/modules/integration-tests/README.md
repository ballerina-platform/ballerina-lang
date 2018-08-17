How to make changes to source gen

Full build method
-----------------

1) Make the changes in tree.g in `web` module.
2) Run `npm run gen-source-gen`.
3) Run `mvn install` in `web`.
4) Keep backend running.
5) In `js-test` run `mvn clean install` to copy the artifacts, you may `Ctrl+C` during test run
6) Run tests using `npm run test`.
   Eg: `npm run test:debug -- --grep 'src/test/resources/passing/annotation-1.bal file serializ'`
   
Hot update method
-----------------

### setup

1) Keep running `npm run dev` form `web` in the background.
2) Change `js-test/.balelrc` file's
   `{ "src": "target/js-files-from-web", "expose": "composer" }`
   to
   `{ "src": "../web/dist", "expose": "composer" }`.
3) Run `npm run compile` from `js-test`.
4) Keep backend running.

### run
1) Make the changes in tree.g in `web` module.
2) Run `npm run gen-source-gen`. This will trigger an automatic webpack build.
3) Run tests using `npm run test`
   Eg: `npm run test:debug -- --grep 'src/test/resources/passing/annotation-1.bal file seri'`


How to run backend for the tests to work
----------------------------------------

* Go to `services/workspace-service`, build if not, and run `java -Dbal.composer.home=.  -jar ./target/*jar-with-dependencies.jar`
* Run the actual pack.
* Run from IDE.
* Run mvn from `js-test` will automatically run the backend using with-dependencies-jar but it will kill it at the build end.
