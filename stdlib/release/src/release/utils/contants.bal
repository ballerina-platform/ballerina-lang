const CONFIG_FILE_PATH = "./resources/stdlib_modules.json";
const ACCESS_TOKEN_ENV = "GITHUB_TOKEN";

const API_PATH = "https://api.github.com/repos/BuddhiWathsala";
const DISPATCHES = "/dispatches";
const RELEASES = "/releases";
const TAGS = "/tags";

const ACCEPT_HEADER_KEY = "Accept";
const ACCEPT_HEADER_VALUE = "application/vnd.github.v3+json";
const AUTH_HEADER_KEY = "Authorization";

const FIELD_RELEASE = "release";
const FIELD_TAG_NAME = "tag_name";
const EVENT_TYPE = "stdlib-release-pipeline";

const OPERATION_RELEASE = "releasing";
const OPERATION_VALIDATE = "validating release of";

const SLEEP_INTERVAL = 60000;
const MAX_WAIT_CYCLES = 20; // Max wait time is 20 minutes
