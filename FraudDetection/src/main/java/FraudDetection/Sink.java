package FraudDetection;

import Constants.Field;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sink extends BaseBasicBolt {

    private static final Logger LOG = LoggerFactory.getLogger(Sink.class);

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {

        long t_start = System.nanoTime();

        String entityID = tuple.getStringByField(Field.ENTITY_ID);
        String score = tuple.getStringByField(Field.SCORE);
        Long states = tuple.getLongByField(Field.STATES);

        LOG.info("[Sink] Entity ID {}, score {}, states {}.", entityID, score, states);

        long t_end = System.nanoTime();
        LOG.warn("[Sink] execute ~ {} ms.", (t_end - t_start) / 1000000); // bolt execution time
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES));
    }
}
