package FraudDetection;

import Constants.FraudDetectionConstants.*;
import Util.config.Configuration;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The topology entry class.
 * @author Alessandra Fais
 */
public class FraudDetection {

    private static final Logger LOG = LoggerFactory.getLogger(FileParserSpout.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            String alert =
                    "In order to correctly run FraudDetection app you need to pass the following arguments:\n" +
                    " file path\n" +
                    "Optional arguments:\n" +
                    " parallelism degree (default 1)\n" +
                    " topology name (default FraudDetection)\n" +
                    " execution mode (default local)";
            LOG.error(alert);
        } else {
            // parse command line arguments
            String file_path = args[0];
            Integer parallelism_degree = (args.length > 1) ? new Integer(args[1]) : 1;
            String topology_name = (args.length > 2) ? args[2] : "FraudDetection";
            String ex_mode = (args.length > 3) ? args[3] : "local";

            // prepare the topology
            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout("spout", new FileParserSpout(file_path, ","), 1);

            builder.setBolt("counter_bolt", new FraudPredictorBolt(), parallelism_degree)
                    .fieldsGrouping("spout", new Fields(Field.ENTITY_ID));

            builder.setBolt("sink", new ConsoleSink(), parallelism_degree)
                    .fieldsGrouping("counter_bolt", new Fields(Field.ENTITY_ID));

            // prepare the configuration
            Config conf = new Config();
            conf.setDebug(false);
            conf.setNumWorkers(1);
            try {
                // load configuration
                String cfg = "/frauddetection/fd.properties";
                Properties p = loadProperties(cfg);

                conf = Configuration.fromProperties(p);
                LOG.info("Loaded configuration file {}.", cfg);
            } catch (IOException e) {
                LOG.error("Unable to load configuration file.", e);
                throw new RuntimeException("Unable to load configuration file.", e);
            }

            // build the topology
            StormTopology topology = builder.createTopology();

            // run the topology
            try {
                if (ex_mode.equals("local"))
                    runTopologyLocally(topology, topology_name, conf, 120); // 2 minutes
                else if (ex_mode.equals("remote"))
                    runTopologyRemotely(topology, topology_name, conf);
            } catch (InterruptedException e) {
                LOG.error("Error in running topology locally.", e);
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
                LOG.error("Error in running topology remotely.", e);
            }
        }
    }

    /**
     * Run the topology locally.
     * @param topology the topology to be executed
     * @param topology_name the name of the topology
     * @param conf the configurations for the execution
     * @param runtime_seconds for how much time the topology will run
     * @throws InterruptedException
     */
    private static void runTopologyLocally(StormTopology topology, String topology_name, Config conf, int runtime_seconds)
            throws InterruptedException {

        LOG.info("[main] Starting Storm in local mode to run for {} seconds.", runtime_seconds);
        LocalCluster cluster = new LocalCluster();

        LOG.info("[main] Topology {} submitted.", topology_name);
        cluster.submitTopology(topology_name, conf, topology);
        Thread.sleep((long) runtime_seconds * 1000);

        cluster.killTopology(topology_name);
        LOG.info("[main] Topology {} finished.", topology_name);

        cluster.shutdown();
        LOG.info("[main] Local Storm cluster was shutdown.");
    }

    /**
     * Run the topology remotely.
     * @param topology the topology to be executed
     * @param topology_name the name of the topology
     * @param conf the configurations for the execution
     * @throws AlreadyAliveException
     * @throws InvalidTopologyException
     * @throws AuthorizationException
     */
    private static void runTopologyRemotely(StormTopology topology, String topology_name, Config conf)
            throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
        StormSubmitter.submitTopology(topology_name, conf, topology);
    }

    /**
     * Load configuration properties for the application.
     * @param filename the name of the properties file
     * @return the persistent set of properties loaded from the file
     * @throws IOException
     */
    private static Properties loadProperties(String filename) throws IOException {
        Properties properties = new Properties();
        InputStream is = FraudDetection.class.getResourceAsStream(filename);
        if (is != null) {
            properties.load(is);
            is.close();
        }
        LOG.info("[main] Properties loaded: {}.", properties.toString());
        return properties;
    }
}
