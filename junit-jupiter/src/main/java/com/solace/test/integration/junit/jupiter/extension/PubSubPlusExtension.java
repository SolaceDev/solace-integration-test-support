package com.solace.test.integration.junit.jupiter.extension;

import com.solace.test.integration.junit.jupiter.extension.pubsubplus.provider.PubSubPlusFileProvider;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solace.test.integration.testcontainer.PubSubPlusContainer;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * <p>Junit 5 extension for using Solace PubSub+.</p>
 * <p>By default a Solace PubSub+ container will be auto-provisioned as necessary.
 * Can be sub-classed to use an externally managed PubSub+ container.</p>
 * <p>Resources (e.g. sessions and endpoints) created through this extension are disconnected and/or deprovisioned from
 * the broker between tests.</p>
 * <p><b>Usage:</b></p>
 * <pre><code>
 *	{@literal @}ExtendWith(PubSubPlusExtension.class)
 *	public class Test {
 *		// At least one of these arguments must be defined on the test function for the session and broker to be provisioned.
 *		{@literal @}Test
 *		public void testMethod(JCSMPSession session, SempV2Api sempV2Api, Queue queue, JCSMPProperties properties) {
 *			// Test logic using JCSMP
 *  	}
 *  }
 * </code></pre>
 * <p><b>To use an External PubSub+ Broker:</b></p>
 * <p>Add a {@value #PROPERTIES_FILENAME} resource file to configure external PubSub+ providers:</p>
 * <pre><code>
 * providers=com.test.OtherExternalProvider,\
 * 	com.solace.test.integration.junit.jupiter.extension.pubsubplus.provider.PubSubPlusFileProvider
 * </code></pre>
 * <p>External providers must implement {@link ExternalProvider}.</p>
 * <p>By default, {@link PubSubPlusFileProvider} is enabled as an external provider.</p>
 */
public class PubSubPlusExtension implements ParameterResolver {
	private static final Logger LOG = LoggerFactory.getLogger(PubSubPlusExtension.class);
	private static final Namespace NAMESPACE = Namespace.create(PubSubPlusExtension.class);
	private static final String PROPERTIES_FILENAME = "pubsubplus-extension.properties";
	private final Supplier<PubSubPlusContainer> containerSupplier;
	private final List<ExternalProvider> externalProviders;

	public PubSubPlusExtension() {
		this(PubSubPlusContainer::new);
	}

	/**
	 * Initialize the extension. If the provided PubSub+ container supplier is {@code null}, container provisioning is
	 * disabled.
	 * @param containerSupplier the PubSub+ container supplier
	 */
	public PubSubPlusExtension(Supplier<PubSubPlusContainer> containerSupplier) {
		this.containerSupplier = containerSupplier;
		this.externalProviders = Optional.ofNullable(createExternalProvidersList())
				.orElse(Collections.singletonList(new PubSubPlusFileProvider()));
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		Class<?> paramType = parameterContext.getParameter().getType();
		return JCSMPProperties.class.isAssignableFrom(paramType) ||
				JCSMPSession.class.isAssignableFrom(paramType) ||
				Queue.class.isAssignableFrom(paramType) ||
				SempV2Api.class.isAssignableFrom(paramType);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		Class<?> paramType = parameterContext.getParameter().getType();
		PubSubPlusContainer container;
		if (getValidResolver(extensionContext) != null) {
			extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(ExternalProvider.class, c -> {
				ExternalProvider externalProvider = getValidResolver(extensionContext);
				LOG.info("Initializing external PubSub+ provider {}", externalProvider.getClass().getSimpleName());
				externalProvider.init(extensionContext);
				return externalProvider;
			});
			container = null;
		} else if (containerSupplier != null) {
			// Store container in root store so that it's only created once for all test classes.
			container = extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(PubSubPlusContainerResource.class,
					c -> {
						LOG.info("Creating PubSub+ container");
						PubSubPlusContainer newContainer = containerSupplier.get();
						if (!newContainer.isCreated()) {
							newContainer.start();
						}
						containerStartCallback(newContainer);
						return new PubSubPlusContainerResource(newContainer);
					}, PubSubPlusContainerResource.class).getContainer();
		} else {
			container = null;
		}

		if (Queue.class.isAssignableFrom(paramType) || JCSMPSession.class.isAssignableFrom(paramType) ||
				JCSMPProperties.class.isAssignableFrom(paramType)) {
			JCSMPProperties jcsmpProperties = Optional.ofNullable(container)
					.map(this::createContainerJcsmpProperties)
					.orElseGet(() -> Optional.ofNullable(getValidResolver(extensionContext))
							.map(p -> p.createJCSMPProperties(extensionContext))
							.orElseGet(this::createDefaultJcsmpProperties));

			if (JCSMPProperties.class.isAssignableFrom(paramType)) {
				return jcsmpProperties;
			}

			JCSMPSession session = extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(
					PubSubPlusSessionResource.class, c -> {
				try {
					LOG.info("Creating JCSMP session");
					JCSMPSession jcsmpSession = JCSMPFactory.onlyInstance().createSession(jcsmpProperties);
					jcsmpSession.connect();
					return new PubSubPlusSessionResource(jcsmpSession);
				} catch (JCSMPException e) {
					throw new ParameterResolutionException("Failed to create JCSMP session", e);
				}
			}, PubSubPlusSessionResource.class).getSession();

			if (JCSMPSession.class.isAssignableFrom(paramType)) {
				return session;
			}

			return extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(PubSubPlusQueueResource.class, c -> {
				Queue queue = JCSMPFactory.onlyInstance().createQueue(RandomStringUtils.randomAlphanumeric(20));
				try {
					LOG.info("Provisioning queue {}", queue.getName());
					session.provision(queue, new EndpointProperties(), JCSMPSession.WAIT_FOR_CONFIRM);
				} catch (JCSMPException e) {
					throw new ParameterResolutionException("Could not create queue", e);
				}
				return new PubSubPlusQueueResource(queue, session);
			}, PubSubPlusQueueResource.class).getQueue();
		} else if (SempV2Api.class.isAssignableFrom(paramType)) {
			return Optional.ofNullable(container)
					.map(this::createContainerSempV2Api)
					.orElseGet(() -> Optional.ofNullable(getValidResolver(extensionContext))
							.map(p -> p.createSempV2Api(extensionContext))
							.orElseGet(this::createDefaultSempV2Api));
		} else {
			throw new IllegalArgumentException(String.format("Parameter type %s is not supported", paramType));
		}
	}

	protected void containerStartCallback(PubSubPlusContainer container) throws ParameterResolutionException {
	}

	protected JCSMPProperties createContainerJcsmpProperties(PubSubPlusContainer container) {
		JCSMPProperties jcsmpProperties = new JCSMPProperties();
		jcsmpProperties.setProperty(JCSMPProperties.HOST, container.getOrigin(PubSubPlusContainer.Port.SMF));
		jcsmpProperties.setProperty(JCSMPProperties.USERNAME, "default");
		jcsmpProperties.setProperty(JCSMPProperties.VPN_NAME, "default");
		return jcsmpProperties;
	}

	protected JCSMPProperties createDefaultJcsmpProperties() {
		return new JCSMPProperties();
	}

	protected SempV2Api createContainerSempV2Api(PubSubPlusContainer container) {
		return new SempV2Api(container.getOrigin(PubSubPlusContainer.Port.SEMP), container.getAdminUsername(),
				container.getAdminPassword());
	}

	protected SempV2Api createDefaultSempV2Api() {
		return new SempV2Api("http://localhost:8080", "admin", "admin");
	}

	private ExternalProvider getValidResolver(ExtensionContext extensionContext) {
		return externalProviders.stream()
				.filter(p -> p.isValid(extensionContext))
				.findFirst()
				.orElse(null);
	}

	private List<ExternalProvider> createExternalProvidersList() {
		try (InputStream stream = PubSubPlusExtension.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME)) {
			if (stream != null) {
				Properties properties = new Properties();
				properties.load(stream);
				String providersString = properties.getProperty("providers");
				if (providersString != null) {
					List<ExternalProvider> providers = new ArrayList<>();
					for (String providerClassName : providersString.split(",")) {
						if (providerClassName.trim().isEmpty()) {
							continue;
						}

						Class<?> clazz;
						try {
							clazz = Class.forName(providerClassName.trim());
						} catch (ClassNotFoundException e) {
							throw new IllegalArgumentException(e);
						}
						providers.add((ExternalProvider) clazz.getDeclaredConstructor().newInstance());
					}
					return providers;
				}
			}
		} catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException |
				InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * An external provider for a PubSub+ broker.
	 */
	public interface ExternalProvider {
		/**
		 * Indicates to the PubSub+ extension whether it can use the external provider to get its test broker.
		 * @param extensionContext extension context
		 * @return true, if the provider is usable.
		 */
		boolean isValid(ExtensionContext extensionContext);

		/**
		 * Initialize the external provider. Is only invoked once.
		 * <p><b>TIP:</b> In most cases, any data that needs to be persisted in the {@code init()} should be stored
		 * in a store on the root context.</p>
		 * @param extensionContext extension context
		 */
		void init(ExtensionContext extensionContext);

		/**
		 * Create a new JCSMPProperties object for this provider's broker.
		 * @param extensionContext extension context
		 * @return a new JCSMPProperties instance
		 */
		JCSMPProperties createJCSMPProperties(ExtensionContext extensionContext);

		/**
		 * Create a new SempV2API object for this provider's broker.
		 * @param extensionContext extension context
		 * @return a new SempV2Api instance
		 */
		SempV2Api createSempV2Api(ExtensionContext extensionContext);
	}

	private static class PubSubPlusContainerResource implements ExtensionContext.Store.CloseableResource {
		private static final Logger LOG = LoggerFactory.getLogger(PubSubPlusContainerResource.class);
		private final PubSubPlusContainer container;

		private PubSubPlusContainerResource(PubSubPlusContainer container) {
			this.container = container;
		}

		public PubSubPlusContainer getContainer() {
			return container;
		}

		@Override
		public void close() {
			LOG.info("Closing PubSub+ container {}", container.getContainerName());
			container.close();
		}
	}

	private static class PubSubPlusSessionResource implements ExtensionContext.Store.CloseableResource {
		private static final Logger LOG = LoggerFactory.getLogger(PubSubPlusSessionResource.class);
		private final JCSMPSession session;

		private PubSubPlusSessionResource(JCSMPSession session) {
			this.session = session;
		}

		public JCSMPSession getSession() {
			return session;
		}

		@Override
		public void close() {
			LOG.info("Closing session");
			session.closeSession();
		}
	}

	private static class PubSubPlusQueueResource implements ExtensionContext.Store.CloseableResource {
		private static final Logger LOG = LoggerFactory.getLogger(PubSubPlusQueueResource.class);
		private final Queue queue;
		private final JCSMPSession session;

		private PubSubPlusQueueResource(Queue queue, JCSMPSession session) {
			this.queue = queue;
			this.session = session;
		}

		public Queue getQueue() {
			return queue;
		}

		@Override
		public void close() throws Throwable {
			LOG.info("Deprovisioning queue {}", queue.getName());
			session.deprovision(queue, JCSMPSession.FLAG_IGNORE_DOES_NOT_EXIST);
		}
	}
}
