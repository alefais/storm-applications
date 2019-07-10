package TrafficMonitoring;

import Constants.BaseConstants;
import Constants.TrafficMonitoringConstants;
import Constants.BaseConstants.*;
import Constants.TrafficMonitoringConstants.*;
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
 *  @author  Alessandra Fais
 *  @version July 2019
 *
 *  The topology entry class.
 */
public class TrafficMonitoring {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficMonitoring.class);

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(BaseConstants.HELP)) {
            String alert =
                    "In order to correctly run TrafficMonitoring app you can pass the following (optional) arguments:\n" +
                    "Optional arguments (default values are specified in tm.properties or defined as constants):\n" +
                    " city (accepted values {beijing, dublin})\n" +
                    " source parallelism degree\n" +
                    " map matching bolt parallelism degree\n" +
                    " speed calculator bolt parallelism degree\n" +
                    " sink parallelism degree\n" +
                    " source generation rate (default 1000 tuples/second)\n" +
                    " topology name (default TrafficMonitoring)\n" +
                    " execution mode (default local)";
            LOG.error(alert);
        } else {
            // load default configuration
            Config conf = new Config();
            conf.setDebug(false);
            conf.setNumWorkers(1);
            try {
                String cfg = TrafficMonitoringConstants.DEFAULT_PROPERTIES;
                Properties p = loadProperties(cfg);

                conf = Configuration.fromProperties(p);
            } catch (IOException e) {
                LOG.error("Unable to load configuration file.", e);
                throw new RuntimeException("Unable to load configuration file.", e);
            }

            // parse command line arguments
            String city = (args.length > 0 && (args[0].equals(City.BEIJING) || args[0].equals(City.DUBLIN))) ?
                    args[0] :
                    ((Configuration) conf).getString(Conf.MAP_MATCHER_SHAPEFILE); // take default value from properties file
            int source_par_deg = (args.length > 1) ?
                    Integer.parseInt(args[1]) :
                    ((Configuration) conf).getInt(Conf.SPOUT_THREADS);
            int map_match_par_deg = (args.length > 2) ?
                    Integer.parseInt(args[2]) :
                    ((Configuration) conf).getInt(Conf.MAP_MATCHER_THREADS);
            int calculator_par_deg = (args.length > 3) ?
                    Integer.parseInt(args[3]) :
                    ((Configuration) conf).getInt(Conf.SPEED_CALCULATOR_THREADS);
            int sink_par_deg = (args.length > 4) ?
                    Integer.parseInt(args[4]) :
                    ((Configuration) conf).getInt(Conf.SINK_THREADS);

            // source generation rate (for tests)
            int gen_rate = (args.length > 5) ? Integer.parseInt(args[5]) : Execution.DEFAULT_RATE;

            String topology_name = (args.length > 6) ? args[6] : TrafficMonitoringConstants.DEFAULT_TOPO_NAME;
            String ex_mode = (args.length > 7) ? args[7] : Execution.LOCAL_MODE;

            // prepare the topology
            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout(Component.SPOUT,
                    new FileParserSpout(city, gen_rate, source_par_deg),
                    source_par_deg);

            builder.setBolt(Component.MAP_MATCHER,
                    new MapMatchingBolt(city, map_match_par_deg),
                    map_match_par_deg)
                    .shuffleGrouping(Component.SPOUT);

            builder.setBolt(Component.SPEED_CALCULATOR,
                    new SpeedCalculatorBolt(calculator_par_deg),
                    calculator_par_deg)
                    .fieldsGrouping(Component.MAP_MATCHER, new Fields(Field.ROAD_ID));

            builder.setBolt(Component.SINK,
                    new ConsoleSink(sink_par_deg, gen_rate),
                    sink_par_deg)
                    .shuffleGrouping(Component.SPEED_CALCULATOR);

            // build the topology
            StormTopology topology = builder.createTopology();

            // print app info
            LOG.info("[SUMMARY] Executing TrafficMonitoring with parameters:\n" +
                    "* city: " + city + "\n" +
                    "* source parallelism degree: " + source_par_deg + "\n" +
                    "* map-match parallelism degree: " + map_match_par_deg + "\n" +
                    "* calculator parallelism degree: " + calculator_par_deg + "\n" +
                    "* sink parallelism degree: " + sink_par_deg + "\n" +
                    "* rate: " + gen_rate + "\n" +
                    "Topology: source -> map-matcher -> speed-calculator -> sink");

            // run the topology
            try {
                if (ex_mode.equals(Execution.LOCAL_MODE))
                    runTopologyLocally(topology, topology_name, conf, Execution.RUNTIME_SEC);
                else if (ex_mode.equals(Execution.REMOTE_MODE))
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
        InputStream is = TrafficMonitoring.class.getResourceAsStream(filename);
        if (is != null) {
            properties.load(is);
            is.close();
        }
        LOG.info("[main] Properties loaded: {}.", properties.toString());
        return properties;
    }
}