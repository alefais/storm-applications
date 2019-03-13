package FraudDetection;

import Constants.FraudDetectionConstants.Field;
import Meter.SpoutMeterHook;
import Util.config.Configuration;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static Util.config.Configuration.METRICS_ENABLED;

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
    private boolean completed;

    /**
     * Constructor: it expects the file path and the split expression needed
     * to parse the file (it depends on the format of the input data)
     * @param file path to the input data file
     * @param split split expression
     */
    FileParserSpout(String file, String split) {
        file_path = file;
        split_regex = split;
        completed = false;
    }

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        LOG.info("[FileParserSpout] Started.");

        config = Configuration.fromMap(conf);
        collector = spoutOutputCollector;
        context = topologyContext;

        if (config.getBoolean(METRICS_ENABLED, false)) {
            context.addTaskHook(new SpoutMeterHook());
        }
    }

    /**
     * It is called in an infinite loop by design. For this reason after the
     * generation is completed the method simply calls a sleep (no operation).
     */
    @Override
    public void nextTuple() {

        long t_start = System.nanoTime();
        long count = 0;

        if (completed) {
            Utils.sleep(1000); // sleep for 1 second
        } else {
            //long startTime = System.nanoTime(); // nanoseconds
            //long endTime = 0; // nanoseconds
            //long timeElapsed = 0; // milliseconds

            File txt = new File(file_path);
            ArrayList<String> entities = new ArrayList<>();
            ArrayList<String> records = new ArrayList<>();

            // generate for at least 60 seconds (60000 ms)
            //while (timeElapsed < 60000) {

                /*
                    split each line of the input dataset in 2 parts:
                    - first string identifies the customer (entityID)
                    - second string contains the transactionID and the transaction type
                 */
                try {
                    Scanner scan = new Scanner(txt);
                    while (scan.hasNextLine()) {
                        String record = scan.nextLine();
                        String[] line = record.split(split_regex, 2);
                        entities.add(line[0]);
                        records.add(line[1]);
                        count++;
                        LOG.debug("[FileParserSpout] EntityID: {}   Record: {}", line[0], line[1]);
                    }
                    LOG.info("[FileParserSpout] No more lines to read, closing file...");
                    scan.close();
                } catch (FileNotFoundException | NullPointerException e) {
                    LOG.error("The file {} does not exists", file_path);
                    throw new RuntimeException("The file '" + file_path + "' does not exists");
                }

            //    endTime = System.nanoTime();
            //    timeElapsed = (endTime - startTime) / 1000000;
            //}

            long b_start = System.nanoTime(); // nanoseconds
            long b_end = 0; // nanoseconds
            long b_elapsed = 0; // milliseconds
            long emitted = 0;
            int sample = 0;
            // emit records (generate the stream) and measure bw at each second
            for (int i = 0; i < entities.size(); i++) {
                if (b_elapsed >= 1000 && sample == 0) { // 1 second
                    sample++;
                    LOG.info("[FileParserSpout] Source bandwidth (emission rate): {} elements per second.",
                            emitted / b_elapsed * 1000);
                }

                collector.emit(new Values(entities.get(i), records.get(i)));
                emitted++;

                b_end = System.nanoTime();
                b_elapsed = (b_end - b_start) / 1000000;
            }
            completed = true;

            long t_end = System.nanoTime();
            LOG.info("[FileParserSpout] Generated the whole stream of {} elements in {} ms.",
                    count, (t_end - t_start) / 1000000);  // spout execution time
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.RECORD_DATA));
    }

    @Override
    public void close() {
        LOG.info("[FileParserSpout] Terminated.");
    }
}
