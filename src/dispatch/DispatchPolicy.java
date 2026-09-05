package dispatch;

public class DispatchPolicy{
    private double lowFuelThreshold;
    private double lowFuelPenalty;
    private double lowBatteryThreshold;
    private double lowBatteryPenalty;
    private double lowWaterThreshold;
    private double lowWaterPenalty;
    private double lowSupplyThreshold;
    private double lowSupplyPenalty;
    private double lowContainmentThreshold;
    private double lowContainmentPenalty;


    public DispatchPolicy(double lowFuelThreshold, double lowFuelPenalty,
                          double lowBatteryThreshold, double lowBatteryPenalty,
                          double lowWaterThreshold, double lowWaterPenalty,
                          double lowSupplyThreshold, double lowSupplyPenalty,
                          double lowContainmentThreshold, double lowContainmentPenalty) {
        this.lowFuelThreshold = lowFuelThreshold;
        this.lowFuelPenalty = lowFuelPenalty;
        this.lowBatteryPenalty = lowBatteryPenalty;
        this.lowBatteryThreshold = lowBatteryThreshold;
        this.lowWaterThreshold = lowWaterThreshold;
        this.lowWaterPenalty = lowWaterPenalty;
        this.lowSupplyThreshold = lowSupplyThreshold;
        this.lowSupplyPenalty = lowSupplyPenalty;
        this.lowContainmentThreshold = lowContainmentThreshold;
        this.lowContainmentPenalty = lowContainmentPenalty;


    }
    public DispatchPolicy() {
        this(0.25, 12, 0.30, 15, 0.40, 20, 0.25, 10, 0.25, 10);
    }

    public double getLowFuelThreshold() { return lowFuelThreshold; }
    public double getLowFuelPenalty() { return lowFuelPenalty; }
    public double getLowBatteryThreshold() { return lowBatteryThreshold; }
    public double getLowBatteryPenalty() { return lowBatteryPenalty; }
    public double getLowWaterThreshold() { return lowWaterThreshold; }
    public double getLowWaterPenalty() { return lowWaterPenalty; }
    public double getLowSupplyThreshold() { return lowSupplyThreshold; }
    public double getLowSupplyPenalty() { return lowSupplyPenalty; }
    public double getLowContainmentThreshold() { return lowContainmentThreshold; }
    public double getLowContainmentPenalty() { return lowContainmentPenalty; }

}
