package net.linlan.dsh.domain.session.log.adapter.repository;

import net.linlan.dsh.domain.session.log.model.entity.SessionEventEntity;
import java.util.List;

/**
 * 定义会话事件日志的领域仓储端口（旧链路任务会话，对应 harness_session_event 表）。
 *
 * <p>调用流程：领域端口 → 基础设施适配器 → 外部系统。</p>
 * <p>示例：适配器只实现端口契约，业务规则保留在 domain。</p>
 *
 */
public interface ISessionEventRepository {

    /**
     * 追加一条事件记录。
     * <p>流程：校验入参 → 执行领域动作 → 返回处理结果。</p>
     * @param event 会话事件实体。
     */
    void append(SessionEventEntity event);

    /**
     * 查询某会话的全部事件记录。
     * <p>流程：校验入参 → 执行领域动作 → 返回处理结果。</p>
     * @param sessionId 会话 ID。
     * @return 按时间顺序排列的事件列表。
     */
    List<SessionEventEntity> queryBySessionId(String sessionId);
}
