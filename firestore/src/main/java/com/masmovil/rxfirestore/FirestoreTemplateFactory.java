package com.masmovil.rxfirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.netty.channel.DefaultChannelId;
import io.reactivex.subjects.SingleSubject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sync.SyncVerticle;


public enum FirestoreTemplateFactory {

	INSTANCE;

	private final static long MAX_EXECUTION_TIME_SEC = 30;
	private final static Logger LOG = LoggerFactory.getLogger(FirestoreTemplateFactory.class);

	private SingleSubject<EventBus> eventBus = SingleSubject.create();

	FirestoreTemplateFactory() {
		// Hack in order to avoid a noisy blocked thread exception at initialization time. Only happens once.
		DefaultChannelId.newInstance();

		//EventBusOptions eventBusOpt = new EventBusOptions();

		// Deployment options to set the verticle as a worker
		/*String poolsize = Optional.ofNullable(System.getenv("DB_THREAD_POOL_SIZE")).orElse("");
		int dbThreadPoolSize = poolsize.isEmpty()?Runtime.getRuntime().availableProcessors() * 2 :Integer.parseInt(poolsize);

		if(poolsize.isEmpty()) {
			System.out.println("No DB_THREAD_POOL_SIZE environment variable has set. Default value " + dbThreadPoolSize);
		}*/

	/*	DeploymentOptions firestoreWorkerDeploymentOptions = new DeploymentOptions().setWorker(true)
				.setInstances(dbThreadPoolSize) // matches the worker pool size below
				.setWorkerPoolName("rxfirestore-worker-pool")
				.setWorkerPoolSize(dbThreadPoolSize)
				.setMaxWorkerExecuteTime(MAX_EXECUTION_TIME_SEC)
				.setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS);*/

		/*List<Class<? extends SyncVerticle>> verticleList = Arrays.asList(FirestoreTemplate.class);
		List<DeploymentOptions> deploymentOptionsList = Arrays.asList(firestoreWorkerDeploymentOptions);

		var vertx = Runner.run(verticleList, new VertxOptions().setEventBusOptions(eventBusOpt), deploymentOptionsList);*/
		Consumer<Vertx> runner = vertx -> {
			vertx.deployVerticle(FirestoreTemplate.class.getName(), h -> {
				if (h.succeeded()) {
					System.out.println("Success: " + h.result());
					eventBus.onSuccess(vertx.eventBus());
				} else {
					System.err.println("Something went wrong: " + h.cause());
				}
			});
		};

		Vertx vertx = Vertx.vertx();
		runner.accept(vertx);

	}

	public SingleSubject<EventBus> getEventBus() {
		return eventBus;
	}

}
