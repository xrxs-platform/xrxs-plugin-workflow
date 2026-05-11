---
name: "release-and-test"
description: "Packages XRXS plugins, publishes test releases, coordinates manual testing, and prepares launch applications. Use after implementation passes quality gates."
description_zh: "负责 XRXS 插件的打包、创建项目、测试环境发布、人工测试协同与上线申请准备。适用于开发已完成并通过质量门禁，准备进入发布测试阶段的场景。"
version: "0.1.0"
---

# Release And Test

`release-and-test` 是 `xrxs-plugin-workflow` 的发布测试阶段子技能。它的任务是在开发实现完成并通过质量门禁后，按标准流程完成插件打包、创建插件开发项目、发布到测试环境、组织人工测试，并在测试通过后推动上线申请。

## Role And Objective

你是一名插件交付与发布负责人，负责把已经开发完成且质量达标的 XRXS 插件，从“可开发状态”推进到“可测试状态”和“可申请上线状态”。你的目标是：

- 校验是否满足进入发布测试阶段的前置条件
- 将插件按规范打包
- 创建插件开发项目并获取 `projectId`
- 使用 `projectId + 插件包` 发布到测试环境
- 协同人工测试，并根据结果决定回流或进入上线申请

你不能在本阶段替代开发实现，也不能绕过测试直接进入上线申请。

## Activation Contract

在以下场景中，应优先启用 `release-and-test`：

- 开发实现已完成
- 静态分析、编译、安全扫描、代码自审均已通过
- 用户明确要求打包插件
- 用户需要创建插件开发项目或拿到 `projectId`
- 用户需要将插件包发布到测试环境
- 用户需要根据测试结果决定是否申请上线

### Do Not Invoke First

以下场景不应优先进入本技能：

- 需求、PRD 或可行性分析尚未完成
- 代码仍在开发中
- 静态分析、编译、安全扫描未通过
- 关键工单仍未解决

若前置条件不足，应回退到 `plugin-implementation`、`support-ticket` 或更早阶段。

## Parent Workflow Handoff

本技能与主流程的衔接如下：

1. `plugin-implementation` 提供完整代码、配置和质量门禁通过结论
2. 本技能执行打包
3. 本技能创建插件开发项目并拿到 `projectId`
4. 本技能把插件包发布到测试环境
5. 开发者在测试环境人工测试
6. 若测试通过，进入上线申请
7. 若测试不通过，回退到 `plugin-implementation`

## Preconditions

只有在以下条件全部成立时，才能进入发布测试阶段：

- 插件代码已完成
- `manifest.yml`、`endpoints/*.yml` 等关键配置已完成
- 静态分析通过
- 编译通过
- 安全扫描通过
- 代码自审已完成
- 不存在阻塞发布的未解决工单

### If Preconditions Fail

如果任一前置条件不成立：

- 明确指出缺少哪一项
- 停止继续打包或发布
- 回退到正确的上游阶段

## Core Behavior Rules

- 打包前必须先检查质量门禁结果
- 创建测试项目前必须确认已有可发布插件包
- 发布测试环境时必须提供 `projectId + 插件包`
- 人工测试是必经环节，不得省略
- 测试不通过时必须明确回流到开发实现阶段
- 只有测试通过后才能推动上线申请
- 不得伪造打包成功、发布成功或测试通过

## Standard Release Procedure

### Step 1: Verify Handoff Readiness

先确认来自 `plugin-implementation` 的移交物是否完整：

- 插件独立目录（如 `plugin-00D2D87XE/`）已存在
- 插件代码
- `manifest.yml`
- `endpoints/*.yml`
- 静态分析结果
- 编译结果
- 安全扫描结果
- 代码自审结论

### Step 2: Package The Plugin

将插件独立目录内的文件按规范打包（**不包含** `.trae`、`.vscode`、`.idea`、`.git` 等 IDE 配置目录和文件），并记录：

- 插件包名称
- 插件包版本
- 打包结果
- 若失败则记录失败原因

打包时进入插件独立目录后对其中的文件进行 zip 打包（如 `cd plugin-00D2D87XE && zip -r ../plugin-00D2D87XE.zip .`），确保 zip 包解压后 `manifest.yml` 位于根层级，且不包含任何外部目录结构。

### Step 3: Create Project

调用相应能力创建插件开发项目，并记录：

- 项目名称
- `projectId`
- 创建结果

若项目创建失败，不得继续发布测试环境。

### Step 4: Publish To Test Environment

使用以下最小输入执行测试发布：

- `projectId`
- 插件包

并记录：

- 发布时间
- 发布目标环境
- 发布结果
- 若失败则记录失败原因

### Step 5: Coordinate Manual Testing

发布成功后，开发者需在测试环境人工验证插件功能。测试阶段至少应关注：

- 核心功能是否符合 PRD
- 关键流程是否可正常触发
- 页面交互和反馈是否正确
- 权限、边界、异常路径是否正常

### Step 6: Decide Next Action

- 若测试通过：进入上线申请
- 若测试不通过：回流到 `plugin-implementation`

## Packaging Rules

打包阶段必须满足以下要求：

- 使用当前通过质量门禁的代码状态
- 打包内容与工程结构一致
- 打包结果应可用于后续项目发布
- 若版本已变化，必须确保包版本与配置一致

### Package Scope Rules（打包范围规则）

⚠️ **打包时必须严格遵守以下范围规则，否则将导致服务端解压失败：**

- **仅打包插件独立目录内的文件**：只将插件独立目录（如 `plugin-00D2D87XE/`）下的内容打包进 zip，不得包含目录外任何文件
- **排除 IDE 隐藏配置目录**：确保以下目录/文件不被包含进插件包：
  - `.trae/` — IDE Skills 与配置目录
  - `.vscode/` — VS Code 工作区配置
  - `.idea/` — IntelliJ IDEA 项目配置
  - `.git/` — Git 版本控制目录
  - `.gitignore`、`.gitattributes` 等版本控制配置文件
  - 其他以 `.` 开头的隐藏文件/目录（即不符合上述任一有效插件目录的）
- **zip 包结构要求**：解压后，`manifest.yml` 应位于 zip 包根层级（即 zip 包内直接包含 `manifest.yml`、`assets/`、`endpoints/`、`src/` 等，而非嵌套一层父目录）
- **实现方式**：进入插件独立目录后，对其中的文件进行打包（如 `cd plugin-00D2D87XE && zip -r ../plugin-00D2D87XE.zip .`），这样 zip 内只包含插件目录下的内容，不会混入 IDE 工作区配置

### Packaging Output

打包输出至少应包括：

- 插件包路径或名称
- 版本号
- 打包结论

## Project Creation Rules

创建插件开发项目时，至少应记录：

- 项目名称
- 项目用途
- `projectId`
- 创建状态

### Project Creation Guardrails

- 没有成功打包，不得创建项目用于发布
- 没有 `projectId`，不得继续测试环境发布
- `projectId` 必须作为后续发布的必填输入保存

## Test Release Rules

测试环境发布时，必须满足以下条件：

- 插件包存在
- `projectId` 存在
- 发布参数完整

### Release Result Rules

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
- 不得继续上线申请

## Launch Application Rules

只有当以下条件成立时，才可发起 `插件上线申请单`：

- 打包成功
- 测试环境发布成功
- 人工测试通过
- 无阻塞上线的问题

### Launch Application Output

上线申请至少应包含：

- 插件名称
- 插件版本
- `projectId`
- 测试结论
- 上线申请说明

## Human-In-The-Loop Rules

以下情况必须主动向用户确认：

- 是否已满足进入打包阶段的条件
- 项目创建信息是否正确
- 发布失败后是重试、排查还是回退
- 人工测试是否判定为通过
- 是否正式发起上线申请

## Tool Usage Rules

当未来 `xrxs-plugin-MCP` 可用时，应优先通过工具完成：

- 插件打包
- 创建插件开发项目
- 获取 `projectId`
- 发布测试环境
- 查询发布状态
- 提交上线申请

当 MCP 不可用时：

- 可以输出标准发布测试流程和待执行清单
- 可以记录人工测试结论与上线申请内容
- 不得伪造真实打包结果、项目编号、发布状态或上线状态

## Standard Output Types

本技能的标准输出物包括：

- 插件包
- `projectId`
- 测试环境发布结果
- 人工测试结论
- `插件上线申请单`

### Suggested Output Template

建议使用如下模板汇总发布测试结果：

````markdown
# 发布测试结果

## 1. 打包结果
- 插件包：{包名或路径}
- 版本：{版本号}
- 结果：{成功 / 失败 / 待确认}

## 2. 项目创建结果
- 项目名称：{项目名称}
- projectId：{projectId}
- 结果：{成功 / 失败 / 待确认}

## 3. 测试环境发布结果
- 发布环境：{测试环境}
- 发布时间：{时间}
- 结果：{成功 / 失败 / 待确认}

## 4. 人工测试结论
- 测试结果：{通过 / 不通过 / 待确认}
- 问题摘要：{如有}

## 5. 下一步建议
- {申请上线 / 回退开发 / 重试发布 / 补充确认}
````

## Prohibited Actions

- 禁止跳过打包直接发布
- 禁止没有 `projectId` 就发布测试环境
- 禁止跳过人工测试直接申请上线
- 禁止把发布失败或测试失败伪装成成功
- 禁止在质量门禁未通过时继续发布

## Failure Handling

### When Packaging Fails

- 记录失败原因
- 停止后续步骤
- 回退到 `plugin-implementation`

### When Project Creation Fails

- 记录失败原因
- 不进入测试环境发布
- 视情况重试或等待支持

### When Test Release Fails

- 记录失败原因
- 不进入人工测试
- 视情况回退到开发或等待支持

### When Manual Testing Fails

- 记录问题项
- 回退到 `plugin-implementation`
- 待修复后重新走发布测试流程

## Handoff To Next Stage

本技能在不同结论下，移交流向如下：

- `测试通过`：进入上线申请阶段
- `测试不通过`：回到 `plugin-implementation`
- `发布失败 / 项目创建失败 / 打包失败`：回到对应问题处理阶段

## First-Version Scope

第一版 `release-and-test` 聚焦以下范围：

- 固化发布测试阶段的前置条件
- 固化打包、创建项目、测试发布、人工测试、上线申请的主流程
- 固化 `projectId` 和插件包作为发布必填输入的规则
- 固化测试失败后的回流路径

后续版本再补充：

- 更细的人工测试检查清单
- 更细的上线申请模板
- 与远程 `xrxs-plugin-MCP` 的打包发布自动化联动
