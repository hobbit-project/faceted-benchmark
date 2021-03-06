package org.aksw.jena_sparql_api;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

// No longer needed - SpringApplication does this

/**
 * The service instance has a fixed environment
 *
 * @author raven
 *
 * @param <T>
 */
public class ServiceContext<T>
    extends AbstractIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(ServiceContext.class);


    protected Class<T> componentClass;

    //@Autowired
    protected ApplicationContext ctx;

    //@Resource(name="commandChannelPub")
    protected Flowable<ByteBuffer> commandPub;


    protected transient T componentInstance;


    // The service wrapper for the componentInstance
    protected transient Service componentService;
    //protected transient Consumer<ByteBuffer> observer;

    protected transient Disposable commandChannelUnsubscribe; 
    
    public ServiceContext(Class<T> componentClass, ApplicationContext ctx,
    		Flowable<ByteBuffer> commandPub) {
        super();
        this.componentClass = componentClass;
        this.ctx = ctx;
        this.commandPub = commandPub;
    }

    public T getComponent() {
        return componentInstance;
    }

    @Override
    protected void startUp() {
        try {
            startUpCore();
        } catch(Exception e) {
            throw new RuntimeException("Failed to start class " + componentClass, e);
        }
    }
    
    protected void startUpCore() throws Exception {
//
//        logger.debug("Starting local component of type " + componentClass);
//        componentInstance = componentClass.newInstance();
//
//
//        // Determine the appropriate service wrapper for the component
//        if(componentInstance instanceof Service) {
//            componentService = (Service)componentInstance;
//        } else if(componentInstance instanceof IdleServiceCapable) {
//            IdleServiceCapable tmp = (IdleServiceCapable)componentInstance;
//            componentService = new IdleServiceDelegate(
//                    () -> { try { tmp.startUp(); } catch(Exception e) { throw new RuntimeException(e); }},
//                    () -> { try { tmp.shutDown(); } catch(Exception e) { throw new RuntimeException(e); }});
//
//
//        } else if(componentInstance instanceof RunnableServiceCapable) {
//            RunnableServiceCapable tmp = (RunnableServiceCapable)componentInstance;
//            //componentService = new ExecutionThreadServiceDelegate(tmp::startUp, tmp::run, tmp::shutDown);
//            componentService = new ExecutionThreadServiceDelegate(
//                    () -> { try { tmp.startUp(); } catch(Exception e) { throw new RuntimeException(e); }},
//                    () -> { try { tmp.run(); } catch(Exception e) { throw new RuntimeException(e); }},
//                    () -> { try { tmp.shutDown(); } catch(Exception e) { throw new RuntimeException(e); }});
//        } else {
//            throw new RuntimeException("Could not determine how to wrap the component as a service: " + componentInstance.getClass());
//        }
//
//
//        ctx.getAutowireCapableBeanFactory().autowireBean(componentInstance);

//        commandChannelUnsubscribe = commandPub.subscribe(
//        		buffer -> PseudoHobbitPlatformController.forwardToHobbit(buffer, componentInstance::receiveCommand));
//
//        // Add a listener to shut down 'this' service wrapper
//        Service self = this;
//        componentService.addListener(new Listener() {
//        	@Override
//        	public void failed(State from, Throwable failure) {
//        		doTermination();
//        		super.failed(from, failure);
//        	}
//        	
//        	@Override
//            public void terminated(State from) {
//        		doTermination();
//        		super.terminated(from);
//            };
//
//            public void doTermination() {
//                self.stopAsync();
//                try {
//                    self.awaitTerminated(60, TimeUnit.SECONDS);
//                } catch (TimeoutException e) {
//                    throw new RuntimeException("Failure during termination " + componentClass, e);
//                }            	
//            }
//        
//        }, MoreExecutors.directExecutor());

        //componentInstance.init();
        componentService.startAsync();
        componentService.awaitRunning(60, TimeUnit.SECONDS);


        logger.debug("Successfully started local component of type " + componentClass);

    }

    @Override
    protected void shutDown() throws Exception {
        //commandChannel.unsubscribe(observer);
    	Optional.ofNullable(commandChannelUnsubscribe).ifPresent(Disposable::dispose);
    	

        if(componentInstance != null) {
            logger.debug("Shutting down component instance: " + componentInstance.getClass());
        }

        componentService.stopAsync();
        try {
            componentService.awaitTerminated(60, TimeUnit.SECONDS);
        } catch(Exception e) {
            logger.error("Failed to shut down component instance in time: " + componentInstance.getClass(), e);
        }
        // After the component served its purpose, deregister it from events
    }
}

