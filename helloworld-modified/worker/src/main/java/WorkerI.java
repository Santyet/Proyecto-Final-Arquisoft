import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class WorkerI implements Demo.Worker {

    @Override
    public String calculateIntegral(String expression, double lowerLimit, double upperLimit, com.zeroc.Ice.Current current) {
        try {
            UnivariateFunction function = new UnivariateFunction() {
                public double value(double x) {
                    Expression e = new ExpressionBuilder(expression)
                            .variables("x")
                            .build()
                            .setVariable("x", x);
                    return e.evaluate();
                }
            };

            // Integración usando el método de Simpson
            SimpsonIntegrator simpsonIntegrator = new SimpsonIntegrator();
            double resultSimpson = simpsonIntegrator.integrate(1000, function, lowerLimit, upperLimit);

            // Integración usando el método del Trapecio
            TrapezoidIntegrator trapezoidIntegrator = new TrapezoidIntegrator();
            double resultTrapezoid = trapezoidIntegrator.integrate(1000, function, lowerLimit, upperLimit);

            // Integración usando el método de Romberg
            RombergIntegrator rombergIntegrator = new RombergIntegrator();
            double resultRomberg = rombergIntegrator.integrate(1000, function, lowerLimit, upperLimit);

            return String.format("Simpson: %f, Trapezoid: %f, Romberg: %f", resultSimpson, resultTrapezoid, resultRomberg);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error in calculation";
        }
    }
}
