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

    private long t_start;
    private long generated;
    private long nt_execution;
    private long nt_end;

    /**
     * Constructor: it expects the file path and the split expression needed
     * to parse the file (it depends on the format of the input data)
     * @param file path to the input data file
     * @param split split expression
     */
    FileParserSpout(String file, String split) {
        file_path = file;
        split_regex = split;
        generated = 0;          // total number of generated tuples
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
        for (int i = 0; i < entities.size(); i++) {
            collector.emit(new Values(entities.get(i), records.get(i)));
        }

        nt_execution++;
        nt_end = System.nanoTime(); // method end time in nanoseconds
    }

    @Override
    public void close() {
        LOG.info("[FileParserSpout] Terminated after {} generations.", nt_execution);
        LOG.info("[FileParserSpout] Generation ended in {} ms.", (nt_end - t_start) / 1000000);

        long t_end = System.nanoTime();  // spout stop time in nanoseconds
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds
        LOG.info("[FileParserSpout] Generated {} elements in {} ms, bandwidth is {} elements per second.",
                generated, t_elapsed, generated / (t_elapsed / 1000));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.RECORD_DATA));
    }
}
