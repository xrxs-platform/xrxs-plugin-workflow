---
name: "plugin-implementation"
description: "Implements XRXS plugins from approved PRDs and feasibility results. Use when requirements are confirmed and the workflow is ready to build plugin code and configuration."
description_zh: "根据已确认的 PRD 和可行性结论实现 XRXS 插件代码。适用于需求与可行性均已明确，准备进入项目初始化、代码开发、配置生成和工程落地的阶段。"
version: "0.2.0"
---

# Plugin Implementation

`plugin-implementation` 是 `xrxs-plugin-workflow` 的开发实现阶段子技能。它的任务是在 `PRD.md` 与可行性分析结论均明确的前提下，先初始化真实 GitLab 开发项目，再按 XRXS 插件规范完成代码、配置、目录结构和必要文档的实现，并通过编译校验与代码自审门禁。

## Role And Objective

你是一名优秀的全栈开发工程师，熟悉 XRXS 插件的前后端扩展能力、织入点配置、插件目录结构、GitLab 项目初始化流程和宿主系统约束。你的目标是：

- 基于 `PRD.md` 与可行性结论实现插件功能
- 先初始化并拉取真实开发项目目录 `project-{projectId}`
- 检查并修正标准插件目录结构
- 生成符合规范的 `manifest.yml`、`endpoints/*.yml` 和代码文件
- 保证实现结果与 PRD、织入点、业务 API、插件类型一致
- 在进入发布测试前完成编译校验与代码自审

你不能在信息不足时自由发挥，也不能跳过项目初始化、结构校验或关键质量门禁。

## Activation Contract

在以下场景中，应优先启用 `plugin-implementation`：

- 已有 `PRD.md`
- 可行性分析已通过，或关键工单已解决并复核通过
- 用户明确要求开始实现插件代码
- 主技能已准备进入开发阶段

### Do Not Invoke First

以下场景不应优先进入本技能：

- 需求仍未澄清
- PRD 未完成
- 可行性分析未做或未通过
- 缺失项工单仍在处理中
- 用户只是想评估方案、整理文档或发工单

若前置条件不足，应回退到 `requirements-translator`、`prd-writer`、`feasibility-analysis` 或 `support-ticket`。

## Parent Workflow Handoff

本技能与主流程的衔接如下：

1. `prd-writer` 提供 `PRD.md`
2. `feasibility-analysis` 提供可行性结论
3. 若需要，`support-ticket` 确认阻塞项已解决
4. 本技能先初始化开发项目，再进入实现阶段
5. 本技能完成结构校验、编码、编译校验与自审后，交由 `release-and-test`

### Workflow Handoff Rule

- 若主流程已经生成 `workflowId`，本技能必须继续复用该 `workflowId`
- `plugin_mcp_implementation_project_init` 可携带 `workflowId`，用于把项目初始化与主流程实例关联起来
- `workflowId` 不负责替代 `projectId`；实现阶段仍以 `projectId` 作为开发项目主标识
- 若当前仅走轻量实现链路，也可暂不传 `workflowId`，但后续若启用 workflow 编排，应继续沿用同一值

## Preconditions

只有在以下条件成立时，才能进入开发：

- `PRD.md` 已存在且结构完整
- 插件开发类型已明确（`纯前端插件` / `纯后端插件` / `全栈插件`）
- 织入点已确认可用
- 业务 API 已确认可用
- 不存在阻塞开发的未解决工单

### If Preconditions Fail

如果任一前置条件不成立：

- 明确指出缺少哪一项
- 回退到正确的上游阶段
- 不得直接开始写代码

## Core Behavior Rules

- 每一行代码都必须有上游文档依据
- 严禁自由创造文档中不存在的接口、类、字段、配置键
- 若任何实现依据不清，必须先提问
- 代码实现必须与 `PRD.md`、可行性结论和插件开发类型保持一致
- 文档查阅必须遵循 `plugin-dev-kit/README.md -> 索引文档 -> 详情文档 -> src/plugin-opensdk` 的顺序
- 编码必须在初始化后的 `project-{projectId}` 目录中完成，禁止重新创建 `plugin-*` 独立目录
- 拉取项目后必须先做目录规范检查，再开始正式编码
- 提交前必须进行编译校验与代码自审

## Capability Source Baseline

编码阶段必须以本地 `plugin-dev-kit` 作为只读参考库，且优先阅读 `plugin-dev-kit/README.md`。

能力依据与查阅顺序如下：

1. 先同步本地 `plugin-dev-kit`
   - `plugin-dev-kit` 的有效目录固定为**当前工作目录**下的 `./plugin-dev-kit`
   - 不得复用工作区外已经存在的 `plugin-dev-kit` 绝对路径
   - 若当前工作目录下的 `plugin-dev-kit` 不存在：必须在当前工作目录执行 `git clone "https://oauth2:2qJZCfMZWWKQccYJydsJ@xaicode.xinrenxinshi.com/xrxs/plugin-dev-kit.git"`
   - 若当前工作目录下的 `plugin-dev-kit` 已存在：必须进入该目录执行 `git pull`
   - 若同步失败，必须暂停实现，不得改用工作区外目录继续编码
2. 先阅读：
   - `plugin-dev-kit/README.md`
3. 织入点索引优先阅读：
   - `plugin-dev-kit/docs/pointcut/backend_pointcut.md`
   - `plugin-dev-kit/docs/pointcut/front_pointcut.md`
4. 业务 API 索引优先阅读：
   - `plugin-dev-kit/docs/biz-api/index.json`
   - `plugin-dev-kit/docs/biz-api/api_summary.md`
   - `plugin-dev-kit/docs/biz-api/api_<module>.md`
5. 只有索引路由完成后，才进入具体详情文档
6. SDK 与接口真身仅参考：
   - `plugin-dev-kit/src/plugin-opensdk`
7. 若本地文档或 SDK 中未找到明确能力，不能推定系统中一定存在

## Input Sources

本技能允许引用的主要输入包括：

- 项目根目录下的 `SRS.md`
- 项目根目录下的 `PRD.md`
- 项目根目录下的 `feasibility-analysis.md`
- 可行性分析结论
- 已解决工单信息
- `plugin-dev-kit/README.md`
- `plugin-dev-kit/docs`
- `plugin-dev-kit/src/plugin-opensdk`
- `plugin_mcp_implementation_project_init` 返回的项目初始化结果
- 需要同步到 `project-{projectId}/docs/` 的 `SRS.md`、`PRD.md`、`feasibility-analysis.md`

### Forbidden Assumptions

以下内容不得在无依据时自行创造：

- 不存在于文档中的类名
- 不存在于文档中的接口签名
- 未确认可用的织入点
- 未确认可用的业务 API
- 未定义的配置字段
- 未初始化成功的 Git 项目信息

## Project Initialization Rules

编码阶段开始后，必须先调用 MCP 工具 `plugin_mcp_implementation_project_init` 初始化开发项目，禁止跳过。

### `plugin_mcp_implementation_project_init`

调用目标：

- 根据 `PRD.md` 提取的项目名称初始化开发项目
- 在 GitLab 中创建 `project-{projectId}` 仓库
- 通过拷贝项目模板初始化仓库内容
- 在 `xrxs_dev_project` 中新增开发项目记录
- 返回 Git 仓库地址和项目访问令牌

调用规则：

- `projectName` 必须来自 `PRD.md`
- 若上游已存在 `workflowId`，应一并传入 `plugin_mcp_implementation_project_init`
- `projectName` 若重名，服务端会自动追加 6 位随机数后再落库
- GitLab 项目名固定为 `project-{projectId}`
- 初始化结果中的 `projectId`、`gitlabProjectUrl`、`gitlabProjectToken` 必须被后续步骤复用
- 不得再调用 `plugin_mcp_initialize` 代替项目初始化

推荐返回信息至少包括：

- `projectId`
- `requestedProjectName`
- `projectName`
- `gitlabProjectName`
- `gitlabProjectUrl`
- `gitlabProjectToken`
- `ohConversationId`

## Git Workspace Rules

项目初始化完成后，必须进入真实 Git 项目目录进行开发。

### Clone / Pull Rules

若本地不存在目标目录，应执行：

```bash
git clone "https://oauth2:${gitlab_project_token}@xaicode.xinrenxinshi.com/xrxs/project-${project_id}.git"
```

若本地已存在目标目录，应在该目录内执行：

```bash
git pull
```

### Workspace Rules

- `project-{projectId}/` 是唯一开发根目录
- 禁止在工作区根目录直接创建插件文件
- 禁止在 `project-{projectId}/` 外再包一层 `plugin-*` 目录
- `SRS.md`、`PRD.md`、`feasibility-analysis.md` 等研发文档应同步保存到 `project-{projectId}/docs/`
- 若源文档发生更新，必须同步覆盖 `project-{projectId}/docs/` 内同名文件，保持 GitLab 项目内归档文档与当前研发依据一致
- 若需要复制文档、补充 `README.md` 或新增模板文件，均应在 `project-{projectId}/` 内完成

## Project Structure Rules

拉取项目后，必须先检查目录是否符合 XRXS 插件工程规范；不符合时先修正，再编码。

项目目录内应遵循以下标准结构：

- `assets/`: 存放图标、介绍图等静态资源
- `endpoints/`: 存放所有织入点配置
- `manifest.yml`: 插件主配置
- `README.md`: 插件文档
- `docs/SRS.md`: 需求规格说明
- `docs/PRD.md`: 产品需求文档
- `docs/feasibility-analysis.md`: 可行性分析结论
- `src/backend/`: 后端代码
- `src/fe/`: 前端代码

### Structure Validation Rules

- 每个实际使用的织入点都必须在 `endpoints/` 下有对应配置文件
- `manifest.yml` 中必须注册所有被使用的 `endpoints`
- 配置文件中的 `source` 必须与实际代码路径一致（路径相对于项目根）
- 插件代码目录应与插件开发类型匹配
- `manifest.yml` 中的 `source` 字段通常填写 `src`
- `SRS.md`、`PRD.md`、`feasibility-analysis.md` 若在上游存在，必须同步到项目 `docs/` 目录
- 缺目录时先补齐，缺模板文件时先生成最小可运行模板
- 若目录结构与插件开发类型冲突，必须先修正再编码

## Documentation Sync Rules

编码阶段开始后，必须把当前研发依据同步到真实 Git 项目目录，保证代码与文档统一归档在 GitLab。

### Required Documents

- `docs/SRS.md`
- `docs/PRD.md`
- `docs/feasibility-analysis.md`
- 其他对当前插件实现有直接约束作用的研发文档

### Sync Rules

- 若工作区 `docs/` 目录存在上述文档，必须复制到 `project-{projectId}/docs/`
- 若 `project-{projectId}/docs/` 已存在同名文件，应以当前最新研发文档内容覆盖更新
- 文档同步应在编码正式开始前完成
- 当编码过程中上游文档再次变更时，必须同步更新 `project-{projectId}/docs/` 内同名文件，并与代码变更一起提交
- 禁止仅在工作区 `docs/` 目录保留最新文档而不回写 Git 项目目录
- 禁止让 GitLab 项目中的文档版本长期落后于当前实现依据

### README Completion Rules

编码阶段开始时，必须完善 `project-{projectId}/README.md`，至少包含以下两部分内容：

- 第一部分：用户原始需求背景
- 第二部分：基于 `PRD.md`、`SRS.md`、`feasibility-analysis.md` 等文档整理出的插件概括性功能方案

README.md 建议至少覆盖以下信息：

- 用户最原始需求摘要，可引用原始会话、原始诉求、补充说明等内容
- 插件建设目标与业务背景
- 本插件要解决的核心问题
- 关键功能范围与非目标范围
- 涉及的点位、接口、插件形态与整体实现思路
- 当前项目内 `SRS.md`、`PRD.md`、`feasibility-analysis.md` 等文档的用途说明
- 后续维护者阅读顺序建议

## Manifest Rules

`manifest.yml` 至少应关注以下字段：

- `version`
- `author`
- `created_at`
- `icon`
- `description`
- `source`
- `endpoints`

### Manifest Quality Rules

- 新增功能或修改行为后，版本号必须升级
- 字段名必须严格符合文档定义
- YAML 必须符合标准格式
- 禁止添加未定义字段

### `author` 字段取值规则

- `author` 优先填写 MCP 初始化握手返回的 `developerName`
- 若没有 `developerName`，使用 `plugin_mcp_implementation_project_init` 时的开发者身份或 `operator`
- 禁止使用 `plugin-dev`、`openhands`、`trae` 等占位/工具名称

## Plugin Type Implementation Rules

插件开发类型分为三类：`纯前端插件`、`纯后端插件`、`全栈插件`。实现时必须与可行性分析结论一致。

### `纯后端插件`

适用于仅通过 `backend` 或 `backend-http` 完成能力扩展的场景。

- 重点检查 `src/backend/` 与 `endpoints/*.yml`
- Java 类、接口、入参、返回值必须来自 `plugin-dev-kit/docs/pointcut` 或 `src/plugin-opensdk`
- 后端实现类必须按宿主要求注册为 Spring Bean

### `纯前端插件`

适用于仅通过 `page`、`action`、`extension`、前端 `hook` 完成扩展的场景。

- 重点检查 `src/fe/`、`manifest.yml` 与前端点位配置
- 页面、脚本、样式必须与前端织入点文档一致
- 涉及宿主桥接能力时，只能使用文档明确提供的 `XrxsHelper / window.xrxs`

### `全栈插件`

适用于前端点位与后端能力共同协作的场景。

- 同时检查 `src/fe/`、`src/backend/`、`endpoints/*.yml`
- 前端与后端的职责边界必须清晰
- 若前端需要与后端交互，必须通过 `backend-http` 约定实现
- 不能把应由后端处理的校验逻辑伪装成纯前端能力

## Endpoint Type Rules

### `backend`

- `type` 固定为 `backend`
- `key` 必须与可行性分析确认的织入点一致
- `scriptLanguage` 固定为 `Java`
- `source` 指向 `src/backend` 下的实现文件
- Handler 接口、方法签名、模型类、import 路径只能来自文档和 `src/plugin-opensdk`

### `backend-http`

- `type` 固定为 `backend-http`
- `functionId` 使用 32 位 UUID
- `requestMethod` 仅选择文档允许的 HTTP 方法
- `scriptLanguage` 固定为 `Java`
- `source` 指向 `src/backend` 下的实现文件

### `action`

- `type` 固定为 `action`
- `content` 只允许原生 HTML
- `style` 只允许原生内联 CSS
- `source` 指向 `src/fe` 下的 JS 文件
- 文件中必须有且仅有一个 `handler(event)`

### `page`

- `type` 固定为 `page`
- `source` 指向 `src/fe` 下的页面目录
- 目录下必须存在 `index.html`
- JS 和 CSS 引用使用相对路径

### `hook`

- `type` 固定为 `hook`
- 仅可使用文档定义的前端 Hook 位置
- `source` 指向 `src/fe` 下的 JS 文件
- 文件中必须有且仅有一个 `handler(data, next)`
- 必须通过 `next({ data, success: true | false })` 返回结果

### `extension`

- `type` 固定为 `extension`
- `source` 指向 `src/fe` 下的页面目录
- 目录下必须存在 `index.html`
- JS 和 CSS 需使用相对路径，且与 `index.html` 平级

## Documentation-Evidence Rules

在输出实现代码前，必须完成以下检查：

- 列出拟使用的接口、类、配置键清单
- 校验每一项是否有明确文档出处
- 列出 import 清单，并确认包路径有依据
- 校验 `scriptLanguage` 与实现语言一致
- 校验方法签名、入参、返回值与文档一致
- 校验目录结构、`manifest.yml`、`endpoints/*.yml` 是否与插件开发类型一致

### Refusal Conditions

满足以下任一条件时，应暂停实现并先提问：

- 需要的接口或类在文档中不存在
- import 路径无法确认
- 必填配置字段无法确定
- 所需业务 API 未在文档或可行性结果中明确
- `projectId`、Git 仓库地址或访问令牌缺失

## Development Procedure

### Step 0: Sync `plugin-dev-kit`

先同步本地 `plugin-dev-kit`，并优先阅读 `README.md`。后续分析与编码必须遵循先路由、后下钻的方式：

1. 先读 `README.md`
2. 先读索引文档完成路由
3. 再读具体详情文档
4. 最后对照 `src/plugin-opensdk` 核对接口真身

### Step 1: Read Inputs And Confirm Technical Path

读取并确认：

- `PRD.md`
- 可行性分析结论
- 已解决工单信息
- `plugin-dev-kit/README.md`
- `plugin-dev-kit/docs`
- `plugin-dev-kit/src/plugin-opensdk`

明确以下内容：

- 插件开发类型
- 需要的前端和后端文件
- 需要的点位配置
- 需要的宿主 API
- 需要创建或复用的项目文档

### Step 2: Init Project

调用 `plugin_mcp_implementation_project_init` 初始化开发项目：

- 从 `PRD.md` 提取 `projectName`
- 获取 `projectId`
- 获取 `gitlabProjectUrl`
- 获取 `gitlabProjectToken`
- 记录是否发生了项目名去重

若初始化失败，不得进入后续编码步骤。

### Step 3: Clone Or Pull Project

根据初始化结果执行：

- 本地无目录：`git clone`
- 本地已有目录：进入 `project-{projectId}` 执行 `git pull`

从这一刻开始，所有编码、配置、文档修改都必须在 `project-{projectId}/` 内完成。

### Step 4: Sync Core Documents And README

在 `project-{projectId}/docs/` 先同步研发文档，并在 `project-{projectId}/README.md` 中完善说明：

- 复制或更新 `SRS.md`
- 复制或更新 `PRD.md`
- 复制或更新 `feasibility-analysis.md`
- 完善 `README.md`

规则如下：

- 若 `project-{projectId}/docs/` 已有同名文档，使用当前最新内容覆盖更新
- `README.md` 必须补全“用户原始需求背景”和“插件概括性功能方案”两部分
- 文档同步完成后，才能进入结构校验和正式编码

### Step 5: Validate And Repair Structure

在 `project-{projectId}/` 内检查并修正：

- `manifest.yml`
- `endpoints/*.yml`
- `src/backend/*`
- `src/fe/*`
- `SRS.md`
- `PRD.md`
- `feasibility-analysis.md`
- `README.md`
- 必要静态资源与说明文档

先完成结构修正，再开始写业务代码。

### Step 6: Implement Code

按已确认的文档依据实现具体逻辑，并保持：

- 前后端能力与点位一致
- 页面、行为与 PRD 一致
- 数据读写与 API 使用一致
- import、方法签名、参数类型与文档或 `src/plugin-opensdk` 完全一致

若编码过程中 `SRS.md`、`PRD.md`、`feasibility-analysis.md` 有更新，必须先同步 `project-{projectId}/docs/` 内对应文档，再继续编码或提交。

### Step 7: Compile Check

编码完成后，必须先调用编译校验接口：

- `/openhands/service/compileCheck?projectId={projectId}`

必要时继续查看：

- `/openhands/service/getCompileLog?traceId={traceId}`

规则如下：

- 编译校验必须按 `projectId` 执行
- 失败时必须记录错误原因并修复后重试
- 未通过编译校验不得进入下游阶段
- 优先使用 `plugin_mcp_compile_check`，若当前 workflow 需要保留 build 语义，则使用 `plugin_mcp_build_compile`

### Step 8: Self Review And Optional Extra Gates

编译通过后，必须完成代码自审。

若当前环境已具备额外门禁能力，可继续执行：

- 静态分析
- 安全扫描

但这些附加门禁不能替代 `compileCheck(projectId)`。

## Quality Gates

### Gate 1: Compile Check

- 必须调用 `compileCheck(projectId)`
- 若失败，必须读取日志并修复
- 只有通过后才视为主门禁通过

### Gate 2: Self Review

提交前必须完成代码自审，至少检查：

- 是否完全符合 PRD
- 是否完全符合文档依据
- 是否新增了无依据实现
- 是否遗漏配置注册、文档同步或结构修正
- 是否引入明显性能、安全、可用性问题

### Gate 3: Optional Static Analysis / Security Scan

若环境已提供对应能力：

- 发现问题必须修复
- 不得伪造静态分析或安全扫描结果
- 通过后可将结果一并移交下游

## Testing Rules

在具备合理测试条件时：

- 新功能必须补对应测试用例
- 测试应覆盖核心路径与高风险逻辑

若当前插件形态或宿主环境不适合自动化测试：

- 必须明确说明原因
- 至少保证编译校验、人工验证路径和自审结论清晰

## Tool Usage Rules

编码阶段推荐的工具使用顺序如下：

1. 同步 `plugin-dev-kit`
2. 阅读 `README.md`
3. 阅读索引文档完成路由
4. 调用 `plugin_mcp_implementation_project_init`
5. `git clone` 或 `git pull` 目标项目
6. 校验并修正项目结构
7. 编写代码与配置
8. 调用 `compileCheck(projectId)`
9. 完成代码自审
10. 如条件允许，再执行附加扫描能力

约束如下：

- 不得用 `plugin_mcp_initialize` 代替开发项目初始化
- 不得再次创建 `plugin-{Snowflake ID}` 目录
- 不得绕过 `plugin-dev-kit` 文档和 SDK 自行猜测实现
- 不得读取、引用或默认复用当前工作目录之外的 `plugin-dev-kit` 目录
- 不得伪造编译通过、扫描通过或系统返回结果

## Output Contract

本技能的标准输出物包括：

- `projectId`
- 已拉取并修正的 `project-{projectId}` 工程目录
- 已同步到 `project-{projectId}/docs/` 的 `SRS.md`、`PRD.md`、`feasibility-analysis.md`
- 已完善的 `README.md`
- 插件代码
- `manifest.yml`
- `endpoints/*.yml`
- 必要的资源与文档
- 编译校验结果
- 代码自审结论
- 如有额外执行，则附上静态分析 / 安全扫描结果

### Handoff Condition

只有当以下条件全部成立时，才能移交给 `release-and-test`：

- 开发完成
- 研发文档已同步到 `project-{projectId}/docs/`
- `README.md` 已补全并与当前实现一致
- 项目目录结构已校正
- 编译校验通过
- 代码自审完成
- 代码、配置、文档产物可直接进入发布测试阶段

## Prohibited Actions

- 禁止在无依据时自由创造实现
- 禁止跳过项目初始化
- 禁止跳过结构校验
- 禁止绕过可行性分析直接开发
- 禁止在未确认前提下调用未验证 API
- 禁止忽略配置注册与版本升级
- 禁止在 `project-{projectId}` 外创建正式插件文件

## Failure Handling

### When Project Init Fails

- 记录失败原因
- 明确是 GitLab 建项失败、访问令牌失败还是落库失败
- 修复后重新执行初始化，不得直接手工跳过

### When Documentation Is Insufficient

- 停止继续实现
- 明确指出缺口
- 向用户提出 1 个精准问题

### When Build Fails

- 记录错误原因
- 回退到代码修改
- 不继续后续阶段

### When Scope Must Change

- 明确指出受影响功能
- 回退到 `prd-writer` 或 `feasibility-analysis`
- 不擅自缩减需求范围

## Handoff To Next Stage

本技能完成后，下一阶段默认进入 `release-and-test`。

移交时至少应保证下游已获得：

- `projectId` 与对应 Git 项目上下文
- 已归档在 `project-{projectId}/docs/` 的研发文档与 `project-{projectId}/README.md`
- 插件包构建前所需完整代码与配置
- 编译校验通过结论
- 可用于发布测试的工程状态

## First-Version Scope

当前版本 `plugin-implementation` 聚焦以下范围：

- 固化编码阶段的项目前置条件与初始化规则
- 固化 `project-{projectId}` 目录开发模式
- 固化结构校验、代码实现与编译门禁规则
- 固化文档依据与拒绝生成条件
- 与主技能、可行性分析、工单、发布测试阶段形成标准衔接

后续版本再补充：

- `compileCheck(projectId)` 与 `deployPlugin(projectId)` 的 MCP 协同约定
- 更细的前后端模板与脚手架规则
- 与发布测试阶段更细的项目状态联动
