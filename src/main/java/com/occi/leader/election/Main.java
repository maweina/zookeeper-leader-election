package com.occi.leader.election;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class Main {
    public static String exec[];

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.err
                    .println("USAGE: Main zkConnectStr znode program [args ...]");
            System.exit(1);
        }

        String zookeeperConnectionString = args[0];
        String znode = args[1];
        exec = new String[args.length - 2];
        System.arraycopy(args, 2, exec, 0, exec.length);

        // these are reasonable arguments for the ExponentialBackoffRetry. The first
        // retry will wait 1 second - the second will wait up to 2 seconds - the
        // third will wait up to 4 seconds.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

        LeaderSelectorListener listener = new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework client) throws Exception {
                // this callback will get called when you are the leader
                // do whatever leader work you need to and only exit
                // this method when you want to relinquish leadership
                System.out.println("I'm the leader now ");
                try {
                    System.out.println("Starting child");
                    Process child = Runtime.getRuntime().exec(exec);
                    // cause this process to stop until process child is terminated
                    child.waitFor();
                    System.out.println("Child is down, exit");
                    System.exit(1);
                } catch (Exception  e) {
                    e.printStackTrace();
                }
            }
        };

        client.start();

        LeaderSelector selector = new LeaderSelector(client, znode, listener);
        selector.autoRequeue();  // not required, but this is behavior that you will probably expect
        selector.start();

        while(true) {
            Thread.sleep(1000);
        }
    }
}
