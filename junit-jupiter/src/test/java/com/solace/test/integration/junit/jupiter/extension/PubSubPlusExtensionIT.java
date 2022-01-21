package com.solace.test.integration.junit.jupiter.extension;

import com.solace.test.integration.junit.jupiter.extension.PubSubPlusExtension.JCSMPProxy;
import com.solace.test.integration.junit.jupiter.extension.PubSubPlusExtension.ToxiproxyContext;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(PubSubPlusExtension.class)
@ExtendWith(PubSubPlusExtensionIT.TestExtension.class)
public class PubSubPlusExtensionIT {
	@ParameterizedTest
	@ValueSource(strings = {JCSMPProperties.HOST, JCSMPProperties.VPN_NAME, JCSMPProperties.USERNAME,
			JCSMPProperties.PASSWORD})
	public void testJCSMPSessionProperty(String jcsmpProperty,
										 JCSMPProperties jcsmpProperties,
										 JCSMPSession jcsmpSession) {
		assertFalse(jcsmpSession.isClosed(), "Session is not connected");
		assertEquals(jcsmpProperties.getStringProperty(jcsmpProperty), jcsmpSession.getProperty(jcsmpProperty));
	}

	@Test
	public void testExtensionIntegration(TestExtension.PubSubPlusContext pubSubPlusContext,
										 JCSMPProperties jcsmpProperties,
										 JCSMPSession jcsmpSession,
										 SempV2Api sempV2Api,
										 Queue queue,
										 @JCSMPProxy ToxiproxyContext toxiproxyContext) {
		assertEquals(jcsmpProperties.toProperties(), pubSubPlusContext.getJcsmpProperties().toProperties());
		assertEquals(jcsmpSession, pubSubPlusContext.getJcsmpSession());
		assertEquals(sempV2Api, pubSubPlusContext.getSempV2Api());
		assertEquals(queue, pubSubPlusContext.getQueue());
		assertEquals(toxiproxyContext, pubSubPlusContext.getToxiproxyContext());
	}

	static class TestExtension implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return parameterContext.getParameter().getType().isAssignableFrom(PubSubPlusContext.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return new PubSubPlusContext(
					PubSubPlusExtension.getJCSMPProperties(extensionContext),
					PubSubPlusExtension.getJCSMPSession(extensionContext),
					PubSubPlusExtension.getSempV2Api(extensionContext),
					PubSubPlusExtension.getQueue(extensionContext),
					PubSubPlusExtension.getToxiproxyContext(extensionContext, new JCSMPProxy() {
						@Override
						public Class<? extends Annotation> annotationType() {
							return JCSMPProxy.class;
						}
					}));
		}

		private static class PubSubPlusContext {
			private final JCSMPProperties jcsmpProperties;
			private final JCSMPSession jcsmpSession;
			private final SempV2Api sempV2Api;
			private final Queue queue;
			private final ToxiproxyContext toxiproxyContext;

			private PubSubPlusContext(JCSMPProperties jcsmpProperties, JCSMPSession jcsmpSession, SempV2Api sempV2Api,
									  Queue queue, ToxiproxyContext toxiproxyContext) {
				this.jcsmpProperties = jcsmpProperties;
				this.jcsmpSession = jcsmpSession;
				this.sempV2Api = sempV2Api;
				this.queue = queue;
				this.toxiproxyContext = toxiproxyContext;
			}

			public JCSMPProperties getJcsmpProperties() {
				return jcsmpProperties;
			}

			public JCSMPSession getJcsmpSession() {
				return jcsmpSession;
			}

			public SempV2Api getSempV2Api() {
				return sempV2Api;
			}

			public Queue getQueue() {
				return queue;
			}

			public ToxiproxyContext getToxiproxyContext() {
				return toxiproxyContext;
			}
		}
	}
}
