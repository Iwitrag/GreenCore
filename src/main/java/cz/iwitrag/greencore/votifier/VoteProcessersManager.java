package cz.iwitrag.greencore.votifier;

import cz.iwitrag.greencore.votifier.processers.VoteProcesser;

import java.util.HashMap;
import java.util.Map;

public class VoteProcessersManager {

    private VoteProcessersManager() {}

    private static Map<String, VoteProcesser> registeredProcessers = new HashMap<>();

    public static void registerVoteProcesser(String service, VoteProcesser voteProcesser) {
        registeredProcessers.put(service, voteProcesser);
    }

    public static void unregisterVoteProcesser(String service) {
        registeredProcessers.remove(service);
    }

    public static VoteProcesser getRegisteredVoteProcesser(String service) {
        return registeredProcessers.get(service);
    }

}
