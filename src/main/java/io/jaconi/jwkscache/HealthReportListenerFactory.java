package io.jaconi.jwkscache;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
import com.nimbusds.jose.util.health.HealthStatus;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportListenerFactory {
	private final MeterRegistry meterRegistry;

	HealthReportListener<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext> create(URL url) {
		var health = meterRegistry.gauge("jwks-cache.endpoints", List.of(
				new ImmutableTag("url", url.toString())
		), new AtomicInteger(1));

		return healthReport -> {
			var before = HealthStatus.HEALTHY;
			if (health.get() == 0) {
				before = HealthStatus.NOT_HEALTHY;
			}

			switch (healthReport.getHealthStatus()) {
				case HEALTHY -> {
					health.set(1);
					if (HealthStatus.NOT_HEALTHY.equals(before)) {
						log.info("JWKS endpoint {} recovered", url);
					}
				}
				case NOT_HEALTHY -> {
					health.set(0);
					if (HealthStatus.HEALTHY.equals(before)) {
						log.warn("JWKS endpoint {} degraded", url, healthReport.getException());
					}
				}
			}
		};
	}
}
