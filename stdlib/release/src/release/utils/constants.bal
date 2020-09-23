const RELEASES = "/releases";
const TAGS = "/tags";

const FIELD_RELEASE = "release";
const FIELD_TAG_NAME = "tag_name";
const EVENT_TYPE = "stdlibReleasePipeline";

const OPERATION_RELEASE = "releasing";
const OPERATION_VALIDATE = "validating release of";

const SLEEP_INTERVAL = 60000;
const MAX_WAIT_CYCLES = 20; // Max wait time is 20 minutes
