package ignite.beginner;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.log4j2.Log4J2Logger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;

public class Clustering101 {

    private static final Logger logger = LogManager.getLogger(Clustering101.class);

    private Clustering101() {}

    public static class Node101 {

        private Node101() {}

        public static void runClientNode(int instance) throws Exception {
            igniteNodeWithName(String.format("ClientNode-%d", instance), true)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        Thread.sleep(5 * 60 * 1000L);
                    });
        }

        public static void runClientNodeUsingCacheForSystemTimeInMillis(String cacheName) throws Exception {
            igniteNodeWithName("ClientNodeUsingCacheForSystemTimeInMillis", true)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCache<Long, String> cache = ignite.getOrCreateCache(cacheName);
                        while(true) {
                            long longValue = System.currentTimeMillis();
                            cache.put(longValue, String.valueOf(longValue));    // Add to cache

                            logger.info(cache.get(longValue));           // Retrieve from cache
                            Thread.sleep(1000);
                        }
                    });
        }

        public static void runServerNode(int instance) throws Exception {
            new IgniteNode()
                    .with(new IgniteConfiguration()
                            .setIgniteInstanceName("Clustering-NodeDiscovery-ServerNode")
                            .setGridLogger(new Log4J2Logger("log4j2.xml")))
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeUsingMulticastIP() throws Exception {
            new IgniteNode()
                    .with(new IgniteConfiguration()
                            .setIgniteInstanceName("Clustering-NodeDiscovery-MulticastWithIp")
                            .setPeerClassLoadingEnabled(true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryMulticastIpFinder().setMulticastGroup("239.255.255.250")))
                            .setGridLogger(new Log4J2Logger("log4j2.xml")))
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeUsingMulticastWithoutIP() throws Exception {
            new IgniteNode()
                    .with(new IgniteConfiguration()
                            .setIgniteInstanceName("Clustering-NodeDiscovery-MulticastWithoutIp")
                            .setPeerClassLoadingEnabled(true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryMulticastIpFinder()))
                            .setGridLogger(new Log4J2Logger("log4j2.xml")))
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeUsingStaticIPs(String staticIpAddresses) throws Exception {
            new IgniteNode()
                    .with(new IgniteConfiguration()
                            .setIgniteInstanceName("Clustering-NodeDiscovery-StaticIPAddresses")
                            .setPeerClassLoadingEnabled(true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryVmIpFinder().setAddresses(Arrays.asList(staticIpAddresses))))
                            .setGridLogger(new Log4J2Logger("log4j2.xml")))
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeWithUserAttributes(Map<String, String> userAttributes) throws Exception {
            new IgniteNode()
                    .with(new IgniteConfiguration()
                            .setIgniteInstanceName("ServerNodeWithUserAttributes")
                            .setPeerClassLoadingEnabled(true)
                            .setUserAttributes(userAttributes)
                            .setGridLogger(new Log4J2Logger("log4j2.xml")))
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s, UserAttributes: %s", ignite.name(), userAttributes.toString()));
                        while(true) {
                            Thread.sleep(1000);
                        }
                    });
        }
    }

    public static class ClusterGroup101 {

        private ClusterGroup101() {}

        public static void runServerNodeBroadcastingComputeTask2AllNodes() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2AllNodes", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCompute compute = ignite.compute();
                        compute.broadcast(() -> {
                            logger.info("Ran Compute Task on all nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2RemoteNodes() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2RemoteNodes", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCompute compute = ignite.compute();
                        compute.broadcast(() -> {
                            logger.info("Ran Compute Task on remote nodes only");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2DataNodesWithCacheName(final String cacheName, final String nodeType) throws Exception {
            igniteNodeWithName(String.format("BroadcastComputeTask2DataNodesWithCacheName-%s", nodeType), false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup cacheGroup;
                        switch(nodeType) {
                            case "DATA_NODES":
                                cacheGroup = cluster.forDataNodes(cacheName);   // Nodes where elements of cache are kept
                                break;
                            case "CLIENT_NODES":
                                cacheGroup = cluster.forClientNodes(cacheName); // Client nodes accessing the cache
                                break;
                            default:
                                cacheGroup = cluster.forCacheNodes(cacheName);  // All nodes where cache is deployed
                        }

                        IgniteCompute compute = ignite.compute(cacheGroup);
                        compute.broadcast(() -> {
                            logger.info(String.format("Ran Compute Task on nodes - cacheName: %s, nodeType: %s", cacheName, nodeType));
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesWithAttributeKeyValue(final String key, final String value) throws Exception {
            igniteNodeWithName("BroadcastComputeTask2NodesWithAttributeKeyValue", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forAttribute(key, value);
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info(String.format("Ran Compute Task on Nodes with Attribute - Key: %s, Value: %s", key, value));
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesBasedOnAge() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2NodesBasedOnAge", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup forOldestClusterGroup = cluster.forOldest();
                        IgniteCompute computeGroup = ignite.compute(forOldestClusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Oldest Node");
                        });

                        ClusterGroup forYoungestClusterGroup = cluster.forOldest();
                        computeGroup = ignite.compute(forYoungestClusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Youngest Node");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2LocalNode() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2LocalNode", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup forLocalClusterGroup = cluster.forLocal();
                        IgniteCompute computeGroup = ignite.compute(forLocalClusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Local Node");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2ClientNodes() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2ClientNodes", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forClients();
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Client Nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2ServerNodes() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2ServerNodes", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forServers();
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Server Nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesBasedOnMetrics() throws Exception {
            igniteNodeWithName("BroadcastComputeTask2NodesBasedOnMetrics", false)
                    .start(ignite -> {
                        logger.info(String.format("Node - name: %s", ignite.name()));
                        IgniteCluster cluster = ignite.cluster();
                        // ClusterMetrics class has all the metrics defined
                        ClusterGroup clusterGroup = cluster.forPredicate(node -> node.metrics().getAverageCpuLoad() < 0.5);
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task based on cluster node metrics - Avg. CPLU Load < 0.5");
                        });
                        Thread.sleep(1 * 60 * 1000);
                    });
        }
    }

    /**
     * Local - fastest data access and modification. Features - Data expiry & eviction, SQL Query and Transaction Management
     *
     * Default caching mode - Partitioned, for high scalability, write performance. Use only - huge dataset, frequent update
     *
     * Replicate, for high availability, read performance. Use only - small cache, infrequent data update
     */
    public static class CachingTopology101 {
        private CachingTopology101() {}

        public static void runServerWithLocalCache() {

        }

        /**
         *
         */
        public static void runServerWithPartitionedCache() {

        }

        public static void runServerWithReplicatedCache() {

        }
    }

    public static IgniteNode igniteNodeWithName(String nodeName, boolean clientMode) throws IgniteCheckedException {
        return new IgniteNode()
                .with(new IgniteConfiguration()
                        .setIgniteInstanceName(nodeName)
                        .setClientMode(clientMode)
                        .setPeerClassLoadingEnabled(true)
                        .setGridLogger(new Log4J2Logger("log4j2.xml")));
    }
}
