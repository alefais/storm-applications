package WordCount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The topology class.
 */
public class WordCount {

    private static final Logger LOG = LoggerFactory.getLogger(Spout.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            String alert =
                    "In order to correctly run WordCount app you need to pass the following arguments:\n" +
                    " file path\n split char\n parallelism degree\n";
            LOG.error(alert);
        } else {
            String file_path = args[0];
            String file_split = (args.length > 1) ? args[1] : " ";
            Integer parallelism_degree = (args.length > 2) ? new Integer(args[2]) : 1;
            //String mode = (args.length > 3) ?
            String topology_name = (args.length > 3) ? args[3] : "WordCount";

            // create the topology

            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout("spout", new Spout(file_path, file_split), 1);

            builder.setBolt("counter_bolt", new CounterBolt(), parallelism_degree)
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
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            cluster.killTopology(topology_name);
            cluster.shutdown();
        }
    }
}
