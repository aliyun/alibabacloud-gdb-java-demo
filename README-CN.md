# Alibaba Graph Database Demo for Java

[English](./README.md) | 简体中文

本文介绍如何基于Java编程环境连接和操作图数据库GDB。这是以常驻服务形式操作图数据库GDB的常用形式。

进行以下操作时，请确保图数据库GDB的实例与您的ECS虚拟机处于同一个Virtual Private Cloud(VPC)网络环境。

## 环境准备
1、添加具有Maven程序包的存储库

```
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
```

2、设置该存储库的版本号

```
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
```
3、下载并安装Maven

```
sudo yum install -y apache-maven
```

### 安装Java
1、 安装JDK 8.0

```
sudo yum install java-1.8.0-devel
```

2、 如果您的ECS实例上有多个Java版本，请将Java8设置为默认运行

```
sudo /usr/sbin/alternatives --config java
共有 4 个提供“java”的程序。
选项    命令
-----------------------------------------------
*+ 1           java-1.8.0-openjdk.x86_64 (/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.191.b12-1.el7_6.x86_64/jre/bin/java)
 2           java-1.8.0-openjdk.x86_64 (/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.191.b12-0.el7_5.x86_64-debug/jre/bin/java)
 3           java-1.7.0-openjdk.x86_64 (/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.191-2.6.15.4.el7_5.x86_64/jre/bin/java)
 4           /usr/lib/jvm/jre-1.6.0-openjdk.x86_64/bin/java
```

## 下载范例代码并执行
您可以直接clone本仓库的范例代码，然后运行并观察结果
1、下载代码
使用git命令直接clone代码到本地
```
git clone https://github.com/aliyun/alibabacloud-gdb-java-demo.git
```

2、编译并执行
使用maven编译仓库代码并执行
```
cd alibabacloud-gdb-java-demo
mvn exec:java -D exec.mainClass=com.alibaba.gdb.demo.GdbTest
```

## 手工编写Java客户端代码
1、 创建gdb-gremlin-test的目录
```
mkdir gdb-gremlin-test;
cd gdb-gremlin-test
```

2、 创建pom.xml文件，并写入如下内容

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.gdb.alibaba</groupId>
  <artifactId>GdbGremlinExample</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>GdbGremlinExample</name>
  <url>http://maven.apache.org</url>
  <dependencies>
    <dependency>
       <groupId>org.apache.tinkerpop</groupId>
       <artifactId>gremlin-driver</artifactId>
       <version>3.4.0</version>
    </dependency>
  </dependencies>
  <build>
     <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>2.0.2</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>1.3</version>
            <configuration>
                <mainClass>com.gdb.alibaba.Test</mainClass>
                <complianceLevel>1.8</complianceLevel>
            </configuration>
        </plugin>
    </plugins>
  </build>
</project>
```
4、创建目录并新建文件

```
mkdir -p src/main/java/com/gdb/alibaba/;

touch src/main/java/com/gdb/alibaba/Test.java
```

5、编写测试程序

创建目录

```
package com.gdb.alibaba;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
public class Test
{
    public static void main( String[] args )
    {
      try {
        if(args.length != 1) {
            System.out.println("gdb-remote.yaml path needed");
            return;
        }
        String yaml = args[0];
        Client client = Cluster.build(new File(yaml)).create().connect();
        client.init();

        String dsl = "g.addV(yourlabel).property(propertyKey, propertyValue)";
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("yourlabel","area");
        parameters.put("propertyKey","wherence");
        parameters.put("propertyValue","shenzheng");
        ResultSet results = client.submit(dsl,parameters);
        List<Result> result = results.all().join();

        result.forEach(p -> System.out.println(p.getObject()));
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
  }
}
```

6、 创建gdb-remote.yaml文件，该文件为Java客户端与GDB图数据库建立连接的配置文件
- 将`${your-gdb-endpoint}`改为您的图数据库GDB实例的域名
- 将`${username}`改为您的图数据库GDB实例的用户名
- 将`${password}`改为您的图数据库GDB实例的密码

```
hosts: [ ${your_gdb_endpoint} ]
port: 8182
username: ${username}
password: ${password}
serializer: {
  className: org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerV3d0,
  config: { serializeResultToString: true }
}
```

7、编译并执行

进入gdb-gremlin-test主目录，编译并执行java程序：

```
mvn compile exec:java  -Dexec.args="/root/apache-tinkerpop-gmlin-console-3.4.0/conf/gdb-remote.yaml"

v[ba8f60b7-0786-4014-a4e2-451f09b79878]
```

## 更多dsl样例

* 上面的示例是使用参数化的方式,通过dsl g.addV(yourlabel).property(propertyKey, propertyValue) 和参数map来添加点。
* 下面结合具体的图的点、边结构来进行更多dsl示例，图的点、边结构链接 http://tinkerpop.apache.org/docs/current/reference/#traversal
* 注意下面dsl需要改造成参数化的调用方式, 我们先用硬编码方式来进行范例讲解

```
DSL硬编码：
dsl = "user_defined_dsl"; 
//比如：g.addV('sand131_id_5_99').property(id,'sand131_id_5_99').property('name','sand131_name_5_99')
ResultSet results = client.submit(dsl);
                |
                |
                v
参数化脚本：
String dsl ="g.addV(vertex).property(id,vertex).property('name',vertex)";
Map<String, Object> parameters = new HashMap<>();
parameters.put("vertex",“sand131_id_5_99”); //填写dsl语句中的vertex参数
ResultSet results = client.submit(dsl, parameters,timeoutInMillis);
```

---

1. 删除指定label的点、边

```
g.E().hasLabel('gdb_sample_knows').drop()
g.E().hasLabel('gdb_sample_created').drop()
g.V().hasLabel('gdb_sample_person').drop()
g.V().hasLabel('gdb_sample_software').drop()
```

2. 添加顶点,为其设置id、property

```
g.addV('gdb_sample_person').property(id, 'gdb_sample_marko').property('age', 28).property('name', 'marko')
g.addV('gdb_sample_person').property(id, 'gdb_sample_vadas').property('age', 27).property('name', 'vadas')
g.addV('gdb_sample_person').property(id, 'gdb_sample_josh').property('age', 32).property('name', 'josh')
g.addV('gdb_sample_person').property(id, 'gdb_sample_peter').property('age', 35).property('name', 'peter')
g.addV('gdb_sample_software').property(id, 'gdb_sample_lop').property('lang', 'java').property('name', 'lop')
g.addV('gdb_sample_software').property(id, 'gdb_sample_ripple').property('lang', 'java').property('name', 'ripple')
```

3. 修改(或新增) age 属性
```
g.V('gdb_sample_marko').property('age', 29)
```

4. 建立关系,设置属性 weight

```
g.addE('gdb_sample_knows').from(V('gdb_sample_marko')).to(V('gdb_sample_vadas')).property('weight', 0.5f)
g.addE('gdb_sample_knows').from(V('gdb_sample_marko')).to(V('gdb_sample_josh')).property('weight', 1.0f)
g.addE('gdb_sample_created').from(V('gdb_sample_marko')).to(V('gdb_sample_lop')).property('weight', 0.4f)
g.addE('gdb_sample_created').from(V('gdb_sample_josh')).to(V('gdb_sample_lop')).property('weight', 0.4f)
g.addE('gdb_sample_created').from(V('gdb_sample_josh')).to(V('gdb_sample_ripple')).property('weight', 1.0f)
g.addE('gdb_sample_created').from(V('gdb_sample_peter')).to(V('gdb_sample_lop')).property('weight', 0.2f)
```

5. 查询所有点/指定label的点数量

```
g.V().count()
g.V().hasLabel('gdb_sample_person').count()
```

6. 查询指定条件的顶点 (>29岁的人, 按name降序排列所有人)

```
g.V().hasLabel('gdb_sample_person').has('age', gt(29))
g.V().hasLabel('gdb_sample_person').order().by('name', decr)
```

7. 关联查询(获取 marko 认识的人, marko认识的人created的software)

```
g.V('gdb_sample_marko').outE('gdb_sample_knows').inV().hasLabel('gdb_sample_person')
g.V('gdb_sample_marko').outE('gdb_sample_knows').inV().hasLabel('gdb_sample_person').outE('gdb_sample_created').inV().hasLabel('gdb_sample_software')
```

8. 删除关系、顶点

```
g.V('gdb_sample_marko').outE('gdb_sample_knows').where(inV().has(id, 'gdb_sample_josh')).drop()
g.V('gdb_sample_marko').drop()
```
---

这里您可以进行其他更多测试，详细的Gremlin查询语句可以参考[TinkerPop的Gremlin文档](http://tinkerpop.apache.org/docs/current/reference/)。

