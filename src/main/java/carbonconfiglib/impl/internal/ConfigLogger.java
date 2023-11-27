package carbonconfiglib.impl.internal;

import java.util.Objects;

import org.apache.logging.log4j.Logger;

import carbonconfiglib.api.ILogger;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ConfigLogger implements ILogger
{
	Logger logger;
	
	public ConfigLogger(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void debug(String s) { logger.debug(s); }
	@Override
	public void debug(String s, Object o) { logger.debug(s, o); }
	@Override
	public void debug(Object o) { logger.debug(Objects.toString(o)); }
	@Override
	public void info(String s) { logger.info(s); }
	@Override
	public void info(String s, Object o) { logger.info(s, o); }
	@Override
	public void info(Object o) { logger.info(Objects.toString(o)); }
	@Override
	public void warn(String s) { logger.warn(s); }
	@Override
	public void warn(String s, Object o) { logger.warn(s, o); }
	@Override
	public void warn(Object o) { logger.warn(Objects.toString(o)); } 
	@Override
	public void error(String s) { logger.error(s); } 
	@Override
	public void error(String s, Object o) { logger.error(s, o); } 
	@Override
	public void error(Object o) { logger.error(Objects.toString(o)); } 
}
