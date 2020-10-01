package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.mqtt.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nina on 2018/3/15.
 */
public final class DownLinkJobManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DownLinkJobManager() {
        this.logJobHandler = new DownLinkLogJobHandler();
        this.forwardJobHandler = new DownLinkForwardJobHandler();
        ExecutorService es = Executors.newFixedThreadPool(5);
        es.execute(new LogPoll());
        es.execute(new ForwardPoll());
        es.execute(new ForwardP2PIMPoll());
        es.execute(new ForwardGroupIMPoll());
        es.execute(new ForwardGroupSystemPoll());
    }
    private DownLinkLogJobHandler logJobHandler;
    private DownLinkForwardJobHandler forwardJobHandler;
    private static final ConcurrentLinkedQueue<DownLinkLogJob> DOWN_LINK_LOG_JOB_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<DownLinkForwardJob> DOWN_LINK_FORWARD_JOB_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<DownLinkForwardJob> DOWN_LINK_FORWARD_JOB_QUEUE_P2P_IM = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<DownLinkForwardJob> DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_IM = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<DownLinkForwardJob> DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_SYSTEM = new ConcurrentLinkedQueue<>();


    private void handleLogJob(DownLinkLogJob downLinkLogJob) {
        logJobHandler.handleDownLinkLogJob(downLinkLogJob);
    }

    private void handleForwardJob(DownLinkForwardJob downLinkForwardJob) {
        forwardJobHandler.handleDownLinkForwardJob(downLinkForwardJob);
    }

    public void addDownLinkLogJob(DownLinkLogJob downLinkLogJob) {
        DOWN_LINK_LOG_JOB_QUEUE.add(downLinkLogJob);
    }

    public void addDownLinkForwardJob(DownLinkForwardJob downLinkForwardJob) {
        Message<?> message = downLinkForwardJob.getMessage();
        Message.MessageType messageType = Message.getMessageType(message.getMessageType());
        Message.SessionType sessionType = Message.getSessionType(message.getSessionType());
        switch (messageType) {
            case MESSAGE_IM_GENERIC:
                switch (sessionType) {
                    case SESSION_P2P:
                        DOWN_LINK_FORWARD_JOB_QUEUE_P2P_IM.add(downLinkForwardJob);
                        break;
                    case SESSION_GROUP:
                        DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_IM.add(downLinkForwardJob);
                        break;
                    default:
                        break;
                }
                break;
            case MESSAGE_GROUP_SYSTEM:
                switch (sessionType) {
                    case SESSION_GROUP:
                        DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_SYSTEM.add(downLinkForwardJob);
                        break;
                    default:
                        break;
                }
                break;
            default:
                DOWN_LINK_FORWARD_JOB_QUEUE.add(downLinkForwardJob);
                break;
        }
    }

    public static DownLinkJobManager getInstance() {return DownLinkLogJobQueueFactory.DOWN_LINK_LOG_JOB_QUEUE;}

    private static class DownLinkLogJobQueueFactory {
        static final DownLinkJobManager DOWN_LINK_LOG_JOB_QUEUE = new DownLinkJobManager();
    }

    static class LogPoll implements Runnable {
        @Override
        public void run() {
            while(true) {
                final DownLinkLogJob downLinkLogJob = DOWN_LINK_LOG_JOB_QUEUE.poll();
                if(null == downLinkLogJob) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    getInstance().handleLogJob(downLinkLogJob);
                }
            }
        }
    }

    static class ForwardPoll implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DownLinkForwardJob downLinkForwardJob = DOWN_LINK_FORWARD_JOB_QUEUE.poll();
                if(null == downLinkForwardJob) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    getInstance().handleForwardJob(downLinkForwardJob);
                }
            }
        }
    }

    static class ForwardP2PIMPoll implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DownLinkForwardJob downLinkForwardJob = DOWN_LINK_FORWARD_JOB_QUEUE_P2P_IM.poll();
                if(null == downLinkForwardJob) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    getInstance().handleForwardJob(downLinkForwardJob);
                }
            }
        }
    }

    static class ForwardGroupIMPoll implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DownLinkForwardJob downLinkForwardJob = DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_IM.poll();
                if(null == downLinkForwardJob) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    getInstance().handleForwardJob(downLinkForwardJob);
                }
            }
        }
    }

    static class ForwardGroupSystemPoll implements Runnable {
        @Override
        public void run() {
            while (true) {
                final DownLinkForwardJob downLinkForwardJob = DOWN_LINK_FORWARD_JOB_QUEUE_GROUP_SYSTEM.poll();
                if(null == downLinkForwardJob) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    getInstance().handleForwardJob(downLinkForwardJob);
                }
            }
        }
    }

}
