package ignite.beginner;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IgniteNode {

    private static final Logger logger = LogManager.getLogger(IgniteNode.class);
    private IgniteConfiguration cfg;

    public IgniteNode with(IgniteConfiguration cfg) {
        this.cfg = cfg;
        return this;
    }

    public void execute(NodeTask nodeTask) throws Exception {
        try (Ignite ignite = (null != cfg ? Ignition.start(cfg) : Ignition.start())) {
            logger.log(Level.INFO, "Node - name: {}", ignite.name());
            if(null != nodeTask)
                nodeTask.accept(ignite);
            logger.log(Level.INFO, () -> "Finished executing tasks, exiting");
        }
    }

    public static interface NodeTask {
        void accept(Ignite ignite) throws Exception;
    }
}
