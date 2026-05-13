# XRXS Plugin Workflow

`xrxs-plugin-workflow` 是 XRXS 插件研发的总入口技能，按照标准研发流程推进从需求到上线的工作。

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

1. 将 `SKILL.md` 及 `references/`、`examples/` 目录放入你的 IDE 技能目录
2. Trae: Settings → Rule & Skills → Skills → Create
3. 其他 IDE 请参考各自文档

## 工作流

1. 需求澄清 → `requirements-translator`
2. PRD 生成 → `prd-writer`
3. 可行性分析 → `feasibility-analysis`
4. 缺失项工单 → `support-ticket`
5. 插件实现 → `plugin-implementation`
6. 质量门禁（静态分析 / 编译 / 安全扫描）
7. 打包与测试发布 → `release-and-test`
8. 上线申请

## 前置依赖

本技能需要配合 `@xrxs-plugin/plugin-mcp` MCP Server 使用，以调用远程 XRXS 平台 API。

MCP 配置示例：

```json
{
  "mcpServers": {
    "xrxs-plugin-mcp": {
      "command": "npx",
      "args": ["-y", "@xrxs-plugin/plugin-mcp@latest"],
      "env": {
        "MCP_BASE_URL": "https://your-server/jeecg-boot/api/v1/mcp",
        "MCP_DEV_KEY": "your-dev-key",
        "MCP_TIMEOUT_SECONDS": "30"
      }
    }
  }
}
```

## 目录结构

```text
xrxs-plugin-workflow/
├── SKILL.md                                   # 主入口技能
├── examples/
│   ├── PRD.md                                 # 示例 PRD
│   ├── 需求描述文档.md                         # 示例需求描述
│   └── plugin-ldljwpe35/                      # 示例插件工程
└── references/                                # 子技能
    ├── requirements-translator/SKILL.md
    ├── prd-writer/SKILL.md
    ├── feasibility-analysis/SKILL.md
    ├── support-ticket/SKILL.md
    ├── plugin-implementation/SKILL.md
    └── release-and-test/SKILL.md
```

## License

MIT
