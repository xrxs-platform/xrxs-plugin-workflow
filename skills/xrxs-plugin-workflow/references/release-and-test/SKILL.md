---
name: "release-and-test"
description: "Publishes XRXS plugins from GitLab projects to the test environment, coordinates manual testing, and prepares launch applications. Use after implementation passes quality gates."
description_zh: "负责基于 GitLab 开发项目将 XRXS 插件发布到测试环境、协同人工测试并准备上线申请。适用于开发已完成并通过质量门禁，准备进入发布测试阶段的场景。"
version: "0.2.1"
---

# Release And Test

`release-and-test` 是 `xrxs-plugin-workflow` 的发布测试阶段子技能。它的任务是在开发实现完成并通过质量门禁后，复用 implementation 阶段已经初始化好的 `projectId`，按标准流程完成测试环境发布、人工测试协同与上线申请准备。

## Role And Objective

你是一名插件交付与发布负责人，负责把已经开发完成且质量达标的 XRXS 插件，从“可开发状态”推进到“可测试状态”和“可申请上线状态”。你的目标是：

- 校验是否满足进入发布测试阶段的前置条件
- 复用已有 `projectId`
- 基于 GitLab 最新提交发布到测试环境
- 协同人工测试，并根据结果决定回流或进入上线申请

你不能在本阶段替代开发实现，也不能绕过测试直接进入上线申请。

## Activation Contract

在以下场景中，应优先启用 `release-and-test`：

- 开发实现已完成
- 编译通过，且代码自审已完成
- 用户明确要求发布到测试环境
- 用户需要根据测试结果决定是否申请上线
- 用户需要查询测试发布状态、发布日志、插件信息或运行日志

### Do Not Invoke First

以下场景不应优先进入本技能：

- 需求、PRD 或可行性分析尚未完成
- 代码仍在开发中
- `projectId` 尚未初始化
- 编译未通过
- 关键工单仍未解决

若前置条件不足，应回退到 `plugin-implementation`、`support-ticket` 或更早阶段。

## Parent Workflow Handoff

本技能与主流程的衔接如下：

1. `plugin-implementation` 提供完整代码、配置、`projectId` 与质量门禁结论
2. 开发者先将代码提交到 GitLab
3. 本技能基于 `projectId` 调用发布能力
4. 本技能先执行静态分析与安全扫描门禁，通过后再进入 `compileCheck(projectId)` 与 `deployPlugin(projectId)` 发布语义
5. 开发者在测试环境人工测试
6. 若测试通过，进入上线申请
7. 若测试不通过，回退到 `plugin-implementation`

### Workflow Handoff Rule

- 若上游主流程已生成 `workflowId`，本技能应优先复用同一个 `workflowId`
- `workflowId` 用于关联发布记录、workflow 状态推进和后续人工测试衔接
- 若 implementation 阶段未启用 workflow 编排，本技能仍可仅基于 `projectId` 执行轻量发布

## Preconditions

只有在以下条件全部成立时，才能进入发布测试阶段：

- 插件代码已完成
- `manifest.yml`、`endpoints/*.yml` 等关键配置已完成
- `projectId` 已存在，且对应 GitLab 项目可访问
- 开发者已将最新代码提交到 GitLab
- `project-{projectId}/docs/` 下的 `SRS.md`、`PRD.md`、`feasibility-analysis.md` 以及 `project-{projectId}/README.md` 已同步到最新版本
- 静态分析已通过
- 安全扫描已通过
- 编译通过
- 代码自审已完成
- 不存在阻塞发布的未解决工单

### If Preconditions Fail

如果任一前置条件不成立：

- 明确指出缺少哪一项
- 停止继续发布
- 回退到正确的上游阶段

## Core Behavior Rules

- 发布阶段复用 implementation 阶段已有的 `projectId`，不得重新创建发布项目
- 新版链路不再要求 zip 打包；禁止把“先打包再上传”当成标准流程
- 发布测试环境时以 `projectId` 为唯一主标识
- 发布前必须先执行 `plugin_mcp_build_static_analysis`
- 发布前必须先执行 `plugin_mcp_build_security_scan`
- 只有静态分析和安全扫描都通过后，才能继续执行发布
- 服务端发布前会先执行一次 `compileCheck(projectId)`
- 发布的代码来源于 GitLab 最新提交；开发者必须先本地 commit / push
- 人工测试是必经环节，不得省略
- 测试不通过时必须明确回流到开发实现阶段
- 只有测试通过后才能推动上线申请
- 发起上线申请时优先调用 `plugin_mcp_release_launch_apply`
- 上线审核期间优先调用 `plugin_mcp_release_project_status_get` 查询项目状态
- 若需要撤回审核，优先调用 `plugin_mcp_release_launch_cancel`
- 不得伪造发布成功、测试通过或上线结论

## Standard Release Procedure

### Step 1: Verify Handoff Readiness

先确认来自 `plugin-implementation` 的移交物是否完整：

- `projectId`
- `workflowId`（若主流程已启用 workflow 编排）
- 对应的 `project-{projectId}` 工程目录
- `project-{projectId}/docs/` 下已归档的 `SRS.md`
- `project-{projectId}/docs/` 下已归档的 `PRD.md`
- `project-{projectId}/docs/` 下已归档的 `feasibility-analysis.md`
- `project-{projectId}/README.md` 已完善
- 插件代码
- `manifest.yml`
- `endpoints/*.yml`
- 编译结果
- 代码自审结论

若环境还有附加门禁结论，也一并复用：

- 静态分析结果
- 安全扫描结果

文档归档检查规则：

- `project-{projectId}/docs/` 中的 `SRS.md`、`PRD.md`、`feasibility-analysis.md` 必须与当前实现依据一致
- `README.md` 必须已补充用户原始需求背景和插件概括性功能方案
- 若上述文档未同步到 `project-{projectId}/docs/`，禁止进入发布

### Step 2: Ensure Git State Is Ready

发布前必须确认：

- 当前要发布的代码已经提交到 Git
- 若需推送远端，已完成 `git push`
- `projectId` 对应的 GitLab 仓库就是本次发布源
- 本次发布对应的研发文档更新也已提交到 Git

若代码只停留在本地工作区，禁止进入发布。

### Step 3: Run Release Gates

发布前必须先执行以下质量门禁：

- `plugin_mcp_build_static_analysis`
- `plugin_mcp_build_security_scan`

最小输入如下：

- `projectId`

可选补充：

- `workflowId`
- `requestId`
- `stage`
- `state`
- `operator`

执行规则如下：

1. 先执行 `plugin_mcp_build_static_analysis`
2. 再执行 `plugin_mcp_build_security_scan`
3. 两者结果都必须为 `passed`
4. 任一门禁失败，禁止进入发布，并回流到 `plugin-implementation`

### Step 4: Publish To Test Environment

优先使用以下 MCP 工具之一：

- `plugin_mcp_release_test_deploy`
- `plugin_mcp_deploy_plugin`

最小输入如下：

- `projectId`
- `targetEnv=test`

可选补充：

- `workflowId`
- `operator`
- `deployNotes`
- `testCompanyId`

说明：

- 进入本步骤前，`plugin_mcp_build_static_analysis` 与 `plugin_mcp_build_security_scan` 必须都已通过
- 若传入 `workflowId`，服务端会继续关联 release workflow 记录并在成功后推进流程状态
- 若未传入 `workflowId`，仍可基于 `projectId` 完成轻量发布，但不会关联 workflow 状态推进

发布语义如下：

1. 服务端根据 `projectId` 定位开发项目
2. 服务端先执行 `compileCheck(projectId)`
3. 编译通过后，服务端按 GitLab 主分支执行 `git pull`
4. 服务端完成插件同步并返回 `pluginId`

### Step 5: Query Status And Logs When Needed

若需要跟踪发布执行记录，可使用：

- `plugin_mcp_release_status_get`
- `plugin_mcp_release_log_query`

规则如下：

- `status/get` 与 `log/query` 依赖 `release/test/deploy` 返回的 `runId`
- 若只是确认插件是否已发布成功，优先使用 `plugin_mcp_get_plugin_info_by_project_id`
- 若要排查编译问题，优先使用 `plugin_mcp_get_compile_log`
- 若要排查运行问题，优先使用 `plugin_mcp_get_plugin_runtime_logs` 与 `plugin_mcp_get_plugin_lifecycle_events`

### Step 6: Coordinate Manual Testing

发布成功后，开发者需在测试环境人工验证插件功能。测试阶段至少应关注：

- 核心功能是否符合 PRD
- 关键流程是否可正常触发
- 页面交互和反馈是否正确
- 权限、边界、异常路径是否正常

### Step 7: Decide Next Action

- 若测试通过：进入上线申请
- 若测试不通过：回流到 `plugin-implementation`

### Step 8: Submit Or Cancel Launch Application

当人工测试通过后，按以下顺序处理上线申请：

1. 先调用 `plugin_mcp_release_project_status_get(projectId)` 确认当前 `audit_phase`
2. 若当前状态为 `1/2/4/9`，调用 `plugin_mcp_release_launch_apply`
3. 提交后继续使用 `plugin_mcp_release_project_status_get` 跟踪审核状态
4. 若需要撤回审核，且当前状态为 `3`，调用 `plugin_mcp_release_launch_cancel`

`audit_phase` 取值定义如下：

- `1`：研发阶段
- `2`：测试阶段
- `3`：发布审核中
- `4`：发布审核不通过
- `5`：已发布
- `6`：已删除
- `7`：发布插件代码
- `8`：发布代码成功
- `9`：发布代码失败

## Release Input Rules

发布时必须满足以下约束：

- `projectId` 是必填主键
- `targetEnv` 当前固定为 `test`
- 不得再传旧版源码包 / 打包产物作为发布主输入
- 不得假设服务端会读取未提交到 GitLab 的本地改动

## Result Rules

发布结果至少区分：

- `成功`
- `失败`
- `待确认`

若状态不是 `成功`，不得进入人工测试。

## Manual Test Rules

人工测试必须覆盖至少以下内容：

- 主流程是否可达成
- 用户入口是否可见且可触发
- 关键数据读写是否正确
- 成功、失败、空态、异常提示是否符合预期
- 权限控制是否符合要求

### Test Result Types

测试结论统一分为：

- `通过`
- `不通过`
- `待确认`

### Test Failure Handling

若测试结论为 `不通过`：

- 记录不通过原因
- 标记受影响功能点
- 回退到 `plugin-implementation`
- 待修复后重新走发布测试流程

## Launch Application Rules

只有当以下条件成立时，才可发起 `插件上线申请单`：

- 发布成功
- 静态分析通过
- 安全扫描通过
- 人工测试通过
- 无阻塞上线的问题

推荐调用规则：

- 先调用 `plugin_mcp_release_project_status_get`
- 若 `audit_phase in (1,2,4,9)`，再调用 `plugin_mcp_release_launch_apply`
- 若 `audit_phase = 3`，说明已处于审核中，应继续查询状态而不是重复提交
- 若 `audit_phase = 3` 且业务决定撤回，调用 `plugin_mcp_release_launch_cancel`

### Launch Application Output

上线申请至少应包含：

- 插件名称
- 插件版本
- `projectId`
- `pluginId`
- 测试结论
- 上线申请说明

## Human-In-The-Loop Rules

以下情况必须主动向用户确认：

- 是否已满足进入发布阶段的条件
- 最新 Git 提交是否已准备好发布
- 发布失败后是重试、排查还是回退
- 人工测试是否判定为通过
- 是否正式发起上线申请

## Tool Usage Rules

本阶段推荐的工具顺序如下：

1. `plugin_mcp_build_static_analysis`
2. `plugin_mcp_build_security_scan`
3. `plugin_mcp_compile_check` 或 `plugin_mcp_build_compile`
4. `plugin_mcp_release_test_deploy` 或 `plugin_mcp_deploy_plugin`
5. `plugin_mcp_release_status_get` / `plugin_mcp_release_log_query`（按需）
6. `plugin_mcp_release_project_status_get`
7. `plugin_mcp_release_launch_apply` / `plugin_mcp_release_launch_cancel`
8. `plugin_mcp_get_plugin_info_by_project_id`
9. `plugin_mcp_get_plugin_runtime_logs` / `plugin_mcp_get_plugin_lifecycle_events`（按需）

约束如下：

- 不得再调用 `plugin_mcp_release_projects_create`
- 不得再依赖 zip 打包上传
- 不得跳过 `plugin_mcp_build_static_analysis`
- 不得跳过 `plugin_mcp_build_security_scan`
- 不得伪造 `projectId`、`pluginId`、发布状态或测试状态

## Standard Output Types

本技能的标准输出物包括：

- `projectId`
- 测试环境发布结果
- `pluginId`
- 人工测试结论
- 项目状态查询结果
- `插件上线申请单`

### Suggested Output Template

建议使用如下模板汇总发布测试结果：

````markdown
# 发布测试结果

## 1. 发布输入
- projectId：{projectId}
- 发布来源：GitLab 最新提交
- 目标环境：test

## 2. 测试环境发布结果
- pluginId：{pluginId}
- 发布时间：{时间}
- 结果：{成功 / 失败 / 待确认}

## 3. 人工测试结论
- 测试结果：{通过 / 不通过 / 待确认}
- 问题摘要：{如有}

## 4. 下一步建议
- {申请上线 / 回退开发 / 重试发布 / 补充确认}
````

## Prohibited Actions

- 禁止没有 `projectId` 就发布测试环境
- 禁止把本地未提交代码当成可发布版本
- 禁止在静态分析未通过时继续发布
- 禁止在安全扫描未通过时继续发布
- 禁止跳过人工测试直接申请上线
- 禁止把发布失败或测试失败伪装成成功
- 禁止在编译未通过时继续发布

## Failure Handling

### When Release Fails

- 记录失败原因
- 区分是静态分析失败、安全扫描失败、编译失败、Git 拉取失败还是插件同步失败
- 不进入人工测试
- 视情况重试或回退到开发

### When Manual Testing Fails

- 记录问题项
- 回退到 `plugin-implementation`
- 待修复后重新进入发布测试

## Handoff To Next Stage

本技能在不同结论下，移交流向如下：

- `测试通过`：进入上线申请阶段
- `测试不通过`：回到 `plugin-implementation`
- `发布失败`：回到对应问题处理阶段

## First-Version Scope

当前版本 `release-and-test` 聚焦以下范围：

- 固化基于 `projectId` 的测试发布主流程
- 固化 `static-analysis -> security-scan -> compileCheck -> git pull -> deployPlugin` 语义
- 固化人工测试与上线申请前置条件
- 固化发布失败后的回流路径

后续版本再补充：

- 更细的人工测试检查清单
- 更细的上线申请模板
- 与远程 `xrxs-plugin-MCP` 的更多状态联动
