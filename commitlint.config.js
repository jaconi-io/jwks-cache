module.exports = {
  extends: ["@commitlint/config-conventional"],
  ignores: [
    (message) =>
      /((ci)|(build\(java\))): bump .+ from .+ to .+/.test(
        message
      ),
  ],
};
