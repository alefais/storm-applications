package WordCount;

import Constants.BaseConstants;
import Constants.BaseConstants.Execution;
import Constants.WordCountConstants;
import Constants.WordCountConstants.*;
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
public class WordCount {

    private static final Logger LOG = LoggerFactory.getLogger(WordCount.class);

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals(BaseConstants.HELP)) {
            String alert =
                    "In order to correctly run WordCount app you can pass the following (optional) arguments:\n" +
                    "Optional arguments (default values are specified in wc.properties or defined as constants):\n" +
                    " source type (assume value in {file, generator})\n" +
                    " file path (valid only if the source type is file)\n" +
                    " source parallelism degree\n" +
                    " splitter bolt parallelism degree\n" +
                    " counter bolt parallelism degree\n" +
                    " sink parallelism degree\n" +
                    " source generation rate (default -1, generate at the max possible rate)\n" +
                    " topology name (default WordCount)\n" +
                    " execution mode (default local)";
            LOG.error(alert);
        } else {
            // load default configuration
            Config conf = new Config();
            conf.setDebug(false);
            conf.setNumWorkers(1);
            try {
                String cfg = WordCountConstants.DEFAULT_PROPERTIES;
                Properties p = loadProperties(cfg);

                conf = Configuration.fromProperties(p);
                LOG.debug("Loaded configuration file {}.", cfg);
            } catch (IOException e) {
                LOG.error("Unable to load configuration file.", e);
                throw new RuntimeException("Unable to load configuration file.", e);
            }

            // parse command line arguments
            String source;
            String file_path = null;
            int source_par_deg;
            int splitter_par_deg;
            int counter_par_deg;
            int sink_par_deg;
            int gen_rate;
            String topology_name;
            String ex_mode;

            source = (args.length > 0 && (args[0].equals(Conf.FILE_SOURCE) || args[0].equals(Conf.GEN_SOURCE))) ?
                    args[0] :
                    Conf.FILE_SOURCE;
            if (source.equals(Conf.FILE_SOURCE)) {
                file_path = (args.length > 1) ?
                        args[1] :
                        ((Configuration) conf).getString(Conf.SPOUT_PATH);
                source_par_deg = (args.length > 2) ?
                        Integer.parseInt(args[2]) :
                        ((Configuration) conf).getInt(Conf.SPOUT_THREADS);
                splitter_par_deg = (args.length > 3) ?
                        Integer.parseInt(args[3]) :
                        ((Configuration) conf).getInt(Conf.SPLITTER_THREADS);
                counter_par_deg = (args.length > 4) ?
                        Integer.parseInt(args[4]) :
                        ((Configuration) conf).getInt(Conf.COUNTER_THREADS);
                sink_par_deg = (args.length > 5) ?
                        Integer.parseInt(args[5]) :
                        ((Configuration) conf).getInt(Conf.SINK_THREADS);

                // source generation rate (for tests)
                gen_rate = (args.length > 6) ? Integer.parseInt(args[6]) : Execution.DEFAULT_RATE;

                topology_name = (args.length > 7) ? args[7] : WordCountConstants.DEFAULT_TOPO_NAME;
                ex_mode = (args.length > 8) ? args[8] : Execution.LOCAL_MODE;
            } else {
                source_par_deg = (args.length > 1) ?
                        Integer.parseInt(args[1]) :
                        ((Configuration) conf).getInt(Conf.SPOUT_THREADS);
                splitter_par_deg = (args.length > 2) ?
                        Integer.parseInt(args[2]) :
                        ((Configuration) conf).getInt(Conf.SPLITTER_THREADS);
                counter_par_deg = (args.length > 3) ?
                        Integer.parseInt(args[3]) :
                        ((Configuration) conf).getInt(Conf.COUNTER_THREADS);
                sink_par_deg = (args.length > 4) ?
                        Integer.parseInt(args[4]) :
                        ((Configuration) conf).getInt(Conf.SINK_THREADS);

                // source generation rate (for tests)
                gen_rate = (args.length > 5) ? Integer.parseInt(args[5]) : Execution.DEFAULT_RATE;

                topology_name = (args.length > 6) ? args[6] : WordCountConstants.DEFAULT_TOPO_NAME;
                ex_mode = (args.length > 7) ? args[7] : Execution.LOCAL_MODE;
            }

            // prepare the topology
            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout(Component.SPOUT,
                    new FileParserSpout(source, file_path, gen_rate, source_par_deg),
                    source_par_deg);

            builder.setBolt(Component.SPLITTER,
                    new SplitterBolt(splitter_par_deg),
                    splitter_par_deg)
                    .shuffleGrouping(Component.SPOUT);

            builder.setBolt(Component.COUNTER,
                    new CounterBolt(counter_par_deg),
                    counter_par_deg)
                    .fieldsGrouping(Component.SPLITTER, new Fields(Field.WORD));

            builder.setBolt(Component.SINK,
                    new ConsoleSink(sink_par_deg, gen_rate),
                    sink_par_deg)
                    .shuffleGrouping(Component.COUNTER);

            // build the topology
            StormTopology topology = builder.createTopology();

            // print app info
            LOG.info("[SUMMARY] Executing WordCount with parameters:\n" +
                    "* file path: " + ((file_path != null) ? file_path : "random sentences") + "\n" +
                    "* source parallelism degree: " + source_par_deg + "\n" +
                    "* splitter parallelism degree: " + splitter_par_deg + "\n" +
                    "* counter parallelism degree: " + counter_par_deg + "\n" +
                    "* sink parallelism degree: " + sink_par_deg + "\n" +
                    "* rate: " + gen_rate + "\n" +
                    "Topology: source -> splitter -> counter -> sink");

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
        InputStream is = WordCount.class.getResourceAsStream(filename);
        if (is != null) {
            properties.load(is);
            is.close();
        }
        LOG.info("[main] Properties loaded: {}.", properties.toString());
        return properties;
    }
}
