package WordCount;

import Constants.WordCountConstants.Field;
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
 */
public class ConsoleSink extends BaseRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleSink.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private long t_start;
    private long t_end;
    private int par_deg;
    private int gen_rate;
    private long bytes;

    private ArrayList<Long> tuple_latencies;

    ConsoleSink(int p_deg, int g_rate) {
        par_deg = p_deg;         // sink parallelism degree
        gen_rate = g_rate;       // generation rate of the source (spout)
        tuple_latencies = new ArrayList<>();
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[ConsoleSink] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        bytes = 0;                   // total number of processed bytes

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField(Field.WORD);
        long count = tuple.getLongByField(Field.COUNT);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        LOG.debug("[ConsoleSink] Received `" +
                word + "` occurred " +
                count + " times since now.");

        if (gen_rate != -1) {   // evaluate latency
            long now = System.nanoTime();
            long tuple_latency = (now - timestamp); // tuple latency in nanoseconds
            tuple_latencies.add(tuple_latency);
        }
        collector.ack(tuple);

        bytes += word.length();
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        if (bytes == 0) {
            LOG.info("[ConsoleSink] No words received.");
        } else {
            if (gen_rate == -1) {  // evaluate bandwidth
                long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

                LOG.info("[ConsoleSink] Processed " + (bytes / 1048576) + " in " + t_elapsed + " ms.");
                LOG.info("[ConsoleSink] Bandwidth is " +
                        (bytes / 1048576) / (t_elapsed / 1000) + " MB per second.");
            } else {  // evaluate latency
                long acc = 0L;
                for (Long tl : tuple_latencies) {
                    acc += tl;
                }
                double avg_latency = (double) acc / tuple_latencies.size(); // average latency in nanoseconds

                LOG.info("[ConsoleSink] Average latency: {} ms.", avg_latency / 1000000); // average latency in milliseconds
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.WORD, Field.COUNT, Field.TIMESTAMP));
    }
}
