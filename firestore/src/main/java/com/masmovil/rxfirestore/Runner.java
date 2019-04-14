package com.masmovil.rxfirestore;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.reactivex.core.Vertx;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Runner is responsible of the verticles deployment and the configuration loading.
 * The configuration is loaded from a YAML file located in ${VERTX_CONFIG_PATH}${VERTX_CONFIG_SLASH}${VERTX_CONFIG_FILE}.
 */
public class Runner {

	public static Vertx run(List<Class<? extends SyncVerticle>> classes, VertxOptions options, List<DeploymentOptions> deploymentOptions) {
		List<String> verticleNames = classes.stream().map(Class::getName).collect(Collectors.toList());
		Consumer<Vertx> runner = vertx -> {
						try {
							for (int i=0;i<verticleNames.size(); i++) {
								if (deploymentOptions.get(i) != null) {
									vertx.deployVerticle(verticleNames.get(i), deploymentOptions.get(i));
								} else {
									vertx.deployVerticle(verticleNames.get(i));
								}
							}
						} catch (Throwable t) {
							t.printStackTrace();
						}
					};

		return deployVertx(options, runner);
	}

	private static Vertx deployVertx(VertxOptions options, Consumer<Vertx> runner){
		Vertx vertx = Vertx.vertx(options);
		runner.accept(vertx);
		return vertx;
	}
}
