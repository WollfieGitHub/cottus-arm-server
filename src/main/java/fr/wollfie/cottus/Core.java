package fr.wollfie.cottus;

import fr.wollfie.cottus.resources.websockets.ArmStateSocket;
import fr.wollfie.cottus.services.ArmAnimatorService;
import fr.wollfie.cottus.services.ArmControllerService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** For lack of a better name */
@Startup
@ApplicationScoped
public class Core {

    private static final long UPDATE_DELAY = 33;
    private ScheduledExecutorService timer;
    
    @Inject ArmControllerService armControllerService;
    @Inject ArmAnimatorService armAnimatorService;
    @Inject ArmStateSocket armStateSocket;
    
    @PostConstruct
    void start() {
        this.timer = Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory());
        this.timer.scheduleAtFixedRate(this::update, 0, UPDATE_DELAY, TimeUnit.MILLISECONDS);
        Log.info("The update loop started...");
    }
    
    /** Update the application's state */
    private void update() {
        try {
            armAnimatorService.update();
            armControllerService.update();
            
            armStateSocket.broadCastArmState();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @PreDestroy
    void onDestroy() {
        if (this.timer != null && !this.timer.isShutdown()) {
            this.timer.shutdown();
            this.timer = null;
        }
        Log.info("Update loop has been shutdown.");
    }
}
