package net.linlan.dsh.domain.session.log.model.entity;

import java.time.Instant;

/**
 * 保存一条追加写的会话事件实体（旧链路任务会话，对应 harness_session_event 表）。
 *
 * <p>调用流程：领域端口 → 基础设施适配器 → 外部系统。</p>
 * <p>示例：适配器只实现端口契约，业务规则保留在 domain。</p>
 *
 */
public record SessionEventEntity(
        String sessionId,
        String eventType,
        String payload,
        Instant occurredAt
) {
}
