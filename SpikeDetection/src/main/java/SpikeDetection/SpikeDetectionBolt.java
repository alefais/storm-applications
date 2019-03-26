package SpikeDetection;

import Constants.SpikeDetectionConstants.*;
import Constants.BaseConstants.*;
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
import java.util.Map;

public class SpikeDetectionBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MovingAverageBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;
    private double spike_threshold;
    private long spikes;

    SpikeDetectionBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[SpikeDetectionBolt] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples
        spikes = 0;                  // total number of spikes detected

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        // the detection threshold of moving average values is set to 0.03.
        spike_threshold = config.getDouble(Conf.SPIKE_DETECTOR_THRESHOLD, 0.03d);
    }

    @Override
    public void execute(Tuple tuple) {
        String deviceID = tuple.getStringByField(Field.DEVICE_ID);
        double moving_avg_instant = tuple.getDoubleByField(Field.MOVING_AVG);
        double next_property_value = tuple.getDoubleByField(Field.VALUE);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        if (Math.abs(next_property_value - moving_avg_instant) > spike_threshold * moving_avg_instant) {
            /*if (tuple.getSourceStreamId().equalsIgnoreCase(BaseStream.Marker_STREAM_ID)) {
                collector.emit(
                        BaseStream.Marker_STREAM_ID,
                        new Values(deviceID, moving_avg_instant, next_property_value, "spike detected",
                                tuple.getLongByField(BaseField.MSG_ID),
                                tuple.getLongByField(BaseField.SYSTEMTIMESTAMP)));
            }*/
            spikes++;
            collector.emit(new Values(deviceID, moving_avg_instant, next_property_value, timestamp));
        }

        processed++;
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[SpikeDetectionBolt] Processed {} tuples in {} ms (detected {} spikes). " +
                        "Source bandwidth is {} tuples per second.",
                processed, t_elapsed,
                processed / (t_elapsed / 1000));  // tuples per second
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.DEVICE_ID, Field.MOVING_AVG, Field.VALUE, Field.TIMESTAMP));
    }
}
