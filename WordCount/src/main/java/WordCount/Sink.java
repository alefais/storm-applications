package WordCount;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Constants.Fields.*;

public class Sink extends BaseBasicBolt {

    private static final Logger LOG = LoggerFactory.getLogger(Sink.class);

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {

        long t_start = System.nanoTime();

        String event = tuple.getStringByField(WORD_COUNT_EVENT);
        String word = tuple.getStringByField(WORD_VALUE);
        Long count = tuple.getLongByField(WORD_COUNT);

        LOG.info("[Sink] word {} appears {} times.", word, count);

        long t_end = System.nanoTime();
        LOG.warn("[Sink] execute ~ {} ms.", (t_end - t_start) / 1000000); // bolt execution time
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(WORD_COUNT_EVENT, WORD_VALUE, WORD_COUNT));
    }
}
