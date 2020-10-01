package com.taiyiyun.passport.service.queue;

import com.taiyiyun.passport.exception.DefinedError;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by okdos on 2017/7/8.
 *
 **/
public class Consumer {

    ConcurrentLinkedQueue<ITask> taskList = new ConcurrentLinkedQueue<>();

    public Consumer(){
        new Thread(new Poll(this)).start();
    }

    public void offerTask(ITask task){
        taskList.offer(task);
    }

    /**
	 * 操作task
	 * 
	 * @throws DefinedError
	 */
	private void run(ITask task){
		try {
			task.run();
		} catch (DefinedError e) {
			e.printStackTrace();
		}
	}

    static class Poll implements Runnable{
        private Consumer consumer;
        Poll(Consumer consumer){
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while(true){
                ITask task = this.consumer.taskList.poll();
                if(task == null){
                    try{
                        Thread.sleep(100);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                } else {
                    this.consumer.run(task);
                }
            }
        }
    }

}
