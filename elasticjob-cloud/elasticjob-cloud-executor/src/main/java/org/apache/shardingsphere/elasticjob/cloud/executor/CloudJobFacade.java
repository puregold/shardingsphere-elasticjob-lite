/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.JobProperties.JobPropertiesEnum;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.Source;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;

import java.util.Collection;

/**
 * Cloud job facade.
 */
@RequiredArgsConstructor
public final class CloudJobFacade implements JobFacade {
    
    private final ShardingContexts shardingContexts;
    
    private final JobTypeConfiguration jobConfig;
    
    private final JobEventBus jobEventBus;
    
    @Override
    public JobTypeConfiguration loadJobRootConfiguration(final boolean fromCache) {
        return jobConfig;
    }
    
    @Override
    public JobConfiguration loadJobConfiguration(final boolean fromCache) {
        return JobConfiguration.newBuilder(jobConfig.getCoreConfig().getJobName(), jobConfig.getCoreConfig().getShardingTotalCount())
                .cron(jobConfig.getCoreConfig().getCron()).shardingItemParameters(jobConfig.getCoreConfig().getShardingItemParameters()).jobParameter(jobConfig.getCoreConfig().getJobParameter())
                .failover(jobConfig.getCoreConfig().isFailover()).misfire(jobConfig.getCoreConfig().isMisfire()).description(jobConfig.getCoreConfig().getDescription())
                .jobExecutorServiceHandlerType(jobConfig.getCoreConfig().getJobProperties().get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER))
                .jobErrorHandlerType(jobConfig.getCoreConfig().getJobProperties().get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER)).build();
        
    }
    
    @Override
    public void checkJobExecutionEnvironment() {
    }
    
    @Override
    public void failoverIfNecessary() {
    }
    
    @Override
    public void registerJobBegin(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public ShardingContexts getShardingContexts() {
        return shardingContexts;
    }
    
    @Override
    public boolean misfireIfRunning(final Collection<Integer> shardingItems) {
        return false;
    }
    
    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
    }
    
    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return false;
    }
    
    @Override
    public boolean isNeedSharding() {
        return false;
    }
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void postJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        jobEventBus.post(jobExecutionEvent);
    }
    
    @Override
    public void postJobStatusTraceEvent(final String taskId, final State state, final String message) {
        TaskContext taskContext = TaskContext.from(taskId);
        jobEventBus.post(new JobStatusTraceEvent(taskContext.getMetaInfo().getJobName(), taskContext.getId(), taskContext.getSlaveId(), 
                Source.CLOUD_EXECUTOR, taskContext.getType().toString(), String.valueOf(taskContext.getMetaInfo().getShardingItems()), state, message));
    }
}
