# XRXS Plugin Workflow

`xrxs-plugin-workflow` 是 XRXS 插件研发的总入口技能，用于把插件需求从“想法”推进到“可发布、可测试、可申请上线”的完整研发流程。

它不是单纯的代码生成技能，而是一个带有**阶段路由、状态约束、质量门禁、文档归档与 MCP 能力协同**的总工作流。

## 支持平台

本技能遵循 [Open Agent Skills Standard](https://agentskills.io/)，兼容以下 AI IDE：

**Trae** · **Cursor** · **Claude Code** · **Codex (OpenAI)** · **Cline** · **OpenCode** · **CodeBuddy** · **OpenClaw** · **Windsurf** 及其他 40+ 平台

## 安装

### 方式 1：使用 npx skills CLI（推荐）

```bash
npx skills add xrxs-platform/xrxs-plugin-workflow -y
```

可指定安装到特定 IDE：

```bash
npx skills add xrxs-platform/xrxs-plugin-workflow -y -a trae -a cursor -a claude-code
```

### 方式 2：npm 安装

```bash
npm install @xrxs-plugin/xrxs-plugin-workflow
```

### 方式 3：手动导入

1. 将 `skills/xrxs-plugin-workflow/` 整个目录导入 IDE 技能目录
2. Trae：`Settings -> Rule & Skills -> Skills -> Create`
3. 其他 IDE 请参考各自文档

## 仓库结构说明

本仓库采用子目录 skill 结构，而不是把主 `SKILL.md` 放在仓库根目录：

- skill 入口：`skills/xrxs-plugin-workflow/SKILL.md`
- 子技能引用：`skills/xrxs-plugin-workflow/references/...`

这样做的原因是 `npx skills add owner/repo` 在 GitHub 安装链路下，对“仓库根目录 skill”通常只会安装根 `SKILL.md`，不会稳定带上根目录下的 `references/`。改为子目录 skill 后，安装器会把整个 skill 目录一起落盘。

## 这个技能解决什么问题

适用场景包括：

- 需求还比较模糊，需要先整理成结构化输入
- 已有需求，想生成或补全 `PRD.md`
- 想判断某个 XRXS 插件需求是否真的可实现
- 已有 PRD 和可行性结论，准备开始开发插件
- 需要执行质量门禁、测试发布、人工测试和上线申请准备
- 需要统一管理 XRXS 插件研发过程中的状态、阻塞项和恢复路径

## 核心能力

- 主流程路由：按当前阶段自动进入需求、PRD、可行性、开发、发布测试等子技能
- 状态管理：使用显式 workflow 状态，而不是只靠自然语言上下文猜测
- 质量门禁：文档门禁、可行性门禁、工程门禁、发布门禁
- 文档归档：要求研发文档与代码一同归档到真实 GitLab 项目
- MCP 协同：从可行性分析阶段开始，优先结合 `plugin-mcp` 能力执行查询、构建、发布和状态推进

## 标准研发流程

XRXS 插件研发主流程如下：

1. 需求澄清：`requirements-translator`
2. PRD 编写：`prd-writer`
3. 可行性分析：`feasibility-analysis`
4. 缺失项工单协同：`support-ticket`
5. 插件实现：`plugin-implementation`
6. 质量门禁：静态分析 / 编译 / 安全扫描 / 代码自审
7. 测试发布与人工测试：`release-and-test`
8. 上线申请

其中有几个硬性约束：

- 不得跳过可行性分析直接开发
- 工单未解除阻塞时，不得恢复主线开发
- 编译、静态分析、安全扫描未通过时，不得进入发布
- 人工测试未通过时，不得进入上线申请

## 子技能结构

当前版本已稳定提供以下 6 个子技能：

| 子技能标识 | 文件路径 | 主要职责 |
| --- | --- | --- |
| `requirements-translator` | `references/requirements-translator/SKILL.md` | 将模糊需求整理为结构化需求输入或 `SRS.md` |
| `prd-writer` | `references/prd-writer/SKILL.md` | 生成或补全 `PRD.md` |
| `feasibility-analysis` | `references/feasibility-analysis/SKILL.md` | 校验织入点、业务 API 与实现可行性 |
| `support-ticket` | `references/support-ticket/SKILL.md` | 创建、跟踪和关闭缺失项技术支持工单 |
| `plugin-implementation` | `references/plugin-implementation/SKILL.md` | 初始化真实项目、实现代码与配置、同步研发文档 |
| `release-and-test` | `references/release-and-test/SKILL.md` | 执行发布门禁、测试环境发布、人工测试与上线申请准备 |

## 状态模型

主技能显式维护 XRXS 插件研发状态。常用状态包括：

- `requirements_pending`
- `requirements_ready`
- `prd_pending`
- `prd_ready`
- `feasibility_pending`
- `feasibility_blocked`
- `feasibility_ready`
- `support_ticket_open`
- `support_ticket_resolved`
- `implementation_in_progress`
- `quality_failed`
- `implementation_ready`
- `release_in_progress`
- `release_blocked`
- `testing_in_progress`
- `testing_failed`
- `launch_ready`
- `workflow_completed`

这意味着：

- 技能需要知道“当前在哪个阶段”
- 技能需要知道“为什么卡在这里”
- 技能需要知道“下一步可以去哪里”

而不是简单根据一句“帮我发版”就直接进入发布阶段。

## `workflowId` 与 `projectId`

这两个标识在最新版本中有明确分工：

- `workflowId`：代表整条插件研发流程实例
- `projectId`：代表具体开发项目 / GitLab 项目

规则如下：

- 进入主流程时，应尽早生成并复用同一个 `workflowId`
- `workflowId` 贯穿可行性分析、工单、开发、发布与测试阶段
- `projectId` 在 implementation 项目初始化时生成
- `workflowId` 不替代 `projectId`，两者职责不同

## 文档归档规则

从最新版本开始，研发文档必须跟随真实 Git 项目一起归档，不能只停留在当前工作区。

在 `plugin-implementation` 阶段初始化 `project-{projectId}` 后，至少需要把以下文档同步到 Git 项目中：

- `docs/SRS.md`
- `docs/PRD.md`
- `docs/feasibility-analysis.md`
- `README.md`

其中 `README.md` 至少要覆盖两部分内容：

1. 用户原始需求背景
2. 基于 `SRS.md`、`PRD.md`、`feasibility-analysis.md` 整理出的插件概括性功能方案

如果上游文档在开发过程中有更新，也必须同步回写到 `project-{projectId}/docs/`，并与代码一起提交到 GitLab。

## 质量门禁

本技能把以下 4 类门禁视为硬约束：

| 门禁 | 作用 |
| --- | --- |
| 文档门禁 | 没有结构化需求、没有完整 `PRD.md`，不得进入可行性或开发 |
| 可行性门禁 | 织入点和业务 API 未确认时，禁止进入开发 |
| 工程门禁 | 静态分析、编译、安全扫描、代码自审未通过时，禁止进入发布 |
| 发布门禁 | 未拿到 `projectId`、未提交 Git、未发布成功或人工测试未通过时，禁止进入上线申请 |

这几个门禁的执行顺序是：

1. 文档门禁
2. 可行性门禁
3. 工程门禁
4. 发布门禁

## MCP 依赖与分层规则

本技能需要配合 `plugin-mcp` 使用，但不是所有阶段都强依赖 MCP。

### 分层原则

- 需求阶段、PRD 阶段：由当前 LLM 直接产出 `SRS.md` 和 `PRD.md`
- 从可行性分析阶段开始：优先进入 MCP 驱动模式

### 典型能力分组

| MCP 能力分组 | 作用 |
| --- | --- |
| `feasibility` | 查询织入点、业务 API、缺失项与可行性 |
| `support-ticket` | 创建、查询、更新、关闭技术支持工单 |
| `build` | 静态分析、编译、安全扫描 |
| `release` | 基于 `projectId` 发布测试环境、查询发布状态 |
| `workflow` | 流程状态读写、阻塞态 / 恢复态同步 |

### MCP 不可用时

- 仍可继续做需求、PRD、方案和文档类工作
- 涉及真实系统能力的步骤，必须退化为人工执行说明
- 不得伪造织入点可用性、工单状态、构建结果或发布结果

## 开发实现阶段的最新规则

最新版本对 `plugin-implementation` 阶段新增了更强约束：

- 必须先调用 `plugin_mcp_implementation_project_init`
- 必须在真实 GitLab 项目 `project-{projectId}` 中编码
- 禁止在工作区根目录直接创建正式插件目录
- 必须优先使用当前工作目录下的 `./plugin-dev-kit`
- 进入开发前必须先同步 `plugin-dev-kit`
- 研发文档必须在编码前同步到 `project-{projectId}/docs/`

也就是说，开发不再是“直接在本地随便起个目录写代码”，而是要先完成**项目初始化、参考库同步、文档归档、结构校验**，再进入正式编码。

## 发布测试阶段的最新规则

最新版本对 `release-and-test` 阶段也做了明确升级：

- 发布前必须先执行 `plugin_mcp_build_static_analysis`
- 发布前必须先执行 `plugin_mcp_build_security_scan`
- 发布链路基于 `projectId`，不再要求 zip 打包上传
- 服务端会基于 GitLab 最新提交执行 `compileCheck(projectId)`、`git pull` 和插件同步
- 人工测试是必经环节
- 人工测试失败必须回流 `plugin-implementation`

推荐顺序如下：

1. 静态分析
2. 安全扫描
3. 编译检查
4. 发布测试环境
5. 查询状态 / 日志
6. 人工测试
7. 决定上线申请或回流开发

## 目录结构

```text
xrxs-plugin-workflow/
├── README.md
├── package.json
└── skills/
    └── xrxs-plugin-workflow/
        ├── SKILL.md
        └── references/
            ├── requirements-translator/SKILL.md
            ├── prd-writer/SKILL.md
            ├── feasibility-analysis/SKILL.md
            ├── support-ticket/SKILL.md
            ├── plugin-implementation/SKILL.md
            └── release-and-test/SKILL.md
```

## 适合什么时候使用这个技能

优先使用 `xrxs-plugin-workflow` 的情况：

- 你想从一个插件想法推进到完整研发闭环
- 你不确定当前应该先做 PRD、可行性、开发还是发布
- 你需要一个受控流程，而不是只要一段代码
- 你要确保 XRXS 插件研发文档、代码、发布动作都能串起来

如果你已经非常明确只做某个单一阶段，也可以直接进入对应子技能。

## License

MIT
