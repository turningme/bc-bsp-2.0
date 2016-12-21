/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chinamobile.bcbsp.action;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

/**
 * KillJobAction Represents a directive from the
 * {@link com.chinamobile.bcbsp.bsp.BSPController} to the
 * {@link com.chinamobile.bcbsp.bsp.WorkerManager} to kill the staff of a job
 * and cleanup resources.
 *
 *
 *
 */
class KillJobAction extends WorkerManagerAction {
  /** State jobID */
  private String jobId;

  /**
   *  constructor
   */
  public KillJobAction() {
    super(ActionType.KILL_JOB);
    jobId = new String();
  }

  /**
   *  constructor
   * @param killJobId
   *        the ID of job killed
   */
  public KillJobAction(String killJobId) {
    super(ActionType.KILL_JOB);
    this.jobId = killJobId;
  }

  public String getJobID() {
    return jobId;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    Text.writeString(out, jobId);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    jobId = Text.readString(in);
  }
}
