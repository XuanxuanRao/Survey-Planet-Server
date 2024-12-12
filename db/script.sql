create table file
(
    fid         bigint auto_increment
        primary key,
    name        varchar(255) not null comment '文件名',
    path        varchar(511) not null comment '文件url',
    size        bigint       not null comment '文件大小(B)',
    uid         bigint       not null,
    create_time datetime     not null comment '创建时间'
)
    comment '文件表';

create table log_entry
(
    id           bigint auto_increment
        primary key,
    args         varchar(1023) null,
    class_method varchar(255)  null,
    end_time     datetime(6)   null,
    http_method  varchar(15)   null,
    ip           varchar(127)  null,
    method_name  varchar(255)  null,
    result       varchar(8191) null,
    start_time   datetime(6)   null,
    take_time    bigint        null,
    uri          varchar(63)   null,
    url          varchar(127)  null,
    user_agent   varchar(255)  null
);

create table message
(
    mid                bigint auto_increment comment '消息ID'
        primary key,
    receiver_uid       bigint               null comment '接收者ID',
    type               int                  not null comment '消息类型',
    create_time        datetime             not null comment '创建时间',
    update_time        datetime             not null comment '更新时间',
    is_read            tinyint(1) default 0 null comment '是否已读',
    content            text                 null comment '系统消息内容(System)',
    sid                bigint               null comment '问卷ID(Invite, NewSubmission)',
    invitation_message text                 null comment '邀请消息内容(Invite)',
    sender_uid         bigint               null comment '发送者ID(Invite)',
    rid                bigint               null comment '提交ID(NewSubmission)'
)
    comment '站内消息表';

create table user
(
    uid         bigint auto_increment comment '用户id'
        primary key,
    username    varchar(64)                                                                                         not null,
    email       varchar(256)                                                                                        not null,
    password    varchar(32)                                                                                         not null,
    avatar      varchar(256) default 'https://survey-planet-test.oss-cn-beijing.aliyuncs.com/avatar_xiangliyao.jpg' null comment '头像url',
    create_time datetime                                                                                            not null comment '账号创建时间',
    update_time datetime     default CURRENT_TIMESTAMP                                                              not null,
    description varchar(1023)                                                                                       null comment '个人介绍',
    constraint email
        unique (email),
    constraint username
        unique (username)
)
    comment '用户表';

create table survey
(
    sid               bigint auto_increment comment '问卷id'
        primary key,
    uid               bigint                                           not null comment '创建用户id',
    title             varchar(64)                                      not null comment '问卷标题',
    description       text                                             null comment '问卷描述',
    type              enum ('normal', 'exam')                          not null comment '问卷类型',
    state             enum ('delete', 'open', 'close') default 'close' null comment '问卷状态',
    create_time       datetime                                         not null comment '问卷创建时间',
    update_time       datetime                                         not null comment '问卷更新时间',
    open_time         datetime                                         null comment '问卷上次开放时间',
    fill_num          int unsigned                     default '0'     null comment '填写人数',
    time_limit        int unsigned                     default '0'     null comment '回答时间限制(min)',
    show_answer       tinyint(1)                       default 1       not null comment '是否在提交后向答卷人展示答案',
    notification_mode int                              default 0       not null comment '问卷通知模式（2bit-mask）',
    constraint survey_ibfk_1
        foreign key (uid) references user (uid)
)
    comment '问卷表';

create table question
(
    qid         bigint auto_increment comment '问题id'
        primary key,
    sid         bigint                                                                  not null,
    title       varchar(64)                                                             not null comment '问题标题',
    description text                                                                    null comment '问题描述',
    type        enum ('single_choice', 'multiple_choice', 'fill_blank', 'file', 'code') not null comment '问题类型',
    required    tinyint(1)                                                              not null comment '是否必填',
    score       int unsigned                                                            null comment '问题分值',
    constraint question_ibfk_1
        foreign key (sid) references survey (sid)
)
    comment '问题表(父表)';

create table code_question
(
    qid                 bigint               not null comment '问题id'
        primary key,
    input_file_urls     json                 not null comment '输入',
    output_file_urls    json                 not null comment '输出',
    time_limit          int unsigned         not null comment '时间限制(ms)',
    memory_limit        int unsigned         not null comment '内存限制',
    stack_limit         int unsigned         not null comment '栈空间限制',
    is_remove_end_blank tinyint(1) default 1 null comment '默认去除每行末尾空白符',
    languages           json                 not null comment '可用的语言',
    constraint code_question_ibfk_1
        foreign key (qid) references question (qid)
)
    comment '代码评测题表(子表)';

create table file_question
(
    qid           bigint       not null comment '问题id'
        primary key,
    max_file_size int unsigned not null comment '文件大小限制(MB)',
    constraint file_question_ibfk_1
        foreign key (qid) references question (qid)
)
    comment '文件问题表(子表)';

create table fill_blank_question
(
    qid    bigint not null comment '问题id'
        primary key,
    answer json   null comment '答案',
    constraint fill_blank_question_ibfk_1
        foreign key (qid) references question (qid)
)
    comment '填空问题表(子表)';

create table multiple_choice_question
(
    qid     bigint not null comment '问题id'
        primary key,
    options json   not null comment '选项',
    answer  json   null comment '答案',
    constraint multiple_choice_question_ibfk_1
        foreign key (qid) references question (qid)
)
    comment '多选问题表(子表)';

create index sid
    on question (sid);

create table response_record
(
    rid         bigint auto_increment comment '回答记录id'
        primary key,
    sid         bigint               not null comment '问卷id',
    uid         bigint               not null comment '回答者id',
    create_time datetime             not null comment '首次回答时间',
    update_time datetime             not null comment '回答更新时间',
    grade       int unsigned         null comment '成绩',
    finished    tinyint(1) default 0 not null comment '答卷是否批改完成',
    valid       tinyint(1) default 1 not null comment '答卷是否有效',
    constraint response_record_ibfk_1
        foreign key (sid) references survey (sid),
    constraint response_record_ibfk_2
        foreign key (uid) references user (uid)
)
    comment '回答记录表';

create table response_item
(
    submit_id   bigint auto_increment comment '回答项id'
        primary key,
    rid         bigint                             not null comment '回答记录id',
    qid         bigint                             not null comment '问题id',
    content     json                               not null comment '回答',
    grade       int unsigned                       null comment '该题得分',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null,
    constraint response_item_ibfk_1
        foreign key (rid) references response_record (rid),
    constraint response_item_ibfk_2
        foreign key (qid) references question (qid)
)
    comment '回答项表';

create table judge
(
    submit_id     bigint                               not null,
    qid           bigint                               not null,
    uid           bigint                               not null,
    code_content  varchar(4096)                        not null comment '代码',
    language      enum ('C', 'C++', 'Java', 'Python3') not null comment '语言',
    status        int                                  not null comment '评测结果',
    score         int unsigned                         null comment '得分',
    error_message text                                 null comment '错误信息提醒',
    create_time   datetime                             not null comment '第一次评测时间',
    update_time   datetime                             not null comment '评测更新时间',
    constraint judge_ibfk_1
        foreign key (submit_id) references response_item (submit_id),
    constraint judge_ibfk_2
        foreign key (qid) references code_question (qid),
    constraint judge_ibfk_3
        foreign key (uid) references user (uid)
)
    comment '代码评测表';

create index qid
    on judge (qid);

create index submit_id
    on judge (submit_id);

create index uid
    on judge (uid);

create table judge_case
(
    submit_id       bigint       not null,
    qid             bigint       not null,
    case_id         int unsigned not null comment '测试用例id',
    status          int          not null,
    time            int unsigned null comment '运行时间(ms)',
    memory          int unsigned null comment '内存消耗(KB)',
    input_data_url  varchar(256) null comment '输入数据的OSS地址',
    output_data_url varchar(256) null comment '输出数据的OSS地址',
    user_output     text         null comment '用户输出',
    create_time     datetime     not null comment '第一次评测时间',
    constraint judge_case_ibfk_1
        foreign key (submit_id) references response_item (submit_id),
    constraint judge_case_ibfk_2
        foreign key (qid) references code_question (qid)
)
    comment '单个测试用例表';

create index qid
    on judge_case (qid);

create index submit_id
    on judge_case (submit_id);

create index qid
    on response_item (qid);

create index rid
    on response_item (rid);

create index sid
    on response_record (sid);

create index uid
    on response_record (uid);

create definer = survey_planet_service@`%` trigger survey_update
    after insert
    on response_record
    for each row
begin
    update survey
    set update_time = now(), fill_num = fill_num + 1
    where survey.sid = new.sid;
end;

create table single_choice_question
(
    qid     bigint not null comment '问题id'
        primary key,
    options json   not null comment '选项',
    answer  json   null comment '答案',
    constraint single_choice_question_ibfk_1
        foreign key (qid) references question (qid)
)
    comment '单选问题表(子表)';

create index uid
    on survey (uid);

create table verification_code
(
    vid         bigint auto_increment comment '验证码id'
        primary key,
    email       varchar(256) not null comment '邮箱',
    code        char(10)     not null comment '验证码',
    expire_time datetime     not null comment '过期时间'
)
    comment '验证码表';


