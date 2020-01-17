/*
 * (C)  2019-present Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */
package com.alibaba.gdb.demo;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : Liu Jianping
 * @date : 2020/1/17
 */

public class GdbPerfTest {
    private static final int TOTAL_COUNT = 100000;

    private static Client client;
    private static int threadNum = 0;
    private static AtomicLong total = new AtomicLong(0);
    private static AtomicBoolean finish = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("args errors: [file_name, thread_num]");
                System.exit(-1);
            }
            String yaml = args[0];
            threadNum = Integer.parseInt(args[1]);

            client = Cluster.build(new File(yaml)).create().connect();
            client.init();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        List<Thread> threads = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < threadNum; i++) {
            final int fin = i;
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    long start_idx = fin * TOTAL_COUNT;
                    long count = 0;

                    while (!finish.get()) {
                        try {
                            String id = String.valueOf(start_idx + count++);
                            String label = String.valueOf(fin + random.nextInt(200));
                            String propKey = String.valueOf(random.nextInt(50));
                            String propValue = String.valueOf(random.nextDouble());
                            add(client, label, id, propKey, propValue);
                            total.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
        }

        threads.forEach(Thread::start);

   		double sleepTime = 0;
		while (total.get() < TOTAL_COUNT) {
			try {
				Thread.sleep(1000);
				sleepTime += 1;
				System.out.println("succ " + total.get() / sleepTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		finish.set(true);
		threads.forEach(t-> {
		    try {
                t.join();
            } catch (Exception e) {
		        e.printStackTrace();
            }
        });

		System.out.println("Bye...");
    }

    private static void add(Client client, String label, String id, String propKey, String propValue) {
		String dsl = "g.addV(GDB__label).property(id, GDB__id).property(P__Key, P__Val)";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("GDB__id", id);
		parameters.put("GDB__label", label);
		parameters.put("P__Key", propKey);
		parameters.put("P__Val", propValue);

		ResultSet results = client.submit(dsl, parameters);
		List<Result> result = results.all().join();
        //result.forEach(System.out::println);
	}

}
