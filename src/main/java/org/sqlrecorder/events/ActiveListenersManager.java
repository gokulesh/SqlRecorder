package org.sqlrecorder.events;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.sqlrecorder.events.event.ExecuteEvent;
import org.sqlrecorder.events.listener.StatementListener;
import org.sqlrecorder.exception.SQLRecorderException;
import org.sqlrecorder.util.LogUtils;

public final class ActiveListenersManager {

    private static Logger LOG = LogUtils.loggerForThisClass();
    private List<StatementListener> listeners = Lists.newArrayList();

    public ActiveListenersManager(StatementListener... listeners) {
        Preconditions.checkArgument(!ArrayUtils.isEmpty(listeners), "must have at least 1 registered listener");
        this.listeners.addAll(Arrays.asList(listeners));
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                  shutdownListeners();
            }
        });

        LOG.info("Finished registering listeners and adding shutdown hook");
    }

    public void notifyListeners(ExecuteEvent e) {
        for (StatementListener listener : listeners) {
            listener.queryExecuted(e);
        }
    }

    //This is mainly used for returning the list of executed queries back to a program/test.
    public List<String> getExecutedQueries() {
        for (StatementListener listener : listeners) {
            //Pick the first available listener 
            if (listener.returnsExecutedQueries()) {
                return listener.executedQueries();
            }
        }
        throw new SQLRecorderException("None of the registered listeners return the executed queries");
    }

    private void shutdownListeners(){
        LOG.info("Started shutdown for all listeners....");

        for (StatementListener listener : listeners) {
            LOG.info("\nStarting shutdown for: " + listener.id() );
            listener.shutDown();
            LOG.info("\nFinished shutdown for: " + listener.id() );            
        }
        LOG.info("Finished shutdown for all listeners");
    }
}
