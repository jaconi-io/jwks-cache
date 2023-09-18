package io.jaconi.jwkscache;

import java.net.URL;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReport;
import com.nimbusds.jose.util.health.HealthReportListener;
import com.nimbusds.jose.util.health.HealthStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class URLAwareHealthReportListener
		implements HealthReportListener<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext> {
	private final URL url;

	@Override
	public void notify(
			HealthReport<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext> healthReport) {
		if (HealthStatus.NOT_HEALTHY.equals(healthReport.getHealthStatus())) {
			log.warn("health degraded for JWKS endpoint {}", url, healthReport.getException());
		}
	}
}
