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

    ConsoleSink(int parallelism_degree) {
        par_deg = parallelism_degree;
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
        processed++;

        LOG.debug("[ConsoleSink] EntityID {}, score {}, states {}.", entityID, score, states);

        collector.ack(tuple);

        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[ConsoleSink] Processed {} tuples (outliers) in {} ms. " +
                        "Bandwidth is {} tuples per second.",
                processed, t_elapsed, (processed / (t_elapsed / 1000) * par_deg));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES));
    }
}
