package FraudDetection;

import Constants.FraudDetectionConstants.Conf;
import Constants.FraudDetectionConstants.Field;
import MarkovModelPrediction.MarkovModelPredictor;
import MarkovModelPrediction.ModelBasedPredictor;
import MarkovModelPrediction.Prediction;
import Meter.BoltMeterHook;
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

import static Util.config.Configuration.METRICS_ENABLED;

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

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[FraudPredictorBolt] Preparing configuration.");

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        if (config.getBoolean(METRICS_ENABLED, false)) {
            context.addTaskHook(new BoltMeterHook());
        }

        String strategy = config.getString(Conf.PREDICTOR_MODEL);
        if (strategy.equals("mm")) {
            LOG.info("[FraudPredictorBolt] Creating Markov Model.");
            predictor = new MarkovModelPredictor(config);
        }
    }

    @Override
    public void execute(Tuple tuple) {

        long t_start = System.nanoTime();

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

        long t_end = System.nanoTime();
        LOG.debug("[Bolt] execute ~ {} ms.", (t_end - t_start) / 1000000); // bolt execution time
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.SCORE, Field.STATES));
    }
}

