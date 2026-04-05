package sb.labs.markets;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import sb.labs.markets.domain.*;
import sb.labs.markets.domain.Order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarketsApplicationTests {

    private ComposedMarket composedMarket;
    private String[] markets = {"NASDAQ", "NYSE", "EURONEXT", "SSE", "JPX" };
    private String[] tickers = {"AAPL", "AMZN", "ORCL", "MSFT", "IBM" };
    private Double[] appleValues = {250.0, 255.0, 260.0, 265.0, 270.0 };
    private Double[] amznValues = {150.0, 145.0, 140.0, 135.0, 130.0 };
    private Double[] orclValues = {50.0, 50.0, 60.0, 45.0, 40.0 };
    private Double[] msftValues = {350.0, 360.0, 330.0, 405.0, 400.0 };
    private Double[] ibmValues = {450.0, 460.0, 445.0, 455.0, 460.0 };

    private Market nasdaq;
    private Market nyse;
    private final String SYMBOL = "AAPL";


//    @BeforeAll
//    void setUp() {
//
//    }


    @BeforeEach
    void setUp() {
        composedMarket = new ComposedMarket("Global Market");

        // Create mocks for individual markets
        nasdaq = mock(Market.class);
        nyse = mock(Market.class);

        when(nasdaq.getName()).thenReturn("NASDAQ");
        when(nyse.getName()).thenReturn("NYSE");

        composedMarket.addMarket(nasdaq);
        composedMarket.addMarket(nyse);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void shouldReturnBestBuyValue() {
        // NASDAQ is cheaper to buy
        when(nasdaq.getBuyValue(SYMBOL)).thenReturn(100.0);
        when(nyse.getBuyValue(SYMBOL)).thenReturn(105.0);

        double bestBuy = composedMarket.getBuyValue(SYMBOL);

        assertEquals(100.0, bestBuy, "Should pick the lowest buy price");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void shouldReturnBestSellValue() {
        // NYSE offers a better selling price
        when(nasdaq.getSellValue(SYMBOL)).thenReturn(110.0);
        when(nyse.getSellValue(SYMBOL)).thenReturn(115.0);

        double bestSell = composedMarket.getSellValue(SYMBOL);

        assertEquals(115.0, bestSell, "Should pick the highest sell price");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void shouldRouteBuyOrderToCheapestMarket() {
        // Setup: NASDAQ (100.0) is cheaper than NYSE (105.0)
        when(nasdaq.getBuyValue(SYMBOL)).thenReturn(100.0);
        when(nyse.getBuyValue(SYMBOL)).thenReturn(105.0);

        Order buyOrder = new Order(SYMBOL, 0, 10, OrderSide.BUY);
        Ticket expectedTicket = new Ticket("NASDAQ", SYMBOL, 100.0, 10);

        // Mock the execution on the specific market
        when(nasdaq.executeOrder(any(Order.class))).thenReturn(expectedTicket);

        Ticket result = composedMarket.executeOrder(buyOrder);

        // Verification
        assertEquals("NASDAQ", result.marketName());
        verify(nyse, never()).executeOrder(any());
        verify(nyse, times(0)).executeOrder(any());

        verify(nasdaq, times(1)).getBuyValue(SYMBOL);
        verify(nyse, times(1)).getBuyValue(SYMBOL);

        verify(nasdaq).executeOrder(argThat(o -> o.price() == 100.0));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void shouldRouteSellOrderToMostExpensiveMarket() {
        // Setup: NYSE (120.0) is better for selling than NASDAQ (110.0)
        when(nasdaq.getSellValue(SYMBOL)).thenReturn(110.0);
        when(nyse.getSellValue(SYMBOL)).thenReturn(120.0);

        Order sellOrder = new Order(SYMBOL, 0, 5, OrderSide.SELL);
        Ticket expectedTicket = new Ticket("NYSE", SYMBOL, 120.0, 5);

        when(nyse.executeOrder(any(Order.class))).thenReturn(expectedTicket);

        Ticket result = composedMarket.executeOrder(sellOrder);

        // Verification
        assertEquals("NYSE", result.marketName());
        verify(nyse).executeOrder(argThat(o -> o.price() == 120.0));
        verify(nasdaq, never()).executeOrder(any());
    }
}

