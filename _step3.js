const fs = require("fs");
const base = "E:/开发/music-dreamer-mini";

// ===== 1. 更新 pages.json =====
let json = JSON.parse(fs.readFileSync(base + "/pages.json", "utf8"));
// 插在 login 之前（player 不是 tabBar）
const idx = json.pages.findIndex(p => p.path === "pages/login/login");
json.pages.splice(idx >= 0 ? idx : 0, 0, {
  "path": "pages/player/player",
  "style": {
    "navigationStyle": "custom",
    "disableScroll": true
  }
});
fs.writeFileSync(base + "/pages.json", JSON.stringify(json, null, 2), "utf8");
console.log("✓ pages.json 已注册 pages/player/player");

// ===== 2. 更新 discover.vue：goPlayer 跳转 =====
let disc = fs.readFileSync(base + "/pages/discover/discover.vue", "utf8");
disc = disc.replace(
  "function goPlayer() { /* 完整播放器 Phase 1.5 再做，先播放栏内嵌 */ }",
  "function goPlayer() { uni.navigateTo({ url: '/pages/player/player' }) }"
);
fs.writeFileSync(base + "/pages/discover/discover.vue", disc, "utf8");
console.log("✓ discover.vue goPlayer 已改为 navigateTo /pages/player/player");

// ===== 3. 清理临时文件 =====
try { fs.unlinkSync("d:/Project/Music/_step1.js"); } catch(e) {}
try { fs.unlinkSync("d:/Project/Music/_step2.js"); } catch(e) {}
console.log("✓ 临时文件已清理");
