package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/23.
 */
public class Users {

    private String UUID;
    private String Address;
    private int Status;
    private int AppID;
    private String Mobile;
    private String Pwd;
    private String Version;
    private String NikeName;
    private String HeadPicture;
    private String DefaultUserEntity;
    private Date CreationTime;
    private String CauseOfFreezing;
    private long OperationUserID;
    private String Oper_user_name;
    private Date OperTime;
    private int PersionFailCount;
    private int ArtificialAuth;
    private Date LastLoginTime;
    private int UserID;
    private String ChainAddress;
    private int ChainErrorCode;
    private String ChainErrorMsg;
    private String MobilePrefix;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getAppID() {
        return AppID;
    }

    public void setAppID(int appID) {
        AppID = appID;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getPwd() {
        return Pwd;
    }

    public void setPwd(String pwd) {
        Pwd = pwd;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getNikeName() {
        return NikeName;
    }

    public void setNikeName(String nikeName) {
        NikeName = nikeName;
    }

    public String getHeadPicture() {
        return HeadPicture;
    }

    public void setHeadPicture(String headPicture) {
        HeadPicture = headPicture;
    }

    public String getDefaultUserEntity() {
        return DefaultUserEntity;
    }

    public void setDefaultUserEntity(String defaultUserEntity) {
        DefaultUserEntity = defaultUserEntity;
    }

    public Date getCreationTime() {
        return CreationTime;
    }

    public void setCreationTime(Date creationTime) {
        CreationTime = creationTime;
    }

    public String getCauseOfFreezing() {
        return CauseOfFreezing;
    }

    public void setCauseOfFreezing(String causeOfFreezing) {
        CauseOfFreezing = causeOfFreezing;
    }

    public long getOperationUserID() {
        return OperationUserID;
    }

    public void setOperationUserID(long operationUserID) {
        OperationUserID = operationUserID;
    }

    public String getOper_user_name() {
        return Oper_user_name;
    }

    public void setOper_user_name(String oper_user_name) {
        Oper_user_name = oper_user_name;
    }

    public Date getOperTime() {
        return OperTime;
    }

    public void setOperTime(Date operTime) {
        OperTime = operTime;
    }

    public int getPersionFailCount() {
        return PersionFailCount;
    }

    public void setPersionFailCount(int persionFailCount) {
        PersionFailCount = persionFailCount;
    }

    public int getArtificialAuth() {
        return ArtificialAuth;
    }

    public void setArtificialAuth(int artificialAuth) {
        ArtificialAuth = artificialAuth;
    }

    public Date getLastLoginTime() {
        return LastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        LastLoginTime = lastLoginTime;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getChainAddress() {
        return ChainAddress;
    }

    public void setChainAddress(String chainAddress) {
        ChainAddress = chainAddress;
    }

    public int getChainErrorCode() {
        return ChainErrorCode;
    }

    public void setChainErrorCode(int chainErrorCode) {
        ChainErrorCode = chainErrorCode;
    }

    public String getChainErrorMsg() {
        return ChainErrorMsg;
    }

    public void setChainErrorMsg(String chainErrorMsg) {
        ChainErrorMsg = chainErrorMsg;
    }

    public String getMobilePrefix() {
        return MobilePrefix;
    }

    public void setMobilePrefix(String mobilePrefix) {
        MobilePrefix = mobilePrefix;
    }
}
