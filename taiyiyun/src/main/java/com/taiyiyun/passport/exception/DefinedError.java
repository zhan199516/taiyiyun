package com.taiyiyun.passport.exception;

import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by okdos on 2017/7/20.
 * 定义一些错误类型
 */
public abstract class DefinedError extends Exception {

    public DefinedError(String readableMsg, String ex, boolean msgIsKey){

    }

    public DefinedError(String readableMsg, String ex){
        super(ex);
        this.readableMsg = readableMsg;
    }

    String readableMsg;

    public String getReadableMsg() {
        return readableMsg;
    }

    public abstract Status getErrorCode();

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        return this.getClass().getName() + ":" + msg;
    }

    /**
     * 无权限错误
     */
    public static class UnauthorizedException extends DefinedError {
        public UnauthorizedException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "无权限";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.UNAUTHORIZED;
        }
    }

    /**
     * 参数不正确
     */
    public static class ParameterException extends DefinedError {
        public ParameterException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "参数不正确";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.PARAM_ERROR;
        }
    }

    /**
     * 过期
     */
    public static class ExpireException extends  DefinedError {
        public ExpireException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "操作过期";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.EXPIRE;
        }
    }

    public static class UnBindException extends  DefinedError {

        public UnBindException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "绑定已经解除，无法进行后续操作";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.UN_BIND;
        }
    }


    /**
     * 找不到第三方服务
     */
    public static class ThirdNotFoundException extends DefinedError {

        public ThirdNotFoundException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "找不到第三方服务或连接失败";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.REMOTE_RESOURCE_NOT_FOUND;
        }
    }

    /**
     * user_key无效
     */
    public static class ThirdUserKeyInvalid extends DefinedError{

        public ThirdUserKeyInvalid(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "User_key或app_key无效";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.THIRD_USER_PASSWORD_ERROR;
        }
    }

    /**
     * 连接第三方服务器错误
     */
    public static class ThirdErrorException extends  DefinedError {
        public ThirdErrorException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "连接第三方服务器错误";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.THIRD_ERROR;
        }
    }

    /**
     * 第三方服务器执行失败
     */
    public static class ThirdRefuseException extends  DefinedError {
        public ThirdRefuseException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "第三方服务器执行失败";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.THIRD_REFUSE;
        }
    }

    /**
     * 交易条件
     */
    public static class ConditionException extends DefinedError {

        public ConditionException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "未满足交易条件";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.CONDITION_ERROR;
        }
    }

    /**
     * 其他错误
     */
    public static class OtherException extends DefinedError {

        public OtherException(String errorMsg, String ex) {
            super(errorMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "其他错误";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.OTHER_ERROR;
        }
    }

    /**
     * 转换错误
     */
    public static class ConvertException extends DefinedError {

        public ConvertException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "转换错误";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.CONVERT;
        }


    }

    /**
     * 短信验证码不正确
     */
    public static class SmsMistakeException extends DefinedError {

        public SmsMistakeException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "短信验证码不正确";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.SMS_INVALID;
        }
    }

    /**
     * 需要短信验证码
     */
    public static class SmsNeedException extends DefinedError {

        public SmsNeedException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "此操作需要短信验证码";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.SMS_NEED;
        }
    }

    /**
     * 找不到与请求匹配的 HTTP 资源
     */
    public static class RemoteResourceNotFoundException extends DefinedError {

        public RemoteResourceNotFoundException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "找不到与请求匹配的 HTTP 资源";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.REMOTE_RESOURCE_NOT_FOUND;
        }
    }

    /**
     * 密码错误
     */
    public static class PasswordWrongException extends DefinedError {
        public PasswordWrongException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "密码错误";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.USER_PASSWORD_INVALID;
        }
    }

    /**
     * 密码锁定错误
     */
    public static class PasswordLockException extends DefinedError {

        public PasswordLockException(String readableMsg, String ex) {
            super(readableMsg, ex);
            if(StringUtil.isEmptyOrBlank(this.readableMsg)){
                this.readableMsg = "由于错误次数过多，密码已经被锁定";
            }
        }

        @Override
        public Status getErrorCode() {
            return Status.LOCK;
        }
    }

    public static enum Status {

        SUCC(0, "成功"),
        PARAM_ERROR(1, "参数错误"),
        SIGN_INVALID(2, "签名错误"),
        OTHER_ERROR(9, "其它错误"),
        UPDATE_NEED(4, "需要升级系统"),
        SMS_INVALID(5, "验证码错误"),
        SMS_NEED(4, "需要输入验证码"),
        LOCK(6, "密码错误次数过多被锁定"),
        REMOTE_RESOURCE_NOT_FOUND(10, "获取远程资源出错"),
        USER_PASSWORD_INVALID(8, "用户名或密码错误"),
        USER_PASSWORD_CHANGED(9, "用户密码已经变更"),
        UNAUTHORIZED(2, "没有权限"),
        EXPIRE(3,"过期"),
        CONVERT(11, "数据转换错误"),
        CONDITION_ERROR(12, "查询条件错误"),
        THIRD_REFUSE(14, "第三方拒绝请求"),
        THIRD_USER_PASSWORD_ERROR(15, "第三方登录用户或密码错误"),
        THIRD_ERROR(16, "第三方请求错误"),
        UN_BIND(5,"未绑定错误"),
        FORCE_OUT(20, "强制下线");


        Integer value;
        String description;

        Status(Integer value, String description){
			this.value = value;
			this.description = description;
        }

        public int getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
