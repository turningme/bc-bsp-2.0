
package com.chinamobile.bcbsp.examples.pagerank;

/**
 * PageRankDriver.java
 */
import com.chinamobile.bcbsp.BSPConfiguration;
import com.chinamobile.bcbsp.Constants;
import com.chinamobile.bcbsp.util.BSPJob;
import com.chinamobile.bcbsp.io.KeyValueBSPFileInputFormat;
import com.chinamobile.bcbsp.io.TextBSPFileOutputFormat;

import org.apache.hadoop.fs.Path;

/**
 * PageRankDriver.java This is used to drive the PageRank example.
 *
 *
 *
 */

public class PageRankDriver {

  /**
   * constructor
   */
  private PageRankDriver() {

  }

  /**
   *  The main function
   * @param args
   *        String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.out
          .println("Usage: <nSupersteps>  <FileInputPath>  <FileOutputPath>" +
                   " <SplitSize(MB)> <PartitionNum>" + "  <SendThreshold>" +
                   "  <SendCombineThreshold>  <MemDataPercent>" +
                   "  <Beta>  <HashBucketNum>  <MsgPackSize> <ConsumerNum>");
      System.exit(-1);
    }
    System.out.print("TEST!");
    /** Set the base configuration for the job */
    BSPConfiguration conf = new BSPConfiguration();
    BSPJob bsp = new BSPJob(conf, PageRankDriver.class);
    bsp.setJobName("PageRank");
    bsp.setNumSuperStep(Integer.parseInt(args[0]));
    bsp.setPartitionType(Constants.PARTITION_TYPE.HASH);
    bsp.setPriority(Constants.PRIORITY.NORMAL);
    /** FOR TEST THE FOLLOWING ONE IS MODIFIED */
    if (args.length > 3) {
      bsp.setSplitSize(Integer.valueOf(args[3]));
    }
    if (args.length > 4) {
      bsp.setNumPartition(Integer.parseInt(args[4]));
    }
    if (args.length > 5) {
      bsp.setSendThreshold(Integer.parseInt(args[5]));
    }
    if (args.length > 6) {
      bsp.setSendCombineThreshold(Integer.parseInt(args[6]));
    }
    if (args.length > 7) {
      bsp.setMemoryDataPercent(Float.parseFloat(args[7]));
    }
    if (args.length > 8) {
      bsp.setBeta(Float.parseFloat(args[8]));
    }
    if (args.length > 9) {
      bsp.setHashBucketNumber(Integer.parseInt(args[9]));
    }
    if (args.length > 10) {
      bsp.setMessagePackSize(Integer.parseInt(args[10]));
    }
    if (args.length > 11) {
      bsp.setMaxConsumerNum(Integer.parseInt(args[11]));
    }
    /** Set the BSP.class */
    bsp.setBspClass(PageRankBSP.class);



    /** Set the vertex and edge class. */
    bsp.setVertexClass(PRVertexLite.class);
    bsp.setEdgeClass(PREdgeLite.class);

    /** Set the InputFormat.class and OutputFormat.class */
    bsp.setInputFormatClass(KeyValueBSPFileInputFormat.class);
    bsp.setOutputFormatClass(TextBSPFileOutputFormat.class);

    /** Set the InputPath and OutputPath */
    KeyValueBSPFileInputFormat.addInputPath(bsp, new Path(args[1]));
    TextBSPFileOutputFormat.setOutputPath(bsp, new Path(args[2]));
    /** Register the aggregators and aggregate values.*/
    bsp.registerAggregator(PageRankBSP.ERROR_SUM, ErrorSumAggregator.class,
        ErrorAggregateValue.class);

    /** Set the combiner class.*/
    bsp.setCombiner(SumCombiner.class);
    bsp.setReceiveCombinerSetFlag(false);

    bsp.setMaxProducerNum(30);

    /** Set the graph data implementation version as disk version. */
    bsp.setGraphDataVersion(bsp.DISK_VERSION);
    /** Set the message queues implementation version as disk version.*/
    bsp.setMessageQueuesVersion(bsp.DISK_VERSION);

    bsp.setCommunicationOption(Constants.RPC_VERSION);


    bsp.waitForCompletion(true);

  }
}
