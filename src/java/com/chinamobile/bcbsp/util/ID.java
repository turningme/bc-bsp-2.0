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

package com.chinamobile.bcbsp.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * ID A general identifier, which internally stores the id as an integer. This
 * is the super class of {@link BSPJobID}, {@link StaffID} and
 * {@link StaffAttemptID}.
 */
public abstract class ID implements WritableComparable<ID> {
  /** Define a separator */
  protected static final char SEPARATOR = '_';
  /** Define a identifier */
  protected int id;

  /**
   * Constructor.
   */
  protected ID() {
  }
  
  /**
   * Constructor.
   *
   * @param id A identifier
   */
  public ID(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }

  @Override
  public int hashCode() {
    return Integer.valueOf(id).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o.getClass() == this.getClass()) {
      ID that = (ID) o;
      return this.id == that.id;
    } else {
      return false;
    }
  }

  /**
   * Compare two integer.
   *
   * @param that A identifier
   * @return 0 if equal, < 0 if this is less than that, etc.
   */
  public int compareTo(ID that) {
    return this.id - that.id;
  }

  /**
   * deserialize
   *
   * @param in Reads some bytes from an input.
   */
  public void readFields(DataInput in) throws IOException {
    this.id = in.readInt();
  }

  /** serialize
   * write this object to out.
   *
   * @param out Writes to the output stream.
   */
  public void write(DataOutput out) throws IOException {
    out.writeInt(id);
  }
}
