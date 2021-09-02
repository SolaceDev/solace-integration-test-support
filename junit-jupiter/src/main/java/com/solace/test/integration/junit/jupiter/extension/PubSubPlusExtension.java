package com.solace.test.integration.junit.jupiter.extension;

import com.solace.test.integration.semp.v2.SempV2Api;
import com.solace.test.integration.testcontainer.PubSubPlusContainer;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class PubSubPlusExtension implements AfterEachCallback, ParameterResolver {
	private static final Logger LOG = LoggerFactory.getLogger(PubSubPlusExtension.class);
	private static final Namespace NAMESPACE = Namespace.create(PubSubPlusExtension.class);
	private final boolean usePubSubPlusTestcontainer;

	public PubSubPlusExtension() {
		this(null);
	}

	public PubSubPlusExtension(Supplier<Boolean> useTestcontainers) {
		usePubSubPlusTestcontainer = useTestcontainers != null ? useTestcontainers.get() : true;
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		Queue queue = context.getStore(NAMESPACE).get(Queue.class, Queue.class);
		JCSMPSession jcsmpSession = context.getStore(NAMESPACE).get(JCSMPSession.class, JCSMPSession.class);
		if (jcsmpSession != null) {
			try {
				if (queue != null) {
					LOG.info("Deprovisioning queue {}", queue.getName());
					jcsmpSession.deprovision(queue, JCSMPSession.FLAG_IGNORE_DOES_NOT_EXIST);
				}
			} finally {
				LOG.info("Closing session");
				jcsmpSession.closeSession();
			}
		}
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
		if (usePubSubPlusTestcontainer) {
			// Store container in root store so that it's only created once for all test classes.
			container = extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(PubSubPlusContainerResource.class,
					c -> {
						LOG.info("Creating PubSub+ container");
						PubSubPlusContainer newContainer = new PubSubPlusContainer();
						newContainer.start();
						containerStartCallback(newContainer);
						return new PubSubPlusContainerResource(newContainer);
					}, PubSubPlusContainerResource.class).getContainer();
		} else {
			container = null;
		}

		if (Queue.class.isAssignableFrom(paramType) || JCSMPSession.class.isAssignableFrom(paramType) ||
				JCSMPProperties.class.isAssignableFrom(paramType)) {
			JCSMPProperties jcsmpProperties = container != null ? createContainerJcsmpProperties(container) :
					createDefaultJcsmpProperties();

			if (JCSMPProperties.class.isAssignableFrom(paramType)) {
				return jcsmpProperties;
			}

			JCSMPSession session = extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(JCSMPSession.class, c -> {
				try {
					LOG.info("Creating JCSMP session");
					JCSMPSession jcsmpSession = JCSMPFactory.onlyInstance().createSession(jcsmpProperties);
					jcsmpSession.connect();
					return jcsmpSession;
				} catch (JCSMPException e) {
					throw new ParameterResolutionException("Failed to create JCSMP session", e);
				}
			}, JCSMPSession.class);

			if (JCSMPSession.class.isAssignableFrom(paramType)) {
				return session;
			}

			return extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(Queue.class, c -> {
				Queue queue = JCSMPFactory.onlyInstance().createQueue(RandomStringUtils.randomAlphanumeric(20));
				try {
					LOG.info("Provisioning queue {}", queue.getName());
					session.provision(queue, new EndpointProperties(), JCSMPSession.WAIT_FOR_CONFIRM);
				} catch (JCSMPException e) {
					throw new ParameterResolutionException("Could not create queue", e);
				}
				return queue;
			}, Queue.class);
		} else if (SempV2Api.class.isAssignableFrom(paramType)) {
			return container != null ? createContainerSempV2Api(container) : createDefaultSempV2Api();
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
}
