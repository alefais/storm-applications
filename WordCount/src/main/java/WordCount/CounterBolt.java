package WordCount;

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

import static Constants.Fields.*;

public class CounterBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(CounterBolt.class);

    OutputCollector collector;

    private final Map<String, MutableLong> counts = new HashMap<>();

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        long t_start = System.nanoTime();

        String event = tuple.getStringByField(WORD_EVENT);
        String word = tuple.getStringByField(WORD_VALUE);
        MutableLong count = counts.computeIfAbsent(word, k -> new MutableLong(0));
        count.increment();

        collector.emit(new Values(WORD_COUNT_EVENT, word, count.get()));

        long t_end = System.nanoTime();
        LOG.warn("[Bolt] execute ~ {} ms.", (t_end - t_start) / 1000000); // bolt execution time
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(WORD_COUNT_EVENT, WORD_VALUE, WORD_COUNT));
    }
}
