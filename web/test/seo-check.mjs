import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const root = new URL("..", import.meta.url);
const legacyDomain = ["bareuang", "vercel", "app"].join("\\.");
const legacyPattern = new RegExp(`${legacyDomain}|Bare-Budget`);
const files = ["index.html", "privacy.html", "terms.html"];
const pages = await Promise.all(files.map(async (file) => [file, await readFile(new URL(file, root), "utf8")]));

for (const [file, html] of pages) {
  assert.match(html, /<link rel="canonical" href="https:\/\/bareuang\.app\//, `${file}: canonical`);
  assert.doesNotMatch(html, legacyPattern, `${file}: legacy URL`);
}
const sitemap = await readFile(new URL("sitemap.xml", root), "utf8");
assert.doesNotMatch(sitemap, legacyPattern);
assert.match(sitemap, /https:\/\/bareuang\.app\//);
console.log("SEO checks passed");
