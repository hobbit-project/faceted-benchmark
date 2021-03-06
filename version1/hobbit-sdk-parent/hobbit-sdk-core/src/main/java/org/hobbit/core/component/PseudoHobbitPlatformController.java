package org.hobbit.core.component;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.ServiceContext;
import org.hobbit.core.Commands;
import org.hobbit.core.service.docker.api.ServiceBuilder;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;

public class PseudoHobbitPlatformController
    implements Consumer<ByteBuffer>
{
    private static final Logger logger = LoggerFactory.getLogger(PseudoHobbitPlatformController.class);

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    @Resource(name="commandPub")
    protected Flowable<ByteBuffer> commandPub;

    @Resource(name="benchmarkControllerServiceFactory")
    protected ServiceBuilder<Service> benchmarkControllerServiceFactory;

    public PseudoHobbitPlatformController() {
        super();
    }

    public static Entry<Byte, byte[]> formatToHobbitApi(ByteBuffer buffer) {
        Entry<Byte, byte[]> result;

        int pos = buffer.position();

        int limit = buffer.limit();
        if(limit == 0) {
            result = null;
        } else {
            buffer.position(0);
            byte b = buffer.get();
            byte[] dst = new byte[limit - 1];
            buffer.get(dst, 0, dst.length);

            result = new SimpleEntry<>(b, dst);
        }

        buffer.position(pos);
        return result;
    }

    public static ByteBuffer toByteBuffer(byte cmd, byte[] data) {
        ByteBuffer result = ByteBuffer.wrap(new byte[1 + data.length]).put(cmd).put(data);
        result.rewind();
        return result;
    }

    public static void forwardToHobbit(ByteBuffer buffer, BiConsumer<Byte, byte[]> consumer) {
        Entry<Byte, byte[]> e = formatToHobbitApi(buffer);
        if(e != null) {
            consumer.accept(e.getKey(), e.getValue());
        }
    }


    @Override
    public void accept(ByteBuffer t) {
        //logger.info("Seen " + t.remaining() + " bytes on command queue: " + Arrays.toString(t.array()));


        if(t.remaining() > 0) {
            if(t.get(0) == Commands.START_BENCHMARK_SIGNAL) {
                logger.info("Starting benchmark");

                // A hacky cast
                @SuppressWarnings("unchecked")
                ServiceContext<BenchmarkController> service = (ServiceContext<BenchmarkController>) benchmarkControllerServiceFactory.get();

                service.startAsync();
                try {
                    service.awaitRunning(BenchmarkControllerComponentImpl.MAX_COMPONENT_STARTUP_TIME_IN_SECONDS, TimeUnit.SECONDS);
                    //Service benchmarkController = service.getComponent();
                    //benchmarkController.executeBenchmark();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    logger.debug("Waiting for benchmark to finish...");
                    ServiceManagerUtils.awaitTerminatedOrStopAfterTimeout(service, BenchmarkControllerComponentImpl.MAX_BENCHMARK_TIME_IN_SECONDS, TimeUnit.SECONDS, BenchmarkControllerComponentImpl.MAX_COMPONENT_SHUTDOWN_TIME_IN_SECONDS, TimeUnit.SECONDS);
//                    service.awaitTerminated(5, TimeUnit.MINUTES);
//                    service.stopAsync();
//                    try {
//                    } catch (TimeoutException e) {
//                        throw new RuntimeException(e);
//                    }
                }

//                // Create a new instance of the benchmark controller
//                BenchmarkController benchmarkController;
//                try {
//                    benchmarkController = benchmarkControllerClass.newInstance();
//                    ctx.getAutowireCapableBeanFactory().autowireBean(benchmarkController);
//
//                    Consumer<ByteBuffer> observer = buffer -> forwardToHobbit(buffer, benchmarkController::receiveCommand);
//
//                    try {
//                        // Register the benchmark controller as a listener to the command queue
//                        cmdQueue.addObserver(observer);
//
//                        benchmarkController.executeBenchmark();
//                    } finally {
//                        IOUtils.closeQuietly(benchmarkController);
//
//                        // After the benchmark controller served its purpose, deregister it from events
//                        cmdQueue.removeObserver(observer);
//                    }
//
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
             }
        }

    }





}
