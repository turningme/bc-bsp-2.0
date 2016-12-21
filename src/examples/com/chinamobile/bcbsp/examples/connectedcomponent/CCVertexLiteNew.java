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

package com.chinamobile.bcbsp.examples.connectedcomponent;

import com.chinamobile.bcbsp.api.Vertex;
import com.chinamobile.bcbsp.Constants;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PageRank vertexValue.
 */
public class CCVertexLiteNew extends Vertex<Integer, Integer, CCEdgeLiteNew> {
  /** Define LOG for outputting log information */
//  public static final Log LOG = LogFactory.getLog(CCVertexLiteNew.class);
  /** vertex ID */
  private int vertexID = 0;
  /** vertex value */
  private int vertexValue = 10;
  /** Edge list */
  private List<CCEdgeLiteNew> edgesList = new ArrayList<CCEdgeLiteNew>();

  @Override
  public void addEdge(CCEdgeLiteNew edge) {
    this.edgesList.add(edge);
  }

  @Override
  public void fromString(String vertexData) throws Exception {
    String[] buffer = new String[2];
    StringTokenizer str = new StringTokenizer(vertexData,
        Constants.KV_SPLIT_FLAG);
    if (str.hasMoreElements()) {
      buffer[0] = str.nextToken();
    } else {
      throw new Exception();
    }
    if (str.hasMoreElements()) {
      buffer[1] = str.nextToken();
    }
    str = new StringTokenizer(buffer[0], Constants.SPLIT_FLAG);
    if (str.countTokens() != 2) {
      throw new Exception();
    }
    this.vertexID = Integer.valueOf(str.nextToken());
    float tmp= Float.valueOf(str.nextToken());
    this.vertexValue = (int)tmp;
    
    if (buffer[1].length() > 0) { // There has edges.
      str = new StringTokenizer(buffer[1], Constants.SPACE_SPLIT_FLAG);
      while (str.hasMoreTokens()) {
        CCEdgeLiteNew edge = new CCEdgeLiteNew();
        edge.fromString(str.nextToken());
        this.edgesList.add(edge);
      }
    }
  }

  @Override
  public List<CCEdgeLiteNew> getAllEdges() {
    return this.edgesList;
  }

  @Override
  public int getEdgesNum() {
    return this.edgesList.size();
  }

  @Override
  public Integer getVertexID() {
    return this.vertexID;
  }

  @Override
  public Integer getVertexValue() {
    return this.vertexValue;
  }

  @Override
  public String intoString() {
    String buffer = vertexID + Constants.SPLIT_FLAG + vertexValue;
    buffer = buffer + Constants.KV_SPLIT_FLAG;
    int numEdges = edgesList.size();
    return buffer;
  }

  @Override
  public void removeEdge(CCEdgeLiteNew edge) {
    this.edgesList.remove(edge);
  }

  @Override
  public void setVertexID(Integer vertexID) {
    this.vertexID = vertexID;
  }

  @Override
  public void setVertexValue(Integer vertexValue) {
    this.vertexValue = vertexValue;
  }

  @Override
  public void updateEdge(CCEdgeLiteNew edge) {
    removeEdge(edge);
    this.edgesList.add(edge);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    this.vertexID = in.readInt();
    this.vertexValue = in.readInt();

  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(this.vertexID);
    out.writeInt(this.vertexValue);
  }
}
