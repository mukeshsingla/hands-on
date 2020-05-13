package ignite.beginner;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.log4j2.Log4J2Logger;

public class IgniteHelperFactory {
    private IgniteHelperFactory() {}

    public static IgniteNode igniteNodeWithName(String nodeName, boolean clientMode, boolean peerClassLoading) throws IgniteCheckedException {
        return new IgniteNode()
                .with(igniteConfigWithLog4j2(nodeName, clientMode, peerClassLoading));
    }

    public static IgniteConfiguration igniteConfig(String nodeName, boolean clientMode) throws IgniteCheckedException {
        return new IgniteConfiguration()
                .setIgniteInstanceName(nodeName)
                .setClientMode(clientMode);
    }

    /**
     * Returns instance of <code>IgniteConfiguration</code> configured to use Log4j2 for logging<BR/>
     * Peer class loading - Inter-node byte-code exchange.
     * Activates special distributed ClassLoader, if enabled, will automatically redeploy
     * Java/Scala code on every node in grid each time the code changes
     *
     * @param nodeName
     * @param clientMode - true: Client, false: Server
     * @param peerClassLoading
     * @return instance of IgniteConfiguration
     * @throws IgniteCheckedException
     */
    public static IgniteConfiguration igniteConfigWithLog4j2(String nodeName, boolean clientMode, boolean peerClassLoading) throws IgniteCheckedException {
        return new IgniteConfiguration()
                        .setIgniteInstanceName(nodeName)
                        .setClientMode(clientMode)
                        .setPeerClassLoadingEnabled(peerClassLoading)
                        .setGridLogger(new Log4J2Logger("log4j2.xml"));
    }

}
