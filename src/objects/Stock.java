package objects;

public class Stock {
    private String symbol, company;
    private double lastPrice;

    public Stock(String symbol, String company) {
        this.symbol = symbol;
        this.company = company;

        lastPrice = 0.00;
    }

    public Stock(String symbol, String company, double lastPrice) {
        this.symbol = symbol;
        this.company = company;
        this.lastPrice = lastPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }
}
