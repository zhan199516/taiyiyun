package com.taiyiyun.passport.sqlserver.po;

/**
 * Created by nina on 2017/10/25.
 */
public class CustomerDetailPerson extends CommonParameter{
    /*身份证号*/
    private String IDCard;
    /*姓名*/
    private String Name;
    /*护照*/
    private String Passport;
    /*用户公钥*/
    private String address;
    /*实体名称*/
    private String EntityName;
    /*身份证有效期始*/
    private String validDateStart;
    /*身份证有效期止*/
    private String validDateEnd;
    /*性别*/
    private String sex;
    /*民族*/
    private String nation;
    /*出生日期*/
    private String birthDay;
    /*家庭住址*/
    private String homeAddr;
    /*国家名称*/
    private String country;
    /*国家码*/
    private String countryCode;

    public String getIDCard() {
        return IDCard;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassport() {
        return Passport;
    }

    public void setPassport(String passport) {
        Passport = passport;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntityName() {
        return EntityName;
    }

    public void setEntityName(String entityName) {
        EntityName = entityName;
    }

    public String getValidDateStart() {
        return validDateStart;
    }

    public void setValidDateStart(String validDateStart) {
        this.validDateStart = validDateStart;
    }

    public String getValidDateEnd() {
        return validDateEnd;
    }

    public void setValidDateEnd(String validDateEnd) {
        this.validDateEnd = validDateEnd;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getHomeAddr() {
        return homeAddr;
    }

    public void setHomeAddr(String homeAddr) {
        this.homeAddr = homeAddr;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
