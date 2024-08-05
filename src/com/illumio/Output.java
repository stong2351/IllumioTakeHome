package com.illumio;

import java.util.HashMap;
import java.util.Map;

public class Output {
    Map<String, Integer> tagToCount;
    Map<Integer, Map<String, Integer>> portAndProtocolCount;

    public Map<String, Integer> getTagToCount() {
        return tagToCount;
    }

    public Map<Integer, Map<String, Integer>> getPortAndProtocolCount() {
        return portAndProtocolCount;
    }

    public void addTag(String tag) {
        String tagStandardized = tag.toLowerCase().trim();
        tagToCount.put(tagStandardized, tagToCount.getOrDefault(tagStandardized, 0) + 1);
    }

    public void addPortAndProtocol(int port, String protocol) {
        String protocolStandardized = protocol.toLowerCase().trim();
        portAndProtocolCount.computeIfAbsent(port, p -> new HashMap<>());
        Map<String, Integer> protocolCount = portAndProtocolCount.get(port);
        protocolCount.put(protocolStandardized, protocolCount.getOrDefault(protocolStandardized, 0) + 1);
    }

    public Output() {
        tagToCount = new HashMap<>();
        portAndProtocolCount = new HashMap<>();
    }
}
