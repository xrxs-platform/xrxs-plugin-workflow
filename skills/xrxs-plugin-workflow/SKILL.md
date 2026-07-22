---
name: "xrxs-plugin-workflow"
description: "Guides XRXS plugin workflow across requirement clarification, PRD, feasibility, implementation, packaging, testing, release, and support. Use for end-to-end XRXS plugin work."
description_zh: "XRXS 插件研发总入口技能，覆盖需求澄清、PRD、可行性分析、开发、质量门禁、基于 GitLab 的测试发布与上线申请。"
version: "0.2.0"
---

# XRXS Plugin Workflow

`xrxs-plugin-workflow` 是 XRXS 插件研发的总入口技能。它负责识别当前任务所处阶段，按照标准研发流程推进工作，并在合适的时候路由到稳定的子技能标识。第一版先以主入口为核心，后续再逐步拆分 `references/` 下的子技能文件。

## Reference Files Location

本技能的主文件与后续子技能建议采用如下目录结构：

```text
xrxs-plugin-workflow/
├── SKILL.md                                   # 主入口技能
└── references/                                # 子技能与辅助文档
    ├── requirements-translator/SKILL.md       # 需求澄清与需求描述文档
    ├── prd-writer/SKILL.md                    # PRD 生成
    ├── feasibility-analysis/SKILL.md          # 织入点 / 业务 API 可行性分析
    ├── plugin-implementation/SKILL.md         # 插件开发实现
    ├── release-and-test/SKILL.md              # 基于 projectId 的测试发布、人工测试、上线申请
    └── support-ticket/SKILL.md                # 技术支持工单协同
```

当主技能提到 `references/<skill-id>/SKILL.md` 时，表示后续应从该稳定子技能标识继续读取。如果某个子技能尚未落盘，当前版本必须回退到本主技能中对应章节继续执行，不得因此中断流程。

### Resolved Child Skill References

当前版本已落盘并可显式引用以下 6 个子技能文件：

- `requirements-translator`: `references/requirements-translator/SKILL.md`
- `prd-writer`: `references/prd-writer/SKILL.md`
- `feasibility-analysis`: `references/feasibility-analysis/SKILL.md`
- `support-ticket`: `references/support-ticket/SKILL.md`
- `plugin-implementation`: `references/plugin-implementation/SKILL.md`
- `release-and-test`: `references/release-and-test/SKILL.md`

主技能在进行阶段路由时，应优先显式引用上述文件路径，而不是只引用子技能标识。

## Activation Contract

在以下场景中，应优先启用 `xrxs-plugin-workflow`：

- 用户提到 `XRXS 插件`、`插件开发`、`插件流程`、`织入点`、`业务 API`、`测试发布`、`上线申请`
- 用户希望从模糊想法开始，逐步推进到可交付插件
- 用户需要判断某个插件需求是否可行
- 用户已经有需求或 PRD，希望继续开发插件代码
- 用户要整理 XRXS 插件研发规范、流程图、工单流程或质量门禁

### Standalone Skill Fallback

如果当前环境只暴露了一个发布后的总技能，则总是从 `xrxs-plugin-workflow` 主入口开始，再根据下面的路由规则跳转到稳定子技能标识和其对应文件：

- `requirements-translator` -> `references/requirements-translator/SKILL.md`
- `prd-writer` -> `references/prd-writer/SKILL.md`
- `feasibility-analysis` -> `references/feasibility-analysis/SKILL.md`
- `plugin-implementation` -> `references/plugin-implementation/SKILL.md`
- `release-and-test` -> `references/release-and-test/SKILL.md`
- `support-ticket` -> `references/support-ticket/SKILL.md`

如果目标子技能文件暂未提供，则继续执行本文件中对应的章节说明作为降级方案。

## Workflow Contract

XRXS 插件研发必须遵循以下主流程，不得跳过关键门禁：

1. 沟通需求、分析需求、生成需求输入
2. 生成或完善 `PRD.md`
3. 对 PRD 进行可行性分析
4. 从织入点列表中选择并确认可行的织入点
5. 从业务 API 列表中选择并确认可行的业务 API
6. 如果存在缺失项，生成缺失单并发起技术支持工单
7. 仅在可行性通过，或技术支持工单已解决后，才进入插件开发
8. 开发插件代码
9. 静态分析
10. 编译
11. 安全扫描
12. 开发者将代码提交并推送到 GitLab
13. 使用 implementation 阶段已有的 `projectId` 发布到测试环境
14. 开发者在测试环境人工测试
15. 测试通过后发起上线申请

### Workflow ID Rule

- `workflowId` 代表一整条插件研发流程实例，不等同于 `projectId`
- 生成方式：由主技能在**首次进入可行性分析阶段**时本地生成，不需要额外的“创建工作流”MCP 调用
  - 推荐格式：`wf_` + 语义化前缀 + 短随机串，例如 `wf_att_leave_guard_00D2D87XE`
  - 短随机串建议直接调用 `plugin_mcp_util_snowflake_id` 获取 9 字符 ID 作为后缀
- 服务端首次持久化时机：在第一次调用 `plugin_mcp_workflow_state_update` 时，服务端会自动按 `workflowId` 创建 workflow 记录并写入首个状态
- 同一个插件需求从可行性分析、工单协同、开发实现、发布测试到上线申请，均必须复用同一个 `workflowId`
- Requirements Stage 与 PRD Stage 由当前 LLM 直接产出文档，可以暂不生成 `workflowId`；一旦进入 Feasibility Analysis Stage，必须生成并从此持续复用，不得中途更换
- `projectId` 在 implementation 项目初始化时由 `plugin_mcp_implementation_project_init` 返回；`workflowId` 与 `projectId` 职责不同，两者都应写入后续所有 MCP 调用参数

### GitLab Archive Rule

- 当 implementation 阶段完成 `project-{projectId}` 初始化后，研发文档必须随真实 Git 项目一起归档到 GitLab
- 至少包括：`docs/SRS.md`、`docs/PRD.md`、`docs/feasibility-analysis.md`、`README.md`
- `README.md` 必须包含用户原始需求背景，以及基于研发文档整理出的插件概括性功能方案
- 若上游文档在开发过程中发生更新，必须同步更新 `project-{projectId}/docs/` 中的同名文件，并与代码一起提交
- 发布前必须确认 `project-{projectId}/docs/` 中的研发文档与当前实现依据一致，不得只提交代码而遗漏文档归档

### Mandatory Loop Rules

- 静态分析发现问题，必须回到代码修改
- 编译失败，必须根据错误信息修改代码并重新编译
- 安全扫描失败，必须回到代码修改
- 测试不通过，必须回到开发阶段重新迭代
- 若织入点或业务 API 不可用，禁止绕过可行性分析直接开发

## Workflow State Model

主技能必须显式维护流程状态，而不是只根据自然语言上下文隐式判断。状态模型用于统一定义当前阶段、允许迁移方向、阻塞态和恢复态。

### State Enumeration

主流程至少包含以下标准状态：

- `requirements_pending`: 需求待澄清
- `requirements_ready`: 需求已结构化，可进入 PRD
- `prd_pending`: PRD 待编写或待补全
- `prd_ready`: PRD 已完成，可进入可行性分析
- `feasibility_pending`: 可行性待分析
- `feasibility_blocked`: 可行性受阻，存在缺失项
- `feasibility_ready`: 可行性通过，可进入开发
- `support_ticket_open`: 技术支持工单已创建但未解决
- `support_ticket_resolved`: 技术支持工单已解决，待复核恢复
- `implementation_pending`: 开发阶段已进入，尚未完成项目初始化或尚未开始编码
- `implementation_in_progress`: 开发进行中
- `quality_failed`: 静态分析、编译、安全扫描或自审未通过
- `implementation_ready`: 开发与质量门禁通过，可进入发布测试
- `release_in_progress`: 基于 `projectId` 发布测试环境进行中
- `release_blocked`: 发布阶段受阻
- `testing_in_progress`: 测试环境人工测试进行中
- `testing_failed`: 人工测试未通过
- `launch_ready`: 满足上线申请条件
- `workflow_completed`: 流程完成

### State Transition Matrix

| 当前状态 | 触发条件 | 下一状态 | 是否允许自动迁移 | 备注 |
|----------|----------|----------|------------------|------|
| `requirements_pending` | 需求信息已结构化 | `requirements_ready` | 是 | 可进入 PRD 阶段 |
| `requirements_ready` | 开始编写 PRD | `prd_pending` | 是 | 进入 PRD 子技能 |
| `prd_pending` | `PRD.md` 完整可用 | `prd_ready` | 是 | 可进入可行性分析 |
| `prd_ready` | 开始可行性分析 | `feasibility_pending` | 是 | 进入可行性分析子技能 |
| `feasibility_pending` | 存在关键缺失项 | `feasibility_blocked` | 是 | 需要缺失单或工单 |
| `feasibility_pending` | 织入点和业务 API 均已确认 | `feasibility_ready` | 是 | 可进入开发 |
| `feasibility_blocked` | 发起技术支持工单 | `support_ticket_open` | 是 | 进入工单协同 |
| `support_ticket_open` | 工单已解决 | `support_ticket_resolved` | 否 | 需要复核恢复条件 |
| `support_ticket_resolved` | 复核通过 | `feasibility_ready` | 否 | 恢复主线开发 |
| `feasibility_ready` | 开始编码 | `implementation_in_progress` | 是 | 进入开发实现 |
| `implementation_in_progress` | 质量门禁失败 | `quality_failed` | 是 | 回流开发修复 |
| `implementation_in_progress` | 开发完成且质量门禁通过 | `implementation_ready` | 是 | 可进入发布测试 |
| `quality_failed` | 修复并重新通过门禁 | `implementation_in_progress` | 否 | 需要重新验证 |
| `implementation_ready` | 开始发布 | `release_in_progress` | 是 | 进入发布测试 |
| `release_in_progress` | 编译/拉取代码/发布失败 | `release_blocked` | 是 | 需定位问题并决定是在发布侧重试还是回流开发 |
| `release_in_progress` | 发布成功并进入测试 | `testing_in_progress` | 是 | 等待人工测试 |
| `release_blocked` | 发布侧问题已修复或已具备重试条件 | `release_in_progress` | 否 | 在发布阶段重试，无需默认回流开发 |
| `release_blocked` | 根因定位为代码或配置问题 | `implementation_in_progress` | 否 | 回流开发修复 |
| `testing_in_progress` | 人工测试不通过 | `testing_failed` | 是 | 回流开发 |
| `testing_in_progress` | 人工测试通过 | `launch_ready` | 否 | 需要确认上线申请 |
| `testing_failed` | 修复后重新进入发布测试 | `implementation_in_progress` | 否 | 回到开发主线 |
| `launch_ready` | 发起并完成上线申请 | `workflow_completed` | 否 | 流程终态 |

### State Diagram

以下状态图用于直观表达主流程、阻塞态、恢复态和终态之间的迁移关系：

```mermaid
stateDiagram-v2
    [*] --> requirements_pending

    requirements_pending --> requirements_ready: 需求信息已结构化
    requirements_ready --> prd_pending: 开始编写 PRD
    prd_pending --> prd_ready: PRD.md 完整可用
    prd_ready --> feasibility_pending: 开始可行性分析

    feasibility_pending --> feasibility_ready: 点位和 API 均已确认
    feasibility_pending --> feasibility_blocked: 存在关键缺失项
    feasibility_blocked --> support_ticket_open: 发起技术支持工单
    support_ticket_open --> support_ticket_resolved: 工单已解决
    support_ticket_resolved --> feasibility_ready: 复核通过

    feasibility_ready --> implementation_in_progress: 开始编码
    implementation_in_progress --> implementation_ready: 开发完成且质量门禁通过
    implementation_in_progress --> quality_failed: 质量门禁失败
    quality_failed --> implementation_in_progress: 修复并重新通过门禁

    implementation_ready --> release_in_progress: 开始发布
    release_in_progress --> release_blocked: 编译/拉取代码/发布失败
    release_in_progress --> testing_in_progress: 发布成功并进入测试
    release_blocked --> release_in_progress: 发布侧修复后重试
    release_blocked --> implementation_in_progress: 根因位于代码或配置

    testing_in_progress --> testing_failed: 人工测试不通过
    testing_in_progress --> launch_ready: 人工测试通过
    testing_failed --> implementation_in_progress: 修复后重新进入发布测试

    launch_ready --> workflow_completed: 发起并完成上线申请
    workflow_completed --> [*]
```

### Blocked States

以下状态视为阻塞态，默认不得继续主流程：

- `feasibility_blocked`
- `support_ticket_open`
- `quality_failed`
- `release_blocked`
- `testing_failed`

### Recovery States

以下状态视为恢复态或待恢复态，需要显式判断是否恢复主流程：

- `support_ticket_resolved`
- `quality_failed`
- `release_blocked`
- `testing_failed`

### Terminal States

以下状态视为终态：

- `workflow_completed`

终态之后若再继续推进，必须被视为新一轮流程，或明确说明是对既有流程的变更重新开启。

### State Management Rules

- 每次阶段切换前，主技能都应能回答“当前状态是什么、为什么处于这个状态、下一状态是什么”
- 状态迁移必须基于明确触发条件，不得凭模糊判断直接跳转
- 进入阻塞态后，必须先解除阻塞，再恢复主流程
- 从恢复态进入主线前，必须重新检查对应门禁和输出物
- 若用户请求跨越多个状态，主技能必须优先定位最前置且尚未满足的状态

### State And Stage Mapping

主技能应使用如下状态到阶段映射：

- `requirements_pending`、`requirements_ready` -> `Requirements Stage`
- `prd_pending`、`prd_ready` -> `PRD Stage`
- `feasibility_pending`、`feasibility_blocked`、`feasibility_ready` -> `Feasibility Analysis Stage`
- `support_ticket_open`、`support_ticket_resolved` -> `Support Ticket Stage`
- `implementation_in_progress` -> `Implementation Stage`
- `quality_failed`、`implementation_ready` -> `Quality Stage`
- `release_in_progress`、`release_blocked`、`testing_in_progress`、`testing_failed`、`launch_ready` -> `Release And Test Stage`
- `workflow_completed` -> `Completed Stage`

## Global Rules Before Action

在开始任何实际工作前，必须先完成以下判断：

1. 识别当前任务处于哪个阶段：需求、PRD、可行性、开发、发布、测试、上线
2. 判断当前输入是否足以进入下一阶段
3. 判断是否缺少 `PRD.md`、织入点信息、业务 API 信息、插件类型、约束条件
4. 判断是否存在必须先解决的前置阻塞项

### Workspace Dependency Rule

- 任何需要引用本地参考库的阶段，必须优先使用**当前工作目录**下的依赖目录，而不是工作区外的历史目录或任意绝对路径。
- 当可行性分析或开发实现阶段需要使用 `plugin-dev-kit` 时，固定使用当前工作目录下的 `./plugin-dev-kit`。
- 进入可行性分析或开发实现前，必须先检查当前工作目录下是否存在 `plugin-dev-kit`：
  - 若存在：必须在该目录内执行 `git pull`
  - 若不存在：必须在当前工作目录内执行 `git clone "https://oauth2:2qJZCfMZWWKQccYJydsJ@xaicode.xinrenxinshi.com/xrxs/plugin-dev-kit.git" plugin-dev-kit`
- 在 `./plugin-dev-kit` 完成同步前，不得开始读取其文档或 SDK。
- 禁止读取、引用或默认复用当前工作目录之外的 `plugin-dev-kit` 目录，即使该目录在本机已存在也不允许直接作为本次流程依据。

### Universal Guardrails

- 始终使用中文与用户沟通，除非用户明确要求其他语言
- 需求分析必须使用标准化模板输出
- 每个任务都必须包含明确验收标准
- 必须显式标注技术约束和外部依赖
- 对缺失上下文只做最少澄清，不得自由发挥
- 文档依据不足时，必须暂停并向用户提问
- 不得跳过可行性分析、质量门禁、测试发布等关键步骤

## High-Priority Routing

### Routing Execution Rules

- 主技能必须先判断当前最主要阶段，再进入对应子技能
- 若同一用户请求横跨多个阶段，只进入当前最前置、最阻塞的那个阶段
- 进入某个子技能前，必须检查其最小输入是否满足
- 子技能完成后，必须根据退出条件决定是否进入下一个子技能，或回退到上游阶段

### Routing Table

| 用户场景 | 子技能标识 | 显式引用文件 | 进入条件 | 退出条件 | 下一跳 | 动作前必须确认 |
|----------|------------|--------------|----------|----------|--------|----------------|
| 需求还模糊，只是描述想法 | `requirements-translator` | `references/requirements-translator/SKILL.md` | 需求缺少角色、场景、入口、目标、约束中的任一关键项 | 产出 `SRS.md` 或形成足以写 PRD 的结构化需求输入 | `prd-writer` | 角色、场景、目标、约束 |
| 需求已基本清晰，但没有正式 PRD | `prd-writer` | `references/prd-writer/SKILL.md` | 已有清晰需求、`SRS.md` 或等效输入，且足以展开 PRD | 产出完整 `PRD.md`，并具备进入可行性分析的必要信息 | `feasibility-analysis` | 用户故事、验收标准、边界、依赖 |
| 需要判断织入点或业务 API 是否可用 | `feasibility-analysis` | `references/feasibility-analysis/SKILL.md` | 已有 `PRD.md`，且需要识别实现所需点位与业务 API | 输出 `可行 / 部分可行 / 不可行` 结论，并形成缺失项或开发准入结论 | `support-ticket` 或 `plugin-implementation` | 缺失项、阻塞项、解决路径 |
| 需要创建、补充或跟踪技术支持工单 | `support-ticket` | `references/support-ticket/SKILL.md` | 已存在 `织入点缺失单`、`业务 API 缺失单` 或明确阻塞项 | 工单已解决并具备恢复主流程条件，或已驳回且已明确回退方案 | `feasibility-analysis` 或 `prd-writer` | 工单状态、恢复条件、是否缩减范围 |
| 已有 PRD 且可行性已通过，要开发插件 | `plugin-implementation` | `references/plugin-implementation/SKILL.md` | `PRD.md` 完整、可行性通过、关键工单已解决、插件类型明确 | 开发完成，且编译与代码自审通过 | `release-and-test` | 插件类型、目录结构、文档依据、质量门禁 |
| 需要发布测试环境、查询发布状态或根据测试决定上线 | `release-and-test` | `references/release-and-test/SKILL.md` | 开发完成且质量门禁通过，已持有 `projectId` | 完成测试环境发布，并得到人工测试结论 | 上线申请或回流 `plugin-implementation` | `projectId`、发布参数、测试结论 |

### Routing Decision Notes

- 若 `requirements-translator` 未能补齐关键信息，则不得进入 `prd-writer`
- 若 `prd-writer` 产出的 `PRD.md` 仍缺关键入口、动作或依赖，则不得进入 `feasibility-analysis`
- 若 `feasibility-analysis` 仍存在关键缺失项，则不得进入 `plugin-implementation`
- 若 `support-ticket` 状态不是 `已解决`，则不得恢复到开发主线
- 若 `plugin-implementation` 的质量门禁未通过，则不得进入 `release-and-test`
- 若 `release-and-test` 的人工测试不通过，则必须回流到 `plugin-implementation`

## Tooling Prerequisite

`xrxs-plugin-MCP` 已作为本技能的默认运行时依赖完成交付（本地以 `@xrxs-plugin/plugin-mcp` 名义预设）。需求与 PRD 阶段仍由当前 LLM 直接完成；从可行性分析阶段开始，主技能优先通过 MCP 驱动运行期关键动作。

### Tool-First Principle

当 `xrxs-plugin-MCP` 可用时，以下能力必须优先通过 MCP 完成：

- 创建、查询、更新、关闭技术支持工单（`plugin_mcp_support_ticket_*`）
- 初始化 GitLab 开发项目（`plugin_mcp_implementation_project_init`）
- 静态分析、编译、安全扫描（`plugin_mcp_build_*`）
- 基于 `projectId` 发布插件到测试环境（`plugin_mcp_release_test_deploy` / `plugin_mcp_deploy_plugin`）
- 查询发布状态、发布日志、插件实例信息（`plugin_mcp_release_status_get` / `plugin_mcp_release_log_query` / `plugin_mcp_get_plugin_info_by_project_id`）
- 查询项目当前审核状态（`plugin_mcp_release_project_status_get`）
- 提交与取消上线申请（`plugin_mcp_release_launch_apply` / `plugin_mcp_release_launch_cancel`）
- 流程状态读写、阻塞与恢复（`plugin_mcp_workflow_*`）

### Fallback Principle

当 MCP 不可用时：

- 可以继续进行需求、PRD、方案、代码结构设计等文档类工作
- 对需要真实系统能力的步骤，只能输出人工执行说明和待补充信息
- 不得伪造织入点、业务 API、工单状态、构建结果、发布结果

### Requirements And PRD Rule

- `SRS.md` 与 `PRD.md` 由当前 LLM 直接生成和更新
- Requirements Stage 与 PRD Stage 不依赖 MCP 能力
- 只有从可行性分析阶段开始，才优先进入 MCP 驱动模式

## MCP Capability Contract

`xrxs-plugin-MCP` 是未来驱动 XRXS 插件研发流程自动化的远程能力层。主技能必须把 MCP 视为“可调用的系统能力契约”，而不是零散工具集合。所有 MCP 能力都应与阶段、状态、门禁、输出物和恢复策略显式绑定。

### Capability Groups

主技能应优先按以下能力分组理解和调用 MCP：

| 能力分组 | 典型工具 | 主要职责 | 典型对应阶段 | 典型对应状态 |
|----------|----------|----------|--------------|--------------|
| `workflow` | `plugin_mcp_workflow_state_get` / `plugin_mcp_workflow_state_update` / `plugin_mcp_workflow_block` / `plugin_mcp_workflow_resume` / `plugin_mcp_workflow_history_query` | 流程状态读写、阶段推进、阻塞标记、恢复标记 | 从 Feasibility Analysis 开始的全阶段 | 全状态 |
| `implementation-init` | `plugin_mcp_implementation_project_init` | 初始化 GitLab 开发项目、申请 project token、返回 `projectId` 与仓库信息 | Implementation Stage | `feasibility_ready` → `implementation_pending` |
| `support-ticket` | `plugin_mcp_support_ticket_create` / `query` / `update` / `close` / `recovery_check` | 工单创建、查询、状态更新、关闭、恢复检查 | Support Ticket Stage | `support_ticket_open`、`support_ticket_resolved` |
| `build` | `plugin_mcp_build_static_analysis` / `plugin_mcp_build_compile` / `plugin_mcp_build_security_scan`；需要无 workflow 上下文的原始编译可用 `plugin_mcp_compile_check` + `plugin_mcp_get_compile_log` | 静态分析、编译、安全扫描，同时向 workflow 写回门禁结果 | Implementation Stage、Quality Stage | `implementation_in_progress`、`quality_failed`、`implementation_ready` |
| `release` | 发布: `plugin_mcp_release_test_deploy`（workflow 感知）/ `plugin_mcp_deploy_plugin`（无 workflow 时的原始发布）；状态与日志: `plugin_mcp_release_status_get` / `plugin_mcp_release_log_query`；上线: `plugin_mcp_release_project_status_get` / `plugin_mcp_release_launch_apply` / `plugin_mcp_release_launch_cancel` | 基于 `projectId` 发布测试环境、查询项目审核状态、提交/取消上线申请 | Release And Test Stage | `release_in_progress`、`release_blocked`、`testing_in_progress`、`launch_ready` |
| `runtime-info` | `plugin_mcp_get_plugin_info_by_project_id` / `plugin_mcp_get_plugin_configs` / `plugin_mcp_add_plugin_config` / `plugin_mcp_delete_plugin_config` / `plugin_mcp_get_plugin_runtime_logs` / `plugin_mcp_get_plugin_lifecycle_events` | 发布成功后查询插件实例、配置、运行日志与生命周期事件 | Release And Test Stage 之后的排查与运维 | 发布后的任何状态 |
| `util` | `plugin_mcp_util_snowflake_id`、`plugin_mcp_get_project_id_by_project_path`、`plugin_mcp_health`、`plugin_mcp_initialize` | 本地短 ID、反查 projectId、服务探活与能力探测 | 全阶段辅助 | 不直接维护状态 |

说明：可行性分析（Feasibility Analysis）**不存在专属 MCP 能力分组**。该阶段的能力校对必须基于本地 `plugin-dev-kit` 文档静态核对，仅使用 `workflow` 分组将分析结论回写到流程状态。

### Capability Invocation Rules

- 主技能必须先判断当前阶段和状态，再决定调用哪个能力分组
- 若一个请求同时涉及多个分组，优先调用当前最前置、最阻塞阶段对应的分组
- MCP 调用结果必须回写到当前流程上下文，用于更新状态、门禁结论和输出物
- 同一个阶段中，若 MCP 能力与现有文档或状态冲突，应优先暂停并做一致性校验

### Stage Binding Matrix

| 阶段 | 优先 MCP 分组 | 允许调用目的 | 不应越权调用的分组 |
|------|---------------|--------------|--------------------|
| Requirements Stage | 无 | 由当前 LLM 生成结构化需求输入与 `SRS.md` | `implementation-init`、`build`、`release`、`workflow`（尚未生成 workflowId）|
| PRD Stage | 无 | 由当前 LLM 生成或更新 `PRD.md` | `implementation-init`、`build`、`release`、`workflow`（尚未生成 workflowId）|
| Feasibility Analysis Stage | `workflow`（+ `util` 生成 workflowId）| 基于本地 `plugin-dev-kit` 静态核对能力；将分析结论同步到流程状态（`feasibility_pending` / `feasibility_blocked` / `feasibility_ready`）| `implementation-init`、`build`、`release` |
| Support Ticket Stage | `support-ticket`、`workflow` | 创建工单、同步阻塞态、查询/更新/关闭工单、通过 `recovery_check` + `workflow_resume` 恢复主线 | `build`、`release` |
| Implementation Stage | `implementation-init`、`build`、`workflow` | 初始化开发项目、静态分析、编译、安全扫描、质量状态更新 | `release`，除非已进入 `implementation_ready` |
| Release And Test Stage | `release`、`runtime-info`、`workflow` | 基于 `projectId` 发布测试环境、查询发布状态与日志、运行时排查、上线申请 | 无，除非发生回流 |

### Input And Output Contract

每类 MCP 能力调用都应满足最小输入与标准输出契约：

| 能力分组 | 最小输入 | 标准输出 | 失败时最少保留信息 |
|----------|----------|----------|--------------------|
| `feasibility` | `PRD.md`、目标功能点、插件形态候选 | 点位查询结果、API 查询结果、缺失项、可行性结论 | 查询条件、缺失项、阻塞说明 |
| `support-ticket` | 缺失单、问题类型、项目上下文 | 工单内容、工单状态、恢复建议 | 工单参数、状态未知原因、阻塞影响 |
| `build` | 代码状态、配置文件、目标构建动作 | 静态分析结果、编译结果、安全扫描结果 | 错误日志、失败位置、相关版本与配置状态 |
| `release` | `projectId`、目标环境 | 发布结果、项目状态、上线申请结果 | 发布参数、失败原因、环境信息 |
| `workflow` | 当前状态、目标动作、上下文标识 | 状态迁移结果、阻塞标记、恢复标记、阶段同步结果 | 原状态、目标状态、迁移失败原因 |

### State Integration Rules

MCP 调用结果必须和 `Workflow State Model` 一一对应：

- 本地基于 `plugin-dev-kit` 完成可行性分析后，必须调用 `plugin_mcp_workflow_state_update` 将状态推动为 `feasibility_ready`（可行）或调用 `plugin_mcp_workflow_block` 将状态标记为 `feasibility_blocked`（不可行/部分可行）
- `plugin_mcp_support_ticket_create` 成功后，服务端会自动把项目阶段推到 10=可行性阻塞，推动 `feasibility_blocked -> support_ticket_open`
- `plugin_mcp_support_ticket_update(status=resolved)` 后，先调 `plugin_mcp_support_ticket_recovery_check`，通过后再调 `plugin_mcp_workflow_resume` 将状态恢复为 `feasibility_ready`
- `plugin_mcp_build_static_analysis` / `plugin_mcp_build_compile` / `plugin_mcp_build_security_scan` 任一失败 → 服务端写入 `quality_failed`；均通过 → 推到 `implementation_ready`
- `plugin_mcp_release_test_deploy` 成功 → `release_in_progress -> testing_in_progress`；失败 → `release_blocked`
- `plugin_mcp_release_launch_apply` 成功 → `launch_ready -> workflow_completed`（待审核通过后）
- 任一状态迁移都应追加调用 `plugin_mcp_workflow_state_update`，以避免状态只停留在文字描述层

### Gate Integration Rules

MCP 调用结果必须参与门禁决策，而不能脱离门禁单独使用：

- 当前 LLM 生成的 `SRS.md` 与 `PRD.md` 参与 `文档门禁`
- 本地基于 `plugin-dev-kit` 的可行性分析结论 + `support-ticket` 的状态参与 `可行性门禁`
- `build` 的结果参与 `工程门禁`
- `release` 的结果（含 `release_test_deploy`、`release_status_get`、`release_project_status_get`）参与 `发布门禁`
- 若对应 MCP 调用失败或结果不明，则对应门禁默认不通过

### Output Integration Rules

MCP 调用结果必须映射到主技能的标准输出物：

- 当前 LLM -> `SRS.md`、`PRD.md`
- 本地可行性分析 -> 可行性结论、缺失单
- `support-ticket` -> 技术支持工单、状态更新、恢复建议
- `implementation-init` -> `projectId`、`gitlabProjectUrl`、`gitlabProjectToken`、开发项目记录
- `build` -> 静态分析结果、编译结果、安全扫描结果
- `release` -> `pluginId`、测试环境发布结果、项目审核状态、上线申请结果
- `runtime-info` -> 插件实例信息、插件配置、运行时日志、生命周期事件
- `workflow` -> 状态变更记录、阻塞态、恢复态、终态同步结果

### Capability Failure Rules

- 任一 MCP 能力调用失败，必须先记录失败类型、输入参数、失败原因和当前状态
- 若失败影响当前阶段的必需输出物或门禁结论，则必须停止主流程
- 若失败仅影响辅助性信息，可在明确风险后退化为人工处理
- 若 `workflow` 分组调用失败，则不得假定状态已成功迁移

### Degradation Rules

当 `xrxs-plugin-MCP` 不可用或部分能力不可用时，主技能必须进入显式降级模式：

- 将当前阶段标记为“人工执行模式”
- 输出人工待执行清单，而不是伪造系统执行结果
- 保留当前状态，不做未经验证的自动迁移
- 等待人工补充结果后，再恢复正常状态推进

### Capability Priority Order

主技能应按以下优先级理解 MCP 调用顺序：

1. 在 Requirements Stage 与 PRD Stage，由当前 LLM 直接生成和更新文档（不调 MCP）
2. 首次进入 Feasibility Analysis Stage 时，本地生成 `workflowId`（可借助 `plugin_mcp_util_snowflake_id`）
3. 完成本地 `plugin-dev-kit` 可行性分析后，优先调用 `plugin_mcp_workflow_state_update` / `plugin_mcp_workflow_block` 将结论写入服务端
4. 如存在缺失项，调用 `plugin_mcp_support_ticket_create` 并推动到 `support_ticket_open`；工单处理中使用 `query` / `update`；解决后 `recovery_check` + `workflow_resume`
5. 进入 Implementation Stage 时，先调 `plugin_mcp_implementation_project_init`，拿到 `projectId` 后进入编码与门禁
6. 进入 Release And Test Stage 时，依次执行 `plugin_mcp_build_static_analysis` → `plugin_mcp_build_security_scan` → `plugin_mcp_release_test_deploy`；必要时用 `release_status_get` / `release_log_query` 追踪
7. 人工测试通过后，调 `plugin_mcp_release_project_status_get` 确认 `audit_phase`，再调 `plugin_mcp_release_launch_apply` 提交上线；如需撤回用 `plugin_mcp_release_launch_cancel`
8. 任一阶段发生阻塞时，先调 `plugin_mcp_workflow_block`、解阻后调 `plugin_mcp_workflow_resume`，避免状态靠文字传递

### Future Extension Rules

后续 `xrxs-plugin-MCP` 正式落地时，本章节可继续补充：

- 每个能力分组的具体方法名约定
- 标准请求参数与响应结构
- 幂等要求与重试策略
- 状态写回与审计日志规范

## XRXS Plugin Project Model

进行插件开发时，应遵循以下基础工程结构：

- `assets`: 静态资源，如图标、说明图
- `endpoints`: 织入点配置集合
- `manifest.yml`: 插件主配置
- `README.md`: 插件文档
- `src/backend`: 后端代码
- `src/fe`: 前端代码

### Manifest Baseline

`manifest.yml` 至少需要关注以下字段：

- `version`
- `author`
- `created_at`
- `icon`
- `description`
- `source`
- `endpoints`

### Endpoint Baseline

每一个实际使用的织入点都必须：

- 在 `endpoints/` 下有对应配置文件
- 在 `manifest.yml` 的 `endpoints` 列表中显式注册
- 与实际代码实现的 `source` 保持一致

## Plugin Type Decision Rules

在开发前，必须先确定插件类型。XRXS 共有 6 种端点类型，分为后端（Java）和前端（JS/HTML/CSS）两大类。Hook 在前后端均有，但形态完全不同：

- `backend`: 后端 Hook 钩子（Java），织入后端业务流程做校验/处理/拦截，如保存校验、字段校验等
- `backend-http`: 后端 Rest 接口（Java），提供 HTTP 接口供前端页面或外部系统调用
- `action`: 前端 Action 动作（JS），在页面位置新增按钮、链接、图标等触发逻辑
- `page`: 前端 Page 页面（HTML/CSS/JS），打开独立插件页面，承载完整交互
- `hook`: 前端 Hook 钩子（JS），织入前端业务流程做校验或控制，通过 `handler(data, next)` 回调
- `extension`: 前端 Extension 扩展（HTML/CSS/JS），在指定位置插入扩展 DOM 和交互

### Type Selection Rules

- 如果目标是**后端业务流程校验/拦截**（如保存前校验身份证号），优先考虑 `backend`
- 如果目标是补充后端接口能力，优先考虑 `backend-http`
- 如果目标是在现有页面增加入口并执行简单动作，优先考虑 `action`
- 如果目标需要承载配置页或完整交互，优先考虑 `page`
- 如果目标是**前端业务流程校验/拦截**（如提交前二次确认），优先考虑 `hook`
- 如果目标是增加信息展示或局部 UI 扩展，优先考虑 `extension`

## Stage Rules

### Requirements Stage

阶段目标：

- 将模糊需求整理为可进入 PRD 阶段的结构化输入
- 明确角色、场景、入口、目标、约束和基础验收标准

最小输入：

- 用户原始需求描述
- 至少一个可识别的业务目标或问题背景

标准输出：

- `SRS.md` 或结构化需求摘要
- 明确的角色、场景、目标、约束、验收标准

禁止跳过条件：

- 需求仍缺少关键角色、场景、入口、目标、约束中的任一项
- 尚未形成足以写 PRD 的结构化输入

默认下一跳：

- `prd-writer`

### PRD Stage

阶段目标：

- 基于已澄清需求生成完整、结构化、可下游使用的 `PRD.md`
- 固化用户故事、验收标准、范围边界、技术约束、依赖、页面和交互说明

最小输入：

- 清晰需求或 `SRS.md`
- 已知的角色、场景、目标、边界

标准输出：

- `PRD.md`
- 明确的用户故事、验收标准、插件形态候选、页面入口与交互流程

禁止跳过条件：

- 尚未产出完整 `PRD.md`
- PRD 缺失关键入口、动作、依赖、验收标准或范围边界

默认下一跳：

- `feasibility-analysis`

### Feasibility Analysis Stage

阶段目标：

- 基于 `PRD.md` 判断实现需求所需的织入点和业务 API 是否可用
- 给出可行、部分可行或不可行的明确结论

最小输入：

- `PRD.md`
- 基本明确的入口位置、触发动作、业务流程和插件形态候选

标准输出：

- 可行性分析结论
- `织入点缺失单`
- `业务 API 缺失单`
- 是否可进入开发或需要工单协同的建议

禁止跳过条件：

- 未分别完成 `织入点可行性` 与 `业务 API 可行性` 判断
- 关键缺失项尚未记录
- 可行性结论仍为 `待确认`

默认下一跳：

- `support-ticket` 或 `plugin-implementation`

### Support Ticket Stage

阶段目标：

- 将缺失项、阻塞项或能力边界问题转化为可跟踪的技术支持工单
- 在工单解决后为主流程恢复提供明确依据

最小输入：

- `织入点缺失单`、`业务 API 缺失单` 或明确阻塞项
- 缺失项对应的 PRD 功能点与影响范围

标准输出：

- `插件技术支持工单`
- 工单状态更新
- 主流程恢复建议

禁止跳过条件：

- 工单尚未解除阻塞
- 工单状态仍为 `待确认`、`已提交` 或 `处理中`
- 尚未明确恢复到哪个上游阶段进行复核

默认下一跳：

- `feasibility-analysis` 或 `prd-writer`

### Implementation Stage

阶段目标：

- 根据 `PRD.md`、可行性结论和官方文档完成插件代码与配置实现
- 形成可进入测试发布的工程状态

最小输入：

- `PRD.md`
- 可行性通过结论，或已解决并复核通过的工单结果
- 已确认插件类型、织入点、业务 API 和实现依据

标准输出：

- 插件代码
- `manifest.yml`
- `endpoints/*.yml`
- 必要资源与文档
- 静态分析、编译、安全扫描、代码自审结论

禁止跳过条件：

- PRD 不完整
- 可行性未通过
- 工单未解决
- 静态分析、编译、安全扫描、代码自审任一未通过

默认下一跳：

- `Quality Stage`

### Quality Stage

阶段目标：

- 对已完成实现的插件执行质量门禁校验
- 形成进入测试发布前的工程准入结论

最小输入：

- 已完成的插件代码与配置
- 可执行静态分析、编译、安全扫描和代码自审的工程状态

标准输出：

- 静态分析结果
- 编译结果
- 安全扫描结果
- 代码自审结论

禁止跳过条件：

- 静态分析未通过
- 编译未通过
- 安全扫描未通过
- 代码自审未完成

默认下一跳：

- `release-and-test`

### Release And Test Stage

阶段目标：

- 将已完成开发且通过质量门禁的插件推进到可测试、可上线申请状态
- 完成基于 `projectId` 的测试环境发布、人工测试和上线申请准备

最小输入：

- 可发布的插件工程状态
- `projectId`
- 静态分析、编译、安全扫描、代码自审全部通过的结论

标准输出：

- `projectId`
- 测试环境发布结果
- 人工测试结论
- 项目状态查询结果
- `插件上线申请单` 或明确回流结论

禁止跳过条件：

- 未完成最新代码提交
- 未拿到 `projectId`
- 未完成测试环境发布
- 人工测试未通过

默认下一跳：

- 上线申请阶段或回流 `plugin-implementation`

## Quality Gates

以下门禁必须作为硬性约束执行，任何阶段都不得以“先继续推进、后面再补”为理由绕过。

### Quality Gate Matrix

| 门禁名称 | 适用阶段 | 通过标准 | 失败回流阶段 | 禁止绕过条件 |
|----------|----------|----------|--------------|--------------|
| 文档门禁 | 需求阶段、PRD 阶段 | 需求已结构化；`PRD.md` 完整；用户故事、验收标准、范围边界、依赖齐备 | `requirements-translator` 或 `prd-writer` | 需求不清、无 PRD、无验收标准时，禁止进入可行性分析和开发 |
| 可行性门禁 | 可行性阶段 | 已分别完成织入点与业务 API 判断；关键缺失项已记录；结论明确为 `可行` 或具备受控的下一步策略 | `prd-writer`、`feasibility-analysis` 或 `support-ticket` | 织入点不明确、业务 API 不明确、缺失项未记录时，禁止进入开发 |
| 工程门禁 | 开发阶段、质量阶段 | 静态分析通过；编译通过；安全扫描通过；代码自审完成 | `plugin-implementation` | 静态分析、编译、安全扫描、代码自审任一未通过时，禁止进入发布 |
| 发布门禁 | 发布阶段、上线阶段 | 已拿到 `projectId`；最新代码已提交 GitLab；测试环境发布成功；人工测试通过；上线申请信息完整 | `release-and-test` 或 `plugin-implementation` | 无 `projectId`、未提交 Git、发布失败、人工测试未通过时，禁止进入上线申请 |

### Gate Enforcement Rules

- 主技能必须在每次阶段切换前显式检查对应门禁
- 若门禁失败，优先回流到最近且能消除阻塞的阶段
- 若多个门禁同时失败，优先处理更前置的门禁，例如先补文档，再做可行性，再处理工程问题
- 门禁结论必须可复核，不能只写“基本没问题”“应该可以”

### Gate Decision Notes

- `文档门禁` 失败时，不能用可行性分析或开发来反向补需求
- `可行性门禁` 失败时，不能假设缺失的织入点或业务 API 会在后续自然补齐
- `工程门禁` 失败时，不能通过人工说明替代静态分析、编译或安全扫描结果
- `发布门禁` 失败时，不能用“先提上线申请、后面再测”作为例外路径

### Minimal Gate Order

主技能应按以下最小门禁顺序执行：

1. `文档门禁`
2. `可行性门禁`
3. `工程门禁`
4. `发布门禁`

若前一门禁未通过，后一门禁不应被触发。

## Child Skills

以下子技能标识为未来稳定路由名。第一版即应固定这些标识，避免后续改名导致路由漂移。

### Child Skill Navigation Rules

- 当任务阶段已明确时，主技能应直接引用对应子技能文件
- 当用户问题横跨多个阶段时，主技能先判断当前主阶段，再引用主阶段对应子技能
- 当某个子技能需要依赖上游输出物时，主技能必须先检查输出物是否存在，再决定是否进入该子技能
- 当子技能文件已落盘时，不再回退到主技能同名章节，除非用户明确要求在主技能中统一处理

### Child Skill Reference Index

| 子技能标识 | 显式引用文件 | 主要职责 | 上游输入 | 默认下游 |
|------------|--------------|----------|----------|----------|
| `requirements-translator` | `references/requirements-translator/SKILL.md` | 需求澄清与需求描述文档 | 用户原始需求 | `prd-writer` |
| `prd-writer` | `references/prd-writer/SKILL.md` | 生成 `PRD.md` | 清晰需求或 `SRS.md` | `feasibility-analysis` |
| `feasibility-analysis` | `references/feasibility-analysis/SKILL.md` | 校验织入点与业务 API 可行性 | `PRD.md` | `support-ticket` 或 `plugin-implementation` |
| `support-ticket` | `references/support-ticket/SKILL.md` | 创建和跟踪技术支持工单 | 缺失单、阻塞项 | `feasibility-analysis` 或 `plugin-implementation` |
| `plugin-implementation` | `references/plugin-implementation/SKILL.md` | 实现插件代码与配置 | `PRD.md`、可行性结论 | `release-and-test` |
| `release-and-test` | `references/release-and-test/SKILL.md` | 测试发布、人工测试、上线申请 | 代码与质量门禁结论、`projectId` | 上线申请或回流开发 |

### `requirements-translator`

显式引用文件：`references/requirements-translator/SKILL.md`

职责：

- 将模糊需求转换为结构化需求描述
- 产出 `SRS.md`
- 为 PRD 阶段提供清晰输入

### `prd-writer`

显式引用文件：`references/prd-writer/SKILL.md`

职责：

- 基于已澄清需求生成 `PRD.md`
- 明确用户故事、验收标准、交互流程、边界与约束

### `feasibility-analysis`

显式引用文件：`references/feasibility-analysis/SKILL.md`

职责：

- 选择并校验织入点
- 选择并校验业务 API
- 形成缺失单与可行性结论

### `support-ticket`

显式引用文件：`references/support-ticket/SKILL.md`

职责：

- 创建技术支持工单
- 跟踪缺失项处理状态
- 在工单解决后恢复研发流程

### `plugin-implementation`

显式引用文件：`references/plugin-implementation/SKILL.md`

职责：

- 基于 `PRD.md` 和开发规范实现插件代码
- 维护 `manifest.yml`、`endpoints/*.yml`、前后端代码与文档

### `release-and-test`

显式引用文件：`references/release-and-test/SKILL.md`

职责：

- 执行质量门禁后的测试发布、人工测试收口
- 在测试通过后准备上线申请

## Human-In-The-Loop Rules

以下情况必须主动向用户确认：

- 需求存在多种业务解释
- 无法唯一确定插件类型
- 织入点与业务 API 文档依据不足
- 方案存在明显范围变更
- 工单需要继续等待或切换替代方案
- 测试不通过时需要决定是否回退范围

## Failure Handling

主技能必须对常见失败场景给出统一、可回流、可审计的恢复策略，而不是只做口头说明。

### Failure Recovery Matrix

| 失败类型 | 典型触发阶段 | 必须保留的信息 | 默认回流阶段 | 是否允许继续主流程 |
|----------|--------------|----------------|--------------|--------------------|
| 需求信息不完整 | 需求阶段、PRD 阶段 | 缺失的角色、场景、入口、目标、约束项；已确认信息；最后一次澄清问题 | `requirements-translator` | 否 |
| PRD 信息不足或结构不完整 | PRD 阶段、可行性阶段 | 缺失的入口、动作、依赖、验收标准、边界；受影响功能点 | `prd-writer` | 否 |
| 可行性失败 | 可行性阶段 | 织入点缺失项；业务 API 缺失项；结论类型；受阻塞的功能点 | `feasibility-analysis` 或 `support-ticket` | 否 |
| 工单未解决或状态不明 | 工单协同阶段、开发阶段 | 工单状态；已解决项；未解决项；阻塞影响；恢复条件 | `support-ticket` | 否 |
| 静态分析失败 | 开发阶段、质量阶段 | 分析错误列表；受影响文件；修复建议；当前代码版本或状态 | `plugin-implementation` | 否 |
| 编译失败 | 开发阶段、质量阶段 | 编译错误信息；失败位置；最近变更；可复现条件 | `plugin-implementation` | 否 |
| 安全扫描失败 | 质量阶段 | 扫描问题列表；风险级别；受影响模块；修复状态 | `plugin-implementation` | 否 |
| 测试环境发布失败 | 发布阶段 | 发布参数；失败原因；目标环境；`projectId` 信息 | `release-and-test` 或 `plugin-implementation` | 否 |
| 人工测试失败 | 发布阶段、上线阶段 | 失败用例；问题摘要；受影响功能点；复现路径；测试结论 | `plugin-implementation` | 否 |
| MCP 不可用 | 任意依赖系统能力的阶段 | 当前阶段；原计划动作；缺失的系统能力；人工待执行清单 | 当前阶段保留，转人工方案 | 有条件允许 |

### Failure Handling Rules

- 失败时必须先保留证据，再决定是否回流
- 失败信息至少要能支持复现、定位和恢复决策
- 若失败影响当前阶段的准入条件，则不得继续主流程
- 若失败只影响可选输出物，可在明确风险后继续，但不得影响必需输出物和门禁结论

### Recovery Decision Notes

- `需求信息不完整` 和 `PRD 信息不足` 都属于前置失败，必须优先处理，不能靠后续阶段反向补齐
- `可行性失败` 不能通过“先开发核心部分”默认绕过，除非已明确缩减范围并重新确认
- `静态分析失败`、`编译失败`、`安全扫描失败` 都属于工程阻塞失败，必须回流开发阶段
- `测试环境发布失败` 属于发布阻塞失败，不能进入人工测试或上线阶段
- `人工测试失败` 直接阻断上线申请，必须回流开发实现
- `MCP 不可用` 不等于流程失败，但必须显式转换为人工执行清单，且不得伪造系统结果

### Minimal Recovery Order

当多个失败同时存在时，主技能应按以下顺序恢复：

1. 先解决前置输入失败：需求、PRD
2. 再解决能力可行性失败：织入点、业务 API、工单阻塞
3. 再解决工程失败：静态分析、编译、安全扫描
4. 最后解决发布失败：测试发布、人工测试

### Continue-Or-Stop Rules

- 仅当失败不影响当前阶段的必需输出物、门禁结论和下一阶段验收条件时，才允许继续主流程
- 只要失败影响 `文档门禁`、`可行性门禁`、`工程门禁` 或 `发布门禁` 中任一项，就必须停止并回流

## Output Contract

主技能必须把每个阶段的输出物视为进入下一阶段的验收依据，而不仅仅是过程产物。

### Stage Output Matrix

| 阶段 | 必需输出物 | 可选输出物 | 进入下一阶段前的验收条件 |
|------|------------|------------|--------------------------|
| 需求阶段 | `SRS.md` 或结构化需求摘要；明确的角色、场景、入口、目标、约束、验收标准 | 需求澄清记录；插件类型初步判断；风险提示 | 需求信息足以支撑 PRD 编写；关键角色、场景、目标、约束不再缺失 |
| PRD 阶段 | `PRD.md`；用户故事；验收标准；页面入口与交互流程；范围边界与依赖 | 插件形态候选；Mermaid 交互流程图；待确认项列表 | `PRD.md` 结构完整；关键入口、动作、依赖、验收标准齐备；足以进入可行性分析 |
| 可行性阶段 | 可行性分析结论；`织入点缺失单`；`业务 API 缺失单`；开发准入建议 | 风险与影响清单；替代方案建议；范围缩减建议 | 已分别完成织入点与业务 API 判断；关键缺失项已记录；结论不再是 `待确认` |
| 工单协同阶段 | `插件技术支持工单`；工单状态更新；主流程恢复建议 | 优先级说明；替代方案说明；驳回原因记录 | 若进入开发主线，工单状态必须已明确解除阻塞；若未解除阻塞，则不得进入开发 |
| 开发阶段 | 插件代码；`manifest.yml`；`endpoints/*.yml`；必要资源与文档；代码自审结论 | 测试用例；README 更新；实现依据清单；配置校验记录 | 开发完成；代码与文档依据一致；配置注册完整；具备进入质量门禁的工程状态 |
| 质量阶段 | 静态分析结果；编译结果；安全扫描结果 | 人工检查记录；问题修复记录；残余风险说明 | 静态分析通过；编译通过；安全扫描通过；不存在阻塞发布的质量问题 |
| 发布阶段 | `projectId`；测试环境发布结果；人工测试结论 | 发布参数记录；测试问题清单；重试记录 | 已拿到 `projectId`；测试环境发布成功；人工测试结论明确 |
| 上线阶段 | `插件上线申请单` 或上线前检查结论 | 发布说明；版本变更说明；测试摘要 | 人工测试通过；无阻塞上线问题；上线申请信息完整 |

### Output Validation Rules

- 任一阶段若缺少必需输出物，不得进入下一阶段
- 任一阶段若验收条件未满足，不得以“后续补充”作为跳过理由
- 可选输出物不作为强制准入条件，但应在降低风险、补充说明或支持协同时优先提供
- 若输出物与上游阶段结论冲突，应优先回退到最近的有效阶段重新确认

### Minimal Acceptance Chain

主技能应按以下最小验收链推进：

1. 有结构化需求输入，才能进入 `PRD`
2. 有完整 `PRD.md`，才能进入可行性分析
3. 有明确可行性结论，才能进入开发
4. 有完整代码与配置，才能进入质量门禁
5. 质量门禁通过，且最新代码已提交 GitLab，才能进入发布
6. 已完成测试环境发布且人工测试通过，才能进入上线申请

## First-Version Scope

第一版 `xrxs-plugin-workflow` 的目标不是一次写完所有子技能，而是先固定以下能力边界：

- 统一主入口命名与路由契约
- 固化 XRXS 插件研发主流程
- 固化“当前 LLM 负责需求与 PRD、MCP 负责运行期能力”的分层原则
- 固化子技能标识与职责边界
- 保证在子技能未拆分前，主技能本身即可独立工作

后续版本再逐步把每个子技能从本主技能中拆出到 `references/` 目录。
