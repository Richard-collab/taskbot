"""
OpenClaw Skill: 外呼运营小助手 (Outbound Call Operations Assistant)

This skill provides tools for managing AI outbound call tasks, tenant lines,
supply lines, scripts, and task templates. Query operations read from local
JSON data files; action operations call the Baize outbound platform API.
"""

import json
import os
import httpx
from pathlib import Path
from typing import Optional

from openclaw.skills import skill, SkillContext

# ---------------------------------------------------------------------------
# Local data helpers
# ---------------------------------------------------------------------------

_DATA_DIR = Path(__file__).parent / "data"

_BAIZE_BASE_URL = os.getenv("BAIZE_BASE_URL", "http://localhost:8860/market")
_BAIZE_TOKEN = os.getenv("BAIZE_TOKEN", "")


def _load(filename: str) -> list:
    """Load a JSON array from a local data file."""
    path = _DATA_DIR / filename
    if not path.exists():
        return []
    with open(path, "r", encoding="utf-8") as fh:
        return json.load(fh)


def _fmt_task(t: dict) -> str:
    status_map = {
        "RUNNING": "运行中",
        "STOP": "已暂停",
        "FINISH": "已完成",
        "INIT": "待启动",
    }
    status = status_map.get(t.get("callStatus", ""), t.get("callStatus", "未知"))
    phone_num = t.get("phoneNum", 0)
    called = t.get("calledPhoneNum", 0)
    put_through = t.get("putThroughPhoneNum", 0)
    rate = f"{t.get('putThroughPhoneRate', 0) * 100:.1f}%" if t.get("putThroughPhoneRate") else "0.0%"
    return (
        f"【{t.get('taskName', 'N/A')}】(ID: {t.get('id')})\n"
        f"  账号: {t.get('account', 'N/A')}  商户: {t.get('tenantName', 'N/A')}  状态: {status}\n"
        f"  话术: {t.get('speechCraftName', 'N/A')}  线路: {t.get('lineName', 'N/A')}  并发: {t.get('concurrency', 0)}\n"
        f"  名单总量: {phone_num}  已呼: {called}  接通: {put_through}  接通率: {rate}\n"
        f"  创建时间: {t.get('createTime', 'N/A')}"
    )


def _fmt_line(line: dict) -> str:
    status = "启用" if line.get("enableStatus") == "ENABLE" else "停用"
    return (
        f"【{line.get('lineName', 'N/A')}】(编号: {line.get('lineNumber')}  ID: {line.get('id')})\n"
        f"  类型: {line.get('lineType', 'N/A')}  状态: {status}\n"
        f"  最大并发: {line.get('concurrentLimit', 0)}  剩余并发: {line.get('lineRemainConcurrent', 0)}\n"
        f"  行业: {', '.join(line.get('secondIndustries', []))}\n"
        f"  备注: {line.get('notes', '')}"
    )


def _fmt_supply_line(sl: dict) -> str:
    status = "启用" if sl.get("enableStatus") == "ENABLE" else "停用"
    return (
        f"【{sl.get('lineName', 'N/A')}】(编号: {sl.get('lineNumber')}  ID: {sl.get('id')})\n"
        f"  供应商: {sl.get('callLineSupplierName', 'N/A')}  状态: {status}\n"
        f"  最大并发: {sl.get('concurrentLimit', 0)}  单价: {sl.get('unitPrice', 0)}元/分钟\n"
        f"  主叫号码: {sl.get('masterCallNumber', 'N/A')}  号码类型: {', '.join(sl.get('phoneTypeList', []))}\n"
        f"  加密号码: {'是' if sl.get('isForEncryptionPhones') else '否'}  接入方式: {sl.get('lineAccessType', 'N/A')}\n"
        f"  行业: {', '.join(sl.get('secondIndustries', []))}"
    )


def _fmt_script(s: dict) -> str:
    status = "启用" if s.get("status") == "ENABLE" else "停用"
    return (
        f"【{s.get('scriptName', 'N/A')}】(ID: {s.get('id')}  版本: v{s.get('version', 1)})\n"
        f"  状态: {status}  行业: {s.get('primaryIndustry', 'N/A')}/{s.get('secondaryIndustry', 'N/A')}\n"
        f"  归属账号: {s.get('ownerAccount', 'N/A')}  最近使用: {s.get('lastUsingDate', 'N/A')}\n"
        f"  备注: {s.get('remark', '')}"
    )


def _fmt_template(tmpl: dict) -> str:
    status_map = {"0": "启用", "1": "停用"}
    status = status_map.get(str(tmpl.get("templateStatus", "0")), "未知")
    return (
        f"【{tmpl.get('templateName', 'N/A')}】(ID: {tmpl.get('id')})\n"
        f"  账号: {tmpl.get('account', 'N/A')}  状态: {status}\n"
        f"  话术: {tmpl.get('speechCraftName', 'N/A')}  任务名: {tmpl.get('taskName', 'N/A')}\n"
        f"  拨打时段: {tmpl.get('startWorkTimes', 'N/A')} - {tmpl.get('endWorkTimes', 'N/A')}\n"
        f"  自动补呼: {'是' if tmpl.get('autoReCall') == 1 else '否'}  隔日续呼: {'是' if tmpl.get('nextDayCall') == 1 else '否'}\n"
        f"  说明: {tmpl.get('comment', '')}"
    )


def _baize_post(path: str, body: dict) -> dict:
    """Send a POST request to the Baize API and return the parsed response."""
    url = _BAIZE_BASE_URL + path
    headers = {"token": _BAIZE_TOKEN, "Content-Type": "application/json"}
    with httpx.Client(timeout=30) as client:
        resp = client.post(url, json=body, headers=headers)
        resp.raise_for_status()
        return resp.json()


# ---------------------------------------------------------------------------
# Query skills — read from local JSON files
# ---------------------------------------------------------------------------


@skill(
    name="query_tasks",
    description=(
        "查询AI外呼任务列表。支持按账号、任务名称关键词、任务状态进行筛选。"
        "任务状态可选值：RUNNING（运行中）、STOP（已暂停）、FINISH（已完成）、INIT（待启动）。"
        "返回符合条件的任务列表，包含任务ID、名称、话术、线路、并发、名单及接通情况。"
    ),
    parameters={
        "account": {
            "type": "string",
            "description": "账号名称，不填则查询所有账号下的任务",
            "required": False,
        },
        "task_name_contain": {
            "type": "string",
            "description": "任务名称包含的关键词，不填则不过滤",
            "required": False,
        },
        "task_status": {
            "type": "string",
            "description": "任务状态（RUNNING / STOP / FINISH / INIT），不填则返回所有状态",
            "required": False,
        },
    },
)
async def query_tasks(
    ctx: SkillContext,
    account: Optional[str] = None,
    task_name_contain: Optional[str] = None,
    task_status: Optional[str] = None,
) -> str:
    tasks = _load("tasks.json")

    if account:
        tasks = [t for t in tasks if t.get("account", "").lower() == account.lower()]
    if task_name_contain:
        tasks = [t for t in tasks if task_name_contain in t.get("taskName", "")]
    if task_status:
        tasks = [t for t in tasks if t.get("callStatus", "").upper() == task_status.upper()]

    if not tasks:
        return "未找到符合条件的任务。"

    return f"共找到 {len(tasks)} 个任务：\n\n" + "\n\n".join(_fmt_task(t) for t in tasks)


@skill(
    name="get_tenant_lines",
    description=(
        "查询商户线路列表。可按启停状态筛选，返回线路ID、名称、类型、最大并发、剩余并发及适用行业等信息。"
    ),
    parameters={
        "enable_status": {
            "type": "string",
            "description": "线路状态：ENABLE（启用）或 DISABLE（停用），不填返回所有",
            "required": False,
        },
        "line_name_contain": {
            "type": "string",
            "description": "线路名称包含的关键词，不填则不过滤",
            "required": False,
        },
    },
)
async def get_tenant_lines(
    ctx: SkillContext,
    enable_status: Optional[str] = None,
    line_name_contain: Optional[str] = None,
) -> str:
    lines = _load("tenant_lines.json")

    if enable_status:
        lines = [ln for ln in lines if ln.get("enableStatus", "").upper() == enable_status.upper()]
    if line_name_contain:
        lines = [ln for ln in lines if line_name_contain in ln.get("lineName", "")]

    if not lines:
        return "未找到符合条件的商户线路。"

    return f"共找到 {len(lines)} 条商户线路：\n\n" + "\n\n".join(_fmt_line(ln) for ln in lines)


@skill(
    name="get_supply_lines",
    description=(
        "查询供应线路列表。可按线路名称关键词或启停状态筛选，"
        "返回线路编号、供应商、并发上限、单价、号码类型及适用行业等详细信息。"
    ),
    parameters={
        "enable_status": {
            "type": "string",
            "description": "线路状态：ENABLE（启用）或 DISABLE（停用），不填返回所有",
            "required": False,
        },
        "line_name_contain": {
            "type": "string",
            "description": "线路名称包含的关键词，不填则不过滤",
            "required": False,
        },
        "is_for_encryption": {
            "type": "boolean",
            "description": "是否只查加密号码线路，不填返回所有",
            "required": False,
        },
    },
)
async def get_supply_lines(
    ctx: SkillContext,
    enable_status: Optional[str] = None,
    line_name_contain: Optional[str] = None,
    is_for_encryption: Optional[bool] = None,
) -> str:
    lines = _load("supply_lines.json")

    if enable_status:
        lines = [sl for sl in lines if sl.get("enableStatus", "").upper() == enable_status.upper()]
    if line_name_contain:
        lines = [sl for sl in lines if line_name_contain in sl.get("lineName", "")]
    if is_for_encryption is not None:
        lines = [sl for sl in lines if sl.get("isForEncryptionPhones") == is_for_encryption]

    if not lines:
        return "未找到符合条件的供应线路。"

    return f"共找到 {len(lines)} 条供应线路：\n\n" + "\n\n".join(_fmt_supply_line(sl) for sl in lines)


@skill(
    name="get_scripts",
    description=(
        "查询话术（AI外呼脚本）列表。可按话术名称关键词或启停状态筛选，"
        "返回话术ID、名称、版本、所属行业、归属账号及使用情况等信息。"
    ),
    parameters={
        "script_status": {
            "type": "string",
            "description": "话术状态：ENABLE（启用）或 DISABLE（停用），不填返回所有",
            "required": False,
        },
        "script_name_contain": {
            "type": "string",
            "description": "话术名称包含的关键词，不填则不过滤",
            "required": False,
        },
        "account": {
            "type": "string",
            "description": "归属账号，不填则返回所有账号的话术",
            "required": False,
        },
    },
)
async def get_scripts(
    ctx: SkillContext,
    script_status: Optional[str] = None,
    script_name_contain: Optional[str] = None,
    account: Optional[str] = None,
) -> str:
    scripts = _load("scripts.json")

    if script_status:
        scripts = [s for s in scripts if s.get("status", "").upper() == script_status.upper()]
    if script_name_contain:
        scripts = [s for s in scripts if script_name_contain in s.get("scriptName", "")]
    if account:
        scripts = [s for s in scripts if s.get("ownerAccount", "").lower() == account.lower()]

    if not scripts:
        return "未找到符合条件的话术。"

    return f"共找到 {len(scripts)} 个话术：\n\n" + "\n\n".join(_fmt_script(s) for s in scripts)


@skill(
    name="get_task_templates",
    description=(
        "查询任务模板列表。可按模板名称关键词或账号筛选，"
        "返回模板ID、名称、关联话术、拨打时段及补呼设置等信息。"
    ),
    parameters={
        "template_name_contain": {
            "type": "string",
            "description": "模板名称包含的关键词，不填则不过滤",
            "required": False,
        },
        "account": {
            "type": "string",
            "description": "归属账号，不填则返回所有账号的模板",
            "required": False,
        },
    },
)
async def get_task_templates(
    ctx: SkillContext,
    template_name_contain: Optional[str] = None,
    account: Optional[str] = None,
) -> str:
    templates = _load("task_templates.json")

    if template_name_contain:
        templates = [t for t in templates if template_name_contain in t.get("templateName", "")]
    if account:
        templates = [t for t in templates if t.get("account", "").lower() == account.lower()]

    if not templates:
        return "未找到符合条件的任务模板。"

    return f"共找到 {len(templates)} 个任务模板：\n\n" + "\n\n".join(_fmt_template(t) for t in templates)


@skill(
    name="get_system_concurrency",
    description=(
        "查询当前系统外呼并发使用情况，统计各账号正在运行的任务并发总量及名单执行进度。"
        "数据来源于本地任务数据文件。"
    ),
    parameters={},
)
async def get_system_concurrency(ctx: SkillContext) -> str:
    tasks = _load("tasks.json")
    running = [t for t in tasks if t.get("callStatus") == "RUNNING"]

    if not running:
        return "当前没有正在运行的外呼任务。"

    total_concurrency = sum(t.get("concurrency", 0) for t in running)
    account_stats: dict = {}
    for t in running:
        acc = t.get("account", "unknown")
        if acc not in account_stats:
            account_stats[acc] = {"tasks": 0, "concurrency": 0}
        account_stats[acc]["tasks"] += 1
        account_stats[acc]["concurrency"] += t.get("concurrency", 0)

    lines = [f"系统当前运行任务数：{len(running)}  总并发：{total_concurrency}\n"]
    lines.append("各账号并发详情：")
    for acc, stats in account_stats.items():
        lines.append(f"  {acc}：{stats['tasks']} 个任务  并发 {stats['concurrency']}")

    return "\n".join(lines)


@skill(
    name="get_task_statistics",
    description=(
        "汇报外呼任务数据统计，包含任务名称、名单总量、已呼数量、接通数量及接通率。"
        "可按账号、任务名称关键词或任务状态进行筛选。"
    ),
    parameters={
        "account": {
            "type": "string",
            "description": "账号名称，不填则汇报所有账号",
            "required": False,
        },
        "task_name_contain": {
            "type": "string",
            "description": "任务名称包含的关键词，不填则不过滤",
            "required": False,
        },
        "task_status": {
            "type": "string",
            "description": "任务状态（RUNNING / STOP / FINISH），不填则返回所有",
            "required": False,
        },
    },
)
async def get_task_statistics(
    ctx: SkillContext,
    account: Optional[str] = None,
    task_name_contain: Optional[str] = None,
    task_status: Optional[str] = None,
) -> str:
    tasks = _load("tasks.json")

    if account:
        tasks = [t for t in tasks if t.get("account", "").lower() == account.lower()]
    if task_name_contain:
        tasks = [t for t in tasks if task_name_contain in t.get("taskName", "")]
    if task_status:
        tasks = [t for t in tasks if t.get("callStatus", "").upper() == task_status.upper()]

    if not tasks:
        return "未找到符合条件的任务。"

    total_phone = sum(t.get("phoneNum", 0) for t in tasks)
    total_called = sum(t.get("calledPhoneNum", 0) for t in tasks)
    total_put_through = sum(t.get("putThroughPhoneNum", 0) for t in tasks)
    overall_rate = (total_put_through / total_called * 100) if total_called > 0 else 0.0

    rows = [
        f"{'任务名称':<30} {'总量':>8} {'已呼':>8} {'接通':>8} {'接通率':>8}",
        "-" * 68,
    ]
    for t in tasks:
        called = t.get("calledPhoneNum", 0)
        put_through = t.get("putThroughPhoneNum", 0)
        rate = (put_through / called * 100) if called > 0 else 0.0
        rows.append(
            f"{t.get('taskName', 'N/A'):<30} {t.get('phoneNum', 0):>8} "
            f"{called:>8} {put_through:>8} {rate:>7.1f}%"
        )
    rows.append("-" * 68)
    rows.append(
        f"{'合计':<30} {total_phone:>8} {total_called:>8} "
        f"{total_put_through:>8} {overall_rate:>7.1f}%"
    )

    return "\n".join(rows)


# ---------------------------------------------------------------------------
# Action skills — call Baize outbound platform API
# ---------------------------------------------------------------------------


@skill(
    name="start_task",
    description=(
        "启动指定的AI外呼任务。需要提供任务ID列表和商户线路ID，可选择设置并发数或预计完成时间。"
        "启动前请先通过 query_tasks 确认任务ID，通过 get_tenant_lines 确认线路ID。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要启动的任务ID列表（数字数组）",
        },
        "tenant_line_id": {
            "type": "integer",
            "description": "用于外呼的商户线路ID",
        },
        "concurrency": {
            "type": "integer",
            "description": "并发数（手动模式时使用），不填则由系统自动分配",
            "required": False,
        },
        "expected_end_time": {
            "type": "string",
            "description": "预计完成时间（格式：yyyy-MM-dd HH:mm:ss），自动模式时使用",
            "required": False,
        },
        "include_auto_stop": {
            "type": "boolean",
            "description": "是否包含止损任务，默认 false",
            "required": False,
        },
    },
)
async def start_task(
    ctx: SkillContext,
    task_ids: list,
    tenant_line_id: int,
    concurrency: Optional[int] = None,
    expected_end_time: Optional[str] = None,
    include_auto_stop: bool = False,
) -> str:
    body: dict = {
        "taskIds": task_ids,
        "tenantLineId": tenant_line_id,
        "isIncludeAutoStop": include_auto_stop,
    }
    if concurrency is not None:
        body["concurrency"] = concurrency
    if expected_end_time:
        body["expectedEndTime"] = expected_end_time

    endpoint = "/AiSpeech/aiOutboundTask/startAiTask"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 已成功启动，线路ID：{tenant_line_id}。"
        return f"启动任务失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"启动任务时发生错误：{exc}"


@skill(
    name="stop_task",
    description=(
        "暂停指定的AI外呼任务。需要提供任务类型和任务ID列表。"
        "暂停后任务保留进度，可通过 resume_task 恢复。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要暂停的任务ID列表（数字数组）",
        },
        "task_type": {
            "type": "string",
            "description": "任务类型：OUTBOUND_AI（AI外呼）或 OUTBOUND_MANUAL（人工外呼），默认 OUTBOUND_AI",
            "required": False,
        },
    },
)
async def stop_task(
    ctx: SkillContext,
    task_ids: list,
    task_type: str = "OUTBOUND_AI",
) -> str:
    body = {"taskIds": task_ids, "taskType": task_type}
    endpoint = "/AiSpeech/aiOutboundTask/stopAiTask"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 已成功暂停。"
        return f"暂停任务失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"暂停任务时发生错误：{exc}"


@skill(
    name="resume_task",
    description=(
        "恢复已暂停的AI外呼任务，继续之前的外呼进度。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要恢复的任务ID列表（数字数组）",
        },
        "task_type": {
            "type": "string",
            "description": "任务类型：OUTBOUND_AI（AI外呼）或 OUTBOUND_MANUAL（人工外呼），默认 OUTBOUND_AI",
            "required": False,
        },
        "include_auto_stop": {
            "type": "boolean",
            "description": "是否包含止损任务，默认 false",
            "required": False,
        },
    },
)
async def resume_task(
    ctx: SkillContext,
    task_ids: list,
    task_type: str = "OUTBOUND_AI",
    include_auto_stop: bool = False,
) -> str:
    body = {
        "taskIds": task_ids,
        "taskType": task_type,
        "isIncludeAutoStop": include_auto_stop,
    }
    endpoint = "/AiSpeech/aiOutboundTask/resumeAiTask"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 已成功恢复运行。"
        return f"恢复任务失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"恢复任务时发生错误：{exc}"


@skill(
    name="change_concurrency",
    description=(
        "调整正在运行的外呼任务的并发数。可同时启动多个任务并设置统一并发。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要调整并发的任务ID列表",
        },
        "tenant_line_id": {
            "type": "integer",
            "description": "使用的商户线路ID",
        },
        "concurrency": {
            "type": "integer",
            "description": "新的并发数",
        },
        "include_auto_stop": {
            "type": "boolean",
            "description": "是否包含止损任务，默认 false",
            "required": False,
        },
    },
)
async def change_concurrency(
    ctx: SkillContext,
    task_ids: list,
    tenant_line_id: int,
    concurrency: int,
    include_auto_stop: bool = False,
) -> str:
    body = {
        "taskIds": task_ids,
        "tenantLineId": tenant_line_id,
        "concurrency": concurrency,
        "isIncludeAutoStop": include_auto_stop,
    }
    endpoint = "/AiSpeech/aiOutboundTask/editConcurrencyAndStartTask"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 并发已调整为 {concurrency}，线路ID：{tenant_line_id}。"
        return f"调整并发失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"调整并发时发生错误：{exc}"


@skill(
    name="change_tenant_line",
    description=(
        "切换外呼任务使用的商户线路，同时可调整并发数。"
        "切换前请通过 get_tenant_lines 确认目标线路ID。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要切换线路的任务ID列表",
        },
        "tenant_line_id": {
            "type": "integer",
            "description": "目标商户线路ID",
        },
        "concurrency": {
            "type": "integer",
            "description": "切换后的并发数",
            "required": False,
        },
        "include_auto_stop": {
            "type": "boolean",
            "description": "是否包含止损任务，默认 false",
            "required": False,
        },
    },
)
async def change_tenant_line(
    ctx: SkillContext,
    task_ids: list,
    tenant_line_id: int,
    concurrency: Optional[int] = None,
    include_auto_stop: bool = False,
) -> str:
    body: dict = {
        "taskIds": task_ids,
        "tenantLineId": tenant_line_id,
        "isIncludeAutoStop": include_auto_stop,
    }
    if concurrency is not None:
        body["concurrency"] = concurrency

    endpoint = "/AiSpeech/aiOutboundTask/editConcurrencyAndStartTask"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 已成功切换至线路ID：{tenant_line_id}。"
        return f"切换线路失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"切换线路时发生错误：{exc}"


@skill(
    name="forbid_district",
    description=(
        "为指定外呼任务设置地区屏蔽。支持屏蔽指定省份或城市，"
        "可选择屏蔽范围（全网 / 移动 / 联通 / 电信 / 虚拟运营商 / 未知运营商）。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要设置屏蔽的任务ID列表",
        },
        "provinces": {
            "type": "array",
            "items": {"type": "string"},
            "description": "要屏蔽的省份名称列表，如 [\"广东\", \"浙江\"]",
            "required": False,
        },
        "cities": {
            "type": "array",
            "items": {"type": "string"},
            "description": "要屏蔽的城市名称列表，如 [\"深圳\", \"广州\"]",
            "required": False,
        },
        "operator": {
            "type": "string",
            "description": (
                "运营商范围：ALL（全网）、YD（移动）、LT（联通）、DX（电信）、"
                "VIRTUAL（虚拟运营商）、UNKNOWN（未知），默认 ALL"
            ),
            "required": False,
        },
    },
)
async def forbid_district(
    ctx: SkillContext,
    task_ids: list,
    provinces: Optional[list] = None,
    cities: Optional[list] = None,
    operator: str = "ALL",
) -> str:
    body: dict = {
        "taskIds": task_ids,
        "changeType": "ADD",
        "operator": operator,
    }
    if provinces:
        body["restrictProvinces"] = provinces
    if cities:
        body["restrictCities"] = cities

    endpoint = "/AiSpeech/aiOutboundTask/changeRestrictArea"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            desc = []
            if provinces:
                desc.append(f"省份：{provinces}")
            if cities:
                desc.append(f"城市：{cities}")
            return f"已为任务 {task_ids} 设置屏蔽（{operator}）：{', '.join(desc)}。"
        return f"设置屏蔽失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"设置屏蔽时发生错误：{exc}"


@skill(
    name="allow_district",
    description=(
        "放开指定外呼任务的地区屏蔽，恢复对指定省份或城市的拨打。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要放开屏蔽的任务ID列表",
        },
        "provinces": {
            "type": "array",
            "items": {"type": "string"},
            "description": "要放开的省份名称列表",
            "required": False,
        },
        "cities": {
            "type": "array",
            "items": {"type": "string"},
            "description": "要放开的城市名称列表",
            "required": False,
        },
        "operator": {
            "type": "string",
            "description": "运营商范围：ALL / YD / LT / DX / VIRTUAL / UNKNOWN，默认 ALL",
            "required": False,
        },
    },
)
async def allow_district(
    ctx: SkillContext,
    task_ids: list,
    provinces: Optional[list] = None,
    cities: Optional[list] = None,
    operator: str = "ALL",
) -> str:
    body: dict = {
        "taskIds": task_ids,
        "changeType": "REMOVE",
        "operator": operator,
    }
    if provinces:
        body["restrictProvinces"] = provinces
    if cities:
        body["restrictCities"] = cities

    endpoint = "/AiSpeech/aiOutboundTask/changeRestrictArea"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            desc = []
            if provinces:
                desc.append(f"省份：{provinces}")
            if cities:
                desc.append(f"城市：{cities}")
            return f"已为任务 {task_ids} 放开屏蔽（{operator}）：{', '.join(desc)}。"
        return f"放开屏蔽失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"放开屏蔽时发生错误：{exc}"


@skill(
    name="create_main_account",
    description=(
        "新建主账号（运营账号）。需提供账号名、密码、显示名称及所属商户ID。"
        "仅限管理员操作。"
    ),
    parameters={
        "account": {
            "type": "string",
            "description": "新账号的登录名（英文字母和数字）",
        },
        "password": {
            "type": "string",
            "description": "账号初始密码",
        },
        "name": {
            "type": "string",
            "description": "账号显示名称（中文姓名或昵称）",
        },
        "tenant_id": {
            "type": "integer",
            "description": "所属商户ID",
        },
        "is_for_encryption": {
            "type": "boolean",
            "description": "是否为加密号码账号，默认 false",
            "required": False,
        },
    },
)
async def create_main_account(
    ctx: SkillContext,
    account: str,
    password: str,
    name: str,
    tenant_id: int,
    is_for_encryption: bool = False,
) -> str:
    body = {
        "account": account,
        "password": password,
        "name": name,
        "tenantId": tenant_id,
        "isForEncryptionPhones": is_for_encryption,
    }
    endpoint = "/AiSpeech/admin/addMainUser"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"主账号 [{account}]（{name}）已成功创建，所属商户ID：{tenant_id}。"
        return f"创建主账号失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"创建主账号时发生错误：{exc}"


@skill(
    name="create_sub_account",
    description=(
        "为当前主账号下新建子账号（操作员账号）。需提供登录名、密码、显示名称及角色ID。"
        "角色ID可通过查询角色列表获取。"
    ),
    parameters={
        "account": {
            "type": "string",
            "description": "子账号登录名",
        },
        "password": {
            "type": "string",
            "description": "子账号密码",
        },
        "name": {
            "type": "string",
            "description": "子账号显示名称",
        },
        "role_id": {
            "type": "integer",
            "description": "分配给该子账号的角色ID",
        },
    },
)
async def create_sub_account(
    ctx: SkillContext,
    account: str,
    password: str,
    name: str,
    role_id: int,
) -> str:
    body = {
        "account": account,
        "password": password,
        "password2": password,
        "name": name,
        "roleId": role_id,
    }
    endpoint = "/AiSpeech/admin/addSubUser"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"子账号 [{account}]（{name}）已成功创建，角色ID：{role_id}。"
        return f"创建子账号失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"创建子账号时发生错误：{exc}"


@skill(
    name="set_line_ratio",
    description=(
        "设置外呼任务的集线比（线路占用比例）。集线比越大，任务占用的线路资源越多。"
    ),
    parameters={
        "task_ids": {
            "type": "array",
            "items": {"type": "integer"},
            "description": "要设置集线比的任务ID列表",
        },
        "line_ratio": {
            "type": "number",
            "description": "集线比，如 1.0、1.2、1.5，通常范围 0.5 ~ 3.0",
        },
    },
)
async def set_line_ratio(
    ctx: SkillContext,
    task_ids: list,
    line_ratio: float,
) -> str:
    body = {"taskIds": task_ids, "lineRatio": line_ratio}
    endpoint = "/AiSpeech/aiOutboundTask/setLineRatio"
    try:
        result = _baize_post(endpoint, body)
        if str(result.get("code")) == "2000":
            return f"任务 {task_ids} 的集线比已设置为 {line_ratio}。"
        return f"设置集线比失败：{result.get('msg', '未知错误')}"
    except Exception as exc:
        return f"设置集线比时发生错误：{exc}"
