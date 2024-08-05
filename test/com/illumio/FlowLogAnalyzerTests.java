package com.illumio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FlowLogAnalyzerTests {
    public static void happyPath() throws IOException, NoSuchAlgorithmException {
        System.out.println("Testing happy path");
        FlowLogAnalyzer flowLogAnalyzer = new FlowLogAnalyzer("test/resources/sampleLookupTable",
                "test/resources/sample-flow-log-data");
        String output = flowLogAnalyzer.analyzeFlowlog();
        compareChecksum("test/expectedOutput/output-happy-path.csv", output);
        deleteFile(output);
        System.out.println();
    }

    public static void emptyFile() throws IOException, NoSuchAlgorithmException {
        System.out.println("Testing empty files");
        FlowLogAnalyzer flowLogAnalyzer = new FlowLogAnalyzer("test/resources/emptyFile",
                "test/resources/emptyFile");
        String output = flowLogAnalyzer.analyzeFlowlog();
        compareChecksum("test/expectedOutput/output-expectedEmpty.csv", output);
        deleteFile(output);
        System.out.println();
    }

    public static void untaggedElements() throws IOException, NoSuchAlgorithmException {
        System.out.println("Testing untagged elements");
        FlowLogAnalyzer flowLogAnalyzer = new FlowLogAnalyzer(
                "test/resources/untagged-elements-lookup-table",
                "test/resources/untagged-elements-flow-log");
        String output = flowLogAnalyzer.analyzeFlowlog();
        compareChecksum("test/expectedOutput/output-untagged-elements.csv", output);
        deleteFile(output);
        System.out.println();
    }

    public static void caseSensitivity() throws IOException, NoSuchAlgorithmException {
        System.out.println("Testing case sensitivity");
        FlowLogAnalyzer flowLogAnalyzer = new FlowLogAnalyzer(
                "test/resources/case-sensitive-lookup-table",
                "test/resources/case-sensitive-flow-log");
        String output = flowLogAnalyzer.analyzeFlowlog();
        compareChecksum("test/expectedOutput/output-case-sensitivity.csv", output);
        deleteFile(output);
        System.out.println();
    }

    public static void customFlowLogFormat() throws IOException, NoSuchAlgorithmException {
        System.out.println("Custom flow log format");
        String customFormat = "${dstaddr} ${protocol}";
        FlowLogAnalyzer flowLogAnalyzer = new FlowLogAnalyzer(
                "test/resources/custom-flow-log-format-lookup-table",
                "test/resources/custom-flow-log-format-flow-log",
                customFormat);
        String output = flowLogAnalyzer.analyzeFlowlog();
        compareChecksum("test/expectedOutput/output-custom-flow-log-format.csv", output);
        deleteFile(output);
        System.out.println();
    }

    private static void deleteFile(String path) {
        File file = new File(path);

        if (!file.delete()) {
            System.out.println("Failed to delete the file: " + path);
        }
    }

    private static String checksum(MessageDigest digest, File file) throws IOException {
        // Get file input stream for reading the file
        // content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // read the data from file and update that data in
        // the message digest
        while ((bytesCount = fis.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        };

        // close the input stream
        fis.close();

        // store the bytes returned by the digest() method
        byte[] bytes = digest.digest();

        // this array of bytes has bytes in decimal format
        // so we need to convert it into hexadecimal format

        // for this we create an object of StringBuilder
        // since it allows us to update the string i.e. its
        // mutable
        StringBuilder sb = new StringBuilder();

        // loop through the bytes array
        for (int i = 0; i < bytes.length; i++) {

            // the following line converts the decimal into
            // hexadecimal format and appends that to the
            // StringBuilder object
            sb.append(Integer
                    .toString((bytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }

        // finally we return the complete hash
        return sb.toString();
    }

    // Ref: https://www.geeksforgeeks.org/how-to-generate-md5-checksum-for-files-in-java/
    private static String getCheckSum(String path) throws NoSuchAlgorithmException, IOException {
        File file = new File(path);

        // instantiate a MessageDigest Object by passing
        // string "MD5" this means that this object will use
        // MD5 hashing algorithm to generate the checksum
        MessageDigest mdigest = MessageDigest.getInstance("MD5");

        // Get the checksum
        String checksum = checksum(mdigest, file);
        return checksum;
    }

    private static void compareChecksum(String file1, String file2) throws NoSuchAlgorithmException, IOException {
        String checkSum1 = getCheckSum(file1);
        String checkSum2 = getCheckSum(file2);
        if (checkSum1.equals(checkSum2)) {
            System.out.println("Expected and output file match");
        } else {
            System.out.println("Expected and output files don't match");
        }
    }

    public static void main(String[] args) {
        try {
            happyPath();
            emptyFile();
            caseSensitivity();
            untaggedElements();
            customFlowLogFormat();
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(String.format("Exception occurred: %s", e.getMessage()));
        }
    }
}
