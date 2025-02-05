# 总览

这是 [Survey Planet](https://github.com/XuanxuanRao/Survey-Planet) 项目的后端仓库。

Survey Planet是一个基于SpringBoot开发的问卷网站，后端使用的技术栈有 Mybatis，WebSocket。

- 支持创建问卷，发布问卷，填写问卷，分析问卷数据并导出这一套常见的问卷收集流程
- 考试问卷支持代码题，创建者可以设置测试点、允许使用的编程语言、时间空间限制等OJ信息，系统对提交代码进行评测得到分数
- 支持通过邮件、站内信这两种方式发布问卷
- 可以关注问卷，在收到信答卷时通过邮件、站内信发送提醒

| 网址                                         | 接口文档                                                     |
| -------------------------------------------- | ------------------------------------------------------------ |
| http://39.105.96.112<br />(可能需要关闭VPN访问) | 【WPS云文档】接口文档<br />https://365.kdocs.cn/l/cthq7lJnus30 |



# 项目结构

项目按照功能可以分为三个主要模块：

- 消息模块：基于WebSocket开发，支持实时消息推送。在问卷收到答卷或收到问卷填写邀请

- 判题模块：由于引入了支持自动评测的代码题，需要单独开发模块实现OJ功能，主要负责调度判题任务，发出请求调用沙盒运行提交的代码得到结果
- 问卷模块：实现常规的问卷功能，包括创建/修改问卷，填写问卷，数据分析，结果导出等

> 判题模块参考了 https://github.com/HimitZH/HOJ 中 JudgeServer 的代码

相比一个成熟可用的问卷网站，缺少下面功能待开发：

- [ ] 设置问卷回答逻辑
- [ ] 在问卷发布收到回答后支持修改问卷
- [ ] 更精细的问卷填写权限设置
- [ ] 实现通讯录&群组功能

----

项目的源代码在 survey-planet-common, survey-planet-pojo, survey-planet-server, survey-planet-parent 这四个目录下，其中 survey-planet-parent 没有任何实际功能，只是方便项目构建。

- common：管理项目通用工具类，方法

  - Result：封装请求响应结果
  - constant：全局使用的常量
  - context：用户信息上下文
  - enumeration：枚举类
  - exception：自定义业务异常信息
  - properties：存储配置信息
  - utils：通用工具方法

- pojo：普通java类，不包含任何业务逻辑

  - entity：与数据库直接关联的实体类
  - dto：传输数据的实体类
  - vo：封装响应数据

- server：业务具体实现

  - annotation：注解

  - aspect：切面类，用于实现切面编程

  - config：加载系统配置信息

  - controller：接收请求返回响应结果

  - converter：转化请求参数

  - handler：进行类型转化和异常处理

  - interceptor：登录拦截器

  - judge：实现OJ系统逻辑

  - mapper：进行数据库操作

  - service：请求处理核心逻辑

  - socket：WebSocket实现

  - task：系统定时任务

    

db 目录下的 script.sql 是创建数据库脚本，db.md 是数据库设计的详细文档。



# 数据库设计

使用 Mysql 数据库，项目中使用了一处触发器来实现收到新答卷时自动更新问卷信息，详细的设计文档在 db 目录下。



# 项目部署

## 1. 启动项目

1. 将 survey-planet-common, survey-planet-pojo, survey-planet-server, survey-planet-parent 四个 module 导入项目中

2. 运行 db/script.sql 完成数据库创建，然后修改 survey-planet-server 下的 application-dev.yml 或 application-prod.yml 数据库配置信息（选哪一个取决于 application.yml，仓库中的是 application-prod.yml） 

   ```yaml
   # 数据库配置
   datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver # mysql 驱动
       host: # 替换为你的数据库 host
       port: # 替换为你的数据库 port
       database: # 替换为你的数据库名
       username: # 替换为你的用户名
       password: # 替换为你的密码
   ```

3. 配置对象存储服务（用于实现文件存取操作），Survey Planet使用的是阿里云的OSS，然后修改上面配置文件中的 OSS 信息

   ```yaml
   aliyun:
       # 阿里云OSS配置，按照官网的示例配置即可
       oss:
           endpoint: 
           accessKeyId: 
           accessKeySecret: 
           bucketName: 
   ```

4. 配置用于发送信息的邮箱信息，如果你要部署在国内服务器上，推荐使用网易邮箱，谷歌邮箱可能会由于网络问题出现连接超时。需要在设置中开启邮箱的 IMAP/POP3，生成授权密码作为配置信息中的password（如果是谷歌邮箱需要在https://myaccount.google.com/security生成你的APP PASSWORD），然后完成修改配置文件中的邮箱信息

   ```yaml
   # 邮箱配置
   mail:
       host: smtp.163.com 				# 如果是谷歌邮箱则是 smtp.gmail.com
       port: 465						# 465 端口在服务器上不会被禁用
       username: surveyplanet@163.com	# 你的邮箱
       password: AD6VtDxH3JmDnXu5		# 生成的password(不是你的账号密码)
       protocol: smtp
   ```

	> 截止到这一步我们还没有进行OJ部分的配置，这会在第二节介绍

5. 现在通过 Maven 下载依赖，就可以成功运行一个不带有OJ功能的问卷系统了。当然为了与前端交互你可能需要配置跨域信息，按照前端信息修改 `survey-planet-server/src/main/java/org/example/config/WebConfiguration.java` 文件的下面内容

   ```java
   @Override
   public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
           // 更改为你的前端url, 允许跨域
           .allowedOrigins("http://localhost:3000", "http://59.110.163.198")
           .allowedMethods("GET", "POST", "PUT", "DELETE")
           .allowedHeaders("*")
           .allowCredentials(true);
   }
   ```

## 2. 服务器部署

1. 如果你的服务器没有任何Java项目的运行环境，你需要先安装 **JDK17** 并添加到环境变量
2. 通过 Xftp 等工具将打包好的 jar 包上传到服务器上，由于打包时 spring 已经在 jar 包中内置了一个 Tomcat 服务器，只要直接通过 `java -jar` 运行，这样就完成了在服务器上的部署
3. 最后，需要在服务器为后端端口设置安全组和防火墙，Survey Planet 默认是在 8088 端口启动的

----

下面的一些建议可能会对你管理项目有所帮助：

- 端口占用问题

  - **查找占用该端口的进程**： 使用 `lsof` 命令查找占用特定端口的进程：

    ```bash
    lsof -i :[端口号]
    ```

    替换 `[端口号]` 为你要检查的实际端口号。命令输出中会包含进程的 PID。

  - **终止进程**： 使用 `kill` 命令终止该进程：

    ```bash
    kill [PID]
    ```

    替换 `[PID]` 为你在第一步中找到的实际进程ID。

    如果进程没有响应，可以使用 `-9` 强制终止：

    ```bash
    kill -9 [PID]
    ```

- 使用 screen 等工具在下线后后台运行服务器

  `screen` 是会话管理工具，允许在创建一个会话后，即使关闭 SSH 连接，该会话中的进程依旧运行。

  - **安装 `screen`：**

    ```bash
    yum install screen
    ```

  - **使用 `screen`：**

    ```bash
    screen -S server
    java -jar SurveyPlanet.jar
    ```

    这样程序会在 `screen` 会话中运行，即使断开 SSH 连接，程序也会继续运行。断开前可以按 `Ctrl+A` 然后按 `D` 来离开该会话，而不终止进程。

    要重新连接到该会话，使用：

    ```bash
    screen -r server
    ```

  同理，后面提到的评测沙盒 `go-judge` 也可以使用 `screen` 后台运行。你也可以使用 tmux 替代 screen

## 3. 沙盒部署

SurveyPlanet 的代码评测功能依赖于开源的安全沙盒 [go-judge](https://github.com/criyle/go-judge/tree/master)，在仓库中下载 release 版本 [go-judge_1.8.5_linux_386.tar.gz](https://github.com/criyle/go-judge/releases/download/v1.8.5/go-judge_1.8.5_linux_386.tar.gz)，在后端的同一台服务器上使用命令解压该文件 `tar -zxvf go-judge_1.8.5_linux_386.tar.gz`，得到两个文件：可执行程序 `go-judge` 和配置文件 `mount.yaml`。

我使用的Linux版本是3.10.1，直接运行 `./go-judge` 会出现下面错误：

```
prefork environment failed  container: failed to start container fork/exec /proc/self/exe: invalid argument
```

为了解决这个问题，需要开启用户命名空间（user namespace），步骤如下：

- 首先在 `/etc/sysctl.conf` 文件末尾新增下面内容以开启用户命名空间：

  ```
  # 开启内核命名空间
  user.max_user_namespaces=10000
  ```

- 保存后执行下面指令使得上一步配置生效

  ```shell
  sysctl -p
  ```

- 执行下面指令以验证用户命名空间是否成功开启，如果成功了会输出 10000

  ```shell
  cat /proc/sys/user/max_user_namespaces
  ```

完成上述步骤后就可以运行 `./go-judge` 来启动沙盒了，默认在本地启动，端口是**5050**（项目的配置文件中的端口就是5050）。

在上线后让 go-judge 运行在服务器的localhost就可以了，如果本地开发过程中需要调试，可以在启动时加上 `-http-addr=0.0.0.0:5051` 来使得任何ip地址都可以访问（同时服务器要开放5051端口）。

---

**更推荐的方式：使用Docker容器部署**

下面的Dockerfile构建了一个包含C,C++,Java(JDK8)的go-judge沙盒，把下载得到的两个文件放在 go-judge 文件夹中，将 go-judeg 文件夹和Dockerfile放在同一个目录下，在该路径下使用 `docker build -t go-judge .`  命令构建镜像 `go-judge`。

```dockerfile
# 使用 Ubuntu 20.04 作为基础镜像（默认支持 OpenJDK 8）
FROM ubuntu:20.04

# 设置工作目录为 /go-judge
WORKDIR /go-judge

COPY ./go-judge /go-judge

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y \
    build-essential \ 
    gcc \             
    g++ \              
    openjdk-8-jdk \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 设置 Java 8 环境变量
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

# 设置容器启动时运行的命令
CMD ["./go-judge"]
```

完成镜像构建后使用下面命令启动容器，为了安全不对外进行端口映射，容器间通过连接到相同网络进行调用。

```shell
docker run -d \
 --name go-judge \
 --privileged \
 --network survey-planet \
 go-judge
```

# 其他

这是我第一个从零开始，从设计功能到代码实现独立完成的项目，过程中经过了多次迭代，整体设计上并不是特别优雅，并且在验证码存储上并没有使用 Redis 而是直接放在了数据库中，存在一些瑕疵，后续有机会可能会继续开发。如果你想要参考这个项目或是在使用过程中遇到了任何问题，可以通过下面方式与我联系：

- 邮箱：chenxuanrao06@gmail.com
- QQ：2987077846

