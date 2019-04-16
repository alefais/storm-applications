package WordCount;

import Constants.WordCountConstants.Field;
import Util.config.Configuration;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts words' occurrences.
 */
public class CounterBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(CounterBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private final Map<String, MutableLong> counts = new HashMap<>();

    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;

    CounterBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[CounterBolt] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField(Field.WORD);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        if (word != null) {
            MutableLong count = counts.computeIfAbsent(word, k -> new MutableLong(0));
            count.increment();

            collector.emit(new Values(word, count.get(), timestamp));
        }
        collector.ack(tuple);

        processed++;
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[CounterBolt] Processed {} tuples in {} ms. " +
                        "Source bandwidth is {} tuples per second.",
                processed, t_elapsed,
                processed / (t_elapsed / 1000));  // tuples per second
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.WORD, Field.COUNT, Field.TIMESTAMP));
    }
}

