package TrafficMonitoring;

import Constants.TrafficMonitoringConstants.Field;
import Util.config.Configuration;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 *  @author Alessandra Fais
 *  @version June 2019
 *
 *  The sink is in charge of printing the results: the average speed
 *  on a certain road (identified by a roadID) and the number of
 *  data collected for that roadID.
 */
public class ConsoleSink extends BaseRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleSink.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;
    private int gen_rate;

    private ArrayList<Long> tuple_latencies;

    ConsoleSink(int p_deg, int g_rate) {
        par_deg = p_deg;         // sink parallelism degree
        gen_rate = g_rate;       // generation rate of the source (spout)
        tuple_latencies = new ArrayList<>();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[Sink] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        int roadID = tuple.getIntegerByField(Field.ROAD_ID);
        int avg_speed = tuple.getIntegerByField(Field.AVG_SPEED);
        int count = tuple.getIntegerByField(Field.COUNT);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        LOG.debug("[Sink] tuple: roadID " + roadID +
                  ", average_speed " + avg_speed +
                  ", counter " + count +
                  ", ts " + timestamp);

        // evaluate latency
        Long now = System.nanoTime();
        Long tuple_latency = (now - timestamp); // tuple latency in nanoseconds
        tuple_latencies.add(tuple_latency);

        collector.ack(tuple);

        processed++;
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        if (processed == 0) {
            LOG.info("[Sink] processed tuples: " + processed);
        } else {
            long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

            LOG.info("[Sink] processed tuples: " + processed +
                     ", bandwidth: " +  processed / (t_elapsed / 1000) +
                     " tuples/s");

            // evaluate average latency
            long acc = 0L;
            for (Long tl : tuple_latencies) {
                acc += tl;
            }
            double avg_latency = (double) acc / tuple_latencies.size(); // average latency in nanoseconds

            LOG.info("[Sink] processed tuples: " + processed +
                     ", latency: " +  avg_latency / 1000000 + // average latency in milliseconds
                     " ms");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ROAD_ID, Field.AVG_SPEED, Field.COUNT, Field.TIMESTAMP));
    }
}
