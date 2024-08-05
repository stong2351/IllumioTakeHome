package com.illumio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlowLogAnalyzer {
    // mapping of IANA protocol numbers from https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
    // I downloaded the text file so that I wouldn't have to create the mapping by manually
    private final String PROTOCOL_NUMBERS_MAPPING_FILE = "src/resources/protocol-numbers-1.csv";
    private final String UNASSIGNED_PROTOCOL_KEYWORD = "unassigned";
    private final String DEFAULT_LOG_FORMAT = "${version} ${account-id} ${interface-id} ${srcaddr} ${dstaddr} " +
            "${srcport} ${dstport} ${protocol} ${packets} ${bytes} ${start} ${end} ${action} ${log-status}";
    private final String OUTPUT_PREFIX = "src/output/output-%s.csv";
    private static final String DSTPORT = "${dstport}";
    private static final String PROTOCOL = "${protocol}";
    private static final String UNTAGGED = "untagged";

    private static final String TAG_COUNTS_HEADER = "Tag Counts:\n";
    private static final String PORT_PROTOCOL_COUNTS_HEADER = "Port/Protocol Combination Counts:\n";

    private static final String TAG_COLUMN = "Tag";
    private static final String COUNT_COLUMN = "Count";

    private static final String PORT_COLUMN = "Port";
    private static final String PROTOCOL_COLUMN = "Protocol";
    private static final String COUNTS_COLUMN = "Counts";

    private String logFormat;
    private String lookupTableDir;
    private String flowLogDir;

    public FlowLogAnalyzer(String lookupTableDir, String flowLogDir) {
        this.lookupTableDir = lookupTableDir;
        this.flowLogDir = flowLogDir;
        logFormat = DEFAULT_LOG_FORMAT;
    }

    public FlowLogAnalyzer(String lookupTableDir, String flowLogDir, String logFormat) {
        this.lookupTableDir = lookupTableDir;
        this.flowLogDir = flowLogDir;
        this.logFormat = logFormat;
    }

    private Map<Integer, Map<String, String>> processLookupTableFile() throws IOException {
        Map<Integer, Map<String, String>> lookupTableMap = new HashMap<>();
        Reader reader = new FileReader(lookupTableDir);

        try(BufferedReader bufferedReader =
                    new BufferedReader(reader)){

            String line = bufferedReader.readLine();
            while((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split(",");
                int dstport = Integer.parseInt(arr[0]);
                String protocol = arr[1].toLowerCase();
                String tag = arr[2].toLowerCase();

                lookupTableMap.computeIfAbsent(dstport, port -> new HashMap<>());
                lookupTableMap.get(dstport).put(protocol, tag);
            }

        }
        return lookupTableMap;
    }

    private Map<Integer, String> getProtocolNumbersMapping() throws IOException {
        Map<Integer, String> protocolNumbersMapping = new HashMap<>();
        Reader reader = new FileReader(PROTOCOL_NUMBERS_MAPPING_FILE);
        try(BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.readLine();  // first line is always the header
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split(",");
                int num = Integer.parseInt(arr[0]);
                String keyword = arr[1].toLowerCase();

                if (keyword.isEmpty()) {
                    keyword = UNASSIGNED_PROTOCOL_KEYWORD;
                }
                protocolNumbersMapping.put(num, keyword);
            }
        }

        return protocolNumbersMapping;
    }

    // Return the indices of 'dstport' and 'protocol'
    private int[] processLogFormat(String logFormat) {
        String[] arr = logFormat.split(" ");
        int[] result = new int[2];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(DSTPORT)) {
                result[0] = i;
            }

            if (arr[i].equals(PROTOCOL)) {
                result[1] = i;
            }
        }
        return result;
    }

    private Output readAndProcessFlowlog(Map<Integer, Map<String, String>> lookupTableMap) throws IOException {
        int[] logFormatIndices = processLogFormat(logFormat);
        Map<Integer, String> protocolNumbersMapping = getProtocolNumbersMapping();
        int dstportIndex = logFormatIndices[0];
        int protocolIndex = logFormatIndices[1];
        Output output = new Output();

        Reader reader = new FileReader(flowLogDir);
        try(BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.readLine();  // first line is always the header
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] arr = line.split(",");
                String flowLog = arr[1];
                String[] values = flowLog.split(" ");

                int dstport = Integer.parseInt(values[dstportIndex]);
                int protocol = Integer.parseInt(values[protocolIndex]);

                String protocolString = protocolNumbersMapping.getOrDefault(protocol, UNASSIGNED_PROTOCOL_KEYWORD);
                String tag = (!lookupTableMap.containsKey(dstport) || !lookupTableMap.get(dstport).containsKey(protocolString))
                        ? UNTAGGED : lookupTableMap.get(dstport).get(protocolString);
                output.addTag(tag);
                output.addPortAndProtocol(dstport, protocolString);
            }
        }

        return output;
    }

    private void writeTagCounts(BufferedWriter bufferedWriter, Map<String, Integer> tagToCount) throws IOException {
        bufferedWriter.write(TAG_COUNTS_HEADER + "\n");
        String tagColumn = String.format("%-20s", TAG_COLUMN);
        String countColumn = String.format("%-20s", COUNT_COLUMN);
        bufferedWriter.write(String.format("%s%s\n", tagColumn, countColumn));
        for (String tag : tagToCount.keySet()) {
            String tagEntry = String.format("%-20s", tag);
            String countEntry = String.format("%-20d", tagToCount.get(tag));
            bufferedWriter.write(String.format("%s%s\n", tagEntry, countEntry));
        }
        bufferedWriter.write("\n");
    }

    private void writePortProtocolCombination(BufferedWriter bufferedWriter,
                                              Map<Integer, Map<String, Integer>> portProtocolCombination) throws IOException {
        bufferedWriter.write(PORT_PROTOCOL_COUNTS_HEADER + "\n");
        String portColumn = String.format("%-20s", PORT_COLUMN);
        String protocolColumn = String.format("%-20s", PROTOCOL_COLUMN);
        String countsColumn = String.format("%-20s", COUNTS_COLUMN);
        bufferedWriter.write(String.format("%s%s%s\n", portColumn, protocolColumn, countsColumn));
        for (int port : portProtocolCombination.keySet()) {
            for (String protocol : portProtocolCombination.get(port).keySet()) {
                String portEntry = String.format("%-20d", port);
                String protocolEntry = String.format("%-20s", protocol);
                String countEntry = String.format("%-20d", portProtocolCombination.get(port).get(protocol));
                bufferedWriter.write(String.format("%s%s%s\n", portEntry, protocolEntry, countEntry));
            }
        }
    }

    private String writeToOutputFile(Output output) throws IOException {
        String outputDir = String.format(OUTPUT_PREFIX, UUID.randomUUID());
        FileWriter file = new FileWriter(outputDir);

        try (BufferedWriter bufferedWriter = new BufferedWriter(file)) {
            writeTagCounts(bufferedWriter, output.getTagToCount());
            writePortProtocolCombination(bufferedWriter, output.getPortAndProtocolCount());
        }

        return outputDir;
    }

    public String analyzeFlowlog() throws IOException {
        try {
            Map<Integer, Map<String, String>> lookupTableMap = processLookupTableFile();
            Output output = readAndProcessFlowlog(lookupTableMap);
            String outputFile = writeToOutputFile(output);

            return outputFile;
        } catch(Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            throw e;
        }
    }
}
