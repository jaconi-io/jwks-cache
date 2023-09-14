module.exports = {
  extends: ["@commitlint/config-conventional"],
  ignores: [
    (message) =>
      /((build\(docker\))|(ci)|(build\(go\))): bump .+ from .+ to .+/.test(
        message
      ),
  ],
};
