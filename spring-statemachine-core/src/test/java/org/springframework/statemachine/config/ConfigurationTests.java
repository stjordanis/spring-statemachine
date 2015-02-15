/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class ConfigurationTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testStates() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(machine, notNullValue());
		TestAction testAction = context.getBean("testAction", TestAction.class);
		TestGuard testGuard = context.getBean("testGuard", TestGuard.class);
		assertThat(testAction, notNullValue());
		assertThat(testGuard, notNullValue());
	}


	@SuppressWarnings({ "unchecked" })
	@Test
	public void testEndState() throws Exception {
		context.register(Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(machine, notNullValue());
		Object endState = TestUtils.readField("endState", machine);
		assertThat(endState, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSimpleSubmachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(machine, notNullValue());
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3)
					.state(TestStates.S4);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.guard(testGuard())
					.action(testAction());
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.end(TestStates.SF)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withSubStates(TestStates.S1, null)
					.initial(TestStates.S1)
					.and()
				.withStates(TestStates.S1)
					.initial(TestStates.S1)
					.end(TestStates.SF)
					.states(EnumSet.allOf(TestStates.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withLocal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.and()
				.withInternal()
					.source(TestStates.S2)
					.event(TestEvents.E3);
		}

	}


}
