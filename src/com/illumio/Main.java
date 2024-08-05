package com.illumio;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String lookupTableDir = args[0];
        String flowLogDir = args[1];
        String customFormat;
        FlowLogAnalyzer flowLogAnalyzer;
        if (args.length == 3) {
            customFormat = args[2];
            flowLogAnalyzer = new FlowLogAnalyzer(lookupTableDir, flowLogDir, customFormat);
        } else {
            flowLogAnalyzer =
                    new FlowLogAnalyzer(lookupTableDir, flowLogDir);
        }

        try {
            String output = flowLogAnalyzer.analyzeFlowlog();
            System.out.println(output);
        } catch (IOException e) {
            System.out.println(String.format("An error occurred: %s", e.getMessage()));
        }

        System.out.println("Done");
    }
}
