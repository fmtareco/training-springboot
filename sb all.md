# training-springboot

----

# Spring Core Fundamentals (Very Common)

What is Dependency Injection (DI)?
--
•	Dependency Injection (DI) is a design pattern 
o	to achieve Inversion of Control (IoC) between classes and their dependencies. 
•	instead of a class creating the objects it needs (using the new keyword), 
o	objects are provided (injected) to it by an external entity—Spring IoC Container. 
•	Without DI: 
o	Class A creates an instance of Class B. 
o	Class A is now "tightly coupled" to the specific implementation of B.
•	With DI: 
o	Class A declares it needs an object of type B. 
o	The Spring Container creates B and gives it to A. 
•	Why use it?
o	Decoupling: 
	don't need to know how to instantiate their dependencies.
o	Easier Testing: 
	can easily inject "Mock" objects during unit tests.
o	Flexibility: 
	can swap implementations (e.g., changing a MySQLRepository for a PostgreSQLRepository) 
	without touching the code of the classes that use them. 
•	Types of Injection in Spring
•	Constructor Injection (Recommended)
o	provided through the constructor. 
o	safest way as it 
	allows for immutable fields (final) and 
	ensures the object is never created in an incomplete state. 
•	Setter Injection
o	provided through a setter method. 
o	Useful for optional dependencies. 
@Autowired
public void setUserRepository(UserRepository userRepository) {
	this.userRepository = userRepository;
}
•	Field Injection
o	injected directly into the field using @Autowired. 
o	generally discouraged because it 
	makes testing harder and 
	hides dependencies
	fields not final


What is the IoC container?
--
•	Inversion of Control (IoC) Container 
o	core engine of the Spring Framework. 
o	responsible for managing the entire lifecycle of your application's objects (known as Beans). 
o	Instead manually creating objects, the container "takes control" of
	instantiating, 
	configuring, and 
	assembling them. 
•	1. How it Works (The Workflow)
o	Metadata: 
	instructions via Annotations (@Component, @Service), 
	Java Config (@Configuration), or 
	XML.
o	Instantiation: 
	container reads this metadata and creates instances of your classes.
o	Dependency Injection: 
	looks at what each object needs and "injects" the required dependencies.
o	Management: 
	keeps the objects in memory (usually as Singletons) and 
	destroys them when the application shuts down. 
•	2. The Two Main Types of Containers
o	BeanFactory: 
	basic version. 
	provides the configuration framework and basic functionality (Lazy loading by default).
o	ApplicationContext: 
	more advanced version (and the one everyone uses). 
	includes everything in BeanFactory plus:
•	Easier integration with Spring AOP.
•	Message resource handling (for internationalization).
•	Event publication (Application Events).
•	Web-specific features. 
•	3. Key Responsibilities
o	Object Creation: 
	Finding classes marked as beans and creating them.
o	Wiring: 
	Connecting objects together (Dependency Injection).
o	Lifecycle Management: 
	Handling initialization methods 
	@PostConstruct - to be executed after dependency injection to perform any initialization
	destruction methods 
	@PreDestroy - to signal that the instance is in the process of being removed by the containe
o	Scope Management: 
	Deciding if it should create a new instance every time (Prototype) or 
	keep just one for the whole app (Singleton). 
•	4. Why call it "Inversion of Control"?
o	framework controls the flow and gives your code the objects it needs. 
o	Control is inverted from the developer to the framework.

Difference between BeanFactory and ApplicationContext
--
•	BeanFactory 
o	basic, lightweight IoC container for bean management and 
o	lazy loading, 
o	ideal for memory-constrained environments. 
•	ApplicationContext 
o	advanced, eager-loading superset that adds enterprise-level features like AOP, i18n, and event publication. ApplicationContext is generally preferred, especially in web applications. 
•	Initialization: 
o	BeanFactory uses lazy initialization (beans created on demand), while 
o	ApplicationContext uses eager initialization (all singleton beans created at startup).
•	Features: 
o	ApplicationContext supports 
	internationalization (i18n), 
	event propagation, and
	 AOP, which BeanFactory does not.
•	Configuration: 
o	ApplicationContext 
	improved annotation-based dependency injection and 
	supports web-specific scopes (Request, Session).
•	Resource Management: 
o	BeanFactory 
	requires manual resource loading, 
o	ApplicationContext 
	manages resources on its own.
•	Performance: 
o	BeanFactory is more memory-efficient due to lazy loading. 
•	When to Use Which
o	BeanFactory: 
	very simple applications or 
	memory-constrained environments (like embedded systems).
o	ApplicationContext: 
	most enterprise-level applications, 
	Spring Boot projects, and 
	when using annotation-based configuration

What is a Spring Bean lifecycle?
--
•	predefined sequence of stages that a Bean goes through from 
o	its creation to its destruction, 
o	managed entirely by the Spring IoC Container.
•	Main Lifecycle Phases
•	Instantiation: 
o	The container creates the bean instance (using its constructor or factory method).
•	Populate Properties: 
•		Spring injects the necessary dependencies (via @Autowired, setters, or constructors).
•	Aware Interfaces: 	 
•		BeanNameAware – 
•			allows know the ID/name assigned to it within the Spring container.
•			void setBeanName(String name)
•		ApplicationContextAware
	provides the bean with a reference to the ApplicationContext it is running in. 
	void setApplicationContext(ApplicationContext applicationContext).
	allows the bean to perform advanced tasks such as:
		Programmatically looking up other beans via getBean().
		Accessing file resources or environment settings.
		Publishing application events.
•	BeanPostProcessor (Before Initialization): 	
o	The postProcessBeforeInitialization methods are triggered 
o		On interface BeanPostProcessor
o		called after bean created and dependencies injected, 
o		before any initialization callbacks (like @PostConstruct, afterPropertiesSet, or a custom init-method).
•	Initialization: 
•		where run custom setup logic. 
•		The order of execution is:
•			Methods annotated with @PostConstruct.
•			The afterPropertiesSet() method from the InitializingBean interface.
•			Custom init-method defined in XML or via @Bean(initMethod = "...").
•	BeanPostProcessor (After Initialization): 
•		The postProcessAfterInitialization methods run, 
•			often wrapping the bean in a proxy (e.g., for AOP or @Transactional).
•	Ready for Use: 
•		The bean is now fully initialized and active within the application.
•	Destruction: 
•		When the ApplicationContext closes, the bean is cleaned up:
•			Methods annotated with @PreDestroy.
•			The destroy() method from the DisposableBean interface.
•			Custom destroy-method defined in configuration.
•	Summary of Control Hooks
•		most common way using JSR-250 annotations @PostConstruct and @PreDestroy


What scopes exist for Spring beans?
--
•	bean's scope defines its lifecycle and visibility within the application. 
o	@Component
o	@Scope("prototype")
o	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
•	
•	Spring provides six built-in scopes:
•		Core Scopes (Available in any Spring Application) 
•			Singleton (Default): 
•				single instance per Spring IoC container. 
•				Every request for that bean name returns the same cached instance.
•			Prototype: 
•				new instance every time the bean is requested from the container. 
•		Web-Aware Scopes (Require Web-Aware ApplicationContext) 
•			Request: 
•				Scopes a bean to the lifecycle of a single HTTP request.
•			Session: 
•				Scopes a bean to the lifecycle of an HTTP Session.
•			Application: 
•				Scopes a bean to the lifecycle of a ServletContext.
•			WebSocket: 
•				Scopes a bean to the lifecycle of a WebSocket connection. 
•	Additionally, you can define Custom Scopes by implementing the Scope interface.


What is component scanning?
--
•	Component Scanning 
•		mechanism to automatically discover and register beans in Application Context. 
•		eliminates the need to manually define every single bean in configuration files.
•	1. How it works
•		startup, "scans" your project’s classpath for classes decorated with stereotype annotations. 
•		When finds one, creates an instance (a bean) and adds it to the IoC container.
o	primary annotations Spring looks for are:
o		@Component: generic parent annotation.
o		@Service: For business logic.
o		@Repository: For data access (DAO).
o		@Controller for returning web pages (Views)
o		@RestController: for returning data (JSON/XML).
•	2. Automatic Activation in Spring Boot
•		don't usually need to enable it manually. 
•		@SpringBootApplication annotation (found on your main class) includes @ComponentScan by default.
•		Default Behavior: scans the package containing your main class and all of its sub-packages.
•	3. Manual Configuration
•		need to scan packages outside your main package hierarchy:
o		@Configuration
o		@ComponentScan(basePackages = {"com.myproject.services", "com.myproject.utils"})
•	Why it matters
•		Less Boilerplate: No need to write @Bean methods for every class.
•		Decoupling: Classes "announce" themselves to the container, making the system modular.
•		Auto-wiring: Once scanned, these beans are immediately available for @Autowired injection.

What is a Circular Dependency
--
•	Constructor Injection, forced to resolve dependency at moment  instantiation. 
•	Bean A needs Bean B in its constructor, and Bean B needs Bean A in its constructor, Spring hits a deadlock
•	How to resolve this:
•		Redesign (Best Practice): 
•			circular dependency is a sign of poor architectural design. 
•			extract the shared logic into a third bean (Bean C) that both A and B can depend on.
•		@Lazy Annotation: 
•			"quick fix." 
•			marking one of the constructor arguments with @Lazy, creates a proxy instead of the actual bean. 
•			real bean is only initialized the first time a method is called.
o			public BeanA(@Lazy BeanB beanB) { 
o				this.beanB = beanB; 
o			}
•		Setter Injection: 
•			Setter Injection allows create the objects first (empty) and then link them together. 
•			generally less preferred than a clean redesign because it makes the bean mutable.
•	Spring Boot 2.6, 
•		circular dependencies are forbidden by default and 
•		will cause your application to fail at startup BeanCurrentlyInCreationException

What is the difference between: @Component, @Service, @Repository, @Controller
--
•	all three (@Service, @Repository, @Controller) are composed annotations that include @Component. 
•	Spring treats them all as managed beans, but they serve different architectural roles.
•	1. @Component
•		generic stereotype for any Spring-managed component.
•		when class doesn't fit into Service, Repository, or Controller (e.g., a utility class or a custom validator).
•	2. @Repository
•		Persistence Layer (Data Access).
•		enables Automatic Persistence Exception Translation.
•			intercepts low-level database exceptions (SQLException . Hibernate exceptions) and 
•			re-throws them as Spring’s DataAccessException. This 
•			makes error handling consistent across different databases.
•	3. @Service
•		Service Layer (Business Logic).
•		provides no extra behavior beyond @Component.
•		Why use it? 
•			purely for semantics. It 
•			tells this class contains the "brain" of your application and 
•			place where @Transactional boundaries usually start.
•	4. @Controller / @RestController
•		Presentation Layer (Web/API).
•		DispatcherServlet scans these classes to find @RequestMapping or @GetMapping annotations to route incoming HTTP requests.
•		@RestController = @Controller + @ResponseBody.
•		@Controller for returning web pages (Views)
•		@RestController: for returning data (JSON/XML).

What happens during Spring context startup?
--
Expected senior knowledge:
Bean creation → dependency injection → post processing → initialization
•	the container follows a precise sequence to transform your classes into a running application. 
•	1. Preparation
•		sets up the environment, 
•		initializes property sources (like application.properties), 
•		prepares the internal structures for the context. 
•	2. Bean Definition Loading (The Blueprint)
•		scans your project or reads @Configuration classes. 
•		doesn't create objects yet.
•		creates BeanDefinitions—metadata "blueprints" that describe the bean's class, scope, and dependencies. 
•	3. BeanFactoryPostProcessing (The Customizer) 
•		Before any beans are instantiated, allows BeanFactoryPostProcessor beans to run. 
•		Example: PropertySourcesPlaceholderConfigurer runs to replace ${db.url} placeholders with values properties file. 
•	4. Bean Instantiation (The Birth)
•		container starts creating the actual bean instances. 
•		Constructor Injection, Spring resolves and injects dependencies immediately.
•		Circular Dependency is found here without @Lazy, the application crashes. 
•	5. Dependency Injection (The Wiring)
•		"populates" the beans by injecting dependencies into fields (@Autowired or setter methods)
•	6. Aware Interface Callbacks 
•		beans implement "Aware" interfaces (like BeanNameAware), passes  framework objects (like the Bean Name or ApplicationContext) to the instance. 
•	7. Bean Post-Processing (The Magic)
•		Before Initialization: 
•			Logic runs before @PostConstruct.
•		Initialization: 
•			@PostConstruct and afterPropertiesSet() methods are called.
•		After Initialization: 
•			where Proxies are created for AOP, @Transactional, or @Async features. 
•	8. Context Ready
•		Once all Singleton beans are instantiated and wired, the ContextRefreshedEvent is published. 
•		application is now fully "up" and ready to handle requests. 
•	Summary Checklist
•		Scan for classes.
•		Register BeanDefinitions.
•		Process Property Placeholders.
•		Instantiate & Inject (DI).
•		Initialize (Call @PostConstruct).
•		Proxy (Wrap beans for Transactions/AOP).

What are AOP, CGLIB and JDK Dynamic Proxies ?
--
•	Spring AOP is the concept/framework for modularity (e.g., transactions, logging). 
•	CGLIB is a mechanism (a bytecode library) to implement that concept.
•	Proxying Mechanism:
•		JDK Dynamic Proxies: 
•			Requires interfaces; 
•			creates a proxy that implements the same interface as the target.
•		CGLIB: 
•			Creates a subclass of the target class at runtime.
•	Usage in Spring: 
•		target bean implements an interface, uses JDK dynamic proxies by default. 
•		no interface is implemented, it defaults to CGLIB.
•	Limitations:
•		CGLIB: 
•			Cannot proxy final classes or final methods because they cannot be subclassed or overridden.
•		JDK: 
•			Limited to interface methods.
•	Performance: 
•		Historically, CGLIB was faster than JDK proxies, but this gap has significantly closed in modern Java versions.

Purpose of @Bean annotation ?
--
•	@Bean annotation is used to explicitly declare a method that produces a bean to be managed by the Spring IoC (Inversion of Control) container.
•	Purpose of @Bean
•		* Manual Bean Creation: It tells Spring that the object returned by the annotated method should be registered as a bean in the application context.
•		* Third-Party Integration: It is primarily used to register classes from external libraries (where you cannot add @Component to the source code).
•		* Custom Initialization: It allows you to write custom logic to instantiate and configure an object before it is managed by Spring.
•		* Decoupling: It separates the bean's declaration from its actual class definition. [3, 4, 5, 6, 7, 8] 
•	
•	Where can it be applied?
•		The @Bean annotation is strictly a method-level annotation. [4, 8] 
•	
•	| Target  | Can @Bean be applied? | Alternative Annotation |
•	| Classes | No | Use @Component, @Service, @Repository, or @Configuration at the class level instead. |
•	| Methods | Yes | This is the standard use; usually inside a @Configuration class. |
•	| Fields | No | Use @Autowired or @Value on fields to inject dependencies or properties into them. |
•	
•	Key Context: 
•		While @Bean is most commonly used within classes annotated with @Configuration, 
•		it can also be used inside a regular @Component class (known as "Lite Mode"), 
•		though this is less common and has different runtime behavior regarding inter-bean dependencies. [4, 15]  
SPRING BOOT INTERNALS (EXTREMELY COMMON)
What is Spring Boot?
Expected answer:
•	Opinionated Spring configuration
•	Auto-configuration
•	Embedded server
•	Starter dependencies

How does Spring Boot Auto-Configuration work?
--
You should mention:
@EnableAutoConfiguration
spring.factories / AutoConfiguration.imports
Conditional annotations
Example:
@ConditionalOnClass
@ConditionalOnMissingBean
@ConditionalOnProperty
They control when a bean is created automatically.
________________________________________
What are Spring Boot Starters?
--
Example:
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
They bundle:
•	dependencies
•	auto-configuration
•	default settings
________________________________________
Bean Management
Common Questions
•	What scopes exist for Spring beans?
Answer:
singleton (default)
prototype
request
session
application
________________________________________
What happens if two beans of the same type exist?
Solutions:
@Qualifier
@Primary

What is @Lazy?
Delays bean creation until needed.


 
CONFIGURATION & PROPERTIES
Common Questions
Difference between:
@Configuration
@Component
@Bean
Example:
@Configuration
class AppConfig {

   @Bean
   MyService myService() {
      return new MyService();
   }
}
________________________________________
What is @ConfigurationProperties?
Used to map external config → POJO
Example:
app:
  timeout: 5000
@ConfigurationProperties(prefix="app")
class AppConfig {
   int timeout;
}
Better than @Value for structured config.

 
REST & Web Layer
Very Common Questions
Difference between:
@Controller
@RestController
Answer:
@RestController = @Controller + @ResponseBody
________________________________________
How does Spring Boot handle HTTP requests?
Pipeline:
Client
↓
DispatcherServlet
↓
HandlerMapping
↓
Controller
↓
Service
↓
Repository
DispatcherServlet is the central entry point.
________________________________________
 
Exception Handling
Common Questions
How do you handle exceptions globally?
Answer:
@ControllerAdvice
@ExceptionHandler
Example:
@RestControllerAdvice
class GlobalExceptionHandler {

   @ExceptionHandler(NotFoundException.class)
   ResponseEntity<?> handle(NotFoundException ex) {
       return ResponseEntity.status(404).body(ex.getMessage());
   }
}
________________________________________
 
Spring Data JPA
Very Common Questions
Difference between:
CrudRepository
JpaRepository
PagingAndSortingRepository
________________________________________
What happens when you define:
findByEmail(String email)
Spring Data generates the query automatically using method naming.
________________________________________
What is the N+1 query problem?
Occurs when lazy loading causes many additional queries.
Solutions:
JOIN FETCH
@EntityGraph
batch fetching
________________________________________
 
Transactions
Very Common
What does: @Transactional do?
It manages:
transaction start
commit
rollback

What are the fields of @Transactional annotation ?
propagation
Transactional fields

Why does @Transactional sometimes not work?
Answer:
Because Spring uses proxy-based AOP.
Fails when:
self-invocation
private methods

What is @Transactional and when does it fail?

Why can @Transactional(readOnly=true) improve performance?
--
It hints the persistence provider:
•	skip dirty checking
•	optimize queries
•	avoid unnecessary locks
But it does not enforce read-only at database level.
•	@Transactional(readOnly = true) 
o	a performance "hint" triggers several optimizations within both Hibernate and Database. 
•	1. Disabling Dirty Checking (The Biggest Gain)
o	By default, Hibernate keeps a "snapshot" of every entity loaded in the persistence context. 
o	At the end of the transaction, compares the current state of the entity with that snapshot 
o	see if any data changed—this is called Dirty Checking.
o	With readOnly = true: skips creating snapshots and bypasses the dirty checking process entirely
•	2. Flush Mode Optimization 
o	standard transaction, performs a "flush" (synchronizing memory with DB) 
o		before every query 
o		at the end of the transaction. 
o	readOnly = true: 
	flush mode is set to MANUAL. 
	no changes expected, never flushes the session. 
o	Result: 
	Fewer round-trips to the database and 
	less internal processing. 
•	3. Database-Level Locks
o	Locks: 
	DB can avoid acquiring certain Write Locks or internal latches, 
	reduces contention and 
	allows other transactions to run concurrently without waiting. 
•	4. Routing to Read Replicas
o	configure Spring to automatically route all @Transactional(readOnly = true) calls to a Read Replica 
o	while keeping the "Write" node free for heavy transactions. [5] 

•	Why @Transactional, if no changes? 
•	1. Consistência de Leitura (Repeatable Read)
o	Sem uma transação, cada consulta SQL dentro do seu método pode ver um "estado" diferente do banco de dados se outros usuários estiverem salvando dados simultaneamente.
o	@Transactional: garante todas as queries dentro daquele método vejam a mesma (snapshot) do banco de dados, 
o	garantindo consistência nos relatórios ou lógicas de decisão.
•	2. Prevenção da LazyInitializationException
o	O Hibernate precisa Session aberta carregar associações LAZY.
o	@Transactional mantém a sessão aberta até fim método. 
o		remover a anotação e tentar acessar uma lista de filhos (parent.getChildren()) fora do repositório, 
o		lançará a famosa exceção porque a "porta" para o banco de dados já fechou.
•	3. Propagação e Reutilização
o	métodos de serviço chamam outros métodos. 
o		Estar em uma transação permite que o Spring gerencie a propagação.
o	Se o Metodo_A é readOnly=false e chama o Metodo_B que é readOnly=true, 
o		o Spring sabe como mesclar sem abrir e fechar múltiplas conexões com BD, economizando recursos.
•	Resumo
o	Mesmo sendo "apenas leitura", a anotação define o limite de onde começa e termina o seu trabalho com o banco de dados, garantindo que você tenha uma conexão disponível e estável durante todo o processo.

Why does @Transactional sometimes not work?
--
Cause: Spring uses proxy-based AOP.
@Transactional fails when:
•	1 Same-Class Method Calls (Self-Invocation Problem)
o	Spring creates proxies around beans to manage transactions. 
o	When you call a transactional method from another method in the same class, the call bypasses the proxy → no transaction.
o	Move the method into another Spring-managed bean and call it via that bean.
o	Or inject self-proxy using AopContext.
•	2️ Private Methods
o	Spring AOP works only with public methods. 
o	If you mark a transactional method as private, the proxy cannot intercept it.
o	Keep transactional methods public.
•	3️ Final or Static Methods
o	Spring uses dynamic proxies (JDK or CGLIB).
o	final methods cannot be overridden → no proxying.
o	static methods belong to the class, not the proxy → no interception.
o	Avoid using final or static with transactional methods.
•	4️ Exceptions Caught Internally
o	By default, @Transactional only rolls back on unchecked (runtime) exceptions. 
o	If you catch exceptions inside the method, Spring never sees them → rollback won’t happen.
o	Re-throw exceptions.
o	Or explicitly declare rollback rules: @Transactional(rollbackFor = Exception.class)
•	5️ Calls Inside Constructors
o	Transactions are not active during bean initialization (constructors). 
o	If you call a transactional method inside a constructor, it won’t work.
o	Don’t use transactional methods in constructors. Use @PostConstruct or call after bean initialization.
•	6️ Wrong Transaction Manager (Multiple Datasources)
o	In multi-database applications, you may have multiple PlatformTransactionManagers. 
o	If the wrong manager is picked, transactions won’t apply correctly.
o	@Transactional("mysqlTransactionManager")
o	public void saveMySqlData() { ... }
o	Always specify the correct transaction manager when using multiple datasources.
•	7️ Non-Spring Managed Beans
o	If you create an object using new, it won’t be a Spring-managed bean → no proxy → no transaction.
o	public void processOrder() {
o	    PaymentService ps = new PaymentService(); // Not Spring bean
o	    ps.savePayment(); // @Transactional won’t work
o	}

 
Spring Boot Testing
--
@SpringBootTest 
	powerful annotation in the Spring Boot Test framework 
	used primarily for integration testing. 
	Unlike standard unit tests that test a single class in isolation, 
	@SpringBootTest bootstraps the full application context, 
		including all beans, configurations, and auto-configurations.

Main Aspects of @SpringBootTest
	1. Full Application Context Loading
		primary purpose is create complete ApplicationContext that mirrors your production environment as closely as possible. 
		automatically searches for your main configuration class (annotated with @SpringBootApplication) to start the entire application.
	2. Web Environment Configuration
		You can control how the web environment is initialized using the webEnvironment attribute:
		* MOCK (Default): 
			Loads a web context but provides a mocked servlet environment. 
			No real HTTP server is started.
		* RANDOM_PORT: 
			Starts a real embedded web server (like Tomcat) on a random available port. 
			This is the preferred mode for "black-box" integration tests.
		* DEFINED_PORT: 
			Starts a real web server on the port defined in your properties (usually 8080).
		* NONE: 
			Loads the application context without any web environment, 
			useful for testing the service or repository layers in isolation. 

3. Support for Test Utilities
	@SpringBootTest automatically makes testing utilities available :
	* TestRestTemplate: 
		fault-tolerant HTTP client for making REST calls to your running server.
	* WebTestClient: 
		fluent API for testing web endpoints, compatible with both standard MVC and reactive WebFlux applications.
	* @MockBean / @SpyBean: 
		Allows easily replace specific Spring beans with Mockito mocks or spies within the context.

4. Customizing the Environment
	provides attributes to further tweak the test environment:
	* properties: 
		Injects specific configuration properties into the Spring Environment for the duration of the test.
	* classes: 
		which configuration classes should be used if you don't want to load the default ones. 

Best Practice Tip
	@SpringBootTest loads entire application, 
		is resource-heavy and slower than 
			unit or 
			"slice" tests (like @WebMvcTest or @DataJpaTest). 
		should be used sparingly for high-level integration scenarios 
			need to verify how different layers of your application work together.
			
Comparing @SpringBootTest with a slice test like @WebMvcTest
	assume you have a UserController that depends on a UserService.
	
1. Slice Test: @WebMvcTest
	test is fast and isolated. 
	only loads the Web layer (controllers, JSON converters, etc.) and 
	ignores your services and repositories. 
	must mock the dependencies yourself.

@WebMvcTest(UserController.class) // Only loads UserController
class UserControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Manually mock the service since it's not loaded
    private UserService userService;

    @Test
    void shouldReturnUser() throws Exception {
 
		// Given: Define mock behavior
        when(userService.getUserById(1L))
			.thenReturn(new User(1L, "John"));

        // When/Then: Execute request via MockMvc
        mockMvc.perform(get("/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("John"));
    }
}

2. Full Integration Test: @SpringBootTest 
	slower but thorough. 
	loads entire application context, including real services 
	and (if configured) a real database

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate; // Real HTTP client

    @Test
    void shouldFetchUserFromRealDatabase() {
        // Act: Make a real HTTP call to the running server
        ResponseEntity<User> response = restTemplate.getForEntity("/users/1", User.class);

        // Assert: Verify the actual data flow across all layers
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("John");
    }
}


@WebMvcTest and @DataJpaTest are "Test Slices". 
	focus on specific layers of your application 
	to make tests faster and more isolated. 
	
1. What is @WebMvcTest?
	used to test the Web Layer (Controllers) in isolation.
	* Focus: 
		only loads beans required for Spring MVC, 
			such as @Controller, @RestController, Filter, and WebMvcConfigurer.
	* Exclusions: 
		does not load @Service, @Component, or @Repository beans.
	* Key Tool: 
		auto-configures MockMvc, 
		allowing simulate HTTP requests without starting a full server.
	* Mocking: 
		doesn't load services, 
		must use @MockBean to provide mock implementations 
			for any dependencies your controller has. 

2. What is @DataJpaTest?
	used to test the Persistence Layer (Repositories).
	* Focus: 
		configures only the components needed for JPA, such as 
			@Entity classes and 
			Spring Data JPA repositories.
	* Database: 
		default, uses an in-memory embedded database (like H2) and 
		configures a DataSource for it.
	* Transactions: 
		Tests are transactional and 
			roll back at the end of each test method by default, 
			keeping your database clean for the next test.
	* Key Tool: 
		provides a TestEntityManager bean, 
		specialized alternative tostandard JPA EntityManager for testing purposes

Best Practice: 
	test slices (@WebMvcTest, @DataJpaTest) for most of your testing to keep your CI/CD pipeline fast, 
	use @SpringBootTest only when you need to verify how all the pieces of your system work together. 
	
@DataJpaTest
	TestEntityManager is a specialized utility provided by SB 
	to interact with the database during tests 
	without relying solely on your repositories. 
	
TestEntityManager instead of the Repository?
	Isolation: 
		If repository.save() method has a bug, 
			test that uses it for setup will fail for the wrong reason.
	Separation of Concerns: 
		clearly distinguishes between setting up data (using entityManager.persist()) 
		and testing logic (using repository.findByName()).
		
Advanced Operations: 
	provides helper methods like flush() and clear() to synchronize the persistence context, 
	ensuring your tests hit the real database rather than just the cache. 

Code Example: Testing a Custom Query
	use TestEntityManager to insert data and 
	then verify if our UserRepository can find it correctly. 

@DataJpaTest // Configures H2 and scans for @Entity and Repositories
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Used for setup

    @Autowired
    private UserRepository userRepository; // The component being tested

    @Test
    void whenFindByEmail_thenReturnUser() {
        // 1. SETUP: Create and persist data using TestEntityManager
        User alex = new User("alex@example.com", "Alex");
        entityManager.persist(alex);
        entityManager.flush(); // Force sync to DB

        // 2. ACT: Call the repository method we want to test
        Optional<User> found = userRepository.findByEmail(alex.getEmail());

        // 3. ASSERT: Verify the results
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(alex.getName());
    }
}

Key Methods in TestEntityManager
	persist(entity): Saves an entity to the database.
	persistAndFlush(entity): Saves and immediately synchronizes with the database.
	find(Class, id): Finds an entity by its primary key.
	flush(): Sends all pending changes to the database.
	clear(): 
		Detaches all entities from the persistence context, 
		forcing the next repository call to fetch fresh data from the DB. 

When use @Mock or @MockBean 
	main difference lies in 
		using the Spring Context or 
		writing a plain Mockito unit test

@Mock (Mockito Library)
	standard Mockito annotation used for isolated unit tests
	* Context: 	
		has no knowledge of the Spring ApplicationContext.
	* Initialization: 
		must initialize it using @ExtendWith(MockitoExtension.class) (JUnit 5) 
		or by calling MockitoAnnotations.openMocks(this).
	* Injection: 
		typically use it with @InjectMocks to manually inject the mock into your class under test.
	* Speed: 
		Very fast because it doesn't start the Spring container.

@MockBean (Spring Boot Test)
	Spring Boot annotation used for integration tests 
		(e.g., with @SpringBootTest or @WebMvcTest).
	* Context: 
		adds or replaces a bean in the Spring ApplicationContext with a Mockito mock.
	* Automatic Injection: 
		Spring automatically injects this mock into any other bean that depends on it (e.g., a Controller that needs a Service).
	* Lifecycle: The mock is managed by Spring and is automatically reset after each test method.
	* Speed: Slower because it triggers a Spring context load or refresh. [3, 9, 10, 11, 12] 

Summary Comparison Table

Origin 
	Mockito Library 
	Spring Boot Test |
Test Type 
	Plain Unit Test (Fast) 
	Integration Test (Slower) 
Spring Context 
	No awareness 
	Integrated into ApplicationContext
Usage 
	Use with @InjectMocks 
	Use with @Autowired or context-aware tests |

Important Note: 
	Spring Boot 3.4+, 
		@MockBean has been deprecated in 
		favor of the new @MockitoBean annotation provided by the core Spring Framework

Difference between @MockBean and @SpyBean 
	how they handle the real logic of the object they are replacing in the Spring ApplicationContext.
	
1. Key Differences
	Object Nature 
		Creates a complete mock (empty shell). 
		Wraps a real instance (partial mock). 
	Default Behavior 
		Returns default values (null, 0, false) unless stubbed. 
		Calls the real methods of the class unless stubbed.
	Requirement 
		Can create a new bean if one doesn't exist in the context. 
		Requires an existing bean to wrap (especially in Spring Boot 3.4+).
	Typical Use 
		To isolate the unit by avoiding real external calls. 
		To test real behavior while only stubbing specific methods. |

2. Mockito's Equivalent to @SpyBean
	Yes, Mockito has a direct equivalent called @Spy. 

* @Spy (Mockito): 
	plain unit tests (no Spring context). 
	creates a spy of a local object. 
	must initialize it using 
		MockitoAnnotations.openMocks(this) or 
		@ExtendWith(MockitoExtension.class).
* @SpyBean (Spring): 
	integration tests where you want 
	to "spy" on a bean that is managed by the Spring container. 
	ensures that any other bean using that service will interact with the spied version. 

3. Important: New @MockitoSpyBean 
	As of Spring Boot 3.4, @SpyBean is deprecated. 
	should now use [@MockitoSpyBean](

Pro-Tip for Spies: 
	stubbing a spy, always use the 
		doReturn(...).when(spy).method() syntax instead of 
		when(spy.method()).thenReturn(...). 
	latter will actually call the real method once before the stub is applied, 
	which can cause unwanted side effects or errors. 

Here is a comparison using a PaymentService that depends on an ExternalGateway.
The Scenario
We have a PaymentService that calculates a fee and then calls an ExternalGateway.

@Servicepublic class PaymentService {
    private final ExternalGateway gateway;

    public PaymentService(ExternalGateway gateway) { this.gateway = gateway; }

    public String process(double amount) {
        double fee = calculateFee(amount); // Internal method
        return gateway.send(amount + fee); // External call
    }

    public double calculateFee(double amount) {
        return amount * 0.1; 
    }
}

1. Using @MockBean (Total Isolation)
	Use this when you want to ignore the real logic of the dependency entirely. 
	gateway becomes a "hollow shell" that returns null unless told otherwise.

@SpringBootTestclass PaymentMockTest {

    @MockitoBean // Replaces the real gateway in the Spring Context
    private ExternalGateway gateway;

    @Autowired
    private PaymentService paymentService;

    @Test
    void testWithMock() {
        // Stubbing: We define exactly what the mock should return
        when(gateway.send(110.0)).thenReturn("SUCCESS");

        String result = paymentService.process(100.0);

        assertThat(result).isEqualTo("SUCCESS");
        verify(gateway).send(110.0); // Verify the interaction occurred
    }
}

2. Using @SpyBean (Partial Mocking)
	Use this when you want to use the real bean but override or "watch" specific methods. 
	Here, we spy on the PaymentService itself to override the internal calculateFee logic.

@SpringBootTestclass PaymentSpyTest {

    @MockitoSpyBean // Wraps the REAL PaymentService bean
    private PaymentService paymentService;

    @MockitoBean
    private ExternalGateway gateway;

    @Test
    void testWithSpy() {
        // IMPORTANT: Use doReturn/when for Spies to avoid calling the real method during stubbing
        doReturn(5.0).when(paymentService).calculateFee(100.0);
        
        when(gateway.send(105.0)).thenReturn("SUCCESS");

        String result = paymentService.process(100.0);

        assertThat(result).isEqualTo("SUCCESS");
        // We verify the spy used our overridden fee (5.0) instead of the real one (10.0)
        verify(gateway).send(105.0); 
    }
}

Difference between: @SpringBootTest @WebMvcTest @DataJpaTest ?
They load different slices of the context.
Example:
@WebMvcTest → controllers only
@DataJpaTest → repositories only

________________________________________
Why can large @SpringBootTest tests be slow?
Because it loads the entire application context.
Better alternatives:
@WebMvcTest
@DataJpaTest
@MockBean
These load only a slice of the application.

What is the difference between @Mock and @MockBean?
@Mock
Mockito mock.
Used in unit tests only.
@Mock
UserRepository repo;

@MockBean
Spring Boot annotation that replaces a bean in the ApplicationContext.
Used in Spring Boot tests.
@MockBean
UserRepository repo;
________________________________________
What is the difference between @SpringBootTest and @WebMvcTest?
@SpringBootTest
Loads the full application context.
Used for integration tests.
@SpringBootTest
class UserServiceTest {}
________________________________________
@WebMvcTest
Loads only the web layer.
Used for controller testing.
@WebMvcTest(UserController.class)
class UserControllerTest {}
________________________________________
6. What is MockMvc?
MockMvc allows testing Spring MVC controllers without starting a server.
Example:
mockMvc.perform(get("/users"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.size()").value(3));
Benefits:
Fast
No Tomcat startup
Focus on controller logic

 
Spring Security 
Basic questions:
•	What is Spring Security?
•	How does authentication work?
•	Difference between:
authentication
authorization
________________________________________
1. How does Spring Security integrate with Spring Boot?
Spring Boot provides auto-configuration for Spring Security when the dependency is present.
Key elements:
•	Filter chain intercepts HTTP requests.
•	AuthenticationManager authenticates credentials.
•	SecurityContext holds authentication information.
Typical configuration:
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .formLogin();

    return http.build();
}
Spring Boot simplifies configuration via auto-configured beans and properties.

Spring Boot Production Topics 
These often separate mid-level from senior.
Actuator
What is Spring Boot Actuator?
Provides endpoints like:
/health
/metrics
/env
/info
Used for monitoring.
________________________________________
Configuration Profiles
spring.profiles.active=prod
Allows environment-specific configs.
________________________________________
External Configuration Priority
Order example:
1 command line
2 environment variables
3 application.properties
4 default values
________________________________________
 
Performance & Scalability
Typical questions:
•	How do you optimize a Spring Boot app?
•	How do you reduce startup time?
•	How do you tune connection pools?
Expected mentions:
HikariCP
lazy initialization
profile-specific configs
caching
________________________________________
 

 
🎯 The 10 Questions Most Likely Asked
If preparing for a senior backend interview, these appear very often:
1.	How does Spring Boot auto-configuration work?
2.	What is DispatcherServlet?
3.	What is the bean lifecycle?
4.	Difference between @Component, @Service, @Repository
5.	How does Spring Data JPA generate queries?
6.	What is the N+1 query problem?
7.	How do you handle global exceptions?
8.	Difference between @SpringBootTest and @WebMvcTest
9.	How do profiles work?
 
15 Trap Questions
________________________________________
________________________________________
Why can parallelStream() be dangerous in backend services?
•	it implicitly utilizes the shared JVM-wide ForkJoinPool.commonPool()
•	This lack of isolation, combined with potential thread safety issues and performance overhead, can lead to system-wide instability. 
•	1. Resource Contention (The "Common Pool" Danger)
o	Shared Pool Exhaustion: 
o		parallelStream() does not have its own dedicated thread pool. 
o		uses the ForkJoinPool.commonPool(), which is shared across the entire JVM. 
o		it can consume all available threads in the common pool.
o	Performance Degradation: 
o		common pool is exhausted, other critical parts application that also use will be forced to wait 
•	2. Inappropriate for I/O-Bound Operations
o	Blocking Threads: 
o		designed for CPU-bound tasks, not I/O-bound tasks. 
o		database calls, network requests, or file I/O, the threads will be waiting, rather than computing.
o	Slow-Down: 
o		number of threads in pool is limited, blocking those threads on I/O can starve CPU-intensive tasks 
•	3. Thread Safety and Data Loss
o	Shared Mutable State: 
o		attempts to modify shared data (e.g., adding ArrayList or HashMap) a forEach, can cause race conditions. 
o		results in lost data, incorrect counts, or ConcurrentModificationException.
o	Unsafe Operations: 
o		collect is generally safe, forEach to populate non-thread-safe collections is a common source of bugs. 
•	4. Hidden Performance Overhead
o	Splitting and Merging Costs: 
o		JVM must split the data (Spliterator), manage threads, and merge results. 
o		the overhead of managing these threads can make parallelStream() slower than a standard sequential stream().
o	Inefficient Data Structures: 
o		on structures that do not split evenly (like LinkedList) can result in poor performance. 
•	5. Lack of Control and Debuggability 
o	Debugging Difficulty: 
o		Exception handling and debugging are significantly harder in parallel streams the stack trace is broken across different threads.
o	No Easy Cancellation: 
o		no built-in, easy way to stop a parallelStream() once it has started. 
•	Summary Recommendation
o	Never use parallelStream() for I/O-bound tasks.
o	Only use it for large datasets with CPU-intensive operations (heavy calculations).
o	Avoid shared mutable state within parallel operations.
o	Prefer explicit ExecutorService (dedicated thread pool) for better isolation and control over your background tasks
	ExecutorService
	CompletableFuture
	custom thread pools
________________________________________
Why is field injection (@Autowired) discouraged?
•	Difficult Unit Testing: 
o	dependencies are not passed through a constructor, cannot easily instantiate the class in a unit test with mocks; 
o	you must use reflection or Spring testing utilities.
•	Lack of Immutability: 
o	Fields must be mutable (non-final) for the container to inject them, 
o	meaning the object's dependencies can be changed after instantiation.
•	Hidden Dependencies: 
o	not obvious what dependencies a class requires just by looking at its constructor, making code less explicit.
o	Easy to Overuse: 
o	encourages adding too many dependencies, as there is no visual clutter (like a long constructor) 
•	Tight Coupling: 
o	Classes are tightly coupled to the Spring DI container and cannot easily be used in a different context.
o	Risk of NullPointerException: 
o	class is instantiated without the container (e.g., in a test), the dependencies will be null. 
•	Recommended Alternative: 
o	Constructor-based injection is preferred because it makes dependencies mandatory, 
o	allows for final fields, and ensures the class is fully initialized upon creation.
o	@Autowired
o	UserService service;
________________________________________
Why can OpenSessionInView be dangerous?
•	OSIV stands for Open Session In View, 
o		a design pattern used in Hibernate to manage database transactions and sessions. 
o		keeping the session open, help simplify the management of Hibernate sessions and transactions, 
o		make it easier to work with lazy-loaded data.
o		designed to prevent LazyInitializationException 
o		keeping the Hibernate Session (Persistence Context) open during rendering, 
•	considered dangerous because it leads to 
o		severe performance bottlenecks, 
o		architectural issues, and 
o	potential data security risks. 
•	1. Severe Performance Degradation (The N+1 Problem) 
o	Lazy Loading in the View: 
o		session remains open, any access to an uninitialized lazy collection trigger a new DBquery.
o	The N+1 Query Problem: 
o		list of 100 items, each item triggers a query to load a lazy collection, end up with 101 queries 
o	Slow Page Load/Response: 
o		db is hammered with many small queries, resulting in high latency. 
•	2. Database Connection Pool Exhaustion
o	Long-Running Connections: 
o		db connection held open entire HTTP request lifecycle, including render the view or serialize JSON.
o	Blocking Other Requests: 
o		high-traffic scenario, this can quickly exhaust the connection pool, leaving no connections 
•	3. Architectural Violations
o	Separation of Concerns: 
o		blurs the line between the Service Layer (business logic/transactions) and the Presentation Layer (UI/view).
o	Unexpected Data Modification: 
o		session is still open, accidental changes to entities can be automatically flushed (saved) to the database 
•	4. Hidden Defects
o	Hiding Errors: 
o		LazyInitializationException occurs when trying to access data outside a transaction, 
o		alerting you to a flaw in your data loading strategy.
o	Production Failures: 
o		OSIV masks this error, allowing code to function development 
o		causing severe performance issues only in production. 
•	5. Potential Data Security Issues
o	Accidental Exposure: 
o		Data binding or JSON serialization frameworks might traverse associations not intend to expose, 
o		potentially serializing sensitive data that. 
•	Best Practice
o	recommended to disable it 
o		(spring.jpa.open-in-view=false) 
o	JPQL/HQL JOIN FETCH: 
o		load required associations in the service layer.
o		@Query("SELECT o FROM Order o JOIN FETCH o.items")
o	Entity Graphs: 
o		To selectively load data.
o	DTO (Data Transfer Object) Projections: 
o		map only needed data
o	
________________________________________
What is the N+1 query problem?
•	The N+1 Query Problem is 
o	a performance issue that occurs when a database query is executed repeatedly in a loop, 
o	resulting in a large number of inefficient database queries. 
List<Order> orders = repo.findAll();
orders.get(0).getItems();
o	Hibernate loads:
	1 query for orders
	N queries for items
•	Solution:
o	JOIN FETCH: 
	Força um join explícito na query.
o	@EntityGraph: 
	Define um plano de carregamento dinâmico que usa joins.
o	@BatchSize: 
	Não reduz a 1 query, mas agrupa as queries em lotes (ex: transforma 101 queries em apenas 2 ou 3).



________________________________________
Why should equals() and hashCode() both be overridden?
•	collections rely on hashCode first, equals second.
Person p1 = new Person("John", 30);
Person p2 = new Person("John", 30);
System.out.println(p1.equals(p2)); // true
•	But if hashCode() is not overridden:
Set<Person> set = new HashSet<>();
set.add(p1);
System.out.println(set.contains(p2)); // false ❌
•	Contract:
•		a.equals(b) → a.hashCode() == b.hashCode()
•		O inverso não é obrigatório collision 
•		(objetos diferentes podem ter o mesmo hash, o que chamamos de colisão)


________________________________________
Why is Optional not recommended for fields or JPA entities?
•	1. Problemas de Serialização
o	A classe Optional não implementa Serializable. 
o	Se sua entidade JPA precisar ser serializada (para sessões HTTP, cache distribuído como Redis, ou via RMI), o processo falhará com uma NotSerializableException.
•	2. Especificação do JPA (Hibernate)
o	O JPA foi projetado o modelo de JavaBeans, que espera getters e setters para campos diretos. 
o	O Hibernate usa reflexão e proxies para manipular os dados. 
o	o motor do JPA, não sabe mapear Optional para uma coluna do banco de dados (SQL não tem um tipo "Optional").
o	exigiria a criação de um AttributeConverter personalizado para cada campo, adicionando complexidade desnecessária. 
•	3. Custo de Memória (Overhead) 
o	Optional é um objeto. 
o	Isso aumenta a pressão no Garbage Collector e o consumo de memória RAM, sem trazer benefícios reais de armazenamento. 
@Entity
public class Usuario {
    
    @Column(nullable = true)
    private String telefone; // O Hibernate lida bem com null

    // O Getter expõe a intenção de que o valor pode ser nulo
    public Optional<String> getTelefone() {

________________________________________
Why is List preferred over Set in JPA relationships?
•	List is often the "safe" default in JPA because it avoids performance traps associated with Set.
•	1. The equals() and hashCode() Nightmare
o	Set relies heavily on hashCode() to ensure uniqueness.
o	The Trap: 
	database-generated @Id in your hashCode(), the hash changes after the entity is persisted 
	entity in a HashSet, it becomes "lost" or unreachable.
•		The List Advantage: 
	doesn't care about hashCode() for basic operations, making more resilient lifecycle of JPA entities.
•	2. Performance: The "Hidden" Select
o	add an element to a Set, Hibernate must ensure it is unique.
o	The Trap: 
	guarantee uniqueness, Hibernate often has to initialize (load) the entire collection from the database just to check if the new item is already there
o	The List Advantage: 
	 can simply execute an INSERT without loading the existing elements.
•	3. The "MultipleBagFetchException"
o	List is easier, it has a famous limitation:
	cannot JOIN FETCH two or more List collections in a single JPQL query 
	triggers MultipleBagFetchException because it would create a massive Cartesian product).
o	sometimes switch to Set just to bypass this error, 
	usually a bad idea because it hides a performance issue 
•	to avoid the MultipleBagFetchException while keeping the benefits of List, 
o	best approach is to fetch the first collection and then use Batch Fetching Problems:
•	Hibernate must call equals/hashCode
•	performance overhead
•	ordering issues
________________________________________
________________________________________
🔟 Why is @ComponentScan sometimes problematic?
Large scans can:
•	slow application startup
•	accidentally include unwanted beans
Better:
@ComponentScan(basePackages="com.myapp")
________________________________________
1️⃣1️⃣ Why should Spring beans generally be stateless?
Default scope is:
singleton
Multiple threads share the same instance.
Stateful beans can cause:
•	race conditions
•	inconsistent data
________________________________________
1️⃣2️⃣ Why can @Async silently fail?
Because it also uses Spring proxies.
Fails if:
•	method called internally
•	method not public
Example:
this.sendEmail(); // async ignored
________________________________________
1️⃣3️⃣ Why should you avoid new for Spring beans?
Example:
UserService service = new UserService();
Problem:
•	Spring cannot manage lifecycle
•	no dependency injection
•	no AOP features (transactions, security)
Always let Spring create beans.
________________________________________
________________________________________
1️⃣5️⃣ Why can ThreadLocal cause memory leaks in servers?
Thread pools reuse threads.
If ThreadLocal is not cleared:
data stays attached to the thread
Example: servlet containers (Tomcat).
Solution:
try {
   ...
} finally {
   threadLocal.remove();
}
________________________________________
⭐ 5 Bonus “Senior-Level” Questions
These appear in top-tier backend interviews.
1️⃣ What is the difference between synchronized and ReentrantLock?
ReentrantLock offers:
tryLock()
timeout
fair locks
interruptible waits
________________________________________
2️⃣ What causes OutOfMemoryError in backend systems?
Typical causes:
memory leaks
unbounded caches
large collections
thread leaks
________________________________________
3️⃣ Why avoid System.currentTimeMillis() for timing?
Use:
System.nanoTime()
Because currentTimeMillis can change due to clock adjustments.
________________________________________
4️⃣ What is backpressure in distributed systems?
It means limiting producers when consumers cannot keep up.
Used in:
Kafka
Reactive Streams
message queues
________________________________________
5️⃣ What is the biggest mistake in microservices?
Most common answer:
distributed monolith
Too many services tightly coupled.
 
Below is a Spring Boot Mastery Interview Cheat Sheet (Part 3) covering Security, Testing, and Observability. The answers are intentionally concise but technically precise, which is ideal for senior-level interviews.
________________________________________
Spring Boot Mastery – Interview Cheat Sheet (Part 3)
________________________________________
2. What is the difference between Authentication and Authorization?
Authentication
•	Verifies who the user is
•	Example: username/password validation
Authorization
•	Determines what the user is allowed to do
•	Example: role-based access
Example:
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {}
________________________________________
3. How do you implement JWT authentication in Spring Boot?
Typical flow:
1.	User authenticates with credentials
2.	Server generates JWT token
3.	Token sent in Authorization header
4.	A filter validates the token
Example header:
Authorization: Bearer <token>
Common components:
•	JwtTokenProvider
•	OncePerRequestFilter
•	SecurityFilterChain
________________________________________
________________________________________
7. What are Spring Boot Actuator endpoints?
Spring Boot Actuator exposes production monitoring endpoints.
Examples:
/actuator/health
/actuator/info
/actuator/metrics
/actuator/env
Configuration:
management.endpoints.web.exposure.include=*
Common production endpoints:
•	health
•	metrics
•	prometheus
•	loggers
________________________________________
8. What is Micrometer?
Micrometer is a metrics instrumentation library used by Spring Boot.
It provides abstraction for monitoring systems:
•	Prometheus
•	Datadog
•	New Relic
•	CloudWatch
Example metric:
Counter counter = meterRegistry.counter("orders.created");
counter.increment();
________________________________________
9. How do you implement centralized logging in Spring Boot?
Typical stack:
•	Logback
•	ELK stack
o	Elasticsearch
o	Logstash
o	Kibana
Common approaches:
1.	Structured logging (JSON)
2.	Correlation IDs
3.	Log aggregation
Example logback config:
<encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
________________________________________
10. What is distributed tracing?
Distributed tracing tracks requests across microservices.
Typical tools:
•	Zipkin
•	Jaeger
•	OpenTelemetry
Spring Boot uses:
•	Micrometer Tracing
•	OpenTelemetry
Trace example:
API Gateway -> Order Service -> Payment Service
All requests share the same traceId.
________________________________________
11. What is the difference between liveness and readiness probes?
Used in Kubernetes deployments.
Liveness probe
•	Checks if the application is alive
•	Restart container if failing
Readiness probe
•	Checks if the application can receive traffic
Spring Boot exposes:
/actuator/health/liveness
/actuator/health/readiness
________________________________________
12. How do you externalize configuration in Spring Boot?
Common mechanisms:
1.	application.properties
2.	application.yml
3.	Environment variables
4.	Command line args
5.	Spring Cloud Config
Example:
server.port=8081
spring.datasource.url=jdbc:mysql://localhost/db
Using environment variable:
SPRING_DATASOURCE_URL
________________________________________
13. What is the difference between application.properties and application.yml?
Both configure Spring Boot.
properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost/db
yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost/db
YAML is more readable for hierarchical configuration.
________________________________________
14. How does Spring Boot support profiles?
Profiles allow environment-specific configuration.
Example:
application-dev.yml
application-prod.yml
Activate profile:
spring.profiles.active=dev
Or via environment variable:
SPRING_PROFILES_ACTIVE=prod
________________________________________
15. What are the most important Spring Boot best practices?
1. Use constructor injection
@Service
public class OrderService {
    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }
}
________________________________________
2. Avoid field injection
Bad practice:
@Autowired
OrderRepository repo;
________________________________________
3. Use layered architecture
Controller
Service
Repository
________________________________________
4. Use DTOs instead of exposing entities
Avoid:
Controller -> Entity
Prefer:
Controller -> DTO -> Service -> Entity
________________________________________
5. Proper exception handling
Use:
@ControllerAdvice
 
𝗧𝗢𝗣 𝟱𝟬 𝗦𝗣𝗥𝗜𝗡𝗚 𝗕𝗢𝗢𝗧 𝗜𝗡𝗧𝗘𝗥𝗩𝗜𝗘𝗪 𝗤&𝗔 
𝗦𝗽𝗿𝗶𝗻𝗴 𝗕𝗼𝗼𝘁 𝗙𝘂𝗻𝗱𝗮𝗺𝗲𝗻𝘁𝗮𝗹𝘀
1. 𝗪𝗵𝘆 𝘄𝗮𝘀 𝗦𝗽𝗿𝗶𝗻𝗴 𝗕𝗼𝗼𝘁 𝗶𝗻𝘁𝗿𝗼𝗱𝘂𝗰𝗲𝗱?
→ Removes boilerplate configuration and enables faster development with auto-configuration and embedded servers.
2. 𝗦𝗽𝗿𝗶𝗻𝗴 𝘃𝘀 𝗦𝗽𝗿𝗶𝗻𝗴 𝗕𝗼𝗼𝘁?
→ Spring needs manual configuration; Spring Boot provides auto-configuration and production-ready defaults.
3. 𝗪𝗵𝗮𝘁 𝗶𝘀 @𝗦𝗽𝗿𝗶𝗻𝗴𝗕𝗼𝗼𝘁𝗔𝗽𝗽𝗹𝗶𝗰𝗮𝘁𝗶𝗼𝗻?
→ Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.
4. 𝗛𝗼𝘄 𝗱𝗼𝗲𝘀 𝗮𝘂𝘁𝗼-𝗰𝗼𝗻𝗳𝗶𝗴𝘂𝗿𝗮𝘁𝗶𝗼𝗻 𝘄𝗼𝗿𝗸?
→ Uses classpath scanning and spring.factories to configure beans automatically.
5. 𝗛𝗼𝘄 𝘁𝗼 𝗱𝗶𝘀𝗮𝗯𝗹𝗲 𝗮𝘂𝘁𝗼-𝗰𝗼𝗻𝗳𝗶𝗴𝘂𝗿𝗮𝘁𝗶𝗼𝗻?
→ Use exclude in @SpringBootApplication or @EnableAutoConfiguration.
6. 𝗪𝗵𝗮𝘁 𝗮𝗿𝗲 𝘀𝘁𝗮𝗿𝘁𝗲𝗿 𝗱𝗲𝗽𝗲𝗻𝗱𝗲𝗻𝗰𝗶𝗲𝘀?
→ Predefined dependency sets for common features (web, JPA, security).
7. 𝗛𝗼𝘄 𝗶𝘀 𝘁𝗵𝗲 𝗲𝗺𝗯𝗲𝗱𝗱𝗲𝗱 𝘀𝗲𝗿𝘃𝗲𝗿 𝘀𝗲𝗹𝗲𝗰𝘁𝗲𝗱?
→ Based on classpath dependency (Tomcat default, Jetty/Undertow if added).
8. 𝗖𝗮𝗻 𝘄𝗲 𝗿𝘂𝗻 𝘄𝗶𝘁𝗵𝗼𝘂𝘁 𝗲𝗺𝗯𝗲𝗱𝗱𝗲𝗱 𝘀𝗲𝗿𝘃𝗲𝗿?
→ Yes, by packaging as WAR and deploying to an external server.
9. 𝗪𝗵𝗮𝘁 𝗵𝗮𝗽𝗽𝗲𝗻𝘀 𝗱𝘂𝗿𝗶𝗻𝗴 𝗦𝗽𝗿𝗶𝗻𝗴 𝗕𝗼𝗼𝘁 𝘀𝘁𝗮𝗿𝘁𝘂𝗽?
→ Creates ApplicationContext, loads beans, applies auto-configurations, starts embedded server.


𝗦𝗽𝗿𝗶𝗻𝗴 𝗖𝗼𝗿𝗲 & 𝗗𝗜
10. 𝗪𝗵𝗮𝘁 𝗮𝗿𝗲 𝗜𝗼𝗖 𝗮𝗻𝗱 𝗗𝗜?
→ IoC delegates object creation to Spring; 
	DI injects dependencies into beans.
11. 𝗪𝗵𝘆 𝗶𝘀 𝗰𝗼𝗻𝘀𝘁𝗿𝘂𝗰𝘁𝗼𝗿 𝗶𝗻𝗷𝗲𝗰𝘁𝗶𝗼𝗻 𝗽𝗿𝗲𝗳𝗲𝗿𝗿𝗲𝗱?
→ Ensures immutability and easier testing.
12. 𝗪𝗵𝗮𝘁 𝗮𝗿𝗲 𝗯𝗲𝗮𝗻 𝘀𝗰𝗼𝗽𝗲𝘀?
→ Singleton, Prototype, Request, Session, Application.
13. 𝗘𝘅𝗽𝗹𝗮𝗶𝗻 𝘁𝗵𝗲 𝗯𝗲𝗮𝗻 𝗹𝗶𝗳𝗲𝗰𝘆𝗰𝗹𝗲.
→ Instantiation → Dependency Injection → Init → Ready → Destroy.
14. 𝗔𝗽𝗽𝗹𝗶𝗰𝗮𝘁𝗶𝗼𝗻𝗖𝗼𝗻𝘁𝗲𝘅𝘁 𝘃𝘀 𝗕𝗲𝗮𝗻𝗙𝗮𝗰𝘁𝗼𝗿𝘆?
→ ApplicationContext is advanced and feature-rich; BeanFactory is basic.
15. 𝗪𝗵𝗮𝘁 𝗶𝘀 @𝗟𝗮𝘇𝘆?
→ Delays bean creation until first use.
16. 𝗪𝗵𝗮𝘁 𝗮𝗿𝗲 𝘀𝘁𝗲𝗿𝗲𝗼𝘁𝘆𝗽𝗲 𝗮𝗻𝗻𝗼𝘁𝗮𝘁𝗶𝗼𝗻𝘀?
→ @Component, @Service, @Repository, @Controller.
17. 𝗛𝗼𝘄 𝗮𝗿𝗲 𝗰𝗶𝗿𝗰𝘂𝗹𝗮𝗿 𝗱𝗲𝗽𝗲𝗻𝗱𝗲𝗻𝗰𝗶𝗲𝘀 𝗵𝗮𝗻𝗱𝗹𝗲𝗱?
→ Allowed with setter injection; constructor injection fails. 
Java Web Servers
What is a Java Web Server?
•	🛠️ Key Components and Features
•		Servlet Container: 
•			Manages the lifecycle of servlets, 
•			mapping URLs to specific servlet classes, and 
•			handling requests and responses efficiently.
•		JSP Engine: 
•			Processes JSP files, converting them into servlets for dynamic content generation.
•		Security Modules: 
•			authentication and authorization protocols to protect web applications from unauthorized access.
•		Logging and Monitoring Tools: 
•			Provides insights into server performance, aiding in troubleshooting and optimization.
•	Popular Java Web Servers
•		Apache Tomcat: 
•			open-source implementation of the Java Servlet and JSP specifications, 
•			widely used for its robustness and scalability.
•		Jetty: 
•			lightweight footprint and embeddable nature, 
•			making it ideal for machine-to-machine communications and microservices architectures .
•		Resin: 
•			high performance and 
•			advanced features like load balancing and clustering, 
•			suitable for enterprise-level applications .
•	choice depends on how much "heavy lifting" you want the server to do out of the box.
•		1. Web Container (The Lightweight Choice)
•			In Java, a "Web Server" is usually a Servlet Container. 
•			implements only the web-related parts of the Jakarta EE (Java EE) spec, 
•				like Servlets, 
•				JSP, and 
•				WebSocket.
•			Examples: Apache Tomcat, Jetty.
•			Best for: 
•				Spring Boot apps, 
•				Microservices, and 
•				REST APIs.
•			Pros: 
•				Fast startup, 
•				low memory footprint, and 
•				easy to containerize (Docker).
•		2. Application Server (The Full-Stack Choice)
•			"Full Profile" server. 
•			includes the Web Container plus 
•			entire Jakarta EE suite: 
•				EJB (Enterprise JavaBeans), 
•				JMS (Messaging), 
•				JTA (Distributed Transactions), and 
•				JPA.
•		Examples: 
•			WildFly (JBoss), 
•			GlassFish, 
•			Open Liberty.
•		Best for: 
•			Massive enterprise systems that 
•			need built-in transaction management and legacy integration.
•		Pros: 
•			"Batteries included"—
•			don't need to add many external libraries because the server provides the APIs.

Servlets vs JSP vs JSF
•	represent different levels of abstraction. 
•		as the Engine (Servlet), 
•		the Template (JSP), and 
•		the Framework (JSF).
•	1. Servlet (The Low-Level Logic)
•		Servlets are Java classes that handle HTTP requests and responses.
•		How it works: You write Java code to generate HTML using out.println().
•		Pros: 
•			Very fast and powerful; 
•			gives you total control.
•		Cons: 
•			Hard to maintain. 
•			Writing HTML inside Java strings is a "coding nightmare."
•		Best for: 
•			Processing data, 
•			acting as a Controller, or 
•			building lightweight APIs.
•	2. JSP - JavaServer Pages (The Scripting Page)
•		JSP is essentially a Servlet in reverse. 
•		It looks like HTML but allows you to drop Java code inside it.
•		How it works: 
•			You write HTML and use tags like <% %> (scriptlets) or JSTL to add logic.
•		Pros: 
•			Better for front-end developers because it looks like a webpage.
•		Cons: 
•			Mixing business logic with UI (HTML) leads to "spaghetti code."
•		Status: 
•			Mostly deprecated in modern development 
•			replaced by Thymeleaf or client-side frameworks like React
•	3. JSF - Jakarta Server Faces (The Component Framework)
•		JSF is a heavy, component-based MVC framework 
•		built on top of Servlets and JSP.
•		How it works: 
•			use pre-built UI components (like <h:dataTable>) that sync automatically with Java objects (Managed Beans).
•		Pros: 
•			Great for complex enterprise "Internal Tools" and forms; 
•			handles state management and validation for you.
•		Cons: 
•			High learning curve; 
•			generates heavy HTML/JavaScript; 
•			hard to customize the look and feel.
•	The Modern Reality
•		In 2024/2025, most Java developers don't use JSP or JSF for new projects. 
•		Backend: 
•			Use Servlets (wrapped inside Spring Boot or Jakarta EE REST APIs).
•		Frontend: 
•			Use React/Angular/Vue (communicating via JSON) or 
•			Thymeleaf (if server-side rendering is needed).

 
Jakarta EE (formerly Java EE) vs Spring Boot
•	Jakarta EE 
•		set of official standards (specifications), 
•	Spring Boot 
•		framework designed for rapid implementation.
•	The Core Difference
•		Jakarta EE (The Specification): 
•			a rulebook. 
•			defines how an API should behave (e.g., JAX-RS for REST). 
•			Different vendors (Red Hat, IBM, Oracle) provide the actual "engine" (Application Server) to run your code. 
•			prioritizes stability and portability—
•			can theoretically move your code from one server to another without changing it.
•		Spring Boot (The Implementation): 
•			power tool. 
•			"opinionated," meaning it makes decisions for you to get you running faster. 
•			embeds the server (Tomcat) directly into your app. 
•			prioritizes developer experience and 
•			speed—you write less boilerplate code and get more features out of the box.
•	Key Technical Comparison
•		Jakarta EE	
•		Spring Boot
•	Philosophy	
•		"Standards first" (Industry-driven)	
•		"Developer first" (Opinionated)
•	Packaging	
•		Usually a .WAR file (needs a server)	
•		Usually an executable .JAR (self-contained)
•	REST Standard	
•		Uses JAX-RS (e.g., @Path, @GET)	
•		Uses Spring MVC (e.g., @RestController, @GetMapping)
•	Dependency Injection	
•		CDI (Contexts and Dependency Injection)	
•		Spring DI (the original "Inversion of Control")
•	Innovation Speed	
•		Slower (requires committee consensus)	
•		Faster (driven by the Spring/VMware team)
•	Which should you choose?
•		Spring Boot 
•			building microservices, 
•			integrate with modern Cloud/K8s tools quickly, or 
•			want the largest ecosystem of third-party plugins (Security, Data, Cloud). 
•			current industry standard for new projects.
•		Choose Jakarta EE 
•			work in a highly regulated environment (Banking, Gov) that 
•			demands strict adherence to open standards, or 
•			deploying to an existing enterprise application server infrastructure.
 
Web Server vs Application Server

•	What is a Web Server?
•		Definition :
•			software or hardware system that serves static content using the HTTP or HTTPS protocol. 
•			user enters a URL , responsible finding HTML page, image, CSS, or JavaScript file — and delivering it to the browser.
•		Characteristics:
•			Serves static content like HTML, images, CSS, JS.
•			Handles HTTP requests and responses.
•			not process backend logic or business rules.
•			Can forward dynamic requests to an application server or backend service.
•		Functions 
•			Handling HTTP Requests: 
•				process incoming HTTP requests and return the requested static resources 
•			Serving Static Content: 
•				store and deliver static files, such as web pages, images, and multimedia.
•			Load Balancing: 
•				can distribute traffic across multiple servers to enhance performance and reliability.
•			Security Features: 
•				features like SSL/TLS encryption, authentication, and access control.
•			Logging and Monitoring: 
•				keep logs of traffic and access patterns to analyze and optimize performance.
•		Examples
•			Apache HTTP Server – 
•				widely used open-source web servers known for its flexibility and extensive module support.
•			Nginx 
•				high-performance web server known for its efficiency in handling concurrent connections and load balancing.
•			Microsoft IIS (Internet Information Services) – 
•				by Microsoft, used for hosting applications on Windows-based systems. 
•				if you prefer Windows this is your go to.
•			LiteSpeed 
•				commercial web server known for its speed and scalability.
•			Resin – 
•				Java-based web server often used for Java applications.
•	
•	What is an Application Server?
•		Definition :
•			more advanced server that runs application code and delivers dynamic content. 
•			designed to process logic, access databases, manage sessions, and execute programs written in languages like Java, Python, PHP, or .NET.
•			software framework that provides a runtime environment for executing application logic. 
•			Unlike a web server, which primarily delivers static content, 
•			application server generates dynamic content by processing business logic and interacting with databases, APIs, and other backend services.	
•		Characteristics:
•			Executes backend business logic.
•			Generates dynamic responses based on user input, database queries, or application workflows.
•			Supports multiple communication protocols (HTTP, RMI, JMS, etc.).
•			Often works in combination with a web server.
•		Functions of an Application Server
•			Executing Business Logic: 
•				run complex backend processes that generate dynamic responses based on user input.
•			Middleware Services: 
•				allow communication between front-end applications and backend databases.
•			Security Management: 
•				provide authentication, authorization, and encryption features.
•			Resource Management: 
•				manage system resources such as memory, threads, and transactions to optimize performance.
•		Examples
•			Apache Tomcat 
•				open-source Java application server used for running Java Servlets and JSP.
•			JBoss (WildFly) 
•				Java EE-based application server developed by Red Hat.
•			IBM WebSphere 
•				enterprise-grade application server with extensive integration capabilities.
•			Oracle WebLogic Server – 
•				Java EE application server used for deploying large-scale enterprise applications
 
JPA
•	JPQL é a linguagem padrão da especificação JPA, focada em entidades e portátil entre provedores (Hibernate, EclipseLink). 
•	HQL é a linguagem proprietária do Hibernate, um superset da JPQL, recursos extras (ex: polimorfismo, JOINs mais ricos). 
•	JPQL é melhor para portabilidade, HQL para recursos avançados
JOIN FETCH
•	The FETCH keyword of the JOIN FETCH statement is JPA-specific. It tells the persistence provider to not only join the 2 database tables within the query but to also initialize the association on the returned entity. You can use it with a JOIN and a LEFT JOIN statement.
1.	Lazy Fetching (FetchType.LAZY) – Loads related data only when accessed.
2.	Eager Fetching (FetchType.EAGER) – Loads related data immediately.
3.	JOIN FETCH — Optimizes lazy fetching by reducing the number of queries.
•	FetchType.EAGER: The persistence provider must load the related annotated field or property. This is the default behavior for @Basic, @ManyToOne, and @OneToOne annotated fields.
•	FetchType.LAZY: The persistence provider should load data when it’s first accessed, but can be loaded eagerly. This is the default behavior for @OneToMany, @ManyToMany and @ElementCollection-annotated fields.
•	@NamedEntityGraph( name = "post-entity-graph", attributeNodes = { @NamedAttributeNode("subject"), @NamedAttributeNode("user"), @NamedAttributeNode("comments"), } )
 

 
 
Spring Security 
	comprehensive framework that provides 
		authentication, 
		authorization, and 
		protection against common exploits for Java applications.
		
1. Security Filter Chain (The Flow)
	Every incoming HTTP request is intercepted by a Security Filter Chain, 
	which is a sequence of filters responsible for different security concerns
	* Interception: 
		request hits the DelegatingFilterProxy, 
			which delegates to the FilterChainProxy 
			containing the actual security filters.
	* Sequential Processing: 
		Filters are executed in a specific order 
		(e.g., CsrfFilter -> Authentication filters -> AuthorizationFilter).
	* Decision Points: 
		Each filter can either let the request pass to the next filter or 
		block it and return an error 
		(e.g., 401 Unauthorized or 403 Forbidden). 

2. Authentication vs. Authorization
	* Authentication (Who are you?): 
		Verifies the user's identity.
		* Process: 
			AuthenticationFilter extracts credentials and 
			creates an unauthenticated Authentication object(like UsernamePasswordAuthenticationToken).
		* Management: 
			AuthenticationManager delegates this to 
				an AuthenticationProvider (e.g., DaoAuthenticationProvider), 
					checks credentials against a user store (via UserDetailsService).
		* Storage: 
			On success, the fully populated Authentication object 
				is stored in the SecurityContextHolder for the duration of the request.
	* Authorization (What can you do?): 
		Determines if the authenticated user has permission to access a specific resource.
		* Process: 
			An AuthorizationManager checks the user's granted authorities (roles/permissions) 
			against the required rules for the requested endpoint. 

3. Handling Different Security Mechanisms
	User Login (Form)
		Uses UsernamePasswordAuthenticationFilter. 
		It redirects to a login page, 
		validates submitted credentials, 
		creates a session, and 
		stores the SecurityContext in the HTTP session.
	API Key
		Typically handled by a custom filter added to the chain. 
		It extracts the key from a header (e.g., X-API-KEY), 
		validates it, and 
		manually populates the SecurityContext. |
	JWT
		Implemented as a stateless mechanism. 
		custom filter (often OncePerRequestFilter) 
			extracts the "Bearer" token from the Authorization header, 
			validates its signature/expiration, and 
			sets the Authentication in the SecurityContextHolder for that specific request. |
	OAuth2
		An authorization protocol that allows a third-party application (Client) to access resources on behalf of a user. 
		Spring Security handles redirections to the Authorization Server, 
		code exchanges for Access Tokens, and 
		token validation at the Resource Server.

choice between a custom Stateless JWT Filter and an OAuth2 Resource Server 
depends on whether you want to manage tokens yourself or 
delegate that responsibility to an external provider (like Keycloak or Okta).

1. Stateless JWT Filter (Custom Implementation)
	approach used when your application issues its own tokens. 
	must manually handle the token's lifecycle, validation, and extraction.
	Key Components:
		* JwtService: 
			utility class to generate, parse, and validate tokens using a secret key.
		* JwtAuthenticationFilter: 
			custom filter extending OncePerRequestFilter. 
				It intercepts every request, 
				extracts the "Bearer" token from the Authorization header, 
				and validates it.
		* SecurityContext Injection: 
			token is valid, 
			filter creates a UsernamePasswordAuthenticationToken and 
			places it into the SecurityContextHolder, 
			making the user "authenticated" for that request.

Example Configuration (Spring Boot 3.4):

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
    return http
        .csrf(csrf -> csrf.disable()) // CSRF is unnecessary for stateless APIs
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // Public login/register
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Custom filter position
        .build();
}

2. OAuth2 Resource Server (Standardized)
	modern standard for microservices. 
	app doesn't issue tokens; it 
	only validates them against an Authorization Server.
	
Key Components:
	* Issuer URI: 
		simply provide the URL of your Authorization Server (e.g., Auth0, Keycloak). 
		Spring will automatically fetch the public keys to verify signatures.
	* JwtDecoder: 
		Spring Boot auto-configures a JwtDecoder bean when it sees the OAuth2 dependencies and properties.
	* BearerTokenAuthenticationFilter: 
		built-in Spring filter that automatically handles token extraction and validation, 
		replacing the need for a custom filter. [9, 10, 11, 12, 13] 

Configuration:

   1. Dependency: Add spring-boot-starter-oauth2-resource-server.
   2. Properties (application.yml):
   
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://your-auth-server.com
   
   3. Security Config:
   
   @Beanpublic SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
           .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
           .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // Simplest config
           .build();
   }
      
Comparison Summary
	Effort
		High (must write Filter, Service, Util)
		Low (mostly configuration) 
	Token Issuer
		Your Application
		External Authorization Server
	Standard
		Custom/Internal
		Industry Standard (OAuth2/OIDC) 
	Scalability
		Good (Stateless)
		Excellent (Distributed) 

How passwords are handled, encrypted, and compared
	typical application using Spring Security, 
	passwords are never decrypted. 
	Instead, we use one-way hashing.

the flow of how passwords are handled, encrypted, and compared:
	1. The Storage (Signup/Registration)
		When user creates an account, 
		you must encode the password before saving it to the database.
		
		* When: 
			During the "Save User" service logic.
		* How: 
			Using the PasswordEncoder bean (usually BCryptPasswordEncoder).
		* Result: 
			The database stores a salted hash (e.g., $2a$10$vI8...), not the plain text.

	2. The Login Flow (Authentication)
		When user logs in, 
		SS handles the "comparison" logic inside the AuthenticationProvider.
		   1. Submission: 
				user sends their username and plain-text password 
				via a login form or API.
		   2. Retrieval: 
				SS uses your UserDetailsService to fetch the UserDetails object 
				(which contains the hashed password from your DB).
		   3. The Matching (The "Decryption" Myth):
			   * Spring does not decrypt the password from the database.
				  * Instead, it takes the plain-text password from the login attempt and 
					runs it through the same hashing algorithm (BCrypt).
				  * It then compares the newly generated hash with the stored hash. 
					If they match, the user is authenticated.
		   
	3. The PasswordEncoder Bean
		must define a bean to tell Spring which algorithm to use. 
		BCrypt is the industry standard because it includes a "salt" automatically, 
			protecting against rainbow table attacks.

	@Beanpublic PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // Strong, slow, and secure hashing
	}

Summary of the Lifecycle

	Registration
		pwd state: Plain Text → Encoded Hash
		handled by : Your Service + PasswordEncoder |
	Database Storage
		pwd state: Encoded Hash
		handled by : Your Database (JPA/Hibernate) |
	Login Attempt
		pwd state: Plain Text (from Request)
		handled by : UsernamePasswordAuthenticationToken |
	Validation
		pwd state: Hash vs Hash comparison
		handled by : DaoAuthenticationProvider |

Why "No Decryption"?
	Decryption implies that if a hacker steals your secret key, they can see everyone's passwords. 
	With Hashing, even the developers and database admins cannot see the original passwords. 
	The process is mathematically designed to be impossible to reverse.

SecurityFilterChain scenarios
	SecurityFilterChain is defined using a functional DSL. 
	Each configuration below defines which filters are active and how they handle credentials.

1. Traditional User Login (Session-Based)
	classic "Form Login" setup. 
	uses the UsernamePasswordAuthenticationFilter and 
	manages state via JSESSIONID cookies.

@Beanpublic SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Disable if using it as an API
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true)
        )
        .logout(logout -> logout.logoutSuccessUrl("/login"));
        
    return http.build();
}

------------------------------
2. Stateless JWT (Custom Filter)
For a REST API where you manage tokens manually, you must disable sessions and inject your own filter before the standard authentication filter.

@Beanpublic SecurityFilterChain jwtFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        // Add the custom JWT filter before the default UsernamePassword filter
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}

------------------------------
3. OAuth2 Resource Server (JWT-based)
This is used when an external provider (like Google or Keycloak) issues the tokens. Spring handles the extraction and validation automatically.

@Beanpublic SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        )
        // Automatically configures BearerTokenAuthenticationFilter
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.decoder(myCustomDecoder())) 
        )
        .build();
}

Comparison Summary

| Mechanism | Main Filter Used | Session Type |
|---|---|---|
| User Login | UsernamePasswordAuthenticationFilter | Stateful (Session) |
| JWT | Custom Filter (e.g., OncePerRequestFilter) | Stateless |
| OAuth2 | BearerTokenAuthenticationFilter | Stateless |

Pro-Tip: Filter Ordering
You can check the exact order of filters in your console during startup by adding logging.level.org.springframework.security=DEBUG to your application.properties. You will see a list like:

   1. DisableEncodeUrlFilter
   2. WebAsyncManagerIntegrationFilter
   3. SecurityContextHolderFilter
   4. ... Your Custom JWT Filter ...
   5. AuthorizationFilter

Would you like to see how to implement the actual logic inside the JwtFilter class to parse the "Bearer" header?

Implementing a custom JwtAuthenticationFilter involves overriding the doFilterInternal method. This filter ensures that for every request, the JWT is extracted, validated, and the user is authenticated in the SecurityContextHolder. [1, 2] 
1. Custom JWT Filter Implementation
This filter typically follows a five-step logic: extract, validate, load user, create authentication, and set context. [2] 

@Component
@RequiredArgsConstructorpublic class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Custom utility for parsing tokens
    private final UserDetailsService userDetailsService; // To load user from DB

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. Validate header format (must start with "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue if no token
            return;
        }

        // 3. Extract and parse the token
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt); // Logic inside your JwtService

        // 4. Authenticate if token is valid and user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create an authenticated token with user details and roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                // Add request-specific details (like IP) to the token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Finalize: Set the context so downstream code knows the user is logged in
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}

2. How it works with the Security Context

* SecurityContextHolder: A helper class that provides static access to the SecurityContext for the current execution thread.
* Thread Safety: Because it uses ThreadLocal, each request has its own isolated security information, which is critical for stateless REST APIs.
* Success Verification: Once the Authentication object is set, downstream components (like @PreAuthorize or your Controllers) can access the current user via SecurityContextHolder.getContext().getAuthentication(). [1, 3, 4] 

3. Error Handling
If the token is expired or malformed, the filter typically catches the exception and allows the AuthenticationEntryPoint to return a standardized 401 Unauthorized response. [5] 
Would you like to see the JwtService utility class that handles the actual signing and parsing of the tokens?

[1] [https://medium.com](https://medium.com/@yasiffkhan/authentication-workflow-with-jwt-b70c0fd011c4)
[2] [https://medium.com](https://medium.com/@himanshupapola/implementing-jwt-authentication-in-spring-security-a-complete-guide-d35e3dfd7a42)
[3] [https://docs.spring.io](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#:~:text=SecurityContextHolder%20%2D%20The%20SecurityContextHolder%20is%20where%20Spring,the%20Authentication%20of%20the%20currently%20authenticated%20user.)
[4] [https://medium.com](https://medium.com/@vuntt1412/spring-security-custom-authentication-cf781d08c4fc)
[5] [https://medium.com](https://medium.com/@afdulrohmat03/implementation-of-jwt-in-spring-boot-3-using-spring-security-af540240d6cf)


To implement the JwtService, you typically use the JJWT (Java JWT) library. It handles the mathematical complexity of signing and parsing tokens using algorithms like HS256.
1. Required Dependency
Add this to your pom.xml:

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if preferred -->
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

2. The JwtService Implementation
This service handles two main tasks: Generating a token (signing) and Extracting data from it (parsing).

@Servicepublic class JwtService {

    // Should be stored in environment variables, NOT hardcoded!
    private static final String SECRET_KEY = "your_very_long_and_secure_random_secret_key_here";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours in ms

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

Key Technical Aspects:

* Claims: These are pieces of info inside the JWT (Subject, Expiration, Roles). The Subject is usually the username/email.
* Signing: The signWith method uses your secret key to create a cryptographic signature. If even one character of the token is changed, the signature becomes invalid.
* Immutability: Once a JWT is issued, it cannot be changed. If a user's roles change, a new token must be generated.
* Statelessness: The server does not store the token in a database. It trustfully "parses" the incoming token using the SECRET_KEY. If the signature is valid, the data is trusted.

Security Warning: Never check your SECRET_KEY into version control (Git). Always use @Value("${jwt.secret}") to load it from an external configuration.
Would you like to see how to add Custom Claims (like roles or userId) to the generateToken method?


To add custom claims (like roles, isAdmin, or userId) to a JWT, you pass a Map to the builder() before compacting the token.
1. Updated generateToken Method
In the JwtService, you modify the generation logic to accept an extra map of claims.

public String generateToken(
        Map<String, Object> extraClaims,
        UserDetails userDetails
) {
    return Jwts.builder()
            .claims(extraClaims) // 1. Add your custom claims here
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(getSignInKey())
            .compact();
}

2. How to call it from your Authentication Service
When a user logs in successfully, you gather their roles or ID and pass them to the service.

public AuthenticationResponse authenticate(LoginRequest request) {
    // ... authentication logic ...
    
    var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
    
    // Create the map of custom claims
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("roles", user.getAuthorities()); // Stores roles like ["ROLE_USER", "ROLE_ADMIN"]
    claims.put("premium", user.isPremiumAccount());

    String jwtToken = jwtService.generateToken(claims, user);
    return new AuthenticationResponse(jwtToken);
}

3. How to extract custom claims later
You can create specific methods in JwtService to pull these values back out when the user sends the token in a request.

public List<String> extractRoles(String token) {
    return extractClaim(token, claims -> claims.get("roles", List.class));
}
public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
}

Why add Custom Claims?

* Performance: You can read the user's roles directly from the token without querying the database on every request.
* Frontend Logic: The frontend (React/Angular) can decode the JWT to show/hide buttons based on the roles claim without making extra API calls.
* Context: Useful for passing non-sensitive data like a tenantId in multi-tenant applications.

Security Rule: Never put sensitive information (like passwords or SSNs) in claims. JWTs are encoded, not encrypted; anyone with the token can read the claims using jwt.io.
Would you like to see how to use @PreAuthorize in your Controllers to restrict access based on these extracted roles?


To use @PreAuthorize, you first need to enable Method Security in your configuration. This allows you to restrict access to specific controller methods based on the roles extracted from the JWT.
1. Enable Method Security
In your Security Configuration class, add the @EnableMethodSecurity annotation.

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // This is the key to using @PreAuthorizepublic class SecurityConfig {
    // ... your securityFilterChain bean
}

2. Using @PreAuthorize in Controllers
Once enabled, you can protect individual endpoints. Spring Security will check the GrantedAuthorities inside the Authentication object (which you populated in your JwtFilter).

@RestController
@RequestMapping("/api/v1/admin")public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // Only users with ROLE_ADMIN can enter
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Welcome to the Admin Dashboard");
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Multiple roles allowed
    public ResponseEntity<String> createUser() {
        return ResponseEntity.ok("User created successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')") // Check for specific permissions
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok("User deleted");
    }
}

3. Logic: How it works with JWT

   1. Extraction: Your JwtFilter reads the roles claim from the JWT.
   2. Authorities: You convert those strings (e.g., "ROLE_ADMIN") into SimpleGrantedAuthority objects.
   3. Authentication: You set these authorities in the UsernamePasswordAuthenticationToken.
   4. Verification: When the request hits the Controller, Spring Security checks if the Authentication.getAuthorities() contains the role required by @PreAuthorize. If not, it throws an AccessDeniedException (returning a 403 Forbidden).

4. Advanced Logic: Expressions
You can also use the # symbol to reference method arguments, which is perfect for ensuring a user only accesses their own data:

@GetMapping("/profile/{username}")
@PreAuthorize("#username == authentication.principal.username")public ResponseEntity<User> getProfile(@PathVariable String username) {
    // Logic...
}

Summary of common expressions

* hasRole('ROLE_NAME'): Checks for a specific role (Spring automatically adds the ROLE_ prefix if missing).
* hasAnyRole('A', 'B'): Checks if the user has at least one of the listed roles.
* hasAuthority('READ_PRIVILEGE'): Checks for a specific permission string (case-sensitive).
* isAuthenticated(): Checks only if the user is logged in.

Would you like to see how to customize the Access Denied response so it returns a clean JSON error instead of the default HTML page?



