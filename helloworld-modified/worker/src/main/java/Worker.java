import Demo.*;

import java.util.UUID;
import com.zeroc.Ice.Util;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.*;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import com.zeroc.Ice.ObjectPrx;
public class Worker
{
    public static com.zeroc.Ice.Communicator communicator;

    public static class StaticWorker implements WorkerI
    {

        private Demo.MasterPrx service = Demo.MasterPrx.checkedCast(communicator.propertyToProxy("MasterI.Proxy"));

        public StaticWorker(Demo.MasterPrx service){
            this.service = service;
        }

        private SimpsonIntegrator simpsonIntegrator = new SimpsonIntegrator();
        private RombergIntegrator rombergIntegrator = new RombergIntegrator();
        private TrapezoidIntegrator trapezoidIntegrator = new TrapezoidIntegrator();

        @Override
        public void requestTask(com.zeroc.Ice.Current current){
            Task task = service.getTask();

            if(task != null){
                processTask(task, current);
            }else{
                System.out.println("No more tasks");
            }

        };

        @Override
        public void endWorkers(com.zeroc.Ice.Current current){
            System.exit(0);
        };

        @Override
        public void processTask(Task task, com.zeroc.Ice.Current current){
            service.addPartialResult(integrate(task.funct, task.lowerLimit, task.upperLimit, task.approach));
        };

        private double integrate(String expression, double lowerLimit, double upperLimit, int approach) {
            UnivariateFunction function = new UnivariateFunction() {
                public double value(double x) {
                    Expression e = new ExpressionBuilder(expression)
                            .variables("x")
                            .build()
                            .setVariable("x", x);
                    return e.evaluate();
                }
            };
            double result = 0;
            switch (approach) {
                case 1:
                    System.out.println("Simpsons method");
                    result = simpsonIntegrator.integrate(1000000, function, lowerLimit, upperLimit);
                    break;
                case 2:
                    System.out.println("Romberg method");
                    result = rombergIntegrator.integrate(1000000, function, lowerLimit, upperLimit);
                    break;
                case 3:
                    System.out.println("Trapezoid method");
                    result = trapezoidIntegrator.integrate(1000000, function, lowerLimit, upperLimit);
                    break;
            }

            return result;
        }
    }

    public static void main(String[] args)
    {

        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        communicator = com.zeroc.Ice.Util.initialize(args, "config.sub", extraArgs);

        Demo.MasterPrx service = Demo.MasterPrx.checkedCast(communicator.propertyToProxy("MasterI.Proxy")).ice_twoway();

        if (service == null) {
            throw new Error("Invalid proxy");
        }

        Thread destroyHook = new Thread(() -> communicator.destroy());
        Runtime.getRuntime().addShutdownHook(destroyHook);

        service.registerWorker();
        try
        {
            status = run(communicator, destroyHook, service);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            status = 1;
        }

        if(status != 0)
        {
            System.exit(status);
        }
    }

    private static int run(com.zeroc.Ice.Communicator communicator, Thread destroyHook, Demo.MasterPrx service)
    {

        String topicName = "time";

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(communicator.propertyToProxy("TopicManager.Proxy"));
        if(manager == null)
        {
            System.err.println("invalid proxy");
            return 1;
        }

        com.zeroc.IceStorm.TopicPrx topic;
        try
        {
            topic = manager.retrieve(topicName);
        }
        catch(com.zeroc.IceStorm.NoSuchTopic e)
        {
            try
            {
                topic = manager.create(topicName);
            }
            catch(com.zeroc.IceStorm.TopicExists ex)
            {
                System.err.println("temporary failure, try again.");
                return 1;
            }
        }

        com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Clock.Subscriber");
        com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new StaticWorker(service), Util.stringToIdentity(UUID.randomUUID().toString()));
        adapter.activate();

        java.util.Map<String, String> qos = new java.util.HashMap<>();


        try
        {
            topic.subscribeAndGetPublisher(qos, subscriber);
        }
        catch(com.zeroc.IceStorm.AlreadySubscribed e)
        {
            System.out.println("reactivating persistent subscriber");
        }
        catch(com.zeroc.IceStorm.InvalidSubscriber e)
        {
            e.printStackTrace();
            return 1;
        }
        catch(com.zeroc.IceStorm.BadQoS e)
        {
            e.printStackTrace();
            return 1;
        }


        final com.zeroc.IceStorm.TopicPrx topicF = topic;
        final com.zeroc.Ice.ObjectPrx subscriberF = subscriber;
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try
            {
                topicF.unsubscribe(subscriberF);
            }
            finally
            {
                service.unregisterWorker();
                communicator.destroy();
            }
        }));
        Runtime.getRuntime().removeShutdownHook(destroyHook);

        return 0;
    }
}