-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null,
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    profile      varchar(512)                       null comment '个人简介',
    userStatus   int      default 0                 not null comment '是否有效,0:正常',
    userRole     int      default 0                 not null comment '用户角色;0:普通用户;1:管理员',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除,0未删除,1删除',
    tags         varchar(1024)                      null comment '标签列表'
)
    comment '用户表';

-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            not null comment '是否是父标签,0不是,1是',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除,0未删除,1删除',
    constraint uniIdx_tagName
        unique (tagName)
)
    comment '标签';

-- auto-generated definition
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                                                                                                                  not null comment '队伍名称',
    avatarUrl   varchar(1024) default 'https://sns-img-qc.xhscdn.com/1000g00824kb2g6sfi0605o8prn6g8i9n5snf540?imageView2/2/w/640/format/webp' not null,
    description varchar(1024)                                                                                                                 null comment '描述',
    maxNum      int           default 1                                                                                                       not null comment '最大人数',
    expireTime  datetime                                                                                                                      null comment '过期时间',
    userId      bigint                                                                                                                        null comment '用户id',
    status      int           default 0                                                                                                       not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                                                                                                                  null comment '密码',
    createTime  datetime      default CURRENT_TIMESTAMP                                                                                       null comment '创建时间',
    updateTime  datetime      default CURRENT_TIMESTAMP                                                                                       null on update CURRENT_TIMESTAMP,
    isDelete    tinyint       default 0                                                                                                       not null comment '是否删除'
)
    comment '队伍';


-- auto-generated definition
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系';

