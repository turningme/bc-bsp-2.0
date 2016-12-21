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
package com.chinamobile.bcbsp.bspcontroller;

import java.io.IOException;

/**
 * This is the class that schedules commands to WorkerManager(s)
 * @author
 * @version
 */
public interface Schedulable {
  /**
   * Schedule job to designated WorkerManager(s) immediately.
   * @param jip
   *        to be scheduled.
   * @throws IOException
   */
  void normalSchedule(JobInProgress jip) throws IOException;

  /**
   * Schedule fault job for recovery to designated WorkerManager(s) immediately.
   * @param jip
   *        to be scheduled
   * @throws IOException
   */
  void recoverySchedule(JobInProgress jip) throws IOException;
}
