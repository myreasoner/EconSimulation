import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * @author lli
 *
 */
public class EconSimulation
{
    private double[] taxRate;
    private double[] tax;
    private double[] gdp;
    private double[] consumption;
    private double[] realInterestRate;
    private double[] nominalInterestRate;
    private double[] inflation;
    private double[] expectedInflation;
    private double[] investment;
    private double[] governmentSpending;
    private double[] fullEmploymentGdp;
    private double[] price;
    private double[] nominalDeficit;
    private double[] bonds;
    private double[] unemploymentRate;
    private double[] lm;
    private double[] lf;
    private double[] total;

    private int t_ = 0;

    private static final double FixedInflationBar = 0.02d;
    private static final NumberFormat PercentFormatter = NumberFormat.getPercentInstance();
    private static final DecimalFormat DoubleFormatter = new DecimalFormat("#,###.00");

    static {
        PercentFormatter.setMaximumFractionDigits(2);
        PercentFormatter.setMinimumFractionDigits(2);
    }

    public void init (int maxRound)
    {
        taxRate = new double[maxRound];
        tax = new double[maxRound];
        consumption = new double[maxRound];

        realInterestRate = new double[maxRound];

        nominalInterestRate = new double[maxRound];
        nominalInterestRate[0] = 0.05d;

        inflation = new double[maxRound];
        inflation[0] = 0.02d;

        expectedInflation = new double[maxRound + 1];
        expectedInflation[0] = 0.0d;
        expectedInflation[1] = 0.02d;

        investment = new double[maxRound];
        governmentSpending = new double[maxRound];

        fullEmploymentGdp = new double[maxRound];
        fullEmploymentGdp[0] = 18000d;

        gdp = new double[maxRound];

        price = new double[maxRound];
        price[0] = 1d;

        nominalDeficit = new double[maxRound];

        bonds = new double[maxRound];
        bonds[0] = 13500d;

        unemploymentRate = new double[maxRound];

        lm = new double[maxRound];
        lm[0] = 0d;

        lf = new double[maxRound];
        lf[0] = 0d;

        total = new double[maxRound];
    }

    /**
     * advance to next round with assigned interest rate and tax rate
     * 
     * @param interestRateAssigned
     * @param taxRateAssigned
     * @return
     */
    public EconSimulation advance (double interestRateAssigned,
                                   double taxRateAssigned)
    {
        t_++;

        nominalInterestRate[t_] = interestRateAssigned;
        taxRate[t_] = taxRateAssigned;

        // equation 7
        fullEmploymentGdp[t_] = fullEmploymentGdp[t_ - 1] * 1.02d;

        // equation 4
        governmentSpending[t_] = fullEmploymentGdp[t_] * 0.2d;

        // equation 6
        expectedInflation[t_ + 1] = inflation[t_ - 1] * 0.7d
                + expectedInflation[t_] * 0.2d + 0.1d * FixedInflationBar;

        // equation 5
        realInterestRate[t_] = nominalInterestRate[t_]
                - expectedInflation[t_ + 1];

        // equation 3
        investment[t_] = 363d
                + 2565d * Math.pow(Math.E, -3.11d * realInterestRate[t_]);

        // Y = C + I + G
        gdp[t_] = (investment[t_] + governmentSpending[t_] + 2665d
                + 1422d * Math.pow(Math.E, -3.11d * realInterestRate[t_]))
                / (0.4d + 0.6d * taxRate[t_]);

        // equation 2
        consumption[t_] = 2665d + 0.6d * (1 - taxRate[t_]) * gdp[t_]
                + 1422d * Math.pow(Math.E, -3.11d * realInterestRate[t_]);

        // equation 1
        tax[t_] = gdp[t_] * taxRate[t_];

        // equation 8
        inflation[t_] = Math.log(gdp[t_] / fullEmploymentGdp[t_]) / 0.98
                + expectedInflation[t_];

        // equation 9
        price[t_] = price[t_ - 1] * (1d + inflation[t_]);

        // equation 10
        nominalDeficit[t_] = price[t_] * governmentSpending[t_]
                + nominalInterestRate[t_ - 1] * bonds[t_ - 1]
                - taxRate[t_] * price[t_] * gdp[t_];

        // equation 11
        bonds[t_] = bonds[t_ - 1] + nominalDeficit[t_];

        // equation 12
        unemploymentRate[t_] = fullEmploymentGdp[t_] > gdp[t_]
                ? (0.06d + 0.5d * (fullEmploymentGdp[t_] - gdp[t_])
                        / fullEmploymentGdp[t_])
                : 0.06d * (0.06d
                        / (0.06d - 0.5d * ((fullEmploymentGdp[t_] - gdp[t_])
                                / fullEmploymentGdp[t_])));

        // equation 13
        lm[t_] = 1000d * Math.pow(unemploymentRate[t_] - 0.06d, 2)
                + 1000d * Math.pow(inflation[t_] - 0.02d, 2) + lm[t_ - 1];

        // equation 14
        lf[t_] = 1000d
                * Math.pow((gdp[t_] - 1.01 * fullEmploymentGdp[t_])
                        / (1.01 * fullEmploymentGdp[t_]), 2)
                + 1000d * Math.pow((bonds[t_] / (price[t_] * gdp[t_])) - 0.5d,
                                   2)
                + lf[t_ - 1];

        total[t_] = lm[t_] + lf[t_];

        return this;
    }

    /**
     * set a EconSimulation {@code copy} to the state of EconSimulation
     * {@code original}
     * 
     * @param original
     * @param copy
     * @param maxRound
     * @param currRound
     */

    public static void setFromOriginal (EconSimulation original,
                                        EconSimulation copy,
                                        int maxRound,
                                        int currRound)
    {
        System.arraycopy(original.taxRate, 0, copy.taxRate, 0, maxRound);
        System.arraycopy(original.tax, 0, copy.tax, 0, maxRound);
        System.arraycopy(original.gdp, 0, copy.gdp, 0, maxRound);
        System.arraycopy(original.consumption,
                         0,
                         copy.consumption,
                         0,
                         maxRound);
        System.arraycopy(original.realInterestRate,
                         0,
                         copy.realInterestRate,
                         0,
                         maxRound);
        System.arraycopy(original.nominalInterestRate,
                         0,
                         copy.nominalInterestRate,
                         0,
                         maxRound);
        System.arraycopy(original.inflation, 0, copy.inflation, 0, maxRound);
        System.arraycopy(original.expectedInflation,
                         0,
                         copy.expectedInflation,
                         0,
                         maxRound);
        System.arraycopy(original.investment, 0, copy.investment, 0, maxRound);
        System.arraycopy(original.governmentSpending,
                         0,
                         copy.governmentSpending,
                         0,
                         maxRound);
        System.arraycopy(original.fullEmploymentGdp,
                         0,
                         copy.fullEmploymentGdp,
                         0,
                         maxRound);
        System.arraycopy(original.price, 0, copy.price, 0, maxRound);
        System.arraycopy(original.nominalDeficit,
                         0,
                         copy.nominalDeficit,
                         0,
                         maxRound);
        System.arraycopy(original.bonds, 0, copy.bonds, 0, maxRound);
        System.arraycopy(original.unemploymentRate,
                         0,
                         copy.unemploymentRate,
                         0,
                         maxRound);
        System.arraycopy(original.lm, 0, copy.lm, 0, maxRound);
        System.arraycopy(original.lf, 0, copy.lf, 0, maxRound);
        System.arraycopy(original.total, 0, copy.total, 0, maxRound);
        copy.t_ = currRound;

    }

    static class SimulationComparator implements Comparator<EconSimulation>
    {
        public int compare (EconSimulation o1, EconSimulation o2)
        {
            double result = o1.total[o1.t_] - o2.total[o1.t_];
            return result > 0.0d ? 1 : -1;
        }

    }

    public static void main (String[] args)
    {
        int maxRound = 50;

        List<EconSimulation> allRoundRecords = new ArrayList<EconSimulation>();

        System.out.println(S("Round\tMonetaryScore\tFiscalScore\tTotalScore\tInterestRate\tGovPurchase\tTaxRate\tExpectedInflation\tRealInterest\tFullGDP\tRealGDP\tFiscalGDP\tConsumption\tInvestment\tUnemployment\tInflation\tPrice\tDeficit\tBonds"));

        for (int round = 1; round < maxRound + 1; round++) {
            List<EconSimulation> currRoundRecords = new ArrayList<EconSimulation>();

            double i = -0.01d;
            while (i <= 0.20d) {

                double t = 0.0d;
                while (t <= 0.40d) {
                    EconSimulation curr = new EconSimulation();
                    curr.init(maxRound + 1);

                    if (!allRoundRecords.isEmpty()) {
                        // recover state from last round and carry on searching
                        setFromOriginal(allRoundRecords.get(allRoundRecords.size()
                                - 1), curr, maxRound, round - 1);
                    }

                    currRoundRecords.add(curr.advance(i, t));

                    t = t + 0.001d;
                }
                i = i + 0.001d;
            }

            Collections.sort(currRoundRecords, new SimulationComparator());

            allRoundRecords.add(currRoundRecords.get(0));
            System.out.println(currRoundRecords.get(0));

        }

    }

    public String toString ()
    {
        return S("Rnd %s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                 t_,
                 DoubleFormatter.format(lm[t_]),
                 DoubleFormatter.format(lf[t_]),
                 DoubleFormatter.format(total[t_]),
                 PercentFormatter.format(nominalInterestRate[t_]),
                 DoubleFormatter.format(governmentSpending[t_]),
                 PercentFormatter.format(taxRate[t_]),
                 PercentFormatter.format(expectedInflation[t_]),
                 PercentFormatter.format(realInterestRate[t_]),
                 DoubleFormatter.format(fullEmploymentGdp[t_]),
                 DoubleFormatter.format(gdp[t_]),
                 DoubleFormatter.format(fullEmploymentGdp[t_] * 1.01d),
                 DoubleFormatter.format(consumption[t_]),
                 DoubleFormatter.format(investment[t_]),
                 PercentFormatter.format(unemploymentRate[t_]),
                 PercentFormatter.format(inflation[t_]),
                 DoubleFormatter.format(price[t_]),
                 DoubleFormatter.format(nominalDeficit[t_]),
                 DoubleFormatter.format(bonds[t_]));

    }

    public static String padLeft (String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }

    public static String S (Object o1)
    {
        return o1.toString();
    }

    public static String S (Object[] objects)
    {
        if (objects.length > 3) {
            StringBuilder s = new StringBuilder();
            s.append("\n[");
            for (int i = 0; i < objects.length; i++) {
                s.append(S(" %s,\n", objects[i]));
            }
            s.append("]\n");
            return s.toString();
        }
        else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (int i = 0; i < objects.length; i++) {
                s.append(S(" %s,", objects[i]));
            }
            s.append("]");
            return s.toString();
        }
    }

    public static String S (List<? extends Object> objects)
    {
        if (objects.size() > 3) {
            StringBuilder s = new StringBuilder();
            s.append("\n[\n");

            for (Object obj : objects) {
                s.append(S(" %s,\n", obj));
            }
            s.append("]\n");
            return s.toString();
        }
        else {
            return objects.toString();
        }
    }

    public static String S (String fmt, Object... array)
    {
        int arrayIndex = 0;
        StringBuilder obj = new StringBuilder();
        char[] chars = fmt.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                if (i + 1 != chars.length && chars[i + 1] == '%') {
                    continue;
                }
                else if (chars[i + 1] == 's') {
                    if (arrayIndex < array.length) {
                        if (array[arrayIndex] == null) {
                            obj.append("null");
                        }
                        else {
                            obj.append(appendObject(array[arrayIndex]));
                        }
                        /* increment index */
                        arrayIndex++;
                        i++;
                    }
                    else {
                        obj.append(chars[i]);
                    }
                }
                else {
                    obj.append(chars[i]);
                }
            }
            else if (chars[i] == '\\') {
                if (i + 1 != chars.length && chars[i + 1] == '\\') {
                    continue;
                }
                else if (chars[i + 1] == 'n') {
                    obj.append("\n");
                }
                else if (chars[i + 1] == 't') {
                    obj.append("    ");
                }
                i++;
            }
            else {
                obj.append(chars[i]);
            }
        }
        return obj.toString();
    }

    private static String appendObject (Object object)
    {
        if (object instanceof Object[]) {
            return S((Object[])object);
        }
        else if (object instanceof List<?>) {
            return S((List<?>)object);
        }
        else if (object instanceof Throwable) {
            Throwable th = (Throwable)object;
            String trace = "\n" + th.toString();
            StackTraceElement[] elements = th.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement elem = elements[i];

                /* don't print the formatter stack on the log */

                String val = elem.toString();

                if (val == null) {
                    continue;
                }

                if (val.indexOf(ConsoleHandler.class.getName()) != -1) {
                    continue;
                }
                if (val.indexOf(StreamHandler.class.getName()) != -1) {
                    continue;
                }
                if (val.indexOf(Logger.class.getName()) != -1) {
                    continue;
                }

                /* print only the actual application level stack trace */
                trace = S("%s\n\tat %s", trace, val);
            }
            return trace;
        }
        else {
            return object.toString();
        }
    }
}
