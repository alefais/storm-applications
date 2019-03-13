package FraudDetection;

import Constants.FraudDetectionConstants.Conf;
import Constants.FraudDetectionConstants.Field;
import MarkovModelPrediction.MarkovModelPredictor;
import MarkovModelPrediction.ModelBasedPredictor;
import MarkovModelPrediction.Prediction;
import Util.config.Configuration;
import org.apache.commons.lang.StringUtils;
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
 * The bolt is in charge of implementing outliers detection.
 * Given a transaction sequence of a customer, there is a
 * probability associated with each path of state transition,
 * which indicates the chances of fraudolent activities.
 *
 * @author Alessandra Fais
 */
public class FraudPredictorBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(FraudPredictorBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private ModelBasedPredictor predictor;
    private long t_start;
    private long t_end;
    private long processed;

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[FraudPredictorBolt] Preparing configuration.");

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of generated tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        String strategy = config.getString(Conf.PREDICTOR_MODEL);
        if (strategy.equals("mm")) {
            LOG.info("[FraudPredictorBolt] Creating Markov Model Predictor.");
            predictor = new MarkovModelPredictor(config);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String entityID = tuple.getStringByField(Field.ENTITY_ID);
        String record = tuple.getStringByField(Field.RECORD_DATA);

        Prediction p = predictor.execute(entityID, record);

        // send outliers
        if (p.isOutlier()) {
            collector.emit(tuple, new Values(entityID, p.getScore(), StringUtils.join(p.getStates(), ",")));

            LOG.info("[FraudPredictorBolt] Sending outlier: EntityID {} score {} states {}",
                    entityID, p.getScore(), StringUtils.join(p.getStates(), ","));
        }
        collector.ack(tuple);

        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[FraudPredictorBolt] Processed {} elements in {} ms, bandwidth is {} elements per second.",
                processed, t_elapsed, processed / (t_elapsed / 1000));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES));
    }
}

