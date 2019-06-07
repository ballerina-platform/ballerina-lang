export function isWindows(): boolean {
	return process.platform === "win32";
}

export function isUnix(): boolean {
	let platform = process.platform;
	return platform === "linux"
		|| platform === "darwin"
		|| platform === "freebsd"
		|| platform === "openbsd";
}