# E-R 图

![](img\ER_diagram.png)



# 数据库基本表定义

## 1. 用户表 user

| 属性名      | 含义         | 数据类型      | 备注             |
| ----------- | ------------ | ------------- | ---------------- |
| uid         | 用户id       | BIGINT        | PRIMARY KEY      |
| username    | 用户名       | varchar(64)   | UNIQUE, NOT NULL |
| email       | 邮箱         | varchar(256)  | UNIQUE, NOT NULL |
| password    | 密码         | varchar(32)   | NOT NULL         |
| avatar      | 用户头像url  | varchar(256)  | NOT NULL         |
| description | 自我介绍     | varchar(1023) |                  |
| create_time | 账户创建时间 | DATETIME      | NOT NULL         |
| update_time | 账户更新时间 | DATETIME      | NOT NULL         |

## 2. 问卷表 survey

| 属性名            | 含义               | 数据类型                  | 备注        |
| ----------------- | ------------------ | ------------------------- | ----------- |
| sid               | 问卷id             | BIGINT                    | PRIMARY KEY |
| uid               | 问卷创建用户id     | BIGINT                    | FOREIGN KEY |
| title             | 问卷名             | varchar(64)               | NOT NULL    |
| description       | 问卷描述           | TEXT                      |             |
| type              | 问卷类型           | enum(normal, exam)        | NOT NULL    |
| fill_num          | 填写人数           | INT                       | NOT NULL    |
| time_limit        | 答卷时间限制       | INT                       |             |
| show_answer       | 提交后是否展示答案 | TINYINT(1)                | NOT NULL    |
| notification_mode | 问卷通知模式       | INT                       | NOT NULL    |
| state             | 问卷状态           | enum(delete, open, close) | NOT NULL    |
| open_time         | 上次发布时间       | DATETIME                  |             |
| create_time       | 问卷创建时间       | DATETIME                  | NOT NULL    |
| update_time       | 问卷更新时间       | DATETIME                  | NOT NULL    |

## 3. 题目表 question

| 属性名      | 含义           | 数据类型                                                     | 备注        |
| ----------- | -------------- | ------------------------------------------------------------ | ----------- |
| qid         | 题目id         | BIGINT                                                       | PRIMARY KEY |
| sid         | 题目所属问卷id | BIGINT                                                       | FOREIGN KEY |
| title       | 题目名         | varchar(64)                                                  | NOT NULL    |
| description | 题目描述       | TEXT                                                         |             |
| type        | 题目类型       | enum(single_choice, multiple_choice, fill_blank, file, code) | NOT NULL    |
| required    | 是否必答       | TINYINT(1)                                                   | NOT NULL    |
| score       | 题目分值       | INT                                                          |             |

## 4. 选择题表 choice_question

| 属性名  | 含义     | 数据类型 | 备注                     |
| ------- | -------- | -------- | ------------------------ |
| qid     | 题目id   | BIGINT   | PRIMARY KEY, FOREIGN KEY |
| options | 题目选项 | JSON     | NOT NULL                 |
| answer  | 题目答案 | JSON     |                          |

## 5. 文件题表 fill_question

| 属性名        | 含义                       | 数据类型 | 备注                     |
| ------------- | -------------------------- | -------- | ------------------------ |
| qid           | 题目id                     | BIGINT   | PRIMARY KEY, FOREIGN KEY |
| max_file_size | 最大允许上传文件大小（MB） | INT      | NOT NULL                 |

## 6. 填空题表 fill_blank_question

| 属性名 | 含义     | 数据类型 | 备注                     |
| ------ | -------- | -------- | ------------------------ |
| qid    | 题目id   | BIGINT   | PRIMARY KEY, FOREIGN KEY |
| answer | 题目答案 | JSON     |                          |

## 7. 代码题表 code_question

| 属性名              | 含义                  | 数据类型   | 备注                     |
| ------------------- | --------------------- | ---------- | ------------------------ |
| qid                 | 题目id                | BIGINT     | PRIMARY KEY, FOREIGN KEY |
| input_file_urls     | 所有输入数据文件的url | JSON       | NOT NULL                 |
| output_file_urls    | 所有输出数据文件的url | JSON       | NOT NULL                 |
| time_limit          | 时间限制（ms）        | INT        | NOT NULL                 |
| memory_limit        | 内存限制（MB）        | INT        | NOT NULL                 |
| stack_limit         | 栈空间限制（MB）      | INT        | NOT NULL                 |
| is_remove_end_blank | 是否默认去除行末空格  | TINYINT(1) | NOT NULL                 |
| languages           | 允许使用的编程语言    | JSON       | NOT NULL                 |

## 8. 回答记录表 response_record

| 属性名      | 含义               | 数据类型   | 备注        |
| ----------- | ------------------ | ---------- | ----------- |
| rid         | 回答记录id         | BIGINT     | PRIMARY KEY |
| sid         | 回答的问卷的id     | BIGINT     | FOREIGN KEY |
| uid         | 进行作答用户的id   | BIGINT     | FOREIGN KEY |
| grade       | 成绩               | INT        |             |
| finished    | 系统是否处理完回答 | TINYINT(1) | NOT NULL    |
| valid       | 回答是否有效       | TINYINT(1) | NOT NULL    |
| create_time | 回答创建时间       | DATETIME   | NOT NULL    |
| update_time | 回答更新时间       | DATETIME   | NOT NULL    |

## 9. 回答项表 response_item

| 属性名      | 含义             | 数据类型 | 备注        |
| ----------- | ---------------- | -------- | ----------- |
| submit_id   | 题目作答id       | BIGINT   | PRIMARY KEY |
| rid         | 所属回答记录的id | BIGINT   | FOREIGN KEY |
| qid         | 回答题目的id     | BIGINT   | FOREIGN KEY |
| grade       | 成绩             | INT      |             |
| content     | 作答内容         | JSON     | NOT NULL    |
| create_time | 回答创建时间     | DATETIME | NOT NULL    |
| update_time | 回答更新时间     | DATETIME |             |

## 10. 评测结果表 judge

| 属性名        | 含义               | 数据类型                | 备注                     |
| ------------- | ------------------ | ----------------------- | ------------------------ |
| submit_id     | 回答记录id         | BIGINT                  | PRIMARY KEY, FOREIGN KEY |
| qid           | 回答的题目的id     | BIGINT                  | FOREIGN KEY              |
| uid           | 提交用户的id       | BIGINT                  | FOREIGN KEY              |
| grade         | 成绩               | INT                     | NOT NULL                 |
| status        | 评测状态           | INT                     | NOT NULL                 |
| code_content  | 用户提交代码内容   | varchar(4096)           | NOT NULL                 |
| language      | 用户选择的编程语言 | enum(C,C++,Java,Python) | NOT NULL                 |
| error_message | 错误信息           | TEXT                    |                          |
| create_time   | 评测创建时间       | DATETIME                | NOT NULL                 |
| update_time   | 评测更新时间       | DATETIME                | NOT NULL                 |

## 11. 测试用例表 judge_case

| 属性名          | 含义                 | 数据类型     | 备注                     |
| --------------- | -------------------- | ------------ | ------------------------ |
| submit_id       | 回答记录id           | BIGINT       | PRIMARY KEY, FOREIGN KEY |
| qid             | 回答的题目的id       | BIGINT       | FOREIGN KEY              |
| case_id         | 测试用例编号         | BIGINT       | PRIMARY KEY              |
| status          | 评测状态             | INT          | NOT NULL                 |
| time            | 运行时间（ms）       | INT          | NOT NULL                 |
| memory          | 空间消耗（MB）       | INT          | NOT NULL                 |
| input_data_url  | 输入数据url          | varchar(256) | NOT NULL                 |
| output_data_url | 输入数据url          | varchar(256) | NOT NULL                 |
| user_output     | 用户输出             | TEXT         | NOT NULL                 |
| create_time     | 评测用例运行完成时间 | DATETIME     | NOT NULL                 |

## 12. 验证码表 verification_code

| 属性名      | 含义           | 数据类型     | 备注        |
| ----------- | -------------- | ------------ | ----------- |
| vid         | 验证码id       | BIGINT       | PRIMARY KEY |
| email       | 对应的生效邮箱 | varchar(256) | NOT NULL    |
| code        | 验证码内容     | char(10)     | NOT NULL    |
| expire_time | 过期时间       | DATETIME     | NOT NULL    |

## 13. 消息表 message

| 属性名             | 含义         | 数据类型   | 备注        |
| ------------------ | ------------ | ---------- | ----------- |
| mid                | 消息id       | BIGINT     | PRIMARY KEY |
| receiver_uid       | 接收用户id   | BIGINT     | FOREIGN KEY |
| sender_uid         | 发送用户id   | BIGINT     | FOREIGN KEY |
| type               | 消息类型     | INT        | NOT NULL    |
| sid                | 关联的问卷id | BIGINT     | FOREIGN KEY |
| invitation_message | 邀请信息     | TEXT       |             |
| is_read            | 是否已读     | TINYINT(1) | NOT NULL    |
| content            | 消息内容     | TEXT       |             |
| create_time        | 消息创建时间 | DATETIME   | NOT NULL    |
| update_time        | 消息更新时间 | DATETIME   | NOT NULL    |

## 14. Controller日志表 log_entry

| 属性名       | 含义           | 数据类型      | 备注        |
| ------------ | -------------- | ------------- | ----------- |
| lid          | 日志id         | BIGINT        | PRIMARY KEY |
| args         | 参数           | varchar(1023) |             |
| class_method | 类方法         | varchar(255)  |             |
| start_time   | 开始时间       | DATETIME      | NOT NULL    |
| end_time     | 结束时间       | DATETIME      |             |
| take_time    | 操作耗时（ms） | BIGINT        |             |
| user_agent   | 请求来源客户端 | varchar(255)  | NOT NULL    |
| ip           | 请求来源ip地址 | varchar(127)  | NOT NULL    |
| http_method  | http请求类型   | varchar(15)   | NOT NULL    |
| url          | 请求url        | varchar(127)  | NOT NULL    |
| uri          | 请求uri        | varchar(63)   | NOT NULL    |

## 15. 文件记录表 file

| 属性名      | 含义           | 数据类型     | 备注             |
| ----------- | -------------- | ------------ | ---------------- |
| fid         | 文件id         | BIGINT       | PRIMARY KEY      |
| name        | 文件名         | varchar(255) | NOT NULL         |
| path        | 文件访问路径   | varchar(511) | NOT NULL, UNIQUE |
| uid         | 上传文件用户id | BIGINT       | FOREIGN KEY      |
| create_time | 文件上传时间   | DATETIME     |                  |