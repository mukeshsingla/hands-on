package ignite.config;

import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClientConfigurationFactory {
    /** Secret properties loading. **/
    private static final Properties props = new Properties();

    static {
        try (InputStream in = IgniteConfiguration.class.getClassLoader().getResourceAsStream("secret.properties")) {
            props.load(in);
        }
        catch (Exception ignored) {
            // No-op.
        }
    }

    /**
     * Configure grid.
     * 
     * @return Ignite configuration.
     * @throws Exception If failed to construct Ignite configuration instance.
     **/
    public static IgniteConfiguration createConfiguration() throws Exception {
        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setClientMode(true);
        cfg.setIgniteInstanceName("DuckAirlines");

        TcpDiscoverySpi discovery = new TcpDiscoverySpi();

        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47510", "192.168.1.151:47500..47510"));

        discovery.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(discovery);

        Map<String, String> userAttrsMap = new HashMap<>();
//        userAttrsMap.put("ROLE", "FlightOperations");
        userAttrsMap.put("ROLE", "FlightReservations");

        cfg.setUserAttributes(userAttrsMap);

        cfg.setPeerClassLoadingEnabled(true);

        return cfg;
    }

}