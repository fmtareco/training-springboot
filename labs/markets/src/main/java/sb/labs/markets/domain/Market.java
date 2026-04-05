package sb.labs.markets.domain;

public interface Market {
    String getName();

    double getBuyValue(String symbol);
    double getSellValue(String symbol);
    Ticket executeOrder(Order order);
}