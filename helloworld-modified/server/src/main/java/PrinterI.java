import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import Demo.*;

public class PrinterI implements Printer {

    private PrinterCallbackPrx currentCallback;
    private java.util.concurrent.Executors Executors;
    private ExecutorService threadPool = Executors.newFixedThreadPool(6);
    private String username;

    public String printString(String s, double lowerLimit, double upperLimit, PrinterCallbackPrx client, com.zeroc.Ice.Current current) {

        Runnable run = new Thread(() -> {
            try {
                currentCallback = client;

                String response = process(s, lowerLimit, upperLimit);

                client.callbackString(response);

            } catch (Exception e) {
                e.printStackTrace();
                client.callbackString("Thread error");
            }

        });
        threadPool.execute(run);
        return "Request processed";
    }

    private String process(String s, double lowerLimit, double upperLimit) {

        String[] parts = s.split("=");
        try {
            String integralFunction = parts[1];
            UnivariateFunction function = new UnivariateFunction() {
                public double value(double x) {
                    Expression e = new ExpressionBuilder(integralFunction)
                            .variables("x")
                            .build()
                            .setVariable("x", x);
                    return e.evaluate();
                }
            };

            // Integración usando el método de Simpson
            SimpsonIntegrator simpsonIntegrator = new SimpsonIntegrator();
            double resultSimpson = simpsonIntegrator.integrate(1000, function, lowerLimit, upperLimit);
            System.out.println("Resultado usando Simpson: " + resultSimpson);

            // Integración usando el método del Trapecio
            TrapezoidIntegrator trapezoidIntegrator = new TrapezoidIntegrator();
            double resultTrapezoid = trapezoidIntegrator.integrate(1000, function, lowerLimit, upperLimit);
            System.out.println("Resultado usando Trapecio: " + resultTrapezoid);

            // Integración usando el método de Romberg
            RombergIntegrator rombergIntegrator = new RombergIntegrator();
            double resultRomberg = rombergIntegrator.integrate(1000, function, lowerLimit, upperLimit);
            System.out.println("Resultado usando Romberg: " + resultRomberg);

        } catch (Exception e) {
            System.out.println("Exception");
        }
        return "";
    }

}