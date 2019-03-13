package FraudDetection;

import Constants.FraudDetectionConstants.Field;
import Meter.BoltMeterHook;
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

import static Util.config.Configuration.METRICS_ENABLED;

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

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        if (config.getBoolean(METRICS_ENABLED, false)) {
            context.addTaskHook(new BoltMeterHook());
        }
    }

    @Override
    public void execute(Tuple tuple) {

        long t_start = System.nanoTime();

        String entityID = tuple.getStringByField(Field.ENTITY_ID);
        Double score = tuple.getDoubleByField(Field.SCORE);
        String states = tuple.getStringByField(Field.STATES);

        LOG.info("[ConsoleSink] EntityID {}, score {}, states {}.", entityID, score, states);

        /*
        Fields schema = context.getComponentOutputFields(tuple.getSourceGlobalStreamId());
        String line = "";
        for (int i = 0; i < tuple.size(); i++) {
            if (i != 0) line += ", ";
            line += String.format("%s=%s", schema.get(i), tuple.getValue(i));
        }
        System.out.println(line);*/
        collector.ack(tuple);

        long t_end = System.nanoTime();
        LOG.debug("[ConsoleSink] execute ~ {} ms.", (t_end - t_start) / 1000000); // bolt execution time
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES));
    }
}
