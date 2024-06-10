// Subscriber.java
import java.util.UUID;
import com.zeroc.Ice.Util;
import Demo.Clock;
import Demo.DataRequestPrx;
import Demo.PublisherPrx;

public class Subscriber {
    public static class ClockI implements Clock {
        private PublisherPrx publisher;

        public ClockI(PublisherPrx publisher) {
            this.publisher = publisher;
        }

        @Override
        public void tick(String message, com.zeroc.Ice.Current current) {
            System.out.println(message);

            if (message.equals("New integration task available")) {
                String data = publisher.getData();
                System.out.println("Received data: " + data);
            }
        }
    }

    public static void main(String[] args) {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.sub", extraArgs);
        Thread destroyHook = new Thread(() -> communicator.destroy());
        Runtime.getRuntime().addShutdownHook(destroyHook);

        try {
            status = run(communicator, destroyHook, extraArgs.toArray(new String[extraArgs.size()]));
        } catch (Exception ex) {
            ex.printStackTrace();
            status = 1;
        }

        if (status != 0) {
            System.exit(status);
        }
    }

    public static void usage() {
        System.out.println("Usage: [--batch] [--datagram|--twoway|--ordered|--oneway] " +
                "[--retryCount count] [--id id] [topic]");
    }

    private static int run(com.zeroc.Ice.Communicator communicator, Thread destroyHook, String[] args) {
        String topicName = "time";

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
                communicator.propertyToProxy("TopicManager.Proxy"));
        if (manager == null) {
            System.err.println("invalid proxy");
            return 1;
        }

        com.zeroc.IceStorm.TopicPrx topic;
        try {
            topic = manager.retrieve(topicName);
        } catch (com.zeroc.IceStorm.NoSuchTopic e) {
            try {
                topic = manager.create(topicName);
            } catch (com.zeroc.IceStorm.TopicExists ex) {
                System.err.println("temporary failure, try again.");
                return 1;
            }
        }

        com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Clock.Subscriber");

        PublisherPrx publisher = PublisherPrx.checkedCast(communicator.stringToProxy("SimplePublisher:default -p 10000"));

        com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new ClockI(publisher), Util.stringToIdentity(UUID.randomUUID().toString()));
        adapter.activate();

        java.util.Map<String, String> qos = new java.util.HashMap<>();

        try {
            topic.subscribeAndGetPublisher(qos, subscriber);
        } catch (com.zeroc.IceStorm.AlreadySubscribed e) {
            System.out.println("reactivating persistent subscriber");
        } catch (com.zeroc.IceStorm.InvalidSubscriber e) {
            e.printStackTrace();
            return 1;
        } catch (com.zeroc.IceStorm.BadQoS e) {
            e.printStackTrace();
            return 1;
        }

        final com.zeroc.IceStorm.TopicPrx topicF = topic;
        final com.zeroc.Ice.ObjectPrx subscriberF = subscriber;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                topicF.unsubscribe(subscriberF);
            } finally {
                communicator.destroy();
            }
        }));
        Runtime.getRuntime().removeShutdownHook(destroyHook);

        return 0;
    }
}
