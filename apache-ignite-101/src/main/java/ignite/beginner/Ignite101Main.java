package ignite.beginner;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

public class Ignite101Main {
    public static void main(String[] args) throws Exception {
//        helloWorld();

//        Clustering101.Node.runClientNode(1);
//        Clustering101.Node.runServerNodeUsingMulticastIP();
//        Clustering101.Node.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.Node.runServerNodeUsingStaticIPs("127.0.0.1:47501..47510");
//        Clustering101.ClusterGroup.runServerNodeBroadcastingComputeTask2AllNodes();
//        Clustering101.ClusterGroup.runServerNodeBroadcastingComputeTask2RemoteNodes();

//        Clustering101.Node.runClientNodeUsingCacheForSystemTimeInMillis("systemTimeInMillis");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "ALL_NODES");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "DATA_NODES");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "CLIENT_NODES");

//        Clustering101.Node.runServerNodeWithUserAttributes(Map.of("instance", "1"));
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesWithAttributeKeyValue("instance", "1");

//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesBasedOnAge();

//        Clustering101.Node.runClientNode(1);
//        Clustering101.Node.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2LocalNode();  // Only on same instance

//        Clustering101.Node.runClientNode(1);
//        Clustering101.Node.runClientNode(2);
//        Clustering101.Node.runClientNode(3);
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2ClientNodes();

//        Clustering101.Node.runClientNode(1);
//        Clustering101.Node.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2ServerNodes();

//        Clustering101.Node.runServerNodeUsingMulticastWithoutIP();
        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesBasedOnMetrics();
    }

    /**
     * First program to start Ignite server instance by creating a basic cache doing putting and pulling operations
     */
    private static void helloWorld() {
        try(Ignite ignite = Ignition.start()) {
            IgniteCache<Integer, String> cache = ignite.getOrCreateCache("firstIgniteCache");

            for (int i = 0; i < 10; i++) {
                cache.put(i, Integer.toString(i));
            }
            System.out.println("Added elements to cache");

            for (int i = 0; i < 10; i++) {
                System.out.println(String.format("Fetched - key: %d, value: %s", i, cache.get(i)));
            }
        }
    }
}
