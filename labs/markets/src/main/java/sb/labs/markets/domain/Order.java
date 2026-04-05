package sb.labs.markets.domain;

public record Order(String stockSymbol, double price, int quantity, OrderSide side) {}