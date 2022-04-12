package ignite.beginner;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.cluster.ClusterGroup;
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

        public static void runBasicClientNode(int instance) throws Exception {
            new IgniteNode().with(IgniteHelperFactory.igniteConfig(String.format("BasicClientNode-%d", instance), true))
                    .execute(ignite -> Thread.sleep(5 * 60 * 1000L));
        }

        public static void runBasicServerNode(int instance) throws Exception {
            new IgniteNode().with(IgniteHelperFactory.igniteConfig(String.format("BasicServerNode-%d", instance), false))
                    .execute(ignite -> Thread.sleep(5 * 60 * 1000L));
        }

        public static void runClientNode(int instance) throws Exception {
            IgniteHelperFactory.igniteNodeWithName(String.format("ClientNode-%d", instance), true, true)
                    .execute(ignite -> Thread.sleep(5 * 60 * 1000L));
        }

        public static void runClientNodeCreatingAndUsingCacheForSystemTimeInMillis(String cacheName) throws Exception {
            IgniteHelperFactory.igniteNodeWithName("ClientNodeUsingCacheForSystemTimeInMillis", true, false)
                    .execute(ignite -> {
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
            IgniteHelperFactory.igniteNodeWithName("Clustering-NodeDiscovery-ServerNode", false, false)
                    .execute(ignite -> Thread.sleep(9999999));
        }

        public static void runServerNodeUsingMulticastIP() throws Exception {
            new IgniteNode()
                    .with(IgniteHelperFactory
                            .igniteConfigWithLog4j2("Clustering-NodeDiscovery-MulticastWithIp", false, true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryMulticastIpFinder().setMulticastGroup("239.255.255.250"))))
                    .execute(ignite -> {
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeUsingMulticastWithoutIP() throws Exception {
            new IgniteNode()
                    .with(IgniteHelperFactory
                            .igniteConfigWithLog4j2("Clustering-NodeDiscovery-MulticastWithoutIp", false, true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryMulticastIpFinder())))
                    .execute(ignite -> {
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeUsingStaticIPs(String staticIpAddresses) throws Exception {
            new IgniteNode()
                    .with(IgniteHelperFactory
                            .igniteConfigWithLog4j2("Clustering-NodeDiscovery-StaticIPAddresses", false, true)
                            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryVmIpFinder().setAddresses(Arrays.asList(staticIpAddresses)))))
                    .execute(ignite -> {
                        Thread.sleep(9999999);
                    });
        }

        public static void runServerNodeWithUserAttributes(Map<String, String> userAttributes) throws Exception {
            new IgniteNode()
                    .with(IgniteHelperFactory
                            .igniteConfigWithLog4j2("ServerNodeWithUserAttributes", false, true)
                            .setUserAttributes(userAttributes))
                    .execute(ignite -> {
                        logger.info(String.format("Node - name: %s, UserAttributes: %s", ignite.name(), userAttributes.toString()));
                        Thread.sleep(9999999);
                    });
        }
    }

    public static class ClusterGroup101 {

        private ClusterGroup101() {}

        public static void runServerNodeBroadcastingComputeTask2AllNodes() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2AllNodes", false, true)
                    .execute(ignite -> {
                        IgniteCompute compute = ignite.compute();
                        compute.broadcast(() -> {
                            logger.info("Ran Compute Task on all nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2RemoteNodes() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2RemoteNodes", false, true)
                    .execute(ignite -> {
                        IgniteCompute compute = ignite.compute();
                        compute.broadcast(() -> {
                            logger.info("Ran Compute Task on remote nodes only");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2DataNodesWithCacheName(final String cacheName, final String nodeType) throws Exception {
            IgniteHelperFactory.igniteNodeWithName(String.format("BroadcastComputeTask2DataNodesWithCacheName-%s", nodeType), false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup cacheGroup = switch (nodeType) {
                            // Nodes where elements of cache are kept
                            case "DATA_NODES" -> cluster.forDataNodes(cacheName);
                            // Client nodes accessing the cache
                            case "CLIENT_NODES" -> cluster.forClientNodes(cacheName);
                            // All nodes where cache is deployed
                            default -> cluster.forCacheNodes(cacheName);
                        };

                        IgniteCompute compute = ignite.compute(cacheGroup);
                        compute.broadcast(() -> {
                            logger.info(String.format("Ran Compute Task on nodes - cacheName: %s, nodeType: %s", cacheName, nodeType));
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesWithAttributeKeyValue(final String key, final String value) throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2NodesWithAttributeKeyValue", false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forAttribute(key, value);
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info(String.format("Ran Compute Task on Nodes with Attribute - Key: %s, Value: %s", key, value));
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesBasedOnAge() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2NodesBasedOnAge", false, true)
                    .execute(ignite -> {
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
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2LocalNode", false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup forLocalClusterGroup = cluster.forLocal();
                        IgniteCompute computeGroup = ignite.compute(forLocalClusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Local Node");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2ClientNodes() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2ClientNodes", false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forClients();
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Client Nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2ServerNodes() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2ServerNodes", false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        ClusterGroup clusterGroup = cluster.forServers();
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task on Server Nodes");
                        });
                    });
        }

        public static void runServerNodeBroadcastingComputeTask2NodesBasedOnMetrics() throws Exception {
            IgniteHelperFactory.igniteNodeWithName("BroadcastComputeTask2NodesBasedOnMetrics", false, true)
                    .execute(ignite -> {
                        IgniteCluster cluster = ignite.cluster();
                        // ClusterMetrics class has all the metrics defined
                        ClusterGroup clusterGroup = cluster.forPredicate(node -> node.metrics().getAverageCpuLoad() < 0.5);
                        IgniteCompute computeGroup = ignite.compute(clusterGroup);
                        computeGroup.broadcast(() -> {
                            logger.info("Ran Compute Task based on cluster node metrics - Avg. CPLU Load < 0.5");
                        });
                        Thread.sleep(60 * 1000);
                    });
        }
    }

    /**
     * Local
     * - fastest data access and modification.
     * - Features: Data expiry & eviction, SQL Query and Transaction Management
     *
     * Default caching mode
     * - Partitioned, for high scalability, write performance
     * - Use only for huge dataset, frequent update
     *
     * Replicate
     * - for high availability, read performance
     * - Use only for small cache, infrequent data update
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

    /**
     * Cache Aside
     * - Application has the responsibility to read data from persistence storage and update the cache.
     * - For Fast data access but forces ust to write boilerplate code to maintain two data sources: DB and cache
     * Read-through & Write-through
     * - Application doesn't talk to persistence storage directly but with cache which is responsible for updating persistence storage.
     * - Offer flexibility to work with cache only and no need to maintain two data sources like Cache Aside
     * Write-behind
     * - Improves write performance as application updates cache and returns, while Ignite cluster is responsible for propagating the change to persistence store
     * - Asynchronously updates persistence store with bulk of amount of data
     */
    public static class CachingStrategy101 {
        private CachingStrategy101() {}
    }

}
