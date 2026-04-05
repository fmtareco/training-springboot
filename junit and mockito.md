# JUnit 5 (Jupiter) and Mockito

* JUnit 5 (Jupiter) and Mockito are the standard duo for Java testing. 
* While JUnit provides the structure for running tests and making assertions, 
* Mockito allows you to simulate dependencies (mocks) so you can test your code in isolation. 

----
## 1. Key JUnit 5 Features & Assertions

* JUnit 5 is modular and supports modern Java features like lambdas. 
* Assertions are used to verify that your code produces the expected results. 

* Core Annotations:
	* @Test: Marks a method as a test.
	* @BeforeEach / @AfterEach: Runs before/after every test for setup/teardown.
	* @DisplayName: Provides a custom, readable name for the test.
	* @Disabled: Skips a test.
* Most Relevant Assertions:
	* assertEquals(expected, actual): Checks if two values are equal.
	* assertNotEquals(unexpected, actual): Checks that values are different.
	* assertTrue(condition) / assertFalse(condition): Validates boolean logic.
	* assertNull(object) / assertNotNull(object): Checks for nullity.
	* assertThrows(Exception.class, executable): Verifies that a specific exception is thrown.
	* assertAll(executables...): Grouped assertions; it runs all of them even if one fails, reporting all failures at once. 

----
## 2. Mockito Features & Verification
* Mockito creates "test doubles" to replace real objects.

* Mock vs. Spy:
	* Mock: 
		* A complete dummy object. 
		* No real code is executed unless you "stub" a method to return a specific value.
	* Spy: 
		* A partial mock. 
		* It wraps a real object; 
		* by default, it calls the real methods unless you specifically stub them.
		
* Verification (The "Verify" API):
	* Used to check if a mock was called correctly after the execution.
		* verify(mock).method(): Checks if the method was called exactly once (default).
		* verify(mock, times(n)).method(): Checks for exactly n calls.
		* verify(mock, never()).method(): Ensures the method was not called.
		* verify(mock, atLeast(n)).method() / atMost(n): Checks for a range of calls.
		* verifyNoMoreInteractions(mock): Ensures no other methods were called on the mock besides what was verified. 
----
## 3. Mockito vs. Spring Boot Mocking
* When using Spring Boot, you often need to deal with the ApplicationContext. 

| Feature  | Mockito (@Mock / @Spy) | Spring Boot (@MockitoBean / @MockitoSpyBean) |
|---|---|---|
| Usage | Plain unit tests (no Spring context). | Integration tests (with @SpringBootTest). |
| Mechanism | Standard Mockito initialization. | Replaces an existing Bean in the Spring Context with a mock. |
| Performance | Very fast. | Slower, as it may trigger Spring context refreshing. |
| Context | Objects are just local variables. | Objects are injected into other Spring Beans. |

Note: 
--
* In recent Spring Boot versions (3.4+), 
	* @MockBean and @SpyBean have been deprecated in favor of @MockitoBean and @MockitoSpyBean.
	
	
----
# standard service-layer test example. 
* uses JUnit 5 to structure the test and Mockito to handle dependencies, 
* specifically highlighting how to 
	* group assertions and 
	* verify method interaction counts.
	
## The Code Example
* Assume we are testing a UserService that 
	* sends a welcome email via an EmailService and 
	* saves the user to a UserRepository. 
---
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables @Mock and @InjectMocks
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully register a new user and send an email")
    void registerUser_Success() {
	
        // 1. Arrange
        User user = new User("John Doe", "john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 2. Act
        User registeredUser = userService.register(user);

        // 3. Assert (JUnit 5 assertAll)
        // Grouping assertions ensures that even if one fails, the others still run
        assertAll("User registration verification",
            () -> assertNotNull(registeredUser, "Registered user should not be null"),
            () -> assertEquals("John Doe", registeredUser.getName()),
            () -> assertEquals("john@example.com", registeredUser.getEmail())
        );

        // 4. Verification (Mockito Verify)
        // Verify method was called exactly once (default)
        verify(userRepository, times(1)).save(user);

        // Verify email was sent exactly once
        verify(emailService, times(1)).sendWelcomeEmail(user.getEmail());

        // Verify a 'never' condition: ensure no error logs were sent
        verify(emailService, never()).sendErrorLog(anyString());
    }
    
    @Test
    @DisplayName("Should throw exception when email is invalid")
    void registerUser_InvalidEmail_ThrowsException() {
        // Using assertThrows for exception verification
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(new User("Invalid", "bad-email"));
        });

        assertEquals("Invalid email format", exception.getMessage());
        
        // Verify that the repository was NEVER called because of the exception
        verify(userRepository, never()).save(any(User.class));
    }
}
---

## Breakdown of Key Features Used

* assertAll: 
	* Instead of individual assertions stopping the test at the first failure, this groups them. 
		* If assertNotNull fails, JUnit still checks the name and email equality, providing a full report of everything that went wrong.
* verify(..., times(n)): 
	* Explicitly checks that the save and sendWelcomeEmail methods were triggered exactly as expected.
* never(): 
	* Crucial for ensuring that "side paths" (like error handling or cleanup) were not triggered during a successful flow.
* assertThrows: 
	* The standard way in JUnit 5 to verify that your code correctly handles and throws specific exceptions.
* @ExtendWith(MockitoExtension.class): 
	* This replaces the older MockitoAnnotations.openMocks(this) and 
	* is the modern way to initialize your @Mock fields in JUnit 5. 

----
## Spring Boot Integration Test Example
* In a Spring Boot integration test, 
	* we replace the standard Mockito annotations (@Mock, @InjectMocks) 
	* with Spring-specific ones. 
* This allows the Spring ApplicationContext to manage the lifecycle and injection of your mocks into other beans.

* Starting with Spring Boot 3.4, @MockBean is deprecated in favor of ***@MockitoBean***.  

* This test loads the Spring context but replaces the UserRepository and EmailService with mocks. 
* Unlike the previous unit test, Spring will automatically inject these mocks into the real UserService bean. 

---
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest // Loads the full Spring ApplicationContext
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService; // The REAL bean from the Spring context

    @MockitoBean // Replaces the real bean in the context with a Mockito mock
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void registerUser_IntegrationFlow() {
        // 1. Arrange
        User user = new User("Jane Smith", "jane@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 2. Act
        User result = userService.register(user);

        // 3. Assert & Verify
        assertAll("Integration Check",
            () -> assertEquals("Jane Smith", result.getName()),
            () -> verify(userRepository, times(1)).save(any()),
            () -> verify(emailService, times(1)).sendWelcomeEmail("jane@example.com")
        );
    }
}
---

## Key Differences from the Unit Test

| Feature  | Unit Test (Mockito) | Integration Test (Spring Boot) |
|---|---|---|
| Annotation | @ExtendWith(MockitoExtension.class) | @SpringBootTest |
| Mocking | @Mock (Local object) | @MockitoBean (Context bean) |
| Injection | @InjectMocks (Manual/Mockito) | @Autowired (Spring Dependency Injection) |
| Execution | Fast, no overhead | Slower, starts the application |

## Best Practices

* Use @MockitoBean for Spring Boot 3.4+. 
	* For older versions, use the now-deprecated @MockBean.
* Prefer Unit Tests with @Mock 
	* for business logic 
	* to keep your build pipeline fast.
* Use @SpringBootTest 
	* sparingly, 
	* mainly for verifying that your beans "wire up" correctly 
	* or for complex flows involving multiple layers. 

----
# @MockitoSpyBean

* In Spring Boot 3.4+, @MockitoSpyBean (the replacement for the deprecated @SpyBean) 
* is used to create a partial mock of a real Spring bean.
* A "spy" wraps an existing instance, 
* allowing you to call the real logic by default 
* while selectively overriding ("stubbing") specific methods. 

## Important: The Migration Difference
* Unlike the old @SpyBean, the 
* new @MockitoSpyBean requires that the bean already exists in the Application Context. 
* If Spring cannot find a real bean of that type to wrap, 
* the test will fail with an IllegalStateException.

## Partial Mocking Example
* In this scenario, we use the real PricingService logic 
	* but mock a single method (getDiscount) 
	* to return a fixed value for our test. [5, 9] 
---
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PricingServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private PricingService pricingService; // Spying on a real Spring bean

    @Test
    void calculateTotal_WithPartialMock() {
        // 1. Stub ONLY the getDiscount method
        // Use doReturn() for spies to avoid calling the real method during stubbing
        doReturn(10.0).when(pricingService).getDiscount("VIP_CODE");

        // 2. Act
        // This will call real methods for calculateBasePrice() 
        // but use our mock for getDiscount()
        double total = orderService.processOrder("VIP_CODE", 100.0);

        // 3. Assert & Verify
        assertEquals(90.0, total); // 100 - 10 (mocked discount)
        
        // Verify the real method was actually called
        verify(pricingService, times(1)).getDiscount("VIP_CODE");
        
        // Verify another method on the same bean that ran its REAL logic
        verify(pricingService, atLeastOnce()).calculateBasePrice(anyDouble());
    }
}

## Best Practices for Spies

* Use **doReturn(...).when(...)**: 
	* When working with spies, 
		* always prefer this syntax over **when(...).thenReturn(...)**. 
		* The latter actually calls the real method once during the setup, 
		* 	which can cause side effects like null pointers or database calls.
* Verify Interactions: 
	* Spies are excellent for verifying that a specific internal method of a bean was called 
		* without having to mock the entire class.
* Existing Beans: 
	* If you need to spy on a class that isn't automatically picked up as a bean, 
		* use @Import(MyClass.class) to ensure it's in the context 
		* before the spy tries to wrap it. 

## When is @SpyBean Useful?
* You should use a spy when you want the bean to behave normally for most methods 
	* but need to "lie" about a specific one.

* Suppressing Side Effects: 
	* If a service performs a complex calculation you want to test, 
	* but it also triggers an unwanted side effect 
		* (like sending a real SMS or calling an external API), 
	* you can spy on the service and mock only the side-effect method.
* Legacy Code: 
	* When testing a large, "monolithic" bean that is hard to break apart, 
		* you can use a spy to isolate just the one method you are currently testing.
* Verifying Real Interactions: 
	* You want to ensure a method was called with specific parameters, 
		* but you still need that method to actually execute its real logic (e.g., saving to a test database).

## Why Mock a Method on a Real Bean?
* Mocking a single method on a real bean is often done 
* to bypass "non-deterministic" or "external" factors 
* while keeping the rest of the business logic intact. 

| Scenario | Why use a Spy? |
|---|---|
| External Dependencies | To mock a method that calls a third-party API that might be down or slow, without mocking the entire service logic. |
| Date/Time Logic | To force a method like getCurrentDate() to return a specific "yesterday" value so you can test time-sensitive logic (like expiration) using the real bean's other methods. |
| Complex Internal Calls | If Method A calls Method B internally, and Method B is extremely slow or requires complex setup, you can mock Method B so you can focus on testing Method A. |

## Example: Partial Mocking in Action
* Imagine a DiscountService where calculateFinalPrice() is the main logic, 
* but it calls fetchExternalExchangeRate() internally.
---
@SpringBootTest
class DiscountServiceTest {

    @MockitoSpyBean
    private DiscountService discountService; // The real bean from the context

    @Test
    void testPriceCalculationWithMockedRate() {
        // We only "mock" the external call, but keep the real calculation logic
        doReturn(1.10).when(discountService).fetchExternalExchangeRate();

        double result = discountService.calculateFinalPrice(100.0);

        // result is 110.0 (uses real calculation + mocked rate)
        assertEquals(110.0, result);
        verify(discountService).fetchExternalExchangeRate(); 
    }
}
---

Crucial Note: 
--
* When using spies, always use doReturn().when() instead of when().thenReturn(). 
* The latter will actually execute the real method one time during the "setup" phase, 
	* which often leads to errors if that method has side effects.

----
# @Mock vs @InjectMocks

* In simple terms: 
	* @Mock creates the "fake" dependencies, and 
	* @InjectMocks creates the "real" object you want to test, automatically plugging those fakes into it.

* Think of it like building a car:
	* @Mock: 
		* These are the fake parts (a plastic engine, a toy steering wheel).
	* @InjectMocks: 
		* This is the actual car body where you want to install those parts to see if the dashboard lights up.

------------------------------
## 1. The Difference in Detail

| Feature | @Mock | @InjectMocks |
|---|---|---|
| What it creates | A "test double" (dummy object). | A real instance of the class under test. |
| Logic | No real code runs (unless stubbed). | The real code of the class runs. |
| Purpose | To simulate dependencies (DB, API, etc.). | To be the "Subject Under Test" (SUT). |
| Initialization | Mockito creates a proxy of the class. | Mockito tries to inject @Mock fields into it. |

## 2. Code Example
* If you are testing a UserService that depends on a UserRepository:

---
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Create a fake repo

    @InjectMocks
    private UserService userService; // Create a REAL service and put the fake repo inside it

    @Test
    void testSave() {
        // Arrange
        User user = new User("Alex");
        
        // Act
        userService.register(user); // This calls the REAL method in UserService

        // Assert
        verify(userRepository).save(user); // Verify the REAL method called the FAKE repo
    }
}
---

## 3. How @InjectMocks Works Internally
* Mockito tries to inject the @Mock fields into the @InjectMocks object using this priority:
	* 1. Constructor Injection: Best practice. It looks for a constructor that matches the mocks.
	* 2. Property (Setter) Injection: If no constructor matches, it looks for setter methods.
	* 3. Field Injection: If all else fails, it uses reflection to set the private fields directly.

## Common Pitfalls

* Don't Mock the Class Under Test: 
	* Never put @Mock and @InjectMocks on the same field. 
	* You want the class you are testing to be real.
* Initialization: 
	* You must use 
		* @ExtendWith(MockitoExtension.class) or 
		* MockitoAnnotations.openMocks(this), 
	* otherwise both will be null.
* Interfaces: 
	* You can only use @InjectMocks on a concrete class (the implementation), not an interface.


## 1. Handling Multiple Mocks/Spies at Scale [2] 
* In modern Java testing, managing multiple dependencies and inspecting complex data flows are critical. 
* Here is how to handle multiple mocks in Spring Boot 3.4+ and use ArgumentCaptors to verify your data. 

* When your test class requires many dependencies, 
	* declaring them individually can become cluttered. 
* Spring Boot 3.4 introduced type-level declarations for @MockitoBean and @MockitoSpyBean, 
	* which can act as a container for multiple mocks.

* Type-Level Declaration: 
	* You can declare all your required mocks at the top of your class.
* Automatic Injection: 
	* Spring will automatically replace these types in the ApplicationContext.
* Disambiguation: 
	* If you have multiple beans of the same type, 
	* you can use the name attribute to specify which one to mock.

---
@SpringBootTest// Container-style declaration for multiple mocks/spies
@MockitoBean(types = {InventoryClient.class, PaymentGateway.class})
@MockitoSpyBean(types = AuditService.class) 
class OrderProcessingTest {

    @Autowired
    private OrderService orderService; // Real service using these mocks

    @Autowired
    private InventoryClient inventoryClient; // Injected automatically for stubbing

    @Test
    void testComplexFlow() {
        // Stubbing the type-level mocks
        when(inventoryClient.checkStock(any())).thenReturn(true);
        
        orderService.placeOrder(new Order("item123", 2));
        
        verify(inventoryClient).checkStock("item123");
    }
}

## 2. Deep Inspection with ArgumentCaptor
* While ***verify*** tells you if a method was called, 
* ***ArgumentCaptor*** lets you "open the package" and 
	* inspect the exact object passed to that method. 
* This is essential for verifying objects created internally by the service you are testing.

* @Captor: 
	* The cleanest way to initialize a captor in JUnit 5.
* capture(): 
	* Used inside the verify block to "grab" the argument.
* getValue() vs getAllValues(): 
	* Use getValue() for a single call or 
	* getAllValues() to retrieve a list if the method was called multiple times (e.g., in a loop). 

---
	@ExtendWith(MockitoExtension.class)
	class NotificationTest {

		@Mock
		private SmsProvider smsProvider;

		@InjectMocks
		private NotificationService service;

		@Captor
		private ArgumentCaptor<SmsRequest> smsCaptor;

		@Test
		void shouldSendProperlyFormattedSms() {
			service.notifyUser("John", "Order Shipped");

			// 1. Capture the argument
			verify(smsProvider).send(smsCaptor.capture());

			// 2. Inspect the captured object
			SmsRequest capturedRequest = smsCaptor.getValue();
			
			assertAll("SMS Payload Check",
				() -> assertEquals("+123456789", capturedRequest.getPhone()),
				() -> assertTrue(capturedRequest.getMessage().contains("John")),
				() -> assertTrue(capturedRequest.getMessage().contains("Shipped"))
			);
		}
	}
---

## Comparison: Matchers vs. Captors

| Feature | Argument Matchers (any(), eq()) | ArgumentCaptor |
|---|---|---|
| Best For | Stubbing behavior (when...then). | Asserting complex internal state. |
| Complexity | Simple type/value checks. | Deep field-by-field assertions. |
| Readability | High for simple verifications. | Can be verbose; use only when needed. |

Tip:
--
* For generic collections (like List<User>), 
	* use ArgumentCaptor.forClass(List.class) or 
	* the newer ArgumentCaptor.captor() to avoid compiler warnings about type erasure.
	
## 1. Capturing Multiple Arguments from a Loop
* When a method is called multiple times (e.g., inside a for loop), 
	* ArgumentCaptor.getValue() only returns the last captured value. 
	* To inspect every single call, you must use getAllValues(). 
---
	@Testvoid testBulkNotification() {
		List<String> users = Arrays.asList("Alice", "Bob", "Charlie");
		
		// Act: Service calls smsProvider.send() 3 times inside a loop
		notificationService.sendBulk(users, "Welcome!");

		// Verify and Capture
		verify(smsProvider, times(3)).send(smsCaptor.capture());

		// Retrieve all captured arguments as a List
		List<SmsRequest> capturedRequests = smsCaptor.getAllValues();

		assertAll("Bulk SMS Check",
			() -> assertEquals(3, capturedRequests.size()),
			() -> assertEquals("Alice", capturedRequests.get(0).getRecipient()),
			() -> assertEquals("Bob", capturedRequests.get(1).getRecipient()),
			() -> assertEquals("Charlie", capturedRequests.get(2).getRecipient())
		);
	}
---

## 2. Testing Asynchronous Code
* Testing code that runs asynchronously (like CompletableFuture or @Async methods) is tricky 
	* because the test thread might finish before the background task even starts.
	
## A. The timeout() Method
* The simplest way is to use Mockito.***timeout(ms)***. 
* This tells Mockito to wait up to a certain duration for the interaction to happen. 
* It passes as soon as the condition is met, so it doesn't always wait the full time.
---
	@Testvoid testAsyncProcessing() {
		// Act: This method returns immediately but starts a background thread
		asyncService.processDataInBackground("data_id_123");

		// Verify with a 500ms timeout
		// It will poll the mock until the call happens or time runs out
		verify(dataRepository, timeout(500)).save("data_id_123");
	}
---

## B. The after() Method (Rare but useful)
* Unlike timeout(), which stops as soon as it succeeds, 
	* ***after(ms)*** waits the entire duration to ensure a condition stays true or to check for late interactions.

| Feature  | timeout(ms) | after(ms) |
|---|---|---|
| Behavior | Passes immediately once the call happens. | Waits the full time before passing. |
| Use Case | Most async tests (fastest). | Verifying something never happens within a window. |

## Summary of Advanced Assertions
--
* verifyNoMoreInteractions(mock): 
	Use this at the end of a test to ensure no other "secret" calls were made to your dependency.
* InOrder: 
	* Use inOrder(mock1, mock2) to verify that methods were called in a specific sequence 
	* (e.g., "Login" must happen before "Delete"). [10] 


## Combining InOrder with ArgumentCaptor Scenario
* Combining InOrder with ArgumentCaptor is the "gold standard" 
	* for testing critical workflows 
	* where the sequence of events and the data integrity are both vital 
	* (e.g., a financial transaction where you must lock an account before withdrawing funds).

* We are testing a PaymentService. The business rule is:
	* 1. First, create a Transaction record.
	* 2. Second, call the BankGateway with that transaction's ID.

## The Complex Code Example
---
	import org.junit.jupiter.api.Test;
	import org.mockito.ArgumentCaptor;
	import org.mockito.InOrder;
	import org.mockito.InjectMocks;
	import org.mockito.Mock;
	import org.mockito.junit.jupiter.MockitoExtension;
	import org.junit.jupiter.api.extension.ExtendWith;
	import static org.junit.jupiter.api.Assertions.*;
	import static org.mockito.Mockito.*;

	@ExtendWith(MockitoExtension.class)
	class PaymentServiceTest {

		@Mock private TransactionRepo repo;
		@Mock private BankGateway bank;

		@InjectMocks 
		private PaymentService service;

		@Test
		void processPayment_ShouldFollowStrictSequence() {
			// 1. Arrange
			PaymentRequest request = new PaymentRequest("USR_1", 500.0);
			
			// 2. Act
			service.process(request);

			// 3. Create the InOrder verifier
			InOrder inOrder = inOrder(repo, bank);

			// 4. Verify Sequence & Capture Data simultaneously
			// Step A: Verify the repo was called first
			ArgumentCaptor<Transaction> transCaptor = ArgumentCaptor.forClass(Transaction.class);
			inOrder.verify(repo).save(transCaptor.capture());

			// Step B: Verify the bank was called second using the ID from the first call
			ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
			inOrder.verify(bank).authorize(idCaptor.capture(), eq(500.0));

			// 5. Deep Assertions on Captured Data
			Transaction capturedTrans = transCaptor.getValue();
			String capturedId = idCaptor.getValue();

			assertAll("Workflow Validation",
				() -> assertEquals("USR_1", capturedTrans.getUserId()),
				() -> assertEquals(capturedTrans.getId(), capturedId, "Bank ID must match Repo ID"),
				() -> verifyNoMoreInteractions(bank) // Ensure no double-charging
			);
		}
	}
---

## Why this is powerful
---
* Strict Order: 
	* If your code calls the bank before the repo, 
		* inOrder.verify() will fail, even if both calls happened eventually.
* Data Linking: 
	* By capturing the ID in the second call, 
	* you ensure the code isn't just calling the bank with any ID, 
	* but specifically the one it just generated in the database.
* Safety: 
	* verifyNoMoreInteractions at the end 
		* ensures no "ghost" calls happen after the process is supposed to be finished.

## Quick Checklist for Success
---
	* 1. inOrder(mockA, mockB): 
		* Pass every mock involved in the sequence to the constructor.
	* 2. Order Matters: 
		* Call inOrder.verify() in the exact order you expect the code to execute.
	* 3. Use eq(): 
		* If you use a Captor for one argument but a hardcoded value for another, 
		/ you must wrap the hardcoded value in eq() (e.g., eq(500.0)).

## 1. Mocking Static Methods (Mockito 3.4+)
* In the past, you needed PowerMock to mock static methods like 
	* LocalDateTime.now() or 
	* UUID.randomUUID(). 
* Now, Mockito handles this natively using mockStatic.

* Important: 
	* Static mocks use a "scoped" object. 
	* You must close the mock (usually in a try-with-resources block) to avoid affecting other tests.
---
	@Test
	void testReportTimestamp() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0);

		// Use try-with-resources to ensure the static mock is closed
		try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
			// Stub the static call
			mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

			// Act
			Report report = reportService.createReport("Monthly Audit");

			// Assert
			assertEquals(fixedTime, report.getCreatedAt());
			
			// Verify the static method was called
			mockedTime.verify(LocalDateTime::now, times(1));
		}
		// Outside this block, LocalDateTime.now() returns the real current time again.
	}
---

## 2. Custom ArgumentMatchers
* When checking complex objects, using ArgumentCaptor can make your verify blocks very long. 
* Custom Matchers allow you to hide that logic behind a readable name.
* Instead of capturing a User and checking its email, 
	* you can create a matcher that says argThat(hasValidEmail()).
---
	import org.mockito.ArgumentMatcher;
	import static org.mockito.ArgumentMatchers.argThat;
	
	// 1. Define the Matcher
	public class UserWithEmailMatcher implements ArgumentMatcher<User> {
		private final String expectedEmail;

		public UserWithEmailMatcher(String expectedEmail) {
			this.expectedEmail = expectedEmail;
		}

		@Override
		public boolean matches(User user) {
			return user != null && expectedEmail.equals(user.getEmail());
		}
	}

	// 2. Use it in the Test
	@Test
	void shouldRegisterUserWithCorrectEmail() {
		userService.register("John", "john@example.com");

		// Clean, readable verification
		verify(userRepository).save(argThat(new UserWithEmailMatcher("john@example.com")));
	}
---
Pro Tip: 
* For simple cases, use a Lambda Matcher instead of creating a whole class:
---
	verify(repo).save(argThat(user -> user.getEmail().equals("john@example.com")));
---
------------------------------
## Master Summary Table

| Tool / Feature | Best Used For... | Key Syntax |
|---|---|---|
| assertAll | Running multiple assertions without stopping at the first failure. | assertAll("label", () -> ..., () -> ...) |
| verify(..., times(n)) | Checking how many times a dependency was called. | verify(mock, times(2)).method() |
| ArgumentCaptor | Deeply inspecting an object created inside the code-under-test. | captor.capture() / getValue() |
| mockStatic | Controlling system clocks, UUIDs, or utility classes. | try (MockedStatic<T> ms = mockStatic(T.class)) |
| InOrder | Ensuring Method A happens strictly before Method B. | inOrder.verify(mock).method() |
| @MockitoBean | Replacing a Spring Bean with a mock in integration tests. | @MockitoBean private MyService mock; |

## 1. Maven Configuration (pom.xml)
* To get all these features (especially Static Mocking and Spring Boot 3.4+ support) working, 
* you need specific dependencies and configurations in your build file.
* Ensure you are using the spring-boot-starter-test, which bundles JUnit 5 and Mockito. 
* For Static Mocking, you must explicitly include mockito-inline.

---
<dependencies>
    <!-- Standard Spring Boot Test Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>3.4.0</version> <!-- Use 3.4+ for @MockitoBean -->
        <scope>test</scope>
    </dependency>

    <!-- REQUIRED for Static Mocking (mockStatic) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-inline</artifactId>
        <version>5.2.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
---

## 2. Gradle Configuration (build.gradle)
* In Gradle, you must also ensure the JUnit Platform is enabled so your @Test annotations are recognized.
---
dependencies {
    // Standard Test Starter
    testImplementation 'org.springframework.boot:spring-boot-starter-test:3.4.0'
    
    // REQUIRED for Static Mocking
    testImplementation 'org.mockito:mockito-inline:5.2.0'
}

test {
    useJUnitPlatform() // Essential for JUnit 5
}
---

## 3. Common Setup Pitfalls
---
* Version Conflicts: 
	* If you use spring-boot-starter-test, 
		* avoid adding manual versions of junit-jupiter or mockito-core, 
		* as Spring manages those versions for compatibility.
* Mockito-Inline: 
	* As of Mockito 5.x, 
		* "inline" mocking (which allows static/final mocking) is the default in some environments, 
		* but adding the dependency explicitly ensures it works across all CI/CD pipelines.
* Parallel Execution: 
	* If you use mockStatic, 
		*be careful with parallel test execution, 
		* as static mocks are thread-local.

## Summary of the "Golden Rule"
--
* Unit Tests: 
	* Use @ExtendWith(MockitoExtension.class) + @Mock. (Fast)
* Integration Tests: 
	* Use @SpringBootTest + @MockitoBean. (Thorough)

----

# Junit versions history

* JUnit has evolved over nearly three decades from a simple framework into a modular testing platform. 
* Below is the historical progression of its major versions and their landmark features.

## JUnit 1.x & 2.x (1997–1999): The Foundations
* Created by Kent Beck and Erich Gamma, 
	* JUnit was inspired by the Smalltalk framework SUnit.

* JUnit 1.0 (1998): 
	* The initial open-source release that established the basic structure of unit testing in Java.
* Core Feature: 
	* Strict inheritance-based testing. 
	* To create a test, you had to extend the TestCase class.
* Naming Convention: 
	* Tests were discovered based on their names; 
	* every test method had to start with the prefix test 
		* (e.g., testCalculateTotal).

## JUnit 3.x (2000–2005): The Legacy Era
* JUnit 3 became the de facto standard for Java testing during the early 2000s.

* Key Features: 
	* Continued reliance on naming conventions and inheritance.
* Lifecycle: 
	* Introduced ***setUp()*** and ***tearDown()*** methods 
		* that automatically ran before and after each test method.
* Limitation: 
	* It was "annotation-light" and rigid, 
	* making it difficult to organize large suites without complex hierarchies. 

## JUnit 4.x (2006–2016): The Annotation Revolution  
* Released in 2006, JUnit 4 leveraged **Java 5's new annotation feature**, which revolutionized the framework.

* Major Introduction: 
	* The ***@Test*** annotation. 
	* Classes no longer had to extend TestCase; 
	* any public method could be a test.
* Lifecycle Annotations: 
	* Replaced setUp/tearDown with more flexible annotations like 
		* @Before, @After, @BeforeClass, and @AfterClass.
* New Capabilities:
	* Timeout & Exceptions: 
		* @Test(timeout=1000) and 
		* @Test(expected=Exception.class) 
		* were added directly to the annotation.
	* Parameterized Tests: 
		* Allowed running the same test with different sets of data.
	* Rules: 
		* Introduced 
			* @Rule and @ClassRule 
				* to add or redefine the behavior of each test method in a reusable way.

## JUnit 5 (2017–Present): The Modular Platform  
* Released in late 2017, JUnit 5 (also called ***JUnit Jupiter***) 
	* was a complete re-architecture designed for Java 8 and beyond.

* Modular Architecture: 
	* Split into three parts to decouple the platform from the test engine:
		1. ***JUnit Platform***: 
			* Launcher for discovering and executing tests on the JVM.
		2. ***JUnit Jupiter***: 
			* The new programming and extension model.
		3. ***JUnit Vintage***: 
			* A test engine to run legacy JUnit 3 and 4 tests.
* Key Modern Features:
	* Lambda Support: 
		* Assertions like ***assertAll*** and ***assertThrows*** now accept lambda expressions.
	* Nested Tests: 
		* Use ***@Nested*** to create hierarchical, readable test structures.
	* Extension Model: 
		* Replaced the rigid Runner and Rule systems with a powerful, unified Extension API.
	* Display Names: 
		* Use ***@DisplayName*** to give tests human-readable names instead of method names. 

## JUnit 6 (2025): The Modern Standard
* Released in late 2025, JUnit 6 focuses on modern Java versions 
	* (requiring Java 17+) and further simplification. 

* Modernization: 
	* Fully embraces modern Java features and removes legacy modules like junit-platform-runner.
* Nullability: 
	* Integrates JSpecify annotations across all modules to improve null-safety in tests.
* Performance: 
	* Optimized for cloud-native microservices and modern build tools.

* Moving from JUnit 4 to JUnit 5 (Jupiter) involves more than just changing names; 
* it changes how the test lifecycle is managed. 
* JUnit 5 annotations are more descriptive and follow a clearer "Before/After" logic.

## Annotation Migration Table

| Feature | JUnit 4 (Legacy) | JUnit 5 (Modern) | Description |
|---|---|---|---|
| Test Method | @Test | @Test | Same name, but JUnit 5's @Test does not support timeout or expected parameters. |
| Before Each | @Before | @BeforeEach | Runs before every individual test method. |
| After Each | @After | @AfterEach | Runs after every individual test method. |
| Before All | @BeforeClass | @BeforeAll | Runs once before all tests in the class (must be static). |
| After All | @AfterClass | @AfterAll | Runs once after all tests in the class (must be static). |
| Ignore Test | @Ignore | @Disabled | Skips the test during execution. |
| Category | @Category | @Tag | Used to filter and group tests (e.g., @Tag("slow")). |
| Extensions | @RunWith / @Rule | @ExtendWith | The new unified extension model (e.g., @ExtendWith(MockitoExtension.class)). |

## Key Functional Differences

## 1. Assertions and Lambdas
* In JUnit 4, assertions were simple. 
	* In JUnit 5, they are functional.
* JUnit 4: 
	* assertTrue(message, condition);
* JUnit 5: 
	* assertTrue(condition, () -> message); 
	* (The message is lazily evaluated only if the test fails, saving resources).

## 2. Exception Testing
* JUnit 5 moved exception handling from the annotation to the method body for better precision.
	* JUnit 4: @Test(expected = Exception.class) — Fails if any part of the method throws.
	* JUnit 5: assertThrows(Exception.class, () -> service.doSomething()); — Fails only if that specific line throws.

## 3. Visibility
* JUnit 5 no longer requires test classes and methods to be public. 
* You can use package-private (default) visibility, which keeps your test API cleaner.

## 4. Parameterized Tests
* JUnit 4 required a special Runner and was cumbersome. 
* JUnit 5 has a dedicated module (junit-jupiter-params) that allows you to pass arguments easily:

---
	@ParameterizedTest
	@ValueSource(strings = {"apple", "banana"})
	void testWithStrings(String candidate) {
		assertNotNull(candidate);
	}
---



