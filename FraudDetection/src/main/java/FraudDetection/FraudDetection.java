package FraudDetection;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The topology class.
 */
public class FraudDetection {

    private static final Logger LOG = LoggerFactory.getLogger(SpoutParser.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            String alert =
                    "In order to correctly run FraudDetection app you need to pass the following arguments:\n" +
                    " file path\n" +
                    "Optional arguments:\n parallelism degree (default 1)\n topology name (default FraudDetection)\n" +
                    "NOTE: default execution mode is local (LocalCluster)";
            LOG.error(alert);
        } else {
            String file_path = args[0];
            Integer parallelism_degree = (args.length > 2) ? new Integer(args[2]) : 1;
            String topology_name = (args.length > 3) ? args[3] : "FraudDetection";

            // create the topology

            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout("spout", new SpoutParser(file_path, ","), 1);

            builder.setBolt("counter_bolt", new FraudPredictorBolt(), parallelism_degree)
                    .shuffleGrouping("spout");

            builder.setBolt("sink", new Sink(), parallelism_degree)
                    .shuffleGrouping("counter_bolt");

            // prepare the configuration
            Config conf = new Config();
            conf.setDebug(false);
            conf.setNumWorkers(1);

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(topology_name, conf, builder.createTopology());

            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cluster.killTopology(topology_name);
            cluster.shutdown();
        }
    }
}
