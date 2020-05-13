package ignite.beginner;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Ignite101Main {

    private static final Logger logger = LogManager.getLogger(Ignite101Main.class);

    public static void main(String[] args) throws Exception {
//        helloWorld();
        
//        Clustering101.Node101.runBasicClientNode(1);
//        Clustering101.Node101.runBasicServerNode(1);
//
//        Clustering101.Node101.runClientNode(1);
//        Clustering101.Node101.runServerNodeUsingMulticastIP();
//        Clustering101.Node101.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.Node101.runServerNodeUsingStaticIPs("127.0.0.1:47501..47510");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2AllNodes();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2RemoteNodes();
//
//        Clustering101.Node101.runClientNodeCreatingAndUsingCacheForSystemTimeInMillis("systemTimeInMillis");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "ALL_NODES");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "DATA_NODES");
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2DataNodesWithCacheName("systemTimeInMillis", "CLIENT_NODES");
//
//        Clustering101.Node101.runServerNodeWithUserAttributes(Map.of("instance", "1"));
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesWithAttributeKeyValue("instance", "1");
//
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesBasedOnAge();
//
//        Clustering101.Node101.runClientNode(1);
//        Clustering101.Node101.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2LocalNode();  // Only on same instance
//
//        Clustering101.Node101.runClientNode(1);
//        Clustering101.Node101.runClientNode(2);
//        Clustering101.Node101.runClientNode(3);
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2ClientNodes();
//
//        Clustering101.Node101.runClientNode(1);
//        Clustering101.Node101.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2ServerNodes();
//
//        Clustering101.Node101.runServerNodeUsingMulticastWithoutIP();
//        Clustering101.ClusterGroup101.runServerNodeBroadcastingComputeTask2NodesBasedOnMetrics();

        InMemoryDataGrids101.JCacheBasedDataGrid.runReplicatedAtomicLRUCacheServerNode();
    }

    /**
     * First program to start Ignite server instance by creating a basic cache doing putting and pulling operations
     */
    public static void helloWorld() {
        try(Ignite ignite = Ignition.start()) {
            IgniteCache<Integer, String> cache = ignite.getOrCreateCache("firstIgniteCache");

            for (int i = 0; i < 10; i++) {
                cache.put(i, Integer.toString(i));
            }
            logger.log(Level.INFO, "Added elements to cache");

            for (int i = 0; i < 10; i++) {
                logger.log(Level.INFO, "Fetched - key: {}, value: {}", i, cache.get(i));
            }
        }
    }
}
