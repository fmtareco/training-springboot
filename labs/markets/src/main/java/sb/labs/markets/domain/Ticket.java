package sb.labs.markets.domain;

public record Ticket(String marketName, String stockSymbol, double price, int quantity) {}