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

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class JUnit4LoadRunner extends BlockJUnit4ClassRunner {

	int timeout = -1;
	int repetitions = 1;
	int numberOfUsers = 1;
	boolean stop = false;

	public JUnit4LoadRunner(Class<?> klass) throws InitializationError {
		super(klass);

		loadProperties();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	private void loadProperties() {
		String prop = System.getProperty("junit4loadrunner", null);
		if (prop != null) {
			String args[] = prop.split("\\s+");
			for (int i = 0; i < args.length; i++) {
				String name = args[i];
				String value = args[++i];
				if ("-repetitions".equalsIgnoreCase(name)) {
					setRepetitions(Integer.parseInt(value));
				} else if ("-timeout".equalsIgnoreCase(name)) {
					setTimeout(Integer.parseInt(value));
				} else if ("-nusers".equalsIgnoreCase(name)) {
					setNumberOfUsers(Integer.parseInt(value));
				}
			}
		}
	}
	
	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		notifier.addListener(new JUnit4LoadRunListener(this));
		for (int i = 0; i < repetitions; i++) {
			super.runChild(method, notifier);
			
			if (isStop()) {
				break;
			}
		}
		System.out.println("Run: iterations = " + repetitions);
	}

	class JUnit4LoadRunListener extends RunListener {
		JUnit4LoadRunner runner = null;

		public JUnit4LoadRunListener(JUnit4LoadRunner runner) {
			this.runner = runner;
		}

		public void testFailure(Failure failure) throws Exception {
			runner.setStop(true);
		}

		public void testAssumptionFailure(Failure failure) {
			runner.setStop(true);
		}

		public void testIgnored(Description description) throws Exception {
			runner.setStop(true);
		}
	}
}
