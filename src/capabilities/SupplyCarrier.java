package capabilities;

public interface SupplyCarrier {
    void loadSupplies(int number);
    void useSupplies(int number);
    int getSupplyLevel();
    int getSupplyCapacity();
}
