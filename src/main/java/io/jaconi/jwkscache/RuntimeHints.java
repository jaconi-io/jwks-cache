package io.jaconi.jwkscache;

import org.hibernate.validator.internal.util.logging.Log_$logger;
import org.hibernate.validator.internal.util.logging.Messages_$bundle;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;

public class RuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
		// Workaround for missing reachability metadata.
		// See https://github.com/spring-projects/spring-boot/issues/50221
		// See https://github.com/oracle/graalvm-reachability-metadata/issues/8397
		hints.reflection().registerType(Log_$logger.class, INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);
		hints.reflection().registerType(Messages_$bundle.class, INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);
	}
}
