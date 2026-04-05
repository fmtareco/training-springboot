package sb.labs.markets.domain;

import java.util.ArrayList;
import java.util.List;

public class ComposedMarket implements Market {
    private String name;
    private List<Market> markets;

    public ComposedMarket(String name) {
        this.name = name;
        markets = new ArrayList<>();
    }
    public ComposedMarket(String name, List<Market> _markets) {
        this(name);
        if (_markets == null || _markets.isEmpty())
            throw new RuntimeException("Markets list can't be null or empty");
        markets.addAll(_markets);
    }

    public void addMarket(Market market) {
        markets.add(market);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getBuyValue(String symbol) {
        return markets.stream()
                .mapToDouble(m -> m.getBuyValue(symbol))
                .filter(v -> v > 0)
                .min()
                .orElse(Double.MAX_VALUE);
    }

    @Override
    public double getSellValue(String symbol) {
        return markets.stream()
                .mapToDouble(m -> m.getSellValue(symbol))
                .max()
                .orElse(0.0);
    }


    @Override
    public Ticket executeOrder(Order order) {
        Market bestMarket = null;
        double bestPrice = (order.side() == OrderSide.BUY) ? Double.MAX_VALUE : -1.0;
        for (Market m : markets) {
            double currentPrice = (order.side() == OrderSide.BUY)
                    ? m.getBuyValue(order.stockSymbol())
                    : m.getSellValue(order.stockSymbol());

            if (order.side() == OrderSide.BUY) {
                if (currentPrice < bestPrice) {
                    bestPrice = currentPrice;
                    bestMarket = m;
                }
            } else {
                if (currentPrice > bestPrice) {
                    bestPrice = currentPrice;
                    bestMarket = m;
                }
            }
        }
        if (bestMarket == null) {
            throw new RuntimeException("Nenhum mercado disponível para esta ação.");
        }

        // Cria uma nova ordem ajustada ao preço do melhor mercado encontrado
        Order optimizedOrder = new Order(
                order.stockSymbol(),
                bestPrice,
                order.quantity(),
                order.side()
        );

        return bestMarket.executeOrder(optimizedOrder);
    }
}
