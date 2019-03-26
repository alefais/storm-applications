package SpikeDetection;

import Constants.SpikeDetectionConstants.*;
import Util.config.Configuration;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The bolt is in charge of implementing outliers detection.
 * Given a transaction sequence of a customer, there is a
 * probability associated with each path of state transition,
 * which indicates the chances of fraudolent activities.
 *
 * @author Alessandra Fais
 */
public class MovingAverageBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private int movingAverageWindow;
    private Map<String, LinkedList<Double>> deviceIDtoStreamMap;
    private Map<String, Double> deviceIDtoSumOfEvents;

    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;

    MovingAverageBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[MovingAverageBolt] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        movingAverageWindow = config.getInt(Conf.MOVING_AVERAGE_WINDOW, 1000);
        deviceIDtoStreamMap = new HashMap<>();
        deviceIDtoSumOfEvents = new HashMap<>();
    }

    @Override
    public void execute(Tuple tuple) {
        String deviceID = tuple.getStringByField(Field.DEVICE_ID);
        Double next_property_value = tuple.getDoubleByField(Field.VALUE);
        Long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        double movingAverageInstant = movingAverage(deviceID, next_property_value);

        collector.emit(tuple, new Values(deviceID, movingAverageInstant, next_property_value, timestamp));

        LOG.debug("[MovingAverageBolt] Sending: DeviceID {} avg {} next_value {}",
                deviceID, movingAverageInstant, next_property_value);
        //collector.ack(tuple);

        processed++;
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[MovingAverageBolt] Processed {} tuples in {} ms. " +
                        "Source bandwidth is {} tuples per second.",
                processed, t_elapsed,
                processed / (t_elapsed / 1000));  // tuples per second
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.DEVICE_ID, Field.MOVING_AVG, Field.VALUE, Field.TIMESTAMP));
    }

    private double movingAverage(String deviceID, double nextDouble) {
        LinkedList<Double> valueList = new LinkedList<>();
        double sum = 0.0;

        if (deviceIDtoStreamMap.containsKey(deviceID)) {
            valueList = deviceIDtoStreamMap.get(deviceID);
            sum = deviceIDtoSumOfEvents.get(deviceID);
            if (valueList.size() > movingAverageWindow - 1) {
                double valueToRemove = valueList.removeFirst();
                sum -= valueToRemove;
            }
            valueList.addLast(nextDouble);
            sum += nextDouble;
            deviceIDtoSumOfEvents.put(deviceID, sum);
            deviceIDtoStreamMap.put(deviceID, valueList);
            return sum / valueList.size();
        } else {
            valueList.add(nextDouble);
            deviceIDtoStreamMap.put(deviceID, valueList);
            deviceIDtoSumOfEvents.put(deviceID, nextDouble);
            return nextDouble;
        }
    }
}

