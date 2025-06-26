package io.jaconi.jwkscache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReport;
import com.nimbusds.jose.util.health.HealthStatus;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
class HealthReportListenerFactoryTest {
	private static final String URL_STRING = "https://example.com";
	private static final URL URL;

	static {
		try {
			URL = URI.create(URL_STRING).toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Autowired
	HealthReportListenerFactory healthReportListenerFactory;

	@Autowired
	MeterRegistry meterRegistry;

	@Mock
	JWKSetSourceWithHealthStatusReporting<SecurityContext> jwksSource;

	@BeforeEach
	void setUp() {
		meterRegistry.clear();
	}

	@Test
	void defaultToHealthy() {
		var listener = healthReportListenerFactory.create(URL);

		listener.notify(new HealthReport<>(jwksSource, HealthStatus.HEALTHY, 0, null));

		var val = meterRegistry.get("jwks-cache.endpoints")
				.tag("url", URL_STRING)
				.gauge()
				.value();

		assertEquals(1.0, val);
	}

	@Test
	void defaultToUnhealthy() {
		var listener = healthReportListenerFactory.create(URL);

		listener.notify(new HealthReport<>(jwksSource, HealthStatus.NOT_HEALTHY, 0, null));

		var val = meterRegistry.get("jwks-cache.endpoints")
				.tag("url", URL_STRING)
				.gauge()
				.value();

		assertEquals(0.0, val);
	}

	@Test
	void unhealthyToHealthy() {
		var listener = healthReportListenerFactory.create(URL);

		listener.notify(new HealthReport<>(jwksSource, HealthStatus.NOT_HEALTHY, 0, null));
		listener.notify(new HealthReport<>(jwksSource, HealthStatus.NOT_HEALTHY, 0, null));
		listener.notify(new HealthReport<>(jwksSource, HealthStatus.HEALTHY, 0, null));

		var val = meterRegistry.get("jwks-cache.endpoints")
				.tag("url", URL_STRING)
				.gauge()
				.value();

		assertEquals(1.0, val);
	}

	@Test
	void healthyToUnhealthy() {
		var listener = healthReportListenerFactory.create(URL);

		listener.notify(new HealthReport<>(jwksSource, HealthStatus.HEALTHY, 0, null));
		listener.notify(new HealthReport<>(jwksSource, HealthStatus.HEALTHY, 0, null));
		listener.notify(new HealthReport<>(jwksSource, HealthStatus.NOT_HEALTHY, 0, null));

		var val = meterRegistry.get("jwks-cache.endpoints")
				.tag("url", URL_STRING)
				.gauge()
				.value();

		assertEquals(0.0, val);
	}
}
