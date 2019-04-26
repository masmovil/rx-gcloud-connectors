package com.masmovil.rxfirestore;

import io.reactivex.subjects.SingleSubject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.netty.channel.DefaultChannelId;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;

public enum FirestoreTemplateFactory {

	INSTANCE;

	private final static long MAX_EXECUTION_TIME_SEC = 30;

	private EventBus eventBus;
	private SingleSubject<Vertx> vertxSubject = SingleSubject.create();

	public void init(Vertx ...vertxArg){

		EventBusOptions eventBusOpt = new EventBusOptions();

		// Deployment options to set the verticle as a worker
		var poolsize = Optional.ofNullable(System.getenv("DB_THREAD_POOL_SIZE")).orElse("");
		var dbThreadPoolSize = poolsize.isEmpty()?Runtime.getRuntime().availableProcessors() * 2 :Integer.parseInt(poolsize);

		if(poolsize.isEmpty()) {
			System.out.println("No DB_THREAD_POOL_SIZE environment variable has set. Default value " + dbThreadPoolSize);
		}

		DeploymentOptions firestoreWorkerDeploymentOptions = new DeploymentOptions().setWorker(true)
				.setInstances(dbThreadPoolSize) // matches the worker pool size below
				.setWorkerPoolName("rxfirestore-worker-pool")
				.setWorkerPoolSize(dbThreadPoolSize)
				.setMaxWorkerExecuteTime(MAX_EXECUTION_TIME_SEC)
				.setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS);

		if(vertxArg.length == 0){
			// Hack in order to avoid a noisy blocked thread exception at initialization time. Only happens once.
			DefaultChannelId.newInstance();

			List<Class<? extends AbstractVerticle>> verticleList = Arrays.asList(FirestoreTemplate.class);
			List<DeploymentOptions> deploymentOptionsList = Arrays.asList(firestoreWorkerDeploymentOptions);

			var vertxInstance = Runner.run(verticleList, new VertxOptions().setEventBusOptions(eventBusOpt), deploymentOptionsList);
			eventBus = vertxInstance.eventBus();
			vertxSubject.onSuccess(vertxInstance);
		}else{
			vertxArg[0].deployVerticle(FirestoreTemplate.class.getName(), firestoreWorkerDeploymentOptions);
			eventBus = vertxArg[0].eventBus();
			vertxSubject.onSuccess(vertxArg[0]);
		}
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public SingleSubject<Vertx> getVertx() {
		return vertxSubject;
	}
}
