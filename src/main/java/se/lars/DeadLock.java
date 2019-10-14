package se.lars;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetServerOptions;

public class DeadLock {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        deployVerticle(vertx, new MonitorVerticle())
                .compose(__ -> deployVerticle(vertx, new RestVerticle()))
                .compose(__ -> deployVerticle(vertx, new ApiVerticle()));
    }


    private static class ApiVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) {
            vertx.deployVerticle(NetVerticle::new, new DeploymentOptions().setInstances(32), ar -> {
                if (ar.succeeded()) {
                    startPromise.complete();
                } else {
                    startPromise.fail(ar.cause());
                }
            });
        }
    }

    private static class NetVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) {
            vertx.createNetServer(new NetServerOptions().setPort(20152))
                    .connectHandler(netSocket -> {
                    })
                    .listen(ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Net Verticle Listening on port: " + ar.result().actualPort());
                            startPromise.complete();
                        } else {
                            System.out.println("Oh noes");
                            startPromise.fail(ar.cause());
                        }
                    });
        }
    }

    private static class RestVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) {
            vertx.createHttpServer(new HttpServerOptions())
                    .requestHandler(req -> {
                    })
                    .listen(15152, ar -> {
                        if (ar.succeeded()) {
                            System.out.println("REST listening on port: " + ar.result().actualPort());
                            startPromise.complete();
                        } else {
                            startPromise.fail(ar.cause());
                        }
                    });
        }
    }

    private static class MonitorVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) {
            vertx.createHttpServer(new HttpServerOptions())
                    .requestHandler(req -> {
                    })
                    .listen(16152, ar -> {
                        if (ar.succeeded()) {
                            System.out.println("Monitor listening on port: " + ar.result().actualPort());
                            startPromise.complete();
                        } else {
                            startPromise.fail(ar.cause());
                        }
                    });
        }
    }

    private static Future<String> deployVerticle(Vertx vertx, Verticle verticle) {
        return Future.future(promise -> vertx.deployVerticle(verticle, promise));
    }

}