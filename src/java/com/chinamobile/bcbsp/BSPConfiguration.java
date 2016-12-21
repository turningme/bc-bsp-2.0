/**
 * CopyRight by Chinamobile
 *
 * BSPConfiguration.java
 */

package com.chinamobile.bcbsp;

import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * BSPConfiguration The configuration information of the BC-BSP cluster. It is
 * generated according to the bcbsp-xml.
 *
 *
 *
 */
public class BSPConfiguration extends Configuration {

  /**
   * constructor
   */
  public BSPConfiguration() {
    super();
    addBSPResources();
  }

  /**
   * constructor
   * @param confFile
   *        file-path of resource to be added, the local filesystem is
   *         examined directly to find the resource,
   *         without referring to the classpath.

   */
  public BSPConfiguration(Path confFile) {
    super();
    this.addResource(confFile);
  }

  /**
   * Create a clone of passed configuration.
   *
   * @param c
   *        Configuration to clone.
   */
  public BSPConfiguration(final Configuration c) {
    this();
    for (Entry<String, String> e : c) {
      set(e.getKey(), e.getValue());
    }
  }

  /**
   * Adds the BC-BSP configuration file to a Configuration
   */
  private void addBSPResources() {
    Configuration.addDefaultResource("bcbsp-site.xml");
  }
}
