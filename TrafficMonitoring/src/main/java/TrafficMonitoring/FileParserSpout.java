package TrafficMonitoring;

import Constants.TrafficMonitoringConstants.*;
import Util.config.Configuration;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * The spout is in charge of reading the input data file containing
 * vehicle-traces, parsing it and generating the stream of records
 * toward the MapMatchingBolt.
 */
public class FileParserSpout extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(FileParserSpout.class);

    protected Configuration config;
    protected SpoutOutputCollector collector;
    protected TopologyContext context;

    private Integer rate;
    private String city;
    private String city_tracefile;

    private ArrayList<String> vehicles;
    private ArrayList<Boolean> occupancies;
    private ArrayList<String> times;
    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<Integer> speeds;
    private ArrayList<Integer> bearings;

    private double min_lat;
    private double max_lat;
    private double min_lon;
    private double max_lon;

    private long t_start;
    private long generated;
    private long emitted;
    private int reset;
    private long nt_execution;
    private long nt_end;
    private int par_deg;

    /**
     * Constructor: it expects the file path, the generation rate and the parallelism degree.
     * @param c city to monitor
     * @param gen_rate if the argument value is -1 then the spout generates tuples at
     *                 the maximum rate possible (measure the bandwidth under this assumption);
     *                 if the argument value is different from -1 then the spout generates
     *                 tuples at the rate given by this parameter (measure the latency given
     *                 this generation rate)
     * @param p_deg source parallelism degree
     */
    FileParserSpout(String c, int gen_rate, int p_deg) {
        city = c;
        rate = gen_rate;        // number of tuples per second
        par_deg = p_deg;        // spout parallelism degree

        vehicles = new ArrayList<>();
        occupancies = new ArrayList<>();
        times = new ArrayList<>();
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        speeds = new ArrayList<>();
        bearings = new ArrayList<>();

        min_lat = 0;
        max_lat = 0;
        min_lon = 0;
        max_lon = 0;

        generated = 0;          // total number of generated tuples
        emitted = 0;            // total number of emitted tuples
        reset = 0;
        nt_execution = 0;       // number of executions of nextTuple() method
    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        LOG.info("[FileParserSpout] Started ({} replicas).", par_deg);

        t_start = System.nanoTime(); // spout start time in nanoseconds

        config = Configuration.fromMap(conf);
        collector = spoutOutputCollector;
        context = topologyContext;

        // set city trace file path
        city_tracefile =  (city.equals(City.DUBLIN)) ?
                config.getString(Conf.SPOUT_DUBLIN) :
                config.getString(Conf.SPOUT_BEIJING);
    }

    /**
     * The method is called in an infinite loop by design, this means that the
     * stream is continuously generated from the data source file.
     * The parsing phase splits each line of the input dataset extracting date and time, deviceID
     * and information provided by the sensor about temperature, humidity, light and voltage.
     */
    @Override
    public void nextTuple() {
        if (city.equals(City.DUBLIN))
            parseDublinBusTrace();
        else
            parseBeijingTaxiTrace();
    }

    @Override
    public void close() {
        long t_elapsed = (nt_end - t_start) / 1000000;  // elapsed time in milliseconds

        LOG.info("[FileParserSpout] Terminated after {} generations.", nt_execution);
        LOG.info("[FileParserSpout] Generated {} tuples in {} ms. Emitted {} tuples in {} ms. " +
                        "Source bandwidth is {} tuples per second.",
                generated, t_elapsed,
                emitted + (rate * reset), t_elapsed,
                generated / (t_elapsed / 1000));  // tuples per second
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
                        Field.MIN_LAT,  // these 4 fields define the city bounding box
                        Field.MAX_LAT,
                        Field.MIN_LON,
                        Field.MAX_LON,
                        Field.TIMESTAMP  // tuple timestamp
                )
        );
    }

    //------------------------------ private methods ---------------------------

    /**
     * Beijing vehicle-trace dataset is used freely, with the following acknowledgement:
     * “This code was obtained from research conducted by the University of Southern
     * California’s Autonomous Networks Research Group, http://anrg.usc.edu“.
     *
     * Format of the dataset:
     * vehicleID, date-time, speed, bearing, latitude, longitude
     */
    private void parseBeijingTaxiTrace() {
        // parsing phase
        try {
            Scanner scan = new Scanner(new File(city_tracefile));
            while (scan.hasNextLine()) {
                String[] fields = scan.nextLine().split(",");

                if (fields.length >= 7) {
                    vehicles.add(fields[BeijingParsing.B_VEHICLE_ID_FIELD]);
                    occupancies.add(true);
                    times.add(fields[BeijingParsing.B_DATE_FIELD]);
                    latitudes.add(Double.parseDouble(fields[BeijingParsing.B_LATITUDE_FIELD]));
                    longitudes.add(Double.parseDouble(fields[BeijingParsing.B_LONGITUDE_FIELD]));
                    speeds.add(((Double)Double.parseDouble(fields[BeijingParsing.B_SPEED_FIELD])).intValue());
                    bearings.add(Integer.parseInt(fields[BeijingParsing.B_DIRECTION_FIELD]));

                    generated++;

                    updateMinMax(latitudes.get(latitudes.size() - 1), longitudes.get(longitudes.size() - 1));
                } else
                    LOG.debug("[FileParserSpout] Incomplete record.");
            }
            scan.close();
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("The file {} does not exists", city_tracefile);
            throw new RuntimeException("The file '" + city_tracefile + "' does not exists");
        }

        // emit tuples
        int interval = 1000000000; // one second (nanoseconds)
        long t_init = System.nanoTime();

        LOG.info("[FileParserSpout] min_lat {}, max_lat {}, min_lon {}, max_lon {}", min_lat, max_lat, min_lon, max_lon);

        for (int i = 0; i < vehicles.size(); i++) {
            if (rate == -1) {       // at the maximum possible rate
                collector.emit(createTuple(i));
                emitted++;
            } else {                // at the given rate
                long t_now = System.nanoTime();
                if (emitted >= rate) {
                    LOG.info("[FileParserSpout] emitted {} VS rate {} in {} ms (delay: {} ns)",
                            emitted, rate, (t_now - t_init) / 1000000, (double)interval / rate);

                    if (t_now - t_init <= interval) {
                        LOG.info("[FileParserSpout] waste {} ns.", interval - (t_now - t_init));
                        active_delay(interval - (t_now - t_init));
                    }
                    emitted = 0;
                    t_init = System.nanoTime();
                    reset++;
                }
                collector.emit(createTuple(i));
                emitted++;
                active_delay((double) interval / rate);
            }
        }

        nt_execution++;
        nt_end = System.nanoTime();
    }

    /**
     * GPS Data about buses across Dublin City are retrieved from Dublin City
     * Council'traffic control.
     * See https://data.gov.ie/dataset/dublin-bus-gps-sample-data-from-dublin-city-council-insight-project
     *
     * Format of the dataset:
     * timestamp, lineID, direction, journeyPatternID, timeFrame, vehicleJourneyID, busOperator, congestion,
     * longitude, latitude, delay, blockID, vehicleID, stopID, atStop
     */
    private void parseDublinBusTrace() {
        // parsing phase
        try {
            Scanner scan = new Scanner(new File(city_tracefile));
            while (scan.hasNextLine()) {
                String[] fields = scan.nextLine().split(",");

                if (fields.length >= 7) {
                    vehicles.add(fields[DublinParsing.D_VEHICLE_ID_FIELD]);
                    occupancies.add(true);
                    times.add(fields[DublinParsing.D_TIMESTAMP_FIELD]);
                    latitudes.add(Double.parseDouble(fields[DublinParsing.D_LATITUDE_FIELD]));
                    longitudes.add(Double.parseDouble(fields[DublinParsing.D_LONGITUDE_FIELD]));
                    speeds.add(0);
                    bearings.add(Integer.parseInt(fields[DublinParsing.D_DIRECTION_FIELD]));

                    generated++;

                    LOG.info("[FileParserSpout] Fields: {} {} {} {} {} {}",
                            fields[DublinParsing.D_VEHICLE_ID_FIELD],
                            fields[DublinParsing.D_TIMESTAMP_FIELD],
                            fields[DublinParsing.D_LATITUDE_FIELD],
                            fields[DublinParsing.D_LONGITUDE_FIELD],
                            fields[DublinParsing.D_DIRECTION_FIELD]);
                } else
                    LOG.debug("[FileParserSpout] Incomplete record.");
            }
            scan.close();
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("The file {} does not exists", city_tracefile);
            throw new RuntimeException("The file '" + city_tracefile + "' does not exists");
        }

        // emit tuples
        int interval = 1000000000; // one second (nanoseconds)
        long t_init = System.nanoTime();

        for (int i = 0; i < vehicles.size(); i++) {
            if (rate == -1) {       // at the maximum possible rate
                collector.emit(createTuple(i));
                emitted++;
            } else {                // at the given rate
                long t_now = System.nanoTime();
                if (emitted >= rate) {
                    LOG.info("[FileParserSpout] emitted {} VS rate {} in {} ms (delay: {} ns)",
                            emitted, rate, (t_now - t_init) / 1000000, (double)interval / rate);

                    if (t_now - t_init <= interval) {
                        LOG.info("[FileParserSpout] waste {} ns.", interval - (t_now - t_init));
                        active_delay(interval - (t_now - t_init));
                    }
                    emitted = 0;
                    t_init = System.nanoTime();
                    reset++;
                }
                collector.emit(createTuple(i));
                emitted++;
                active_delay((double) interval / rate);
            }
        }

        nt_execution++;
        nt_end = System.nanoTime();
    }

    /**
     * Create the list of values to be sent to the
     * MapMatchingBolt.
     * @param i index of the tuple
     * @return list of values content of the tuple
     */
    private Values createTuple(int i) {
        return new Values(
                vehicles.get(i),
                times.get(i),
                occupancies.get(i),
                speeds.get(i),
                bearings.get(i),
                latitudes.get(i),
                longitudes.get(i),
                min_lat,
                max_lat,
                min_lon,
                max_lon,
                System.nanoTime()
        );
    }

    /**
     * Helper method used to fined the city bounding box interested
     * by the trace file data.
     * @param cur_lat current latitude
     * @param cur_lon current longitude
     */
    private void updateMinMax(double cur_lat, double cur_lon) {
        // first initialization
        if (min_lat == 0 && max_lat == 0) {
            min_lat = cur_lat;
            max_lat = cur_lat;
        }
        if (min_lon == 0 && max_lon == 0) {
            min_lon = cur_lon;
            max_lon = cur_lon;
        }

        // update min
        if ((cur_lat != 0 && cur_lat < min_lat) || (min_lat == 0))
            min_lat = cur_lat;
        if ((cur_lon != 0 && cur_lon < min_lon) || (min_lon == 0))
            min_lon = cur_lon;

        // update max
        if ((cur_lat != 0 && cur_lat > max_lat) || (max_lat == 0))
            max_lat = cur_lat;
        if ((cur_lon != 0 && cur_lon > max_lon) || (max_lon == 0))
            max_lon = cur_lon;
    }

    /**
     * Add some active delay (busy-waiting function).
     * @param nsecs wait time in nanoseconds
     */
    private void active_delay(double nsecs) {
        long t_start = System.nanoTime();
        long t_now;
        boolean end = false;

        while (!end) {
            t_now = System.nanoTime();
            end = (t_now - t_start) >= nsecs;
        }
        LOG.debug("[FileParserSpout] delay {} ns.", nsecs);
    }
}
