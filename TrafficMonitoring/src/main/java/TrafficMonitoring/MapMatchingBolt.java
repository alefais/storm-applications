package TrafficMonitoring;

import Constants.TrafficMonitoringConstants.*;
import RoadModel.GPSRecord;
import RoadModel.RoadGridList;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This operator receives traces of an object (e.g. GPS loggers and GPS phones)
 * including altitude, latitude and longitude, and uses them to determine the
 * location (regarding a road ID) of this object in real-time.
 */
public class MapMatchingBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MapMatchingBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private RoadGridList sectors;
    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;

    private long t_start;
    private long t_end;
    private long processed;
    private long outliers;
    private int par_deg;

    MapMatchingBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        LOG.info("[MapMatchingBolt] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples
        outliers = 0;                // total number of outliers

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        String shapeFile = config.getString(Conf.MAP_MATCHER_SHAPEFILE);

        latMin = config.getDouble(Conf.MAP_MATCHER_LAT_MIN);
        latMax = config.getDouble(Conf.MAP_MATCHER_LAT_MAX);
        lonMin = config.getDouble(Conf.MAP_MATCHER_LON_MIN);
        lonMax = config.getDouble(Conf.MAP_MATCHER_LON_MAX);

        try {
            sectors = new RoadGridList(config, shapeFile);
        } catch (SQLException | IOException ex) {
            LOG.error("Error while loading shape file", ex);
            throw new RuntimeException("Error while loading shape file");
        }
    }

    @Override
    public void execute(Tuple tuple) {
        try {
            String vehicleID = tuple.getStringByField(Field.VEHICLE_ID);
            String date_time = tuple.getStringByField(Field.DATE_TIME);
            boolean occ = tuple.getBooleanByField(Field.OCCUPIED);
            int speed = tuple.getIntegerByField(Field.SPEED);
            int bearing = tuple.getIntegerByField(Field.BEARING);
            double latitude = tuple.getDoubleByField(Field.LATITUDE);
            double longitude = tuple.getDoubleByField(Field.LONGITUDE);
            long timestamp = tuple.getLongByField(Field.TIMESTAMP);

            if (speed <= 0) return;
            if (longitude > lonMax || longitude < lonMin || latitude > latMax || latitude < latMin) return;

            GPSRecord record = new GPSRecord(longitude, latitude, speed, bearing);

            int roadID = sectors.fetchRoadID(record);

            if (roadID != -1) {
                //List<Object> values = tuple.getValues();
                //values.add(roadID);

                collector.emit(tuple,
                        new Values(
                                vehicleID,
                                date_time,
                                occ,
                                speed,
                                bearing,
                                latitude,
                                longitude,
                                roadID,
                                timestamp
                        )
                );
            }
            collector.ack(tuple);

            processed++;
            t_end = System.nanoTime();
        } catch (SQLException ex) {
            LOG.error("Unable to fetch road ID", ex);
        }
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        LOG.info("[MapMatchingBolt] Processed {} tuples in {} ms (found {} outliers). " +
                        "Source bandwidth is {} tuples per second.",
                processed, t_elapsed, outliers,
                processed / (t_elapsed / 1000));  // tuples per second
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(
            new Fields(
                Field.VEHICLE_ID,
                Field.DATE_TIME,
                Field.OCCUPIED,
                Field.SPEED,
                Field.BEARING,
                Field.LATITUDE,
                Field.LONGITUDE,
                Field.ROAD_ID,
                Field.TIMESTAMP)
        );
    }
}
