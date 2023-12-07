package org.apache.dolphinscheduler.plugin.task.guicang.util;

import java.text.MessageFormat;

public enum ResultCodeEnum {

    SUCCESS("0", "操作成功"),
    ERROR("500", "操作失败"),
    REDIRECT("302", "重定向"),
    HTTP_UNAUTHORIZED("401", "认证失败，无法访问系统资源"),

    LOGIN_ERROR("901", "用户名或密码错误"),
    LOGIN_E_LOCKED("902", "账户已锁定"),
    LOGIN_E_EXCESS("903", "用户名或密码错误次数过多"),
    NOLOGIN("904", "登录状态已失效，请重新登录"),

    UNAUTH_ERROR("905", "无访问权限"),

    EXCEPTION_ERROR("906", "服务异常错误"),
    NOT_NULL_ERROR("907", "用户名或密码不为空"),
    NO_RULECODE("2000", "无匹配脱敏字段"),

    // 错误状态码

    // 网关服务 bywin-guicang-gateway 200
    E_200_0001("E-200-0001", ""),
    W_200_0002("W-200-0002", ""),

    // 权限认证服务 bywin-guicang-auth 201
    E_201_0001("E-201-0001", ""),
    W_201_0002("W-201-0002", ""),

    // 系统服务 bywin-guicang-console 202
    W_202_0001("E-202-0001", "名称不可为空"),
    W_202_0002("W-202-0002", ""),

    // 任务调度服务 bywin-guicang-core-schedule 203
    E_203_0001("E-203-0001", ""),
    E_203_0002("E-203-0002", ""),

    // 消息中心服务 bywin-guicang-message 204
    E_204_0001("E-204-0001", ""),
    E_204_0002("E-204-0002", ""),

    // 数据开发服务 bywin-guicang-datawork 205
    W_205_0001("W-205-0001", "创建空间失败,原因：空间标识不合法！只能以字母开头、数字字母结尾；不能包含中以及除'_'外的特殊符号"),
    W_205_0002("W-205-0002", "创建空间失败，MinIo已存在同名桶bucket名称！"),
    W_205_0003("W-205-0003", "创建空间失败，starrocks已存在同名库！"),
    W_205_0004("W-205-0004", "创建空间失败,新建表存储引擎失败！"),
    W_205_0005("W-205-0005", "修改失败，空间名称重复"),

    // 数据质量服务 bywin-guicang-quality 206
    E_206_0001("E-206-0001", ""),
    E_206_0002("E-206-0002", ""),

    // 数据标准服务 bywin-guicang-standard 207
    E_207_0001("E-207-0001", ""),
    E_207_0002("E-207-0002", ""),

    // 数据安全服务 bywin-guicang-security 208
    E_208_0001("E-208-0001", "新增失败，安全等级名称重复"),
    E_208_0002("E-208-0002", "修改失败，安全等级名称重复"),
    E_208_0003("E-208-0003", "删除失败，该安全等级已绑定规则"),
    E_208_0004("E-208-0004", "新增失败，脱敏规则名称重复"),
    E_208_0005("E-208-0005", "脱敏失败，未能获取数据源类型"),

    // 数据服务 bywin-guicang-rest-api 8209
    E_209_0001("E-209-0001", ""),
    E_209_0002("E-209-0002", ""),

    // 单机部署服务 bywin-guicang-standalone 8300
    E_300_0001("E_300-0001", ""),
    E_300_0002("E_300-0002", ""),

    ;

    private String code;
    private String message;

    private ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResultCodeEnum build(Object... arguments) {
        String msg = MessageFormat.format(this.getMessage(), arguments);
        this.setMessage(msg);
        return this;
    }

}
