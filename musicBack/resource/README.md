# 统一直播资源目录

本项目所有被前端引用/播放的资源（音乐、封面、歌词）统一存放在这里的相对目录中，
供 `Mod_upload`（端口 8005）通过静态资源映射对外提供，前端以**相对路径**引用。

## 目录结构

```
musicBack/resource/
├─ music/   # 音乐文件（.mp3 / .wav / .flac ...）
├─ image/   # 封面图片（.jpg / .png / .webp ...）
└─ lrc/     # 歌词（.lrc）
```

## URL 约定（四种前缀都可访问，指向同一批文件）

| URL 前缀 | 来源 | 实际目录 |
|---|---|---|
| `/music/**`、`/uploads/music/**` | 历史数据 / 上传返回 | resource/music/ |
| `/img/**`、`/uploads/image/**` | 历史数据 / 上传返回 | resource/image/ |
| `/lyric/**`、`/uploads/lrc/**` | 历史数据 / 上传返回 | resource/lrc/ |

即：数据库里的旧地址 `/music/daoxiang.mp3` 与上传服务返回的 `/uploads/music/.../daoxiang.mp3` 都能访问到同一物理文件。

## 重要说明

1. **示例/种子数据目前没有真实文件**：数据库里的 `/music/xxx.mp3`、`/img/xxx.jpg`、
   `/lyric/xxx.lrc` 只是记录，仓库里并没有对应的二进制文件。
   请把真实资源放入上述对应子目录，并**提交进 git**，协作者 clone 后即可获取并使用。

2. **上传的新文件**：`Mod_upload` 会写入这里的相对目录（`../resource/<type>/`）。
   可通过环境变量覆盖实际存放位置：
   - `UPLOAD_IMAGE_PATH`
   - `UPLOAD_LRC_PATH`
   - `UPLOAD_MUSIC_PATH`

3. **启动目录要求**：`Mod_upload` 的静态映射 `file:../resource/...` 是**相对工作目录**，
   请确保从 `musicBack/Mod_upload` 目录（IDEA 中该模块工作目录）启动，否则相对路径会定位失败。

4. **前端访问链路**：
   - 开发：`playerFront` 的 Vite dev server 已把 `/music`、`/img`、`/lyric`、`/uploads`
     代理到 `Mod_upload`（8005）。
   - 生产：需用 Nginx 把上述前缀转发到 `Mod_upload`，或直接托管本目录。
