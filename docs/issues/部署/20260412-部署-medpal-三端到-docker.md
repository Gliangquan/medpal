# Issue: 部署 MedPal 三端到 Docker（复用 shared-infra）

## Goal

参考 FoodPal 的部署方式，将 MedPal 的后端、PC 端、uniapp H5 部署到服务器 Docker，复用现有 `shared-infra` 网络中的公共容器 `mysql` 和 `minio`。

## Scope

- 后端端口：9010 -> 容器 9901
- PC 端端口：9011 -> 容器 80
- uniapp H5 端口：9012 -> 容器 8080
- 复用服务器已有：`mysql:3306`、`minio:9000`

## Acceptance Criteria

- [ ] `http://107.148.176.142:9010/api` 可访问后端
- [ ] `http://107.148.176.142:9011` 可访问 PC 端
- [ ] `http://107.148.176.142:9012` 可访问 uniapp H5
- [ ] MedPal 后端成功连接 `shared-infra` 的 mysql 和 minio
- [ ] `medpal` 数据库完成初始化
- [ ] 三个容器均处于 Up 状态

## Plan

- [x] 对照 FoodPal 现有 compose / Dockerfile / nginx 配置
- [x] 识别 MedPal 当前 API / WebSocket / MinIO / 数据库配置
- [x] 修改本地部署相关配置与文件
- [ ] 上传变更到服务器 `/root/MedPal`
- [ ] 在服务器创建 compose 并初始化数据库
- [ ] 构建并启动三端容器
- [ ] 验证端口与页面

## Notes

- FoodPal 复用 `shared-infra` 外部网络，后端通过容器名 `mysql` / `minio` 访问公共基础设施。
- MedPal 后端默认 server.port 为 `9901`，适合容器内直接复用。
- MedPal PC 端已支持 `VITE_SERVICE_ORIGIN`，已改为未配置时默认使用当前站点 origin。
- MedPal uniapp 源码原本写死 `127.0.0.1:9901/api`，已改为 H5 下优先使用 `window.location.origin/api`。
- 当前仓库没有 uniapp H5 的标准构建脚本，但存在现成产物 `MedPal-uniapp/unpackage/dist/build/web`，可先直接容器化部署该产物。

## Risks

- uniapp 当前是预构建静态产物部署，不是服务器实时构建；后续若要重建 H5，需要补标准构建链路。
- 若数据库导入存在旧数据/外键顺序问题，需要按脚本顺序初始化。

## Review

- 进行中
