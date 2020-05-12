package ignite.beginner;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

public class IgniteNode {

    private IgniteConfiguration cfg;

    public IgniteNode with(IgniteConfiguration cfg) {
        this.cfg = cfg;
        return this;
    }

    public void start(NodeTask nodeTask) throws Exception {
        try (Ignite ignite = (null != cfg ? Ignition.start(cfg) : Ignition.start())) {
            nodeTask.accept(ignite);
        }
    }

    public static interface NodeTask {
        void accept(Ignite ignite) throws Exception;
    }
}
