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

import com.chinamobile.bcbsp.BSPConfiguration;
import com.chinamobile.bcbsp.thirdPartyInterface.HDFS.BSPFileUtil;
import com.chinamobile.bcbsp.thirdPartyInterface.HDFS.impl.BSPFileUtilImpl;
import com.chinamobile.bcbsp.util.StaffAttemptID;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.hadoop.fs.FileUtil;

/**
 * StaffLog A simple logger to handle the staff-specific user logs.
 * @author
 * @version
 */
public class StaffLog {
  /**
   * The log in log4j,to write logs.
   */
  private static final Log LOG = LogFactory.getLog(StaffLog.class.getName());
  /**
   * Create the log directory.
   */
  private static final File LOG_DIR = new File(
      System.getProperty("bcbsp.log.dir"), "userlogs").getAbsoluteFile();
  
  static {
    if (!LOG_DIR.exists()) {
      LOG_DIR.mkdirs();
    }
  }
  /**
   * Get the staff log file
   * @param sid the staff id
   * @param filter log file name
   * @return the staff log file
   */
  public static File getStaffLogFile(StaffAttemptID sid, LogName filter) {
    return new File(new File(LOG_DIR, sid.toString()), filter.toString());
  }

  /**
   * The filter for userlogs.
   */
  public static enum LogName {
    /** Log on the stdout of the task. */
    STDOUT("stdout"),

    /** Log on the stderr of the task. */
    STDERR("stderr"),

    /** Log on the map-reduce system logs of the task. */
    SYSLOG("syslog"),

    /** The java profiler information. */
    PROFILE("profile.out"),

    /** Log the debug script's stdout. */
    DEBUGOUT("debugout");
    /**
     * The log prefix.
     */
    private String prefix;
    /**
     * Set the log prefix.
     * @param prefix the prefix
     */
    private LogName(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public String toString() {
      return prefix;
    }
  }
  /**
   * The staff log class.
   *
   */
  private static class StaffLogsPurgeFilter implements FileFilter {
    long purgeTimeStamp;
    /**
     * constructor.
     * @param purgeTimeStamp
     */
    StaffLogsPurgeFilter(long purgeTimeStamp) {
      this.purgeTimeStamp = purgeTimeStamp;
    }
    /**
     * Get the staff log file.
     */
    public boolean accept(File file) {
      LOG.debug("PurgeFilter - file: " + file + ", mtime: "
          + file.lastModified() + ", purge: " + purgeTimeStamp);
      return file.lastModified() < purgeTimeStamp;
    }
  }

  /**
   * Purge old user logs.
   * @throws IOException e
   */
  public static synchronized void cleanup(int logsRetainHours)
      throws IOException {
    // Purge logs of tasks on this tasktracker if their
    // mtime has exceeded "mapred.task.log.retain" hours
    long purgeTimeStamp = System.currentTimeMillis()
        - (logsRetainHours * 60L * 60 * 1000);
    File[] oldStaffLogs = LOG_DIR.listFiles(new StaffLogsPurgeFilter(
        purgeTimeStamp));
    if (oldStaffLogs != null) {
      for (int i = 0; i < oldStaffLogs.length; ++i) {
        // FileUtil.fullyDelete(oldStaffLogs[i]);
        BSPFileUtil bFU = new BSPFileUtilImpl();
        bFU.fullyDelete(oldStaffLogs[i]);
      }
    }
  }

  /**
   * Read a log file from start to end positions.
   *
   */
  static class Reader extends InputStream {
    /**
     * The remain bytes.
     */
    private long bytesRemaining;
    /**
     * A FileInputStream obtains input bytes from a file in a file system.
     */
    private FileInputStream file;

    /**
     * Read a log file from start to end positions. The offsets may be negative,
     * in which case they are relative to the end of the file. For example,
     * Reader(taskid, kind, 0, -1) is the entire file and Reader(sid, kind,
     * -4197, -1) is the last 4196 bytes.
     * @param sid
     *        the id of the task to read the log file for
     * @param kind
     *        the kind of log to read
     * @param start
     *        the offset to read from (negative is relative to tail)
     * @param end
     *        the offset to read upto (negative is relative to tail)
     * @throws IOException e
     */
    public Reader(StaffAttemptID sid, LogName kind, long start, long end)
        throws IOException {
      // find the right log file
      File filename = getStaffLogFile(sid, kind);
      // calculate the start and stop
      long size = filename.length();
      if (start < 0) {
        start += size + 1;
      }
      if (end < 0) {
        end += size + 1;
      }
      start = Math.max(0, Math.min(start, size));
      end = Math.max(0, Math.min(end, size));
      bytesRemaining = end - start;
      file = new FileInputStream(filename);
      // skip upto start
      long pos = 0;
      while (pos < start) {
        long result = file.skip(start - pos);
        if (result < 0) {
          bytesRemaining = 0;
          break;
        }
        pos += result;
      }
    }

    @Override
    public int read() throws IOException {
      int result = -1;
      if (bytesRemaining > 0) {
        bytesRemaining -= 1;
        result = file.read();
      }
      return result;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
      length = (int) Math.min(length, bytesRemaining);
      int bytes = file.read(buffer, offset, length);
      if (bytes > 0) {
        bytesRemaining -= bytes;
      }
      return bytes;
    }

    @Override
    public int available() throws IOException {
      return (int) Math.min(bytesRemaining, file.available());
    }

    @Override
    public void close() throws IOException {
      file.close();
    }
  }
  
  /**
   * The bash command.
   */
  private static final String bashCommand = "bash";
  private static final String tailCommand = "tail";
  
  /**
   * Get the desired maximum length of staff's logs.
   * @param conf
   *        the job to look in
   * @return the number of bytes to cap the log files at
   */
  public static long getStaffLogLength(BSPConfiguration conf) {
    return conf.getLong("mapred.userlog.limit.kb", 100) * 1024;
  }
  
  /**
   * Wrap a command in a shell to capture stdout and stderr to files. If the
   * tailLength is 0, the entire output will be saved.
   * @param cmd
   *        The command and the arguments that should be run
   * @param stdoutFilename
   *        The filename that stdout should be saved to
   * @param stderrFilename
   *        The filename that stderr should be saved to
   * @param tailLength
   *        The length of the tail to be saved.
   * @return the modified command that should be run
   * @throws IOException e
   */
  public static List<String> captureOutAndError(List<String> cmd,
      File stdoutFilename, File stderrFilename, long tailLength)
      throws IOException {
    return captureOutAndError(null, cmd, stdoutFilename, stderrFilename,
        tailLength);
  }

  /**
   * Wrap a command in a shell to capture stdout and stderr to files. Setup
   * commands such as setting memory limit can be passed which will be executed
   * before exec. If the tailLength is 0, the entire output will be saved.
   * @param setup
   *        The setup commands for the execed process.
   * @param cmd
   *        The command and the arguments that should be run
   * @param stdoutFilename
   *        The filename that stdout should be saved to
   * @param stderrFilename
   *        The filename that stderr should be saved to
   * @param tailLength
   *        The length of the tail to be saved.
   * @return the modified command that should be run
   * @throws IOException e
   */
  public static List<String> captureOutAndError(List<String> setup,
      List<String> cmd, File stdoutFilename, File stderrFilename,
      long tailLength) throws IOException {
    // String stdout = FileUtil.makeShellPath(stdoutFilename);
    // String stderr = FileUtil.makeShellPath(stderrFilename);
    BSPFileUtil bFU = new BSPFileUtilImpl();
    String stdout = bFU.makeShellPath(stdoutFilename);
    String stderr = bFU.makeShellPath(stderrFilename);
    List<String> result = new ArrayList<String>(3);
    result.add(bashCommand);
    result.add("-c");
    StringBuffer mergedCmd = new StringBuffer();
    if (setup != null && setup.size() > 0) {
      mergedCmd.append(addCommand(setup, false));
      mergedCmd.append(";");
    }
    if (tailLength > 0) {
      mergedCmd.append("(");
    } else {
      mergedCmd.append("exec ");
    }
    mergedCmd.append(addCommand(cmd, true));
    mergedCmd.append(" < /dev/null ");
    if (tailLength > 0) {
      mergedCmd.append(" | ");
      mergedCmd.append(tailCommand);
      mergedCmd.append(" -c ");
      mergedCmd.append(tailLength);
      mergedCmd.append(" >> ");
      mergedCmd.append(stdout);
      mergedCmd.append(" ; exit $PIPESTATUS ) 2>&1 | ");
      mergedCmd.append(tailCommand);
      mergedCmd.append(" -c ");
      mergedCmd.append(tailLength);
      mergedCmd.append(" >> ");
      mergedCmd.append(stderr);
      mergedCmd.append(" ; exit $PIPESTATUS");
    } else {
      mergedCmd.append(" 1>> ");
      mergedCmd.append(stdout);
      mergedCmd.append(" 2>> ");
      mergedCmd.append(stderr);
    }
    result.add(mergedCmd.toString());
    return result;
  }

  /**
   * Add quotes to each of the command strings and return as a single string.
   * @param cmd
   *        The command to be quoted
   * @param isExecutable
   *        makes shell path if the first argument is executable
   * @return returns The quoted string.
   * @throws IOException e
   */
  public static String addCommand(List<String> cmd, boolean isExecutable)
      throws IOException {
    StringBuffer command = new StringBuffer();
    for (String s : cmd) {
      command.append('\'');
      if (isExecutable) {
        // the executable name needs to be expressed as a shell path for
        // the
        // shell to find it.
        // command.append(FileUtil.makeShellPath(new File(s)));
        BSPFileUtil bFU = new BSPFileUtilImpl();
        command.append(bFU.makeShellPath(new File(s)));
        isExecutable = false;
      } else {
        command.append(s);
      }
      command.append('\'');
      command.append(" ");
    }
    return command.toString();
  }

  /**
   * Wrap a command in a shell to capture debug script's stdout and stderr to
   * debugout.
   * @param cmd
   *        The command and the arguments that should be run
   * @param debugoutFilename
   *        The filename that stdout and stderr should be saved to.
   * @return the modified command that should be run
   * @throws IOException e
   */
  public static List<String> captureDebugOut(List<String> cmd,
      File debugoutFilename) throws IOException {
    // String debugout = FileUtil.makeShellPath(debugoutFilename);
    BSPFileUtil bFU = new BSPFileUtilImpl();
    String debugout = bFU.makeShellPath(debugoutFilename);
    List<String> result = new ArrayList<String>(3);
    result.add(bashCommand);
    result.add("-c");
    StringBuffer mergedCmd = new StringBuffer();
    mergedCmd.append("exec ");
    boolean isExecutable = true;
    for (String s : cmd) {
      if (isExecutable) {
        // the executable name needs to be expressed as a shell path for
        // the
        // shell to find it.
        // mergedCmd.append(FileUtil.makeShellPath(new File(s)));
        mergedCmd.append(bFU.makeShellPath(new File(s)));
        isExecutable = false;
      } else {
        mergedCmd.append(s);
      }
      mergedCmd.append(" ");
    }
    mergedCmd.append(" < /dev/null ");
    mergedCmd.append(" >");
    mergedCmd.append(debugout);
    mergedCmd.append(" 2>&1 ");
    result.add(mergedCmd.toString());
    return result;
  }
  
} // StaffLog
