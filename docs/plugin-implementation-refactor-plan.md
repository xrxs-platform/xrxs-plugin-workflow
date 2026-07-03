# plugin-implementation 重构分析与技术方案

## 1. 目的

本文档用于梳理 `plugin-implementation` 技能在 `编码阶段` 的重构方案，先明确：

- 现有 skill 与目标流程的差异
- 当前 MCP / 后端 / GitLab 能力现状
- 推荐的技术改造路径
- 后续修改 `references/plugin-implementation/SKILL.md` 时应替换的章节

当前阶段只做分析和方案设计，**暂不直接修改** `plugin-implementation/SKILL.md`。

## 2. 输入背景

根据最新约束，`plugin-implementation` 编码阶段需要做如下调整：

1. 阶段开始后首先调用 MCP 工具执行项目初始化 `initProject(String projectName)`
2. `projectName` 从 `PRD.md` 提取，需校验唯一性；如重名则追加 6 位随机数
3. 初始化时需要同时完成两件事：
   - 在 GitLab 中创建项目 `project-{projectId}`
   - 在表 `xrxs_dev_project` 中新增一条开发项目记录
4. GitLab 项目必须通过“拷贝项目模板”的方式创建，而不是创建空仓库
5. 初始化时还需生成该项目的读写访问令牌
6. 开发目录不再使用 `plugin-{Snowflake ID}`，而是直接使用 `git clone` 下来的 `project-{projectId}` 目录
7. 拉取项目后先检查并修正项目结构，再基于 `plugin-dev-kit` 的 `docs` 和 `src/plugin-opensdk` 编码
8. 编码完成后调用 `/openhands/service/compileCheck?projectId=xxx` 进行编译校验，后续再封装为 MCP 能力

## 3. 现状梳理

### 3.1 当前 `plugin-implementation` skill 的主要假设

当前 [SKILL.md](file:///c:/johome/newspace/openhands/xrxs-plugin-workflow/references/plugin-implementation/SKILL.md) 仍基于旧流程，核心假设包括：

- 开发前必须创建独立目录 `plugin-{Snowflake ID}/`
- 目录名通过 `plugin_mcp_util_snowflake_id` 生成
- `Step 3.5` 强依赖 feasibility 阶段 MCP 返回的 `interfaceSignature`
- 质量门禁默认走 `plugin_mcp_build_source_upload`、`plugin_mcp_build_compile`、`plugin_mcp_build_static_analysis`、`plugin_mcp_build_security_scan`

这些假设与新的编码阶段目标流程存在明显冲突。

### 3.2 当前 `plugin-dev-kit` 的真实定位

根据 [plugin-dev-kit/README.md](file:///c:/johome/newspace/openhands/plugin-dev-kit/README.md)：

- `plugin-dev-kit/` 是只读参考库
- 实际开发项目应位于 `project-XXXXXXXX/`
- AI / 开发者应遵循 `README.md -> 索引文档 -> 详情文档` 的查阅顺序
- SDK 源码真实路径为 `plugin-dev-kit/src/plugin-opensdk/`

这说明新技能的工程根目录应该围绕 `project-{projectId}/` 组织，而不是自建 `plugin-*` 目录。

### 3.3 当前 MCP 能力现状

已存在的 MCP 工具中：

- `plugin_mcp_initialize`：只是调用 `/initialize` 获取服务能力清单，不是项目初始化
- `plugin_mcp_release_projects_create`：用于发布阶段创建发布项目，不适合直接承担编码阶段 GitLab 项目初始化
- `plugin_mcp_build_source_upload` / `plugin_mcp_build_compile`：更偏向上传源码包后做构建门禁
- `plugin_mcp_util_snowflake_id`：只生成 9 位短 ID，用于旧目录命名逻辑

结论：

- **当前 MCP 中没有用户目标里的 `initProject(String projectName)` 等价能力**
- **当前 `plugin_mcp_initialize` 不能直接复用为“项目初始化”**

### 3.4 当前后端能力现状

在业务后端中已发现两类与新流程高度相关的能力：

#### A. 编译校验接口已存在

在 [OpenhandsController.java](file:///c:/johome/newspace/openhands/lowcode-springboot/jeecg-boot-module-biz/src/main/java/org/jeecg/modules/xrxs/plugin/controller/OpenhandsController.java) 中已提供：

- `GET /openhands/service/compileCheck?projectId=xxx`
- `GET /openhands/service/getCompileLog?traceId=xxx`
- `GET /openhands/service/getDevProjectById?projectId=xxx`

在 [OpenHandsService.java](file:///c:/johome/newspace/openhands/lowcode-springboot/jeecg-boot-module-biz/src/main/java/org/jeecg/modules/xrxs/plugin/service/OpenHandsService.java) 中，`compileCheck(projectId)` 会：

- 根据 `projectId` 查询 `xrxs_dev_project`
- 取出 `gitlabProjectId`
- 调用 `operationOtherPartyService.checkCode(gitlabProjectId)` 执行校验

这与新要求中的“按 `projectId` 触发编译检查”完全一致。

#### B. GitLab 项目创建能力部分存在

在 [OperationOtherPartyServiceImpl.java](file:///c:/johome/newspace/openhands/lowcode-springboot/jeecg-boot-module-biz/src/main/java/org/jeecg/modules/xrxs/plugin/service/impl/OperationOtherPartyServiceImpl.java) 中已存在：

- `createProjectByCopyingTemplate(String name)`：通过 GitLab `PRIVATE-TOKEN` 创建项目并从模板复制初始内容
- `createDevProjectForOpenhands(String pluginName, XrxsDevProject devProject)`：创建 GitLab 项目并落库 `xrxs_dev_project`

已知实现特征：

- GitLab 地址默认为 `https://xaicode.xinrenxinshi.com`
- GitLab Token 配置默认值为 `2Erh8qQx9QuM34HZr6sw`
- 已能保存 `projectId`、`projectName`、`gitlabProjectId`、`gitlabProjectPathWithNs`

但当前仍缺少两项用户目标能力：

- **项目名唯一性处理策略未固化为 MCP / skill 规则**
- **项目专用访问令牌生成与返回能力未打通**

#### C. `XrxsDevProjectController#add` 已定义现有“新增开发项目”的基线行为

在 [XrxsDevProjectController.java](file:///c:/johome/newspace/openhands/lowcode-springboot/jeecg-boot-module-biz/src/main/java/org/jeecg/modules/xrxs/plugin/controller/XrxsDevProjectController.java) 的 `add()` 中，当前新增开发项目遵循如下逻辑：

- 先将 `auditPhase` 设置为研发阶段
- 校验 `pluginName` 不能为空
- 按 `plugin_name` 查重，发现重复则拒绝
- 当 `pluginType = "main"` 时，调用 `operationOtherPartyService.createDevProjectForOpenhands(pluginName, devProject)`
- 随后调用 `xrxsDevProjectService.save(xrxsDevProject)` 将记录写入 `xrxs_dev_project`

其中 `createDevProjectForOpenhands()` 的核心行为是：

- 调用 `createProjectByCopyingTemplate(String name)`
- 通过 GitLab `PRIVATE-TOKEN` 创建项目
- **使用模板项目拷贝初始化仓库内容**
- 回填 `gitlabProjectId`、`gitlabProjectPathWithNs`、`ohConversationId`

这意味着新的 `initProject(projectName)` 不能只做 GitLab 建项，必须把“按 `add()` 现有实现方式新增 `xrxs_dev_project` 记录”纳入标准职责。

## 4. 差距分析

### 4.1 工程目录模型冲突

旧 skill：

- 以 `plugin-{Snowflake ID}/` 作为开发根目录

新目标：

- 以 `git clone` 下来的 `project-{projectId}/` 作为开发根目录

需要调整：

- 删除“必须创建独立插件目录”的规则
- 将“目录隔离”解释从“自建 plugin 目录”改成“直接在 Git 项目目录中开发，避免工作区根目录打包”

### 4.2 项目初始化能力缺口

用户目标要求：

- 编码阶段一开始调用 `initProject(projectName)`
- 生成 `projectId`
- 创建 GitLab 项目 `project-{projectId}`
- 按 `XrxsDevProjectController#add` 的实现方式写入 `xrxs_dev_project`
- GitLab 项目必须通过拷贝项目模板的方式创建
- 生成项目访问令牌
- 返回完整 Git URL 和 token

当前现状：

- `plugin_mcp_initialize` 不是这个能力
- `release_projects_create` 是发布域能力，不宜直接承担编码项目初始化
- 后端虽已有 GitLab 建项与 `xrxs_dev_project` 落库逻辑，但尚未抽象为适配编码阶段的 MCP 工具

结论：

- **应新增专用 MCP 能力，不建议复用 `plugin_mcp_initialize` 名称和语义**

### 4.3 代码依据来源冲突

旧 skill：

- `Step 3.5` 明确要求从 feasibility MCP 返回的 `interfaceSignature` 锁定后端 Handler 接口

新目标：

- 编码主要参考 `plugin-dev-kit/docs` 和 `plugin-dev-kit/src/plugin-opensdk`

结论：

- `Step 3.5` 应整体重写
- 代码依据应切换为：
  - `plugin-dev-kit/README.md`
  - `plugin-dev-kit/docs/...`
  - `plugin-dev-kit/src/plugin-opensdk/...`

### 4.4 编译门禁口径冲突

旧 skill：

- 以 `plugin_mcp_build_source_upload -> plugin_mcp_build_compile` 为主路径

新目标：

- 编写后直接调用 `/openhands/service/compileCheck?projectId=xxx`

结论：

- 编码阶段主编译门禁应改为 `compileCheck(projectId)`
- 旧的 `source_upload/build_compile` 可保留为兼容路径或后续阶段能力，不应再作为编码阶段唯一主路径

## 5. 推荐目标流程

### Step 0: 读取输入并确认前置条件

输入仍以以下内容为主：

- `PRD.md`
- 可行性分析结论
- 已解决工单
- `plugin-dev-kit/README.md`
- `plugin-dev-kit/docs`
- `plugin-dev-kit/src/plugin-opensdk`

前置条件仍需成立：

- PRD 完整
- 插件开发类型明确
- 所需 pointcut / biz-api 已确认可用
- 无阻塞性工单

### Step 1: 初始化项目 `initProject(projectName)`

#### 目标职责

编码阶段开始后，首先初始化一个真实开发项目。

#### 推荐输入

```json
{
  "projectName": "根据 PRD 提取的项目名称"
}
```

#### 推荐处理逻辑

1. 从 `PRD.md` 提取项目名称
2. 校验项目名称唯一性
3. 若重名，则在末尾追加 6 位随机数
4. 生成 `projectId`
5. 创建 `XrxsDevProject` 实体，并按 `XrxsDevProjectController#add` 的现有方式填充基础字段
6. 以“拷贝项目模板”的方式在 GitLab `xrxs` 组下创建项目 `project-{projectId}`
7. 将 GitLab 返回的 `gitlabProjectId`、`gitlabProjectPathWithNs` 等字段回填到 `XrxsDevProject`
8. 生成该项目的读写权限访问令牌
9. 将项目记录写入 `xrxs_dev_project`

#### 推荐返回值

```json
{
  "project_id": "1782712240806",
  "gitlab_project_name": "project-1782712240806",
  "gitlab_project_url": "https://xaicode.xinrenxinshi.com/xrxs/project-1782712240806.git",
  "gitlab_project_token": "2qJZCfMZWWKQccYJydsJ"
}
```

#### 设计建议

- 不要复用现有 `plugin_mcp_initialize`
- 本次实现已落地为 `plugin_mcp_implementation_project_init`
- 对应后端路由为 `/api/v1/mcp/implementation/project/init`
- 项目初始化实现应复用 `XrxsDevProjectController#add` 背后的建项与落库模式，而不是另起一套完全独立的数据结构
- GitLab 建项必须复用 `createProjectByCopyingTemplate(String name)` 一类“拷贝模板”逻辑，禁止创建空仓库后再手工补文件

原因：

- `initialize` 现在已明确表示“服务初始化/能力发现”
- 若强行改语义，会破坏 MCP 工具名与现有使用方的兼容性

### Step 2: 拉取或同步项目目录

项目初始化完成后，进入真实开发目录：

```bash
git clone "https://oauth2:${gitlab_project_token}@xaicode.xinrenxinshi.com/xrxs/project-${project_id}.git"
```

若目录已存在，则在目录内执行：

```bash
git pull
```

此后 **`project-{projectId}/` 就是唯一开发根目录**。

### Step 3: 校验并修正项目结构

不再新建 `plugin-{Snowflake ID}`，而是直接检查 `project-{projectId}/` 是否符合 XRXS 插件工程规范。

建议检查项：

- `manifest.yml` 是否存在
- `endpoints/` 是否存在
- `src/` 是否存在
- `src/backend/` 是否按需存在
- `src/fe/` 是否按需存在
- `README.md` 是否存在
- `PRD.md` 是否已同步到项目目录

修正规则：

- 缺目录则补齐
- 缺模板文件则生成最小可运行模板
- 存在与插件类型冲突的目录结构时，按实际插件类型修正

### Step 4: 基于 `plugin-dev-kit` 编码

编码依据应固定为：

- 入口：`plugin-dev-kit/README.md`
- pointcut：`plugin-dev-kit/docs/pointcut/...`
- biz-api：`plugin-dev-kit/docs/biz-api/...`
- SDK 真身：`plugin-dev-kit/src/plugin-opensdk/...`

注意：

- 用户描述里写的是 `plugin-dev-kit/src/opensdk`
- 但当前仓库真实路径是 `plugin-dev-kit/src/plugin-opensdk`
- skill 重构时应以**真实路径**为准，必要时在文档中加一句“若外部口语描述为 opensdk，实际对应 `src/plugin-opensdk`”

### Step 5: 调用编译工具校验代码

编码完成后，先调用：

- `GET /openhands/service/compileCheck?projectId=xxx`

必要时继续调用：

- `GET /openhands/service/getCompileLog?traceId=xxx`

这一阶段的主语义是：

- 编译检查按 `projectId` 执行
- 编译对象是 GitLab 项目当前代码状态
- skill 输出中必须记录编译结果和失败原因

### Step 6: 兼容后续门禁能力

若后续仍需静态分析、安全扫描、打包，可继续保留以下能力作为后续阶段或附加门禁：

- `plugin_mcp_build_static_analysis`
- `plugin_mcp_build_security_scan`
- `plugin_mcp_build_package`

但编码阶段的“主编译检查”应优先以 `compileCheck(projectId)` 为准。

## 6. 对 skill 文档的具体改造建议

### 6.1 必须删除或重写的内容

以下内容应从 `plugin-implementation/SKILL.md` 中删除或整体重写：

- `Project Structure Rules` 中关于 `plugin-{Snowflake ID}` 的目录规则
- `Mandatory Structure Rules` 中“必须先创建独立插件目录”的描述
- `Development Procedure / Step 3` 中“先创建独立插件目录”的流程
- `Step 3.5 Cross-Reference MCP Pointcut Documentation`
- “代码依据只能来自 MCP feasibility 返回的 interfaceSignature”相关描述
- “编码阶段主编译门禁必须走 source_upload/build_compile”相关描述

### 6.2 必须新增的章节

建议新增以下章节：

#### A. Project Initialization Rules

至少包括：

- 从 `PRD.md` 提取 `projectName`
- 项目名唯一性校验规则
- 重名时追加 6 位随机数
- 初始化输出字段定义
- `project-{projectId}` 为唯一开发目录

#### B. Git Workspace Rules

至少包括：

- `git clone` / `git pull` 规则
- `project-{projectId}/` 为唯一开发根目录
- 禁止再额外套一层 `plugin-*` 目录

#### C. Project Structure Validation Rules

至少包括：

- 拉取后先检查目录规范
- 对缺失目录和模板文件进行自动修正
- 结构修正优先于正式编码

#### D. Compile Check Rules

至少包括：

- 编码完成后调用 `compileCheck(projectId)`
- 若失败，必须读取日志并修复
- 成功后才能进入后续发布测试阶段
- 后续可再封装为 MCP

### 6.3 需要保留但要改口径的内容

以下内容可以保留，但要切换依据来源：

- 文档依据规则：从 MCP 文档改为 `plugin-dev-kit/docs + src/plugin-opensdk`
- 前后端插件实现规则：保留
- `manifest.yml` / `endpoints/*.yml` / `src/backend` / `src/fe` 规范：保留
- 自审和质量门禁：保留，但重排顺序

## 7. MCP / 后端改造建议

### 7.1 新增 MCP 工具

已实现新增：

- `plugin_mcp_implementation_project_init`

推荐参数：

```json
{
  "projectName": "项目名称"
}
```

推荐返回：

```json
{
  "project_id": "1782712240806",
  "gitlab_project_name": "project-1782712240806",
  "gitlab_project_url": "https://xaicode.xinrenxinshi.com/xrxs/project-1782712240806.git",
  "gitlab_project_token": "2qJZCfMZWWKQccYJydsJ"
}
```

### 7.2 后端实现建议

后端建议复用已有能力并补齐缺口：

- 复用 `createProjectByCopyingTemplate(String name)` 创建 GitLab 项目
- 复用 `createDevProjectForOpenhands(String pluginName, XrxsDevProject devProject)` 的回填逻辑
- 复用 `xrxs_dev_project` 持久化项目记录
- 对齐 `XrxsDevProjectController#add` 的新增流程与字段设置方式
- 新增“按项目名检查是否重名”的逻辑
- 新增“生成项目访问令牌”的逻辑
- 返回 Git URL、project token、projectId 等前端/AI 可直接使用的字段

### 7.3 编译能力封装建议

当前可先直接使用：

- `/openhands/service/compileCheck?projectId=xxx`

后续可封装为 MCP，例如：

- `plugin_mcp_project_compile_check`

建议参数：

```json
{
  "projectId": "1782712240806"
}
```

这样可以避免当前 `plugin_mcp_build_compile` 的“源码包上传式编译”语义与真实需求混淆。

## 8. 推荐重构顺序

建议按以下顺序实施，而不是一次性同时改 skill 和 MCP：

1. 先确认本文档方案
2. 先改 `plugin-implementation/SKILL.md`
3. 再在 MCP / 后端补齐 `project_init` 能力
4. 再调整编码阶段的实际工具调用
5. 最后联动 `release-and-test`，避免项目创建语义重复

## 9. 风险与注意事项

### 9.1 与 `release-and-test` 的项目语义冲突

当前 `release-and-test` 中仍有 `plugin_mcp_release_projects_create` 的项目创建语义。

若编码阶段新增“真实开发项目初始化”，则必须明确：

- 编码阶段创建的是 **开发 GitLab 项目**
- 发布测试阶段创建的是 **发布/测试记录项目**
- 二者不是同一概念，避免字段名和 tool 名混淆

### 9.2 项目访问令牌尚未落地

当前代码中已看到 GitLab 主 Token 配置和建项能力，但尚未看到“项目专属访问令牌”持久化逻辑。

因此该能力属于本次 MCP / 后端改造的重点。

### 9.3 `projectName` 与 `gitlab_project_name` 不应混用

建议明确区分：

- `projectName`：来自 PRD 的业务项目名，可能需要去重
- `gitlab_project_name`：固定为 `project-{projectId}`

否则后续在发布、日志、数据库字段中容易混淆。

## 10. 结论

本次 `plugin-implementation` 重构的本质不是“换一个目录名”，而是把编码阶段从“本地临时插件目录模式”切换为“真实 GitLab 项目驱动模式”。

建议的主线如下：

1. 用 `PRD.md` 提取项目名
2. 调用专用 `initProject(projectName)` MCP 能力初始化项目
3. `git clone` 得到 `project-{projectId}/`
4. 在该目录内检查并修正规范结构
5. 基于 `plugin-dev-kit/README.md + docs + src/plugin-opensdk` 实现代码
6. 调用 `compileCheck(projectId)` 做主编译门禁
7. 通过后再进入发布测试阶段

按这个方案重构后，`plugin-implementation` 会与 `plugin-dev-kit`、GitLab 项目模型、OpenHands 编译检查能力保持一致。
