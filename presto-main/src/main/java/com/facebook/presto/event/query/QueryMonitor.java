package com.facebook.presto.event.query;

import com.facebook.presto.execution.FailureInfo;
import com.facebook.presto.execution.QueryInfo;
import com.facebook.presto.execution.StageInfo;
import com.google.inject.Inject;
import io.airlift.event.client.EventClient;
import io.airlift.json.JsonCodec;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryMonitor
{
    private final JsonCodec<StageInfo> stageInfoJsonCodec;
    private final JsonCodec<List<FailureInfo>> failureInfoJsonCodec;
    private final EventClient eventClient;

    @Inject
    public QueryMonitor(JsonCodec<StageInfo> stageInfoJsonCodec, JsonCodec<List<FailureInfo>> failureInfoJsonCodec, EventClient eventClient)
    {
        this.stageInfoJsonCodec = checkNotNull(stageInfoJsonCodec, "stageInfoJsonCodec is null");
        this.failureInfoJsonCodec = checkNotNull(failureInfoJsonCodec, "failureInfoJsonCodec is null");
        this.eventClient = checkNotNull(eventClient, "eventClient is null");
    }

    public void createdEvent(QueryInfo queryInfo)
    {
        eventClient.post(
                new QueryCreatedEvent(
                        queryInfo.getQueryId(),
                        queryInfo.getState(),
                        queryInfo.getSelf(),
                        queryInfo.getQuery(),
                        queryInfo.getQueryStats().getCreateTime()
                )
        );
    }

    public void completionEvent(QueryInfo queryInfo)
    {
        eventClient.post(
                new QueryCompletionEvent(
                        queryInfo.getQueryId(),
                        queryInfo.getState(),
                        queryInfo.getSelf(),
                        queryInfo.getFieldNames(),
                        queryInfo.getQuery(),
                        queryInfo.getQueryStats().getCreateTime(),
                        queryInfo.getQueryStats().getExecutionStartTime(),
                        queryInfo.getQueryStats().getEndTime(),
                        queryInfo.getQueryStats().getQueuedTime(),
                        queryInfo.getQueryStats().getAnalysisTime(),
                        queryInfo.getQueryStats().getDistributedPlanningTime(),
                        queryInfo.getQueryStats().getSplits(),
                        stageInfoJsonCodec.toJson(queryInfo.getOutputStage()),
                        failureInfoJsonCodec.toJson(queryInfo.getFailures())
                )
        );
    }
}
