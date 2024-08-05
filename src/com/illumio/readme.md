<b>Assumptions made:</b>

- The format of the flow log file will be as follows with no extra space at the end:
```
timestamp,message
1722725616000,2 638734183345 eni-027fb7097740170c9 172.31.14.155 101.206.211.7 22 44766 6 4 216 1722725616 1722725674 ACCEPT OK
...
```
This was based on a real vpc flow log data that I downloaded from my personal AWS account and
uses the default format`${version} ${account-id} ${interface-id} ${srcaddr} ${dstaddr} ${srcport} ${dstport} ${protocol} ${packets} ${bytes} ${start} ${end} ${action} ${log-status}`

Ref: https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html

According to the doc, users can also define their own custom format so I added an option to define a custom format as well.
But this program will use the default format if not specified.

Note: To retrieve that format, you can go to your AWS account and copy and paste the format of the flow log.

- The format of the lookup table file will be as follows:
```
dstport,protocol,tag
0,icmp,icmp_tag
...
```
- The output file will contain tags in lowercase. All conversions will be converted to lowercase to ensure case insensitivity.
- If there are any untagged elements, they will be marked as "untagged" in the output file

<b>How to run:</b>

Starting from the root of the repository:
```
javac -d out src/com/illumio/FlowLogAnalyzer.java src/com/illumio/Output.java src/com/illumio/Main.java

java -cp out com.illumio.Main "test/resources/sampleLookupTable" "test/resources/sample-flow-log-data"
```
The first argument (ex: "test/resources/sampleLookupTable") is the directory of the lookup table.
The second argument (ex: "test/resources/sample-flow-log-data") is the directory of the flow log file.
The output directory will be printed out. Check there for the output.

If you have flow logs that do not use the default format, you can still specify them as well by adding an additional
argument.

```
javac -d out src/com/illumio/FlowLogAnalyzer.java src/com/illumio/Output.java src/com/illumio/Main.java

java -cp out com.illumio.Main "test/resources/custom-flow-log-format-lookup-table" "test/resources/custom-flow-log-format-flow-log" "${dstaddr} ${protocol}"
```

^^ In this example, "${dstaddr} ${protocol}" is the additional argument specifying the custom format used.

<b>Tests done:</b>
All tests are done under the `test` folder in `FlowLogAnalyzerTests`. You can run them by running the main method.

Test cases tested:

- Testing when flow log file and look up table file are empty
- Happy path (basic test to see if all tags and dstport/protocol mappings match)
- Tags are case insensitive
- Any untagged elements are tagged as "untagged"
- Ability to add a flow log file with a custom format other than the default

For the unit tests, I did not use any extra libraries because the assignment specified not to but if I can, I would have
used JUnit.