package io.jaconi;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller
public class TestController {

	@Get("/test")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	HttpResponse<Void> test() {
		return HttpResponse.noContent();
	}
}
