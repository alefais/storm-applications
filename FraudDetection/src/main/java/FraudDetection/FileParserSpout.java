package FraudDetection;

import Constants.FraudDetectionConstants.Field;
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
 * The spout is in charge of reading the input data file, parsing it
 * and generating the stream of records toward the FraudPredictorBolt.
 *
 * Format of the input file:
 * EntityID,record_of<EntityID, op_type>
 *
 * @author Alessandra Fais
 */
public class FileParserSpout extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(FileParserSpout.class);

    protected Configuration config;
    protected SpoutOutputCollector collector;
    protected TopologyContext context;
    protected Map<String, Fields> fields;

    private String file_path;
    private String split_regex;
    private Integer rate;

    private long t_start;
    private long generated;
    private long emitted;
    private long nt_execution;
    private long nt_end;

    /**
     * Constructor: it expects the file path and the split expression needed
     * to parse the file (it depends on the format of the input data)
     * @param file path to the input data file
     * @param split split expression
     * @param gen_rate if the argument value is -1 then the spout generates tuples at
     *                 the maximum rate possible (measure the bandwidth under this assumption);
     *                 if the argument value is different from -1 then the spout generates
     *                 tuples at the rate given by this parameter (measure the latency given
     *                 this generation rate)
     */
    FileParserSpout(String file, String split, Integer gen_rate) {
        file_path = file;
        split_regex = split;
        rate = gen_rate;        // number of tuples per second
        generated = 0;          // total number of generated tuples
        emitted = 0;            // total number of emitted tuples
        nt_execution = 0;       // number of executions of nextTuple() method
    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        LOG.info("[FileParserSpout] Started.");

        t_start = System.nanoTime(); // spout start time in nanoseconds

        config = Configuration.fromMap(conf);
        collector = spoutOutputCollector;
        context = topologyContext;
    }

    /**
     * The method is called in an infinite loop by design, this means that the
     * stream is continuously generated from the data source file.
     * The parsing phase splits each line of the input dataset in 2 parts:
     * - first string identifies the customer (entityID)
     * - second string contains the transactionID and the transaction type
     */
    @Override
    public void nextTuple() {
        File txt = new File(file_path);
        ArrayList<String> entities = new ArrayList<>();
        ArrayList<String> records = new ArrayList<>();

        // parsing phase
        try {
            Scanner scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                String[] line = scan.nextLine().split(split_regex, 2);
                entities.add(line[0]);
                records.add(line[1]);
                generated++;
                LOG.debug("[FileParserSpout] EntityID: {} Record: {}", line[0], line[1]);
            }
            scan.close();
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("The file {} does not exists", file_path);
            throw new RuntimeException("The file '" + file_path + "' does not exists");
        }

        // emit tuples
        if (rate == -1) { // at the maximum possible rate
            for (int i = 0; i < entities.size(); i++) {
                collector.emit(new Values(entities.get(i), records.get(i), System.nanoTime()));
                emitted++;
            }
        } else { // at the given rate, with a bursty emission
            int interval = 1000000000; // one second (nanoseconds)
            long t_start = System.nanoTime();

            for (int i = 0; i < entities.size(); i++) {
                if (emitted > rate) {
                    long t_now = System.nanoTime();
                    if (t_now - t_start <= interval) {
                        active_delay(interval - (t_now - t_start));
                    }
                    emitted = 0;
                    t_start = System.nanoTime();
                }
                collector.emit(new Values(entities.get(i), records.get(i), System.nanoTime()));
                emitted++;
                active_delay((double)rate / interval);
            }
        }

        nt_execution++;
        nt_end = System.nanoTime();
    }

    @Override
    public void close() {
        long t_elapsed = (nt_end - t_start) / 1000000;

        LOG.info("[FileParserSpout] Terminated after {} generations.", nt_execution);
        LOG.info("[FileParserSpout] Generated {} tuples in {} ms. Emitted {} tuples in {} ms. " +
                        "Source bandwidth is {} tuples per second.",
                generated, t_elapsed,
                emitted, t_elapsed,
                emitted / (t_elapsed / 1000));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.RECORD_DATA, Field.TIMESTAMP));
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
        LOG.debug("[FileParserSpout] waited for {} ns.", nsecs);
    }
}
