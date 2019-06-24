package WordCount;

import Constants.WordCountConstants.*;
import Util.config.Configuration;
import org.apache.storm.shade.org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *  @author  Alessandra Fais
 *  @version June 2019
 *
 *  Splits all the received lines into words.
 */
public class SplitterBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(SplitterBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private long t_start;
    private long t_end;
    private int par_deg;
    private long bytes;

    SplitterBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[Splitter] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        bytes = 0;                   // total number of processed bytes

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String line = tuple.getStringByField(Field.LINE);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        if (line != null) {
            LOG.debug("[Splitter] Received line `" + line + "`");
            bytes += line.length();

            String[] words = line.split("\\W");
            for (String word : words) {
                if (!StringUtils.isBlank(word)) {
                    collector.emit(tuple, new Values(word, timestamp));
                    LOG.debug("[SplitterBolt] Sending `" + word + "`");
                }
            }
        }
        collector.ack(tuple);

        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[Splitter] execution time: " + t_elapsed +
                " ms, processed: " + (bytes / 1048576) +
                " MB, bandwidth: " + (bytes / 1048576) / (t_elapsed / 1000) +  // MB per second
                " (MB/s) " + bytes / (t_elapsed / 1000) + " bytes/s");         // bytes per second
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.WORD, Field.TIMESTAMP));
    }
}

