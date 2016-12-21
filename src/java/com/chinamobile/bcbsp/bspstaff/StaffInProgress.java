/*
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

package com.chinamobile.bcbsp.bspstaff;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobStatus;

import com.chinamobile.bcbsp.bspcontroller.BSPController;
import com.chinamobile.bcbsp.bspcontroller.JobInProgress;
import com.chinamobile.bcbsp.client.BSPJobClient.RawSplit;
import com.chinamobile.bcbsp.util.BSPJobID;
import com.chinamobile.bcbsp.util.StaffAttemptID;
import com.chinamobile.bcbsp.util.StaffID;
import com.chinamobile.bcbsp.util.StaffStatus;
import com.chinamobile.bcbsp.workermanager.WorkerManagerStatus;

/**
 * StaffInProgress StaffInProgress maintains all the info needed for a Staff in
 * the lifetime of its owning Job.
 * @author
 * @version
 */
public class StaffInProgress {
  /**
   * The log in log4j,to write logs.
   */
  private static final Log LOG = LogFactory.getLog(StaffInProgress.class);

  // Constants
  /**
   * The Max task executables.
   */
  private static final int MAX_TASK_EXECS = 1;
  /**
   * The max staff attempts.
   */
  private int maxStaffAttempts = 4;
  /**
   * true: the staff is failed;false:the staff is normal.
   */
  private boolean failed = false;
  /**
   * 
   */
  private static final int NUM_ATTEMPTS_PER_RESTART = 1000;
  
  // Job Meta
  /**
   * BSP job file.
   */
  private String jobFile = null;
  /**
   * Partition number.
   */
  private int partition;
  /**
   * Staff id.
   */
  private StaffID id;
  /**
   * JobInProgress is the center for controlling the job running.
   */
  private JobInProgress job;
  /**
   * if the staff complete.
   */
  private int completes = 0;
  
  /** RawSplit info.
   * 
   */
  private RawSplit rawSplit;
  
  /**
   *  WorkerManagertatus info.
   */
  private WorkerManagerStatus wms = null;
  /**
   * start time .
   */
  private long startTime = 0;
  /**
   *  The 'next' usable staff ID of this tip.
   */
  private int nextStaffId = 0;
  /**
   *  The staff ID that took this TIP to SUCCESS.
   */
  private StaffAttemptID successfulStaffID;
  /**
   *  The first Staff ID of this tip.
   */
  private StaffAttemptID firstStaffID;
  /**
   *  Map from task Id -> GroomServer Id, contains tasks that are
   *  currently runnings.
   */
  private ConcurrentHashMap<StaffAttemptID, String> activeStaffs = new ConcurrentHashMap<StaffAttemptID, String>();
  // All attempt Ids of this TIP
  
  /** Map from taskId -> StaffStatus */
  /**
   * @param staffID
   *        Review comment: This TreeMap object is used in some critical
   *        sections, it may not be thread-safe Review time: 2011-11-30
   *        Reviewer: Hongxu Zhang Fix log: Truly the use of staffStatus in
   *        critical sections may not be thread-safe because TreeMap is not
   *        thread-safe, so we change activeStaffs to ConcurrentHashMap
   */
  private ConcurrentHashMap<StaffAttemptID, StaffStatus> staffStatuses = new ConcurrentHashMap<StaffAttemptID, StaffStatus>();
  /**
   * BSP job id.
   */
  private BSPJobID jobId;
  /**
   * BSP staff.
   */
  private Staff s = null;
  /**
   * BSP staff id.
   */
  private StaffAttemptID sid = null;
  /**
   * The current worker state.
   */
  private boolean changeWorkerState = false;
  
  /**
   * Constructor for new nexus between BSPController and WorkerManager.
   * @param jobId
   *        is identification of JobInProgress.
   * @param jobFile
   *        the path of job file
   * @param partition
   *        which partition this StaffInProgress owns.
   */
  public StaffInProgress(BSPJobID jobId, String jobFile, int partition) {
    this.jobId = jobId;
    this.jobFile = jobFile;
    this.partition = partition;
    
    this.id = new StaffID(jobId, partition);
  }

  /**
   * Constructor for new nexus between BSPController and WorkerManager.
   * @param jobId is identification of JobInProgress.
   * @param jobFile  the path of job file
   * @param master the master node
   * @param conf BSP job configuration
   * @param job the job in progress
   * @param partition which partition this StaffInProgress owns.
   * @param rawSplit
   */
  public StaffInProgress(BSPJobID jobId, String jobFile, BSPController master,
      Configuration conf, JobInProgress job, int partition, RawSplit rawSplit) {
    this.jobId = jobId;
    this.jobFile = jobFile;
    this.job = job;
    this.partition = partition;
    
    this.rawSplit = rawSplit;
    
    this.id = new StaffID(jobId, partition);
  }

  /**
   * Return a Staff that can be sent to a WorkerManager for execution.
   */
  public Staff getStaffToRun(WorkerManagerStatus status) throws IOException {
    this.wms = status;
    if (nextStaffId < (MAX_TASK_EXECS + maxStaffAttempts)) {
      int attemptId = job.getNumAttemptRecovery() * NUM_ATTEMPTS_PER_RESTART
          + nextStaffId;
      sid = new StaffAttemptID(id, attemptId);
      ++nextStaffId;
    } else {
      LOG.warn("Exceeded limit of " + (MAX_TASK_EXECS + maxStaffAttempts)
          + " attempts for the tip '" + getSIPId() + "'");
      return null;
    }

    this.s = new BSPStaff(jobId, jobFile, sid, partition,
        rawSplit.getClassName(), rawSplit.getBytes());
    activeStaffs.put(sid, status.getWorkerManagerName());

    return s;
  }

  /**
   * Get a staff to run BSP job.
   * @param status worker manager status
   * @param recovery if the staff is a recovery staff
   * @throws IOException
   */
  public void getStaffToRun(WorkerManagerStatus status, boolean recovery)
      throws IOException {
    this.wms = status;

    LOG.info("Recovery: getStaffToRun" + " " + recovery);
    activeStaffs.put(sid, status.getWorkerManagerName());
  }

  /**
   * Get the changeWorker State.
   * @return
   */
  public boolean getChangeWorkerState() {
    return changeWorkerState;
  }

  /**
   * Set the change worker state.
   * @param changeWorkerState
   */
  public void setChangeWorkerState(boolean changeWorkerState) {
    this.changeWorkerState = changeWorkerState;
  }

  /**
   * Return the start time.
   */
  public long getStartTime() {
    return startTime;
  }
  
  /**
   * Return the parent job.
   */
  public JobInProgress getJob() {
    return job;
  }

  /**
   * get the staff parent id.
   * @return staff parent id
   */
  public StaffID getSIPId() {
    return id;
  }

  /**
   * get the staff id.
   * @return the staff id
   */
  public StaffID getStaffId() {
    return this.id;
  }

  /**
   * Get new raw split.
   * @return
   */
  public RawSplit getRawSplit() {
    return this.rawSplit;
  }

  /**
   * Get the workermanager status
   * @return workermanager status
   */
  public WorkerManagerStatus getWorkerManagerStatus() {
    return this.wms;
  }

  /**
   * Get the current Staffs.
   * @return the current staffs.
   */
  public Map<StaffAttemptID, String> getStaffs() {
    return activeStaffs;
  }

  /**
   * Get BSP Staff.
   * @return Staff
   */
  public Staff getS() {
    return s;
  }

  public StaffAttemptID getStaffID() {
    return sid;
  }

  /**
   * Is the Staff associated with staffID the first attempt of the tip?
   * @param staffID
   * @return Returns true if the Staff is the first attempt of the tip
   */
  public boolean isFirstAttempt(StaffAttemptID staffID) {
    return firstStaffID == null ? false : firstStaffID.equals(staffID);
  }

  /**
   * Is this tip currently running any tasks?
   * @return true if any tasks are running
   */
  public boolean isRunning() {
    return !activeStaffs.isEmpty();
  }

  /**
   * Is this tip complete?
   * @return <code>true</code> if the tip is complete, else <code>false</code>
   */
  public synchronized boolean isComplete() {
    return (completes > 0);
  }

  /**
   * Is the given staffID the one that took this tip to completion?
   * @param staffID
   *        staffID of attempt to check for completion
   * @return <code>true</code> if staffID is complete, else <code>false</code>
   */
  public boolean isComplete(StaffAttemptID staffID) {
    return (completes > 0 && staffID.equals(getSuccessfulStaffid()));
  }

  /**
   * New staff tree set.
   */
  private TreeSet<StaffAttemptID> staffReportedClosed =
      new TreeSet<StaffAttemptID>();

  /**
   * Judge if the staff should close.
   * @param sid the staff id
   * @return true:the staff should close;false:the staff should not close
   */
  public boolean shouldCloseForClosedJob(StaffAttemptID sid) {
    StaffStatus ss = (StaffStatus) staffStatuses.get(sid);
    if ((ss != null) && (!staffReportedClosed.contains(sid))
        && (job.getStatus().getRunState() != JobStatus.RUNNING)) {
      staffReportedClosed.add(sid);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Judge if the staff complete.
   * @param staffID the staff id
   */
  public void completed(StaffAttemptID staffID) {
    LOG.info("Staff '" + staffID.getStaffID().toString() + "' has completed.");

    StaffStatus status = (StaffStatus) staffStatuses.get(staffID);
    status.setRunState(StaffStatus.State.SUCCEEDED);
    activeStaffs.remove(staffID);

    setSuccessfulStaffid(staffID);
    this.completes++;
  }

  /**
   * Terminate a staff.
   * @param staffID the staff id.
   */
  public void terminated(StaffAttemptID staffID) {
    LOG.info("Staff '" + staffID.getStaffID().toString() + "' has failed.");
 
    StaffStatus status = (StaffStatus) staffStatuses.get(staffID);
    status.setRunState(StaffStatus.State.FAILED);
    activeStaffs.remove(staffID);
  }

  /**
   * Set the successful staff id.
   * @param staffID staff id
   */
  private void setSuccessfulStaffid(StaffAttemptID staffID) {
    this.successfulStaffID = staffID;
  }

  /**
   * Get the successful staff id.
   * @return the successful staff id.
   */
  private StaffAttemptID getSuccessfulStaffid() {
    return successfulStaffID;
  }

  /**
   * Update staff status.
   * @param status the staff status.
   */
  public void updateStatus(StaffStatus status) {
    staffStatuses.put(status.getStaffId(), status);
  }

  /**
   * Get the staff status.
   * @param staffID the staff id
   * @return the staff status.
   */
  public StaffStatus getStaffStatus(StaffAttemptID staffID) {
    return this.staffStatuses.get(staffID);
  }

  /**
   * prepare to kill a staff.
   */
  public void kill() {
    this.failed = true;
  }

  /**
   * If the staff  is failed.
   * @return true: the staff is failed
   */
  public boolean isFailed() {
    return failed;
  }

  /* Zhicheng Liu added */
  /**
   * Get the staff to run for migrate.
   * @param status worker manager status
   * @param migrate true:the staff is a migrate staff
   * @throws IOException
   */
  public void getStaffToRunForMigrate(WorkerManagerStatus status,
      boolean migrate) throws IOException {
    LOG.info("The target worker to migrate into is "
        + status.getWorkerManagerName());

    this.wms = status;

    LOG.info("Migrate: getStaffToRun" + " " + migrate);
    activeStaffs.put(sid, status.getWorkerManagerName());
  }

  /**
   * For JUnit test.
   */
  public void setSid(StaffAttemptID sid) {
    this.sid = sid;
  }

}
