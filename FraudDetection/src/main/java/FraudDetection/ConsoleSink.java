package FraudDetection;

import Constants.FraudDetectionConstants.Field;
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
 * The sink is in charge of printing the results.
 *
 * @author Alessandra Fais
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

    private ArrayList<Long> tuple_latencies;

    ConsoleSink(int par_deg) {
        this.par_deg = par_deg;
        tuple_latencies = new ArrayList<>();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[ConsoleSink] Started.");

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String entityID = tuple.getStringByField(Field.ENTITY_ID);
        Double score = tuple.getDoubleByField(Field.SCORE);
        String states = tuple.getStringByField(Field.STATES);
        Long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        processed++;
        LOG.debug("[ConsoleSink] EntityID {}, score {}, states {}.", entityID, score, states);

        Long now = System.nanoTime();
        Long tuple_latency = (now - timestamp); // tuple latency in nanoseconds
        tuple_latencies.add(tuple_latency);

        collector.ack(tuple);

        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        // evaluate bandwidth
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[ConsoleSink] Processed {} tuples (outliers) in {} ms. " +
                        "Bandwidth is {} tuples per second.",
                processed, t_elapsed, (processed / (t_elapsed / 1000) * par_deg));

        // evaluate latency
        long acc = 0L;
        for (Long tl : tuple_latencies) {
            acc += tl;
        }
        long avg_latency = acc / tuple_latencies.size(); // average latency in nanoseconds
        LOG.info("[ConsoleSink] Average latency: {} ms.", avg_latency / 1000000); // average latency in milliseconds
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES, Field.TIMESTAMP));
    }
}
