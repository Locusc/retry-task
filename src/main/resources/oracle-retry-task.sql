-- 重试任务表
create table RETRY_TASK
(
    ID               NUMBER not null
        primary key,
    MAX_ATTEMPTS     NUMBER(2),
    METHOD_INFO      VARCHAR2(4000),
    RETRY_STATUS     NUMBER,
    CREATE_DATE      DATE,
    UPDATE_DATE      DATE,
    CURRENT_ATTEMPTS NUMBER(2),
    MAX_INTERVAL     NUMBER(8),
    TASK_NAME        VARCHAR2(255),
    PLAT_NO          VARCHAR2(500),
    IS_DEL           NUMBER(2),
    RESPONSE_DATA    VARCHAR2(4000),
    EXCEPTION_JSON   VARCHAR2(2000),
    RETRY_PARAMS     VARCHAR2(4000),
    RETRY_RULE       VARCHAR2(1000)
);

comment on column RETRY_TASK.ID is '主键';

comment on column RETRY_TASK.MAX_ATTEMPTS is '最大重试次数';

comment on column RETRY_TASK.METHOD_INFO is '重试方法路径';

comment on column RETRY_TASK.RETRY_STATUS is '状态';

comment on column RETRY_TASK.CREATE_DATE is '创建时间';

comment on column RETRY_TASK.UPDATE_DATE is '更新时间';

comment on column RETRY_TASK.CURRENT_ATTEMPTS is '当前重试次数';

comment on column RETRY_TASK.MAX_INTERVAL is '最大休眠时间';

comment on column RETRY_TASK.TASK_NAME is '任务名';

comment on column RETRY_TASK.PLAT_NO is '平台号';

comment on column RETRY_TASK.RESPONSE_DATA is '重试后返回的结果';

comment on column RETRY_TASK.EXCEPTION_JSON is '错误信息';

comment on column RETRY_TASK.RETRY_PARAMS is '入参信息';

comment on column RETRY_TASK.RETRY_RULE is '规则';

create index RETRY_TASK_METHOD_INFO_INDEX
    on RETRY_TASK (METHOD_INFO);

create sequence SEQ_RETRY_TASK
    maxvalue 999999999;