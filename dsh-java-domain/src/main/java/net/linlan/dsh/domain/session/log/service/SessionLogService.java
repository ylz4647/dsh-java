package net.linlan.dsh.domain.session.log.service;

import net.linlan.dsh.domain.session.ISessionLogService;
import net.linlan.dsh.domain.session.log.adapter.repository.ISessionEventRepository;
import net.linlan.dsh.domain.session.log.model.entity.SessionEventEntity;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * 旧链路任务会话的事件日志服务（对应 harness_session_event 表）。
 * <p>
 * 执行流程：
 * 1. 领域服务收到记录请求；
 * 2. 转换为追加写事件（user.message / assistant.message / 状态审计事件）；
 * 3. 经仓储端口落库，供回放、查询与执行链读取。
 * <p>
 * 事件类型约定：user.message 与 assistant.message 为 UI 可见消息，
 * 其余为审计轨迹。读取方见 ConversationQueryService 与 HarnessExecutionService。
 *
 */
@Service
public class SessionLogService implements ISessionLogService {

    private final ISessionEventRepository sessionEventRepository;

    /**
     * 初始化会话日志服务。
     * <p>流程：注入仓储端口 → 追加写事件 → 供事件溯源回放。</p>
     */
    public SessionLogService(ISessionEventRepository sessionEventRepository) {
        this.sessionEventRepository = sessionEventRepository;
    }

    /**
     * 记录任务提交。
     * <p>流程：写入用户消息事件（供执行链提取 prompt）→ 写入任务与审批审计事件。</p>
     */
    @Override
    public void recordTaskSubmission(String sessionId, String taskId, String profileCode,
            String prompt, boolean approvalRequired, String approvalReason) {
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "user.message", prompt, Instant.now()));
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "task.submitted",
                "taskId=" + taskId + ",profile=" + profileCode, Instant.now()));
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, approvalRequired ? "approval.required" : "approval.auto",
                approvalReason, Instant.now()));
    }

    /**
     * 记录审批通过。
     * <p>流程：写入审批审计事件。</p>
     */
    @Override
    public void recordApprovalAccepted(String sessionId) {
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "approval.accepted", "approved from web console", Instant.now()));
    }

    /**
     * 记录执行开始。
     * <p>流程：写入执行审计事件（含解析后的模型）。</p>
     */
    @Override
    public void recordExecutionStarted(String sessionId, String resolvedModel) {
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "execution.started", "model=" + resolvedModel, Instant.now()));
    }

    /**
     * 记录执行完成。
     * <p>流程：写入模型回复消息事件（UI 可见）→ 写入完成审计事件。</p>
     */
    @Override
    public void recordExecutionCompleted(String sessionId, String modelCode, String assistantMessage) {
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "assistant.message", assistantMessage, Instant.now()));
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "execution.completed", "model=" + modelCode, Instant.now()));
    }

    /**
     * 记录执行失败。
     * <p>流程：写入失败原因消息事件（UI 可见）→ 写入失败审计事件。</p>
     */
    @Override
    public void recordExecutionFailed(String sessionId, String failureMessage) {
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "assistant.message", failureMessage, Instant.now()));
        sessionEventRepository.append(new SessionEventEntity(
                sessionId, "execution.failed", failureMessage, Instant.now()));
    }
}
