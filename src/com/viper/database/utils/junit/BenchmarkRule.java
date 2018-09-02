/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 *
 * -----------------------------------------------------------------------------
 */

package com.viper.database.utils.junit;

import java.util.concurrent.CountDownLatch;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

public class BenchmarkRule implements MethodRule, TestRule {

    private TestRule rule;

    int repetitions = 1;
    int warmup = 0;
    int timeout = -1;
    int nthreads = 1;

    public BenchmarkRule() {
        String prop = System.getProperty("benchmarks", null);
        if (prop != null) {
            String args[] = prop.split("\\s+");
            for (int i = 0; i < args.length; i++) {
                String name = args[i];
                String value = args[++i];
                if ("-repetitions".equalsIgnoreCase(name)) {
                    repetitions = Integer.parseInt(value);
                } else if ("-warmup".equalsIgnoreCase(name)) {
                    warmup = Integer.parseInt(value);
                } else if ("-timeout".equalsIgnoreCase(name)) {
                    timeout = Integer.parseInt(value);
                } else if ("-nthreads".equalsIgnoreCase(name)) {
                    nthreads = Integer.parseInt(value);
                }
            }
        }

        // RuntimeAnnotation.addAnnotationToMethod(String className, String methodName, Class
        // annotationClass, String... keyValue)BenchmarkHistoryChart

        // this(dbFileName, getDefaultChartsDir(), getDefaultCustomKey())
        H2Consumer consumer = new H2Consumer();
        rule = new com.carrotsearch.junitbenchmarks.BenchmarkRule(consumer);
    }

    public BenchmarkRule(int repetitions, int warmup, int timeout, int nthreads) {
        this();

        this.repetitions = repetitions;
        this.warmup = warmup;
        this.timeout = timeout;
        this.nthreads = nthreads;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getWarmup() {
        return warmup;
    }

    public void setWarmup(int warmup) {
        this.warmup = warmup;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getNthreads() {
        return nthreads;
    }

    public void setNthreads(int nthreads) {
        this.nthreads = nthreads;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
        final Statement statement = rule.apply(base, null);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (int i = 0; i < repetitions; i++) {
                    statement.evaluate();
                }
            }
        };
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        final Statement statement = rule.apply(base, description);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (int i = 0; i < repetitions; i++) {
                    statement.evaluate();
                }
            }
        };
    }

    // addAnnotationToMethod(String className, String methodName, Class annotationClass, String...
    // keyValue)

    public String toString() {
        return "Repetitions(" + repetitions + ")";
    }

    public Statement apply1(Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (nthreads <= 1)
                    frameworkMethod.invokeExplosively(o);
                else {
                    final String name = frameworkMethod.getName();
                    final Thread[] threads = new Thread[nthreads];
                    final CountDownLatch go = new CountDownLatch(1);
                    final CountDownLatch finished = new CountDownLatch(threads.length);
                    for (int i = 0; i < threads.length; i++) {
                        threads[i] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    go.await();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                try {
                                    frameworkMethod.invokeExplosively(o);
                                } catch (Throwable throwable) {
                                    if (throwable instanceof RuntimeException)
                                        throw (RuntimeException) throwable;
                                    if (throwable instanceof Error)
                                        throw (Error) throwable;
                                    RuntimeException r = new RuntimeException(throwable.getMessage(), throwable);
                                    r.setStackTrace(throwable.getStackTrace());
                                    throw r;
                                } finally {
                                    finished.countDown();
                                }
                            }
                        }, name + "-Thread-" + i);
                        threads[i].start();
                    }
                    go.countDown();
                    finished.await();
                }
            }
        };
    }
}
