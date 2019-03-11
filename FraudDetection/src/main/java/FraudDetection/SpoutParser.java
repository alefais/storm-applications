package FraudDetection;

import Constants.Field;
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

/**
 * The spout is in charge of reading the input raw data file, parsing it
 * and generating the stream of words toward the FraudPredictorBolt.
 */
public class SpoutParser extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(SpoutParser.class);

    SpoutOutputCollector collector;

    private String file_path;
    private String split_regex;
    private boolean completed;

    /**
     * Constructor: it expects the file path and the split expression needed
     * to parse the file (it depends on the format of the input data)
     * @param file path to the input data file
     * @param split split expression
     */
    SpoutParser(String file, String split) {
        file_path = file;
        split_regex = split;
        completed = false;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        LOG.info("[SpoutParser] Started.");
        collector = spoutOutputCollector;
    }

    /**
     * It is called in an infinite loop by design. For this reason after the
     * generation is completed the method simply calls a sleep (no operation).
     */
    @Override
    public void nextTuple() {

        long t_start = System.nanoTime();

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

                // read input dataset and write words into an array list
                // split each line on the first occurrence of comma character
                // first string identifies the entity (source bank account) of the payment
                // second string identifies the destination bank account and the type of the operation
                try {
                    Scanner scan = new Scanner(txt);
                    while (scan.hasNextLine()) {
                        String line = scan.nextLine();
                        entities.add(line.split(split_regex)[0]);
                        records.add(line);
                        LOG.info("[SpoutParser] EntityID: {}   Record: {}", line.split(split_regex)[0], line);
                    }
                    scan.close();
                } catch (FileNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                }

            //    endTime = System.nanoTime();
            //    timeElapsed = (endTime - startTime) / 1000000;
            //}

            // send words to the counter bolt (generate the stream)
            for (int i = 0; i < entities.size(); i++) {
                collector.emit(new Values(entities.get(i), records.get(i)));
            }
            completed = true;
            LOG.info("[SpoutParser] generated the whole stream.");

            long t_end = System.nanoTime();
            LOG.warn("[SpoutParser] nextTuple ~ {} ms.", (t_end - t_start) / 1000000); // spout execution time
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ENTITY_ID, Field.RECORD_DATA));
    }

    @Override
    public void close() {
        LOG.info("[SpoutParser] Terminated.");
    }
}
