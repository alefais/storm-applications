package SpikeDetection;

import Constants.SpikeDetectionConstants.*;
import Util.config.Configuration;
import com.google.common.collect.ImmutableMap;
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
 * measurements from a set of sensor devices, parsing it
 * and generating the stream of records toward the MovingAverageBolt.
 *
 * Format of the input file:
 * date:yyyy-mm-dd	time:hh:mm:ss.xxx	epoch:int	deviceID:int	temperature:real	humidity:real	light:real	voltage:real
 *
 * Data example can be found here: http://db.csail.mit.edu/labdata/labdata.html
 */
public class FileParserSpout extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(FileParserSpout.class);

    protected Configuration config;
    protected SpoutOutputCollector collector;
    protected TopologyContext context;

    // maps the property that the user wants to monitor (value from sd.properties:sd.parser.value_field)
    // to the corresponding field index
    private static final ImmutableMap<String, Integer> field_list = ImmutableMap.<String, Integer>builder()
            .put("temp", DatasetParsing.TEMP_FIELD)
            .put("humid", DatasetParsing.HUMID_FIELD)
            .put("light", DatasetParsing.LIGHT_FIELD)
            .put("volt", DatasetParsing.VOLT_FIELD)
            .build();

    private String file_path;
    private Integer rate;
    private String value_field;
    private int value_field_key;

    private long t_start;
    private long generated;
    private long emitted;
    private int reset;
    private long nt_execution;
    private long nt_end;
    private int par_deg;

    /**
     * Constructor: it expects the file path, the generation rate and the parallelism degree.
     * @param file path to the input data file
     * @param gen_rate if the argument value is -1 then the spout generates tuples at
     *                 the maximum rate possible (measure the bandwidth under this assumption);
     *                 if the argument value is different from -1 then the spout generates
     *                 tuples at the rate given by this parameter (measure the latency given
     *                 this generation rate)
     * @param p_deg source parallelism degree
     */
    FileParserSpout(String file, int gen_rate, int p_deg) {
        file_path = file;
        rate = gen_rate;        // number of tuples per second
        par_deg = p_deg;        // spout parallelism degree
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

        value_field = config.getString(Conf.PARSER_VALUE_FIELD);
        value_field_key = field_list.get(value_field);
    }

    /**
     * The method is called in an infinite loop by design, this means that the
     * stream is continuously generated from the data source file.
     * The parsing phase splits each line of the input dataset extracting date and time, deviceID
     * and information provided by the sensor about temperature, humidity, light and voltage.
     */
    @Override
    public void nextTuple() {
        File txt = new File(file_path);
        ArrayList<String> date = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        ArrayList<Integer> epoc = new ArrayList<>();
        ArrayList<String> devices = new ArrayList<>();
        ArrayList<Double> temperature = new ArrayList<>();
        ArrayList<Double> humidity = new ArrayList<>();
        ArrayList<Double> light = new ArrayList<>();
        ArrayList<Double> voltage = new ArrayList<>();

        /*  example of the result obtained by parsing one line
             0 = "2004-03-31"
             1 = "03:38:15.757551"
             2 = "2"
             3 = "1"
             4 = "122.153"
             5 = "-3.91901"
             6 = "11.04"
             7 = "2.03397"
         */

        // parsing phase
        try {
            Scanner scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                String[] fields = scan.nextLine().split("\\s+"); // regex quantifier (matches one or many whitespaces)
                //String date_str = String.format("%s %s", fields[DATE_FIELD], fields[TIME_FIELD]);
                if (fields.length >= 8) {
                    date.add(fields[DatasetParsing.DATE_FIELD]);
                    time.add(fields[DatasetParsing.TIME_FIELD]);
                    epoc.add(new Integer(fields[DatasetParsing.EPOCH_FIELD]));
                    devices.add(fields[DatasetParsing.DEVICEID_FIELD]);
                    temperature.add(new Double(fields[DatasetParsing.TEMP_FIELD]));
                    humidity.add(new Double(fields[DatasetParsing.HUMID_FIELD]));
                    light.add(new Double(fields[DatasetParsing.LIGHT_FIELD]));
                    voltage.add(new Double(fields[DatasetParsing.VOLT_FIELD]));

                    generated++;
                    LOG.debug("[FileParserSpout] DeviceID: {} Request property: {} {}",
                            fields[DatasetParsing.DEVICEID_FIELD], value_field, fields[value_field_key]);
                    LOG.debug("[FileParserSpout] Fields: {} {} {} {} {} {} {} {}",
                            fields[DatasetParsing.DATE_FIELD],
                            fields[DatasetParsing.TIME_FIELD],
                            fields[DatasetParsing.EPOCH_FIELD],
                            fields[DatasetParsing.DEVICEID_FIELD],
                            fields[DatasetParsing.TEMP_FIELD],
                            fields[DatasetParsing.HUMID_FIELD],
                            fields[DatasetParsing.LIGHT_FIELD],
                            fields[DatasetParsing.VOLT_FIELD]);
                } else
                    LOG.debug("[FileParserSpout] Incomplete record.");
            }
            scan.close();
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("The file {} does not exists", file_path);
            throw new RuntimeException("The file '" + file_path + "' does not exists");
        }

        // emit tuples
        int interval = 1000000000; // one second (nanoseconds)
        long t_init = System.nanoTime();
        ArrayList<Double> data;
        if (value_field_key == DatasetParsing.TEMP_FIELD)
            data = temperature;
        else if (value_field_key == DatasetParsing.HUMID_FIELD)
            data = humidity;
        else if (value_field_key == DatasetParsing.LIGHT_FIELD)
            data = light;
        else
            data = voltage;

        for (int i = 0; i < devices.size(); i++) {
            if (rate == -1) {       // at the maximum possible rate
                collector.emit(new Values(devices.get(i), data.get(i), System.nanoTime()));
                emitted++;
            } else {                // at the given rate
                long t_now = System.nanoTime();
                if (emitted >= rate) {
                    if (t_now - t_init <= interval)
                        active_delay(interval - (t_now - t_init));

                    emitted = 0;
                    t_init = System.nanoTime();
                    reset++;
                }
                collector.emit(new Values(devices.get(i), data.get(i), System.nanoTime()));
                emitted++;
                active_delay((double) interval / rate);
            }
        }

        nt_execution++;
        nt_end = System.nanoTime();
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
        outputFieldsDeclarer.declare(new Fields(Field.DEVICE_ID, Field.VALUE, Field.TIMESTAMP));
    }

    //------------------------------ private methods ---------------------------

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
