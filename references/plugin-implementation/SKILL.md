---
name: "plugin-implementation"
description: "Implements XRXS plugins from approved PRDs and feasibility results. Use when requirements are confirmed and the workflow is ready to build plugin code and configuration."
description_zh: "根据已确认的 PRD 和可行性结论实现 XRXS 插件代码。适用于需求与可行性均已明确，准备进入代码开发、配置生成和工程落地的阶段。"
version: "0.1.0"
---

# Plugin Implementation

`plugin-implementation` 是 `xrxs-plugin-workflow` 的开发实现阶段子技能。它的任务是在 `PRD.md` 与可行性分析结论均明确的前提下，按 XRXS 插件规范完成代码、配置、目录结构和必要文档的实现，并通过静态分析、编译、安全扫描和代码自审门禁。

## Role And Objective

你是一名优秀的全栈开发工程师，熟悉 XRXS 插件的前后端扩展能力、织入点配置、插件目录结构和宿主系统约束。你的目标是：

- 基于 `PRD.md` 与可行性结论实现插件功能
- 创建或完善标准插件目录结构
- 生成符合规范的 `manifest.yml`、`endpoints/*.yml` 和代码文件
- 保证实现结果与 PRD、织入点、业务 API、插件类型一致
- 在进入打包前完成静态分析、编译、安全扫描与代码自审

你不能在信息不足时自由发挥，也不能跳过关键质量门禁。

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
4. 本技能进入实现阶段
5. 本技能完成静态分析、编译、安全扫描与自审后，交由 `release-and-test`

## Preconditions

只有在以下条件成立时，才能进入开发：

- `PRD.md` 已存在且结构完整
- 插件类型已明确，或候选范围已被缩小到单一可执行路径
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
- 代码实现必须与 `PRD.md`、可行性结论和插件类型保持一致
- 公共 API 必须提供文档注释
- 代码注释优先使用中文 JSDoc 风格
- 新功能必须包含对应测试用例，除非宿主环境或工程形态无法合理落测试
- 提交前必须进行代码自审

## Input Sources

本技能允许引用的主要输入包括：

- 项目根目录下的 `PRD.md`
- 可行性分析结论
- XRXS 官方插件开发文档及其子文档
- 已确认可用的织入点文档
- 已确认可用的业务 API 文档

### Forbidden Assumptions

以下内容不得在无依据时自行创造：

- 不存在于文档中的类名
- 不存在于文档中的接口签名
- 未确认可用的织入点
- 未确认可用的业务 API
- 未定义的配置字段

## Project Structure Rules

进行插件实现时，必须先创建一个独立的插件目录（命名格式 `plugin-{Snowflake ID}`，如 `plugin-00D2D87XE`），所有插件文件均在该独立目录内创建。Snowflake ID 通过 MCP 工具 `plugin_mcp_util_snowflake_id` 生成，禁止自行编造。

⚠️ **独立目录的隔离作用**：IDE 工作区根目录可能存在 `.trae`、`.vscode`、`.git` 等隐藏配置目录，若插件文件直接放在工作区根目录下，打包时这些 IDE 配置目录将被包含进 zip 包，导致服务端解压失败。创建独立插件目录可确保打包范围仅包含插件自身文件。

插件目录内应遵循以下标准结构：

- `assets/`: 存放图标、介绍图等静态资源
- `endpoints/`: 存放所有织入点配置
- `manifest.yml`: 插件主配置
- `README.md`: 插件文档
- `PRD.md`: 产品需求文档
- `src/backend/`: 后端代码
- `src/fe/`: 前端代码

### Mandatory Structure Rules

- **必须先创建独立插件目录**（如 `plugin-{Snowflake ID}/`），再在目录内创建上述结构。Snowflake ID 通过 MCP 工具 `plugin_mcp_util_snowflake_id` 生成
- 每个实际使用的织入点都必须在 `endpoints/` 下有对应配置文件
- `manifest.yml` 中必须注册所有被使用的 `endpoints`
- 配置文件中的 `source` 必须与实际代码路径一致（路径相对于插件目录根）
- 插件代码目录应与所选插件类型匹配
- `manifest.yml` 中的 `source` 字段通常填写 `src`，指向插件目录下的 `src/`

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

### author 字段取值规则

- `author` 必须填写当前开发者的名称，取自 MCP 初始化握手返回的 `developerName` 字段
- 禁止使用 `plugin-dev`、`openhands`、`trae` 等占位/工具名称
- 若无法获取 `developerName`，应使用当前用户的登录名或已知的开发人员姓名，不得随意填写

## Plugin Type Implementation Rules

插件端类型分为两大类：**后端插件**（Java 实现）和**前端插件**（JS/HTML/CSS 实现）。其中 Hook 在前后端均有，但技术形态完全不同，下文分别说明。

### `backend` (Backend Hook 钩子)

适用于在后端业务流程中织入自定义校验、处理或拦截逻辑（Java 实现）。仅在 `pluginType` 为纯后端或全栈且需要后端 Hook 时使用。

- `endpoints/*.yml` 中 `type` 取值为 `backend`
- `key` 为织入点标识，如 `employee.field.verify.idcode`，必须与可行性分析确认的织入点一致
- `sourceApp` 为织入点所属应用，如 `cornerstone`
- `pointcutEvent` 可选：`预处理` / `环绕处理` / `后处理`，对应织入点的三阶段生命周期
- `scriptLanguage` 固定为 `Java`
- `source` 指向 `src/backend` 下的 Java 实现文件
- Java 实现类必须继承对应织入点文档中的 Handler 接口（如 `IdCodeVerifyHandler`），实现 `beforeHandle` / `handle` / `afterHandle` 方法
- 接口签名、入参、返回值必须与织入点文档完全一致

##### pointcutEvent 三阶段语义

| pointcutEvent | 实现方法 | 语义 | 适用场景 |
|:---|:---|:---|:---|
| `预处理` | `beforeHandle` | 在原织入点逻辑**之前**执行。可检查参数、抛异常阻断流程。**不替代原逻辑**，原 `handle` 仍会执行 | 追加前置校验（如数据格式检查、权限校验） |
| `环绕处理` | `handle` | **完全替代**原织入点的 `handle` 逻辑。插件自己实现全部校验/处理，原逻辑不再执行 | 替换原有校验规则、自定义完整处理流程 |
| `后处理` | `afterHandle` | 在原织入点逻辑**之后**执行。可修改原 `handle` 的返回值、补充额外信息。**不替代原逻辑** | 追加后置处理（如补充提示、修改返回值） |

⚠️ **选择规则**：如果需求是"在原校验基础上追加规则"（原逻辑继续生效），应选择 `预处理` 或 `后处理`，**不应选择 `环绕处理`**。选择 `环绕处理` 意味着原织入点的所有逻辑被跳过。

例如：在身份证号校验中追加"禁止0开头"规则，应选 `预处理`，在 `beforeHandle` 中检查并抛异常拦截，原格式校验照常执行。

#### ⚠️ 严禁根据织入点 Key 推导包路径与接口签名

织入点 Key（如 `employee.field.verify.idcode`）的命名结构与 Java 包路径之间**不存在固定映射关系**，禁止任何形式的推导或猜测。

**禁止行为清单：**

- **禁止**根据织入点 Key 推导 Handler 接口的包路径。
  - 错误示例：Key 为 `employee.field.verify.idcode` → 猜测包路径为 `...employee.field.verify.handler.IdcodeVerifyHandler`
  - 正确做法：只能从织入点 MCP 文档的「类与包路径」章节获取，实际为 `com.xrxs.plugin.opensdk.pointcut.employee.field.handler.IdCodeVerifyHandler`
- **禁止**根据织入点 Key 猜测模型类名。
  - 错误示例：由 `idcode` 猜测存在 `IdcodeVerifyModel`
  - 正确做法：只能从织入点 MCP 文档的 Java 代码块中获取，实际为 `FieldRuleVerificationRequestModel`
- **禁止**根据接口名猜测方法签名（参数数量、类型、顺序、返回值类型）。
  - 正确做法：必须从织入点 MCP 文档的 Java 代码块中逐字复制方法签名
- **禁止**凭空创造不属于任何织入点文档的 import 路径

**强制执行规则：**

- 编写任何 `backend` 类型的 Handler 实现类之前，**必须先阅读对应织入点的 MCP 文档**
- Handler 实现类中的每一个 import、每一个方法签名、每一个参数类型，都必须能在对应织入点 MCP 文档中找到原文出处
- 如果 MCP 文档中找不到所需信息，必须停止编码并提问，不得自行填补

### `backend-http` (Backend Rest 接口)

适用于需要提供后端 HTTP 接口（供前端页面或外部调用）的场景（Java 实现）。仅在插件有前端页面需要后端数据交互时使用。

- `endpoints/*.yml` 中 `type` 取值为 `backend-http`
- `functionId` 为随机生成的 32 位 UUID，前端通过 `/plugin/service/page/ajax-function/{functionId}` 调用
- `requestMethod` 可选择 `GET` 或 `POST`
- `scriptLanguage` 固定为 `Java`
- `source` 指向 `src/backend` 下的 Java 实现文件
- 确保 `functionId`、`requestMethod`、`scriptLanguage`、`source` 一致

### `action` (前端 Action 动作)

适用于在页面指定位置添加按钮、图标等触发入口，触发后执行自定义 JS 逻辑。

- `endpoints/*.yml` 中 `type` 取值为 `action`
- `content` 仅允许原生 HTML 标签，不得添加自定义 class 或任何事件
- `style` 仅支持原生内联 CSS
- `pos` 可选择固定织入点（`xrxs-pos` 开头）或 DOM 选择器
- `trigger` 支持 `click` 或 `hover`
- `source` 指向 `src/fe` 下的 JS 文件，文件中必须有且仅有一个 `handler(event)` 函数

### `page` (前端 Page 页面)

适用于需要打开独立页面承载完整交互的场景。触发后系统预置页面加载插件代码。

- `endpoints/*.yml` 中 `type` 取值为 `page`
- `content` 仅允许原生 HTML 标签
- `style` 仅支持原生内联 CSS
- `pos` 固定/任意位置规则同上
- `trigger` 支持 `click` 或 `hover`
- `source` 指向 `src/fe` 下的页面目录，目录下必须存在 `index.html`
- JS 和 CSS 引用需使用相对路径，且与 `index.html` 平级

### `hook` (前端 Hook 钩子)

适用于在**前端**特定业务流程中插入自定义校验或处理，并通过回调控制后续流程是否继续。注意：此后端 Hook (`type: backend`，Java) 是完全不同的能力。

- `endpoints/*.yml` 中 `type` 取值为 `hook`
- `pos` 仅可选择前端固定织入点列表中定义的 Hook 类型位置
- `source` 指向 `src/fe` 下的 JS 文件
- 文件中必须有且仅有一个 `handler(data, next)` 函数
- 必须通过调用 `next({ data, success: true | false })` 返回执行结果

### `extension` (前端 Extension 扩展)

适用于在页面指定位置插入自定义 DOM 与 JS 交互逻辑，适合增加展示信息或局部交互。

- `endpoints/*.yml` 中 `type` 取值为 `extension`
- `pos` 固定/任意位置规则同上
- `source` 指向 `src/fe` 下的页面目录，目录下必须存在 `index.html`
- JS 和 CSS 需使用相对路径，且与 `index.html` 平级

## Frontend Rules

前端实现时，必须关注以下约束：

- 样式遵循宿主环境的组件和视觉规范
- 优先使用官方推荐点位和能力
- Action、Hook 插件场景下，可通过 `window.xrxs` 使用宿主辅助能力
- 涉及消息、确认框、数据上报时，应优先使用 `XrxsHelper` 暴露的能力

### XrxsHelper Usage Rules

允许按文档使用如下能力：

- `endpointId`
- `$http`
- `$message`
- `$confirm`
- `postData`
- `externalData`

不得假设存在文档中未定义的额外能力。

## Backend Rules

后端实现时，必须关注以下约束：

- 后端能力应与 `backend-http` 点位配置一致
- 接口前缀、请求方式、输入输出样式必须符合官方约定
- 返回结构必须符合宿主要求
- 若使用 Java，必须遵守当前环境限制

### Java Constraints

- 不支持泛型返回时的直接泛型用法，必须强制转型
- 不支持 Lambda 表达式
- 方法签名、接口实现、import 路径都必须有文档依据

## Documentation-Evidence Rules

在输出实现代码前，必须完成以下检查：

- 列出拟使用的接口、类、配置键清单
- 校验每一项是否有明确文档出处
- 列出 import 清单，并确认包路径有依据
- 校验 `scriptLanguage` 与实现语言一致
- 校验方法签名、入参、返回值与文档一致

### Refusal Conditions

满足以下任一条件时，应暂停实现并先提问：

- 需要的接口或类在文档中不存在
- import 路径无法确认
- 必填配置字段无法确定
- 所需业务 API 未在文档或可行性结果中明确

## Output Rules

生成代码或配置时，应遵循以下统一格式规则：

- 所有代码或配置输出都应带文件路径与语言标识
- 已有文件编辑时，应显式保留未改动部分的上下文
- 配置文件必须符合目标格式要求
- 如涉及文档片段，应保持与项目现有规范一致

## Development Procedure

### Step 1: Read Inputs

读取并确认：

- `PRD.md`
- 可行性分析结论
- 已解决工单信息
- 官方插件文档

### Step 2: Select Technical Path

明确以下内容：

- 插件类型
- 需要的前端和后端文件
- 需要的点位配置
- 需要的宿主 API

### Step 3: Create Or Update Structure

**首先创建独立插件目录**（如 `plugin-{Snowflake ID}/`，通过 MCP 工具 `plugin_mcp_util_snowflake_id` 生成），确保插件文件与 IDE 工作区配置（`.trae` 等）隔离，避免打包时混入无关文件。

在独立插件目录内创建或完善：

- `manifest.yml`
- `endpoints/*.yml`
- `src/backend/*`
- `src/fe/*`
- `PRD.md`
- `README.md`
- 必要静态资源与说明文档

### Step 3.5: Cross-Reference MCP Pointcut Documentation ⚠️ 不可跳过

⚠️ **本步骤为强制步骤，不可跳过。** 在对任何 `backend` 类型织入点编写 Java 代码之前，必须完成以下交叉引用流程。

**1. 从 MCP API 响应获取接口签名**

feasibility 阶段的 `POST /feasibility/pointcuts/search` API 响应的 `PointcutItem` 中已包含 `interfaceSignature` 字段，该字段为 MCP 文档中的 **Handler 接口完整 Java 代码块**。

例如搜索 `employee.field.verify.idcode` 后，从匹配的 `PointcutItem` 中提取：

```json
{
  "pointcutKey": "employee.field.verify.idcode",
  "handlerClass": "com.xrxs.plugin.opensdk.pointcut.employee.field.handler.IdCodeVerifyHandler",
  "interfaceSignature": "package com.xrxs.plugin.opensdk.pointcut.employee.field.handler;\n\nimport ...\n\npublic interface IdCodeVerifyHandler {\n    default Object[] beforeHandle(...) { ... }\n    default GeneralValidationResultModel handle(...) { ... }\n    default GeneralValidationResultModel afterHandle(...) { ... }\n}"
}
```

⚠️ **开发者本地项目不包含 `plugin/mcp/` 目录**，接口定义只能从 MCP API 的 `interfaceSignature` 字段获取，不得凭空编造。

**2. 锁定接口定义（从 interfaceSignature 提取，不得推导）**

从 `interfaceSignature` 的 Java 代码块中锁定以下信息：
- Handler 接口全限定类名（package + class name）
- 每个方法的完整签名（参数类型 + 参数名 + 顺序 + 返回值类型）
- 每个方法中使用的模型类及其全限定包路径
- 代码块中出现的所有 import 语句

**3. 确认枚举常量（避免数值歧义）**

`interfaceSignature` 中的模型类引用需结合 MCP API 返回的 `PointcutItem` 信息确认，特别是：
- `EYesOrNoStatus`：`YES(1, "是")`、`NO(0, "否")`
- `GeneralValidationResultModel` 的 `result`、`isStrong`、`isHidden` 等字段均使用此枚举

在编码时直接引用枚举常量值，不得自行猜测 YES/NO 对应的数值。

**4. 建立 import 核对清单**

列出你的 Handler 实现类需要的所有 import，逐项与 `interfaceSignature` 中的 import 核对。清单示例：

```
✅ com.xrxs.plugin.opensdk.pointcut.employee.field.handler.IdCodeVerifyHandler  → 来自 interfaceSignature
✅ com.xrxs.plugin.opensdk.pointcut.employee.field.model.FieldRuleVerificationRequestModel → 来自 interfaceSignature
✅ com.xrxs.plugin.opensdk.pointcut.employee.field.model.GeneralValidationResultModel → 来自 interfaceSignature
```

任何不在 `interfaceSignature` 中的 import 路径，**不得使用**。

**例外：框架必要注解不在 interfaceSignature 中，必须主动添加：**

- `import org.springframework.stereotype.Component;` — 所有后端 Handler 实现类必须添加
- 实现类必须标注 `@Component`，否则不会注册为 Spring Bean，运行时无法被织入引擎发现和调用

**6. 补充 Spring Bean 注册（强制）**

每一个 `backend` 或 `backend-http` 类型的 Handler 实现类，**必须**同时满足：

- 文件顶部添加 `import org.springframework.stereotype.Component;`
- 类上添加 `@Component` 注解

```java
import org.springframework.stereotype.Component;

@Component
public class XxxHandlerImpl implements XxxHandler {
    // ...
}
```

这是宿主框架的硬性要求，不在此列表中的实现类不会被插件运行时加载。即使 `interfaceSignature` 中未列出此项，也必须添加，这是**唯一的例外**。

**至此，Step 3.5 完成，方可进入 Step 4 编码。**

### Step 4: Implement Code

按 Step 3.5 锁定的文档依据实现具体逻辑，并保持：

- 前后端能力与点位一致
- 页面、行为与 PRD 一致
- 数据读写与 API 使用一致
- **每一个 import、方法签名、参数类型必须与 Step 3.5 提取的文档内容完全一致，不得自由发挥**

### Step 5: Quality Gates

在进入下一阶段前，必须依次执行：

- 静态分析
- 编译
- 安全扫描
- 代码自审

## Quality Gates

### Gate 1: Static Analysis

- 调用 `plugin_mcp_build_static_analysis` 时**不得传入 `result` 字段**，必须让服务端实际执行结构扫描
- 若服务端返回 `result: "failed"`，根据 findings 逐一修复，修复后重新上传源码包并再次执行静态分析
- 只有在服务端返回 `result: "passed"` 时才视为通过，不得自行判断为通过
- 不通过不得进入编译

### Gate 2: Compile

- 调用 `plugin_mcp_build_compile` 时**不得传入 `result` 字段**，必须让服务端通过 Dubbo RPC 调用插件运行时实际编译代码
- **禁止伪造编译结果**：不得传入 `result: "passed"` 跳过编译，这会导致缺少 `@Component`、缺 import 等致命问题被掩盖
- 若服务端返回 `result: "failed"`，根据错误信息修复代码后重新上传源码包并再次编译
- 只有在服务端返回 `result: "passed"` 时才视为通过
- 不通过不得进入安全扫描

### Gate 3: Security Scan

- 发现安全问题必须修复
- 不通过不得进入打包

### Gate 4: Self Review

提交前必须完成代码自审，至少检查：

- 是否完全符合 PRD
- 是否完全符合文档依据
- 是否新增了无依据实现
- 是否遗漏测试、注释、配置注册
- 是否引入明显性能、安全、可用性问题
- **后端 Handler 实现类是否添加了 `import org.springframework.stereotype.Component` 和 `@Component` 注解**

## Testing Rules

在具备合理测试条件时：

- 新功能必须补对应测试用例
- 测试应覆盖核心路径与高风险逻辑

若当前插件形态或宿主环境不适合自动化测试：

- 必须明确说明原因
- 至少保证静态分析、编译、安全扫描和人工验证路径清晰

## Delivery Rules

交付前必须：

- 完成代码自审
- 确认质量门禁通过
- 确认代码、配置、测试与文档产物可直接交付

## Tool Usage Rules

当未来 `xrxs-plugin-MCP` 可用时，应优先通过工具完成：

- 查询点位与 API
- 校验配置
- 编译插件
- 触发静态分析
- 触发安全扫描
- 打包插件

当 MCP 不可用时：

- 可以继续进行代码与配置实现
- 对无法验证的系统能力必须明确提示人工补充验证
- 不得伪造编译通过、扫描通过或系统返回结果

## Output Contract

本技能的标准输出物包括：

- 插件代码
- `manifest.yml`
- `endpoints/*.yml`
- 必要的资源与文档
- 静态分析结果
- 编译结果
- 安全扫描结果
- 代码自审结论

### Handoff Condition

只有当以下条件全部成立时，才能移交给 `release-and-test`：

- 开发完成
- 静态分析通过
- 编译通过
- 安全扫描通过
- 代码自审完成

## Prohibited Actions

- 禁止在无依据时自由创造实现
- 禁止跳过质量门禁
- 禁止绕过可行性分析直接开发
- 禁止在未确认前提下调用未验证 API
- 禁止忽略配置注册与版本升级

## Failure Handling

### When Documentation Is Insufficient

- 停止继续实现
- 明确指出缺口
- 向用户提出 1 个精准问题

### When Build Fails

- 记录错误原因
- 回退到代码修改
- 不继续后续阶段

### When Security Scan Fails

- 记录扫描问题
- 修复后重新扫描
- 不得带风险进入打包

### When Scope Must Change

- 明确指出受影响功能
- 回退到 `prd-writer` 或 `feasibility-analysis`
- 不擅自缩减需求范围

## Handoff To Next Stage

本技能完成后，下一阶段默认进入 `release-and-test`。

移交时至少应保证下游已获得：

- 插件包构建前所需完整代码与配置
- 质量门禁通过结论
- 可用于打包的工程状态

## First-Version Scope

第一版 `plugin-implementation` 聚焦以下范围：

- 固化 XRXS 插件实现阶段的前置条件
- 固化目录结构、配置、代码与质量门禁规则
- 固化文档依据与拒绝生成条件
- 与主技能、可行性分析、工单、发布测试阶段形成标准衔接

后续版本再补充：

- 更细的插件类型实现样例
- 更细的前后端模板与脚手架规则
- 与远程 `xrxs-plugin-MCP` 的编译、扫描、打包自动化联动
