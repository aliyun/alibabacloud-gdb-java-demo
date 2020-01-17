/*
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
package com.alibaba.gdb.demo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;

/**
 * @author jerrywang
 */
public class GdbTest {

	public static void main(String[] args) {
		try {
			String yaml = Thread.currentThread().getContextClassLoader().getResource("gdb.yaml").getFile();
			if (args.length > 0) {
				yaml = args[0];
			}

			Client client = Cluster.build(new File(yaml)).create().connect();
			client.init();

			String dsl = "g.addV(yourLabel).property(id, yourId).property(propertyKey, propertyValue)";
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("yourLabel", "area");
			parameters.put("yourId", "loc1125");
			parameters.put("propertyKey", "wherence");
			parameters.put("propertyValue", "shenzheng");
			ResultSet results = client.submit(dsl, parameters);
			List<Result> result = results.all().join();
			result.forEach(p -> System.out.println(p));
			client.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
